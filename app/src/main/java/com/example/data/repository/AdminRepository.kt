package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AccountStatus
import com.example.data.model.AccountType
import com.example.data.model.CategoryItem
import com.example.data.model.EgyptCatalog
import com.example.data.model.LocationItem
import com.example.data.model.PropertyItem
import com.example.data.model.PropertyStatus
import com.example.data.model.ReportItem
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AdminRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireAdminId(): String {
        val user = auth.currentUser
            ?: throw IllegalStateException("يجب تسجيل الدخول أولاً.")
        check(AuthManager.isBootstrappedAdmin(user)) {
            "هذه العملية تتطلب صلاحيات مسؤول النظام."
        }
        return user.uid
    }

    private fun <T> Flow<T>.logAdminErrors(
        uid: String,
        operationType: OperationType,
        path: String
    ): Flow<T> = catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
        throw error
    }

    fun observeAllProperties(adminUid: String): Flow<List<PropertyItem>> = flow {
        if (adminUid.isBlank()) return@flow
        emitAll(
            db.collection("properties")
                .snapshots()
                .map { snap ->
                    snap.toObjects(PropertyItem::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logAdminErrors(adminUid, OperationType.LIST, "properties")
        )
    }

    fun observeAllUsers(adminUid: String): Flow<List<UserProfile>> = flow {
        if (adminUid.isBlank()) return@flow
        emitAll(
            db.collection("users")
                .snapshots()
                .map { snap ->
                    snap.toObjects(UserProfile::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logAdminErrors(adminUid, OperationType.LIST, "users")
        )
    }

    fun observeAllReports(adminUid: String): Flow<List<ReportItem>> = flow {
        if (adminUid.isBlank()) return@flow
        emitAll(
            db.collection("reports")
                .snapshots()
                .map { snap ->
                    snap.toObjects(ReportItem::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logAdminErrors(adminUid, OperationType.LIST, "reports")
        )
    }

    suspend fun reviewProperty(
        property: PropertyItem,
        approve: Boolean,
        rejectionReason: String? = null
    ): Result<Unit> {
        val path = "properties/${property.id}"
        return try {
            val adminUid = requireAdminId()
            val nextStatus = if (approve) PropertyStatus.APPROVED.key else PropertyStatus.REJECTED.key
            val updates = mutableMapOf<String, Any>(
                "status" to nextStatus,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!approve && !rejectionReason.isNullOrBlank()) {
                updates["rejectionReason"] = rejectionReason.trim().take(300)
            }
            db.collection("properties").document(property.id).update(updates).await()

            if (property.ownerId.isNotBlank()) {
                db.collection("users").document(property.ownerId).collection("notifications").document().set(
                    mapOf(
                        "userId" to property.ownerId,
                        "senderId" to adminUid,
                        "title" to if (approve) "تمت الموافقة على إعلانك" else "تم رفض إعلانك",
                        "body" to if (approve) {
                            "أصبح عقارك \"${property.title.take(60)}\" منشوراً الآن للجمهور."
                        } else {
                            "سبب الرفض: ${rejectionReason?.take(150) ?: "مخالفة سياسة النشر"}"
                        },
                        "type" to "listing_status",
                        "targetId" to property.id,
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleFeatured(propertyId: String, featured: Boolean): Result<Unit> {
        val path = "properties/$propertyId"
        return try {
            requireAdminId()
            db.collection("properties").document(propertyId).update(
                mapOf("featured" to featured, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateUserAccount(
        userId: String,
        accountType: AccountType,
        status: AccountStatus
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            requireAdminId()
            db.collection("users").document(userId).update(
                mapOf(
                    "accountType" to accountType.key,
                    "status" to status.key,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun resolveReport(reportId: String, adminNotes: String): Result<Unit> {
        val path = "reports/$reportId"
        return try {
            requireAdminId()
            db.collection("reports").document(reportId).update(
                mapOf(
                    "status" to "resolved",
                    "adminNotes" to adminNotes.trim().ifBlank { "تمت المعالجة من قبل الإدارة" }.take(500),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertCategory(category: CategoryItem): Result<Unit> {
        val docId = category.id.ifBlank { category.key }
        val path = "categories/$docId"
        return try {
            requireAdminId()
            db.collection("categories").document(docId).set(
                mapOf(
                    "key" to category.key.trim().take(40),
                    "nameAr" to category.nameAr.trim().take(60),
                    "enabled" to category.enabled,
                    "sortOrder" to category.sortOrder.coerceIn(0L, 100L)
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun upsertLocation(locationId: String, nameAr: String, cities: List<String>, enabled: Boolean): Result<Unit> {
        val path = "locations/$locationId"
        return try {
            requireAdminId()
            db.collection("locations").document(locationId).set(
                mapOf(
                    "nameAr" to nameAr.trim().take(60),
                    "cities" to cities.take(40),
                    "enabled" to enabled
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun seedDemoCatalogAndProperties(beityRepository: BeityRepository): Result<Int> {
        return try {
            val adminUid = requireAdminId()
            val adminName = auth.currentUser?.displayName ?: "إدارة بيتي"
            EgyptCatalog.defaultCategories.forEach { cat ->
                upsertCategory(cat)
            }
            EgyptCatalog.governorates.forEach { gov ->
                upsertLocation(gov.id, gov.nameAr, gov.cities, true)
            }
            var inserted = 0
            val demoProps = EgyptCatalog.sampleDemoPropertiesForAdmin(adminUid, adminName)
            demoProps.forEach { prop ->
                val res = beityRepository.createProperty(prop, isAdminUser = true)
                if (res.isSuccess) inserted++
            }
            Result.success(inserted)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "properties")
            Result.failure(e)
        }
    }
}
