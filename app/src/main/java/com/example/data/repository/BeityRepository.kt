package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AccountType
import com.example.data.model.CategoryItem
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.FavoriteItem
import com.example.data.model.LocationItem
import com.example.data.model.NotificationItem
import com.example.data.model.PropertyFilter
import com.example.data.model.PropertyItem
import com.example.data.model.PropertyReview
import com.example.data.model.PropertyStatus
import com.example.data.model.ReportItem
import com.example.data.model.SearchHistoryItem
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

class BeityRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("يجب تسجيل الدخول عبر Google أولاً.")

    private fun <T> Flow<T>.logFirestoreErrors(
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

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        if (userId.isBlank()) return@flow
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { it.toObject(UserProfile::class.java, ServerTimestampBehavior.ESTIMATE) }
                .logFirestoreErrors(userId, OperationType.GET, path)
        )
    }

    suspend fun ensureUserProfile(
        displayName: String,
        email: String?,
        avatarUrl: String?
    ): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            path = "users/$uid"
            val docRef = db.collection("users").document(uid)
            val snap = docRef.get().await()
            val safeName = displayName.trim().ifBlank { "مستخدم بيتي" }.take(80)
            if (!snap.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "email" to email?.take(120),
                    "accountType" to AccountType.SEEKER.key,
                    "status" to "active",
                    "avatarUrl" to avatarUrl?.take(90000),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                docRef.set(payload).await()
            }
            val pubRef = db.collection("public_profiles").document(uid)
            val pubSnap = pubRef.get().await()
            if (!pubSnap.exists()) {
                val pubPayload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "accountType" to AccountType.SEEKER.key,
                    "verified" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                pubRef.set(pubPayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(
        displayName: String,
        phone: String?,
        city: String?,
        accountType: AccountType,
        avatarUrl: String?
    ): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            path = "users/$uid"
            val cleanName = displayName.trim()
            require(cleanName.length in 1..80) { "الاسم يجب أن يكون بين 1 و 80 حرفاً" }

            val updates = mutableMapOf<String, Any>(
                "displayName" to cleanName,
                "accountType" to accountType.key,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            phone?.trim()?.takeIf { it.isNotEmpty() }?.let { updates["phone"] = it.take(30) }
            city?.trim()?.takeIf { it.isNotEmpty() }?.let { updates["city"] = it.take(60) }
            avatarUrl?.trim()?.takeIf { it.isNotEmpty() }?.let { updates["avatarUrl"] = it.take(90000) }
            db.collection("users").document(uid).update(updates).await()

            val pubRef = db.collection("public_profiles").document(uid)
            val pubSnap = pubRef.get().await()
            if (pubSnap.exists()) {
                val pubUpdates = mutableMapOf<String, Any>(
                    "displayName" to cleanName,
                    "accountType" to accountType.key,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                city?.trim()?.takeIf { it.isNotEmpty() }?.let { pubUpdates["city"] = it.take(60) }
                pubRef.update(pubUpdates).await()
            } else {
                val pubPayload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "displayName" to cleanName,
                    "accountType" to accountType.key,
                    "verified" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                city?.trim()?.takeIf { it.isNotEmpty() }?.let { pubPayload["city"] = it.take(60) }
                pubRef.set(pubPayload).await()
            }

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    fun observeApprovedProperties(userId: String): Flow<List<PropertyItem>> = flow {
        if (userId.isBlank()) return@flow
        val path = "properties"
        emitAll(
            db.collection(path)
                .whereEqualTo("status", PropertyStatus.APPROVED.key)
                .snapshots()
                .map { snap ->
                    snap.toObjects(PropertyItem::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logFirestoreErrors(userId, OperationType.LIST, path)
        )
    }

    fun observeMyProperties(userId: String): Flow<List<PropertyItem>> = flow {
        if (userId.isBlank()) return@flow
        val path = "properties"
        emitAll(
            db.collection(path)
                .whereEqualTo("ownerId", userId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(PropertyItem::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logFirestoreErrors(userId, OperationType.LIST, path)
        )
    }

    suspend fun createProperty(property: PropertyItem, isAdminUser: Boolean): Result<String> {
        var path = "properties"
        return try {
            val uid = requireUserId()
            require(property.title.trim().length in 3..120) { "عنوان العقار يجب أن يكون بين 3 و 120 حرفاً" }
            require(property.description.trim().length in 10..2500) { "وصف العقار يجب أن يكون بين 10 و 2500 حرفاً" }
            require(property.contactPhone.trim().length in 6..25) { "رقم التواصل غير صالح" }
            require(property.price > 0.0) { "السعر يجب أن يكون أكبر من صفر" }
            require(property.areaSqm > 0.0) { "المساحة يجب أن تكون أكبر من صفر" }

            val docRef = db.collection("properties").document()
            path = docRef.path
            val initialStatus = if (isAdminUser && property.status == PropertyStatus.APPROVED.key) {
                PropertyStatus.APPROVED.key
            } else {
                PropertyStatus.PENDING.key
            }

            val payload = mapOf(
                "ownerId" to uid,
                "ownerName" to property.ownerName.trim().ifBlank { "معلن بيتي" }.take(80),
                "contactPhone" to property.contactPhone.trim().take(25),
                "title" to property.title.trim().take(120),
                "description" to property.description.trim().take(2500),
                "propertyType" to property.propertyType,
                "listingType" to property.listingType,
                "price" to property.price,
                "areaSqm" to property.areaSqm,
                "governorate" to property.governorate.trim().take(60),
                "city" to property.city.trim().take(60),
                "district" to property.district.trim().ifBlank { property.city.trim() }.take(80),
                "address" to property.address.trim().ifBlank { property.city.trim() }.take(200),
                "bedrooms" to property.bedrooms.coerceIn(0L, 50L),
                "bathrooms" to property.bathrooms.coerceIn(0L, 30L),
                "floor" to property.floor.coerceIn(-2L, 150L),
                "finishingType" to property.finishingType,
                "furnished" to property.furnished,
                "hasElevator" to property.hasElevator,
                "hasGarage" to property.hasGarage,
                "hasBalcony" to property.hasBalcony,
                "features" to property.features.take(15),
                "amenities" to property.amenities.take(15),
                "imageUrls" to property.imageUrls.take(10),
                "videoUrl" to property.videoUrl?.trim()?.takeIf { it.isNotEmpty() }?.take(300),
                "latitude" to property.latitude.coerceIn(-90.0, 90.0),
                "longitude" to property.longitude.coerceIn(-180.0, 180.0),
                "status" to initialStatus,
                "featured" to (isAdminUser && property.featured),
                "viewsCount" to 0L,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateOwnProperty(property: PropertyItem, keepStatusIfAllowed: String = PropertyStatus.PENDING.key): Result<Unit> {
        var path = "properties/${property.id}"
        return try {
            val uid = requireUserId()
            require(property.id.isNotBlank()) { "معرف العقار مفقود" }
            require(property.ownerId == uid) { "غير مصرح بتعديل هذا العقار" }
            require(property.title.trim().length in 3..120) { "عنوان العقار يجب أن يكون بين 3 و 120 حرفاً" }
            require(property.description.trim().length in 10..2500) { "وصف العقار يجب أن يكون بين 10 و 2500 حرفاً" }

            val updates = mutableMapOf<String, Any>(
                "ownerName" to property.ownerName.trim().ifBlank { "معلن بيتي" }.take(80),
                "contactPhone" to property.contactPhone.trim().take(25),
                "title" to property.title.trim().take(120),
                "description" to property.description.trim().take(2500),
                "propertyType" to property.propertyType,
                "listingType" to property.listingType,
                "price" to property.price,
                "areaSqm" to property.areaSqm,
                "governorate" to property.governorate.trim().take(60),
                "city" to property.city.trim().take(60),
                "district" to property.district.trim().ifBlank { property.city.trim() }.take(80),
                "address" to property.address.trim().ifBlank { property.city.trim() }.take(200),
                "bedrooms" to property.bedrooms.coerceIn(0L, 50L),
                "bathrooms" to property.bathrooms.coerceIn(0L, 30L),
                "floor" to property.floor.coerceIn(-2L, 150L),
                "finishingType" to property.finishingType,
                "furnished" to property.furnished,
                "hasElevator" to property.hasElevator,
                "hasGarage" to property.hasGarage,
                "hasBalcony" to property.hasBalcony,
                "features" to property.features.take(15),
                "amenities" to property.amenities.take(15),
                "imageUrls" to property.imageUrls.take(10),
                "latitude" to property.latitude.coerceIn(-90.0, 90.0),
                "longitude" to property.longitude.coerceIn(-180.0, 180.0),
                "status" to keepStatusIfAllowed,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            property.videoUrl?.trim()?.takeIf { it.isNotEmpty() }?.let {
                updates["videoUrl"] = it.take(300)
            }

            db.collection("properties").document(property.id).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleOwnPropertyPause(propertyId: String, pause: Boolean): Result<Unit> {
        val path = "properties/$propertyId"
        return try {
            requireUserId()
            require(propertyId.isNotBlank())
            val nextStatus = if (pause) PropertyStatus.PAUSED.key else PropertyStatus.APPROVED.key
            db.collection("properties").document(propertyId).update(
                mapOf("status" to nextStatus, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteProperty(propertyId: String): Result<Unit> {
        val path = "properties/$propertyId"
        return try {
            requireUserId()
            require(propertyId.isNotBlank())
            db.collection("properties").document(propertyId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun incrementPropertyViews(propertyId: String): Result<Unit> {
        val path = "properties/$propertyId"
        return try {
            requireUserId()
            db.collection("properties").document(propertyId)
                .update("viewsCount", FieldValue.increment(1))
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    fun observeReviews(userId: String, propertyId: String): Flow<List<PropertyReview>> = flow {
        if (userId.isBlank() || propertyId.isBlank()) return@flow
        val path = "properties/$propertyId/reviews"
        emitAll(
            db.collection("properties").document(propertyId).collection("reviews")
                .whereEqualTo("propertyId", propertyId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(PropertyReview::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logFirestoreErrors(userId, OperationType.LIST, path)
        )
    }

    suspend fun addReview(propertyId: String, authorName: String, rating: Int, comment: String): Result<Unit> {
        var path = "properties/$propertyId/reviews"
        return try {
            val uid = requireUserId()
            require(comment.trim().length in 2..600) { "التعليق يجب أن يكون بين 2 و 600 حرف" }
            val docRef = db.collection("properties").document(propertyId).collection("reviews").document()
            path = docRef.path
            docRef.set(
                mapOf(
                    "propertyId" to propertyId,
                    "authorId" to uid,
                    "authorName" to authorName.trim().ifBlank { "مستخدم بيتي" }.take(80),
                    "rating" to rating.coerceIn(1, 5).toLong(),
                    "comment" to comment.trim().take(600),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeFavorites(userId: String): Flow<List<FavoriteItem>> = flow {
        if (userId.isBlank()) return@flow
        val path = "users/$userId/favorites"
        emitAll(
            db.collection("users").document(userId).collection("favorites")
                .snapshots()
                .map { snap ->
                    snap.toObjects(FavoriteItem::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logFirestoreErrors(userId, OperationType.LIST, path)
        )
    }

    suspend fun toggleFavorite(property: PropertyItem, currentlyFavorite: Boolean): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("favorites").document(property.id)
            path = docRef.path
            if (currentlyFavorite) {
                docRef.delete().await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "propertyId" to property.id,
                        "title" to property.title.take(120),
                        "price" to property.price,
                        "city" to property.city.take(60),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    fun observeSearchHistory(userId: String): Flow<List<SearchHistoryItem>> = flow {
        if (userId.isBlank()) return@flow
        val path = "users/$userId/search_history"
        emitAll(
            db.collection("users").document(userId).collection("search_history")
                .snapshots()
                .map { snap ->
                    snap.toObjects(SearchHistoryItem::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                        .take(20)
                }
                .logFirestoreErrors(userId, OperationType.LIST, path)
        )
    }

    suspend fun saveSearchHistory(filter: PropertyFilter): Result<Unit> {
        var path = "users"
        return try {
            val uid = requireUserId()
            if (!filter.hasActiveFilters()) return Result.success(Unit)
            val docRef = db.collection("users").document(uid).collection("search_history").document()
            path = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "querySummary" to filter.summaryAr().take(200),
                "governorate" to filter.governorate.takeIf { it.isNotBlank() }?.take(60),
                "propertyType" to filter.propertyType?.key,
                "listingType" to filter.listingType?.key,
                "maxPrice" to filter.maxPrice,
                "createdAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeNotifications(userId: String): Flow<List<NotificationItem>> = flow {
        if (userId.isBlank()) return@flow
        val path = "users/$userId/notifications"
        emitAll(
            db.collection("users").document(userId).collection("notifications")
                .snapshots()
                .map { snap ->
                    snap.toObjects(NotificationItem::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .logFirestoreErrors(userId, OperationType.LIST, path)
        )
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> {
        var path = "notifications"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("notifications").document(notificationId)
            path = docRef.path
            docRef.update("read", true).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    fun observeConversations(userId: String): Flow<List<Conversation>> = flow {
        if (userId.isBlank()) return@flow
        val path = "conversations"
        emitAll(
            db.collection(path)
                .whereArrayContains("participantIds", userId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(Conversation::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt?.seconds ?: 0L }
                }
                .logFirestoreErrors(userId, OperationType.LIST, path)
        )
    }

    fun observeMessages(userId: String, conversationId: String): Flow<List<ChatMessage>> = flow {
        if (userId.isBlank() || conversationId.isBlank()) return@flow
        val path = "conversations/$conversationId/messages"
        emitAll(
            db.collection("conversations").document(conversationId).collection("messages")
                .whereArrayContains("participantIds", userId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(ChatMessage::class.java, ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt?.seconds ?: 0L }
                }
                .logFirestoreErrors(userId, OperationType.LIST, path)
        )
    }

    suspend fun startOrSendMessage(
        propertyId: String,
        propertyTitle: String,
        recipientId: String,
        recipientName: String,
        senderName: String,
        text: String
    ): Result<String> {
        var path = "conversations"
        return try {
            val uid = requireUserId()
            val cleanText = text.trim()
            require(cleanText.length in 1..1000) { "الرسالة يجب أن تكون بين 1 و 1000 حرف" }
            val sortedIds = listOf(uid, recipientId).sorted()
            val convId = "${propertyId}_${sortedIds[0]}_${sortedIds[1]}".take(120)
            val convRef = db.collection("conversations").document(convId)
            path = convRef.path
            val existingConv = convRef.get().await()
            val participantNames = mapOf(
                uid to senderName.trim().ifBlank { "مستخدم بيتي" }.take(80),
                recipientId to recipientName.trim().ifBlank { "المعلن" }.take(80)
            )
            if (!existingConv.exists()) {
                convRef.set(
                    mapOf(
                        "participantIds" to sortedIds,
                        "propertyId" to propertyId.take(128),
                        "propertyTitle" to propertyTitle.trim().take(120),
                        "participantNames" to participantNames,
                        "lastMessage" to cleanText,
                        "lastSenderId" to uid,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                convRef.update(
                    mapOf(
                        "lastMessage" to cleanText,
                        "lastSenderId" to uid,
                        "participantNames" to participantNames,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            val msgRef = convRef.collection("messages").document()
            msgRef.set(
                mapOf(
                    "conversationId" to convId,
                    "senderId" to uid,
                    "senderName" to senderName.trim().ifBlank { "مستخدم بيتي" }.take(80),
                    "participantIds" to sortedIds,
                    "text" to cleanText,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            if (recipientId != uid) {
                db.collection("users").document(recipientId).collection("notifications").document().set(
                    mapOf(
                        "userId" to recipientId,
                        "senderId" to uid,
                        "title" to "رسالة جديدة حول: ${propertyTitle.take(50)}",
                        "body" to cleanText.take(200),
                        "type" to "message",
                        "targetId" to convId,
                        "read" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            Result.success(convId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun submitReport(
        reporterName: String,
        propertyId: String?,
        propertyTitle: String?,
        reportType: String,
        reason: String,
        details: String
    ): Result<Unit> {
        var path = "reports"
        return try {
            val uid = requireUserId()
            require(reason.trim().length in 2..120) { "سبب البلاغ قصير جداً" }
            require(details.trim().length in 5..1500) { "التفاصيل يجب أن تكون 5 أحرف على الأقل" }
            val docRef = db.collection("reports").document()
            path = docRef.path
            val payload = mapOf(
                "reporterId" to uid,
                "reporterName" to reporterName.trim().ifBlank { "مستخدم بيتي" }.take(80),
                "propertyId" to propertyId?.take(128),
                "propertyTitle" to propertyTitle?.take(120),
                "reportType" to reportType,
                "reason" to reason.trim().take(120),
                "details" to details.trim().take(1500),
                "status" to "open",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    fun observeCategories(userId: String): Flow<List<CategoryItem>> = flow {
        if (userId.isBlank()) return@flow
        val path = "categories"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snap ->
                    snap.toObjects(CategoryItem::class.java)
                        .sortedBy { it.sortOrder }
                }
                .logFirestoreErrors(userId, OperationType.LIST, path)
        )
    }

    fun observeLocations(userId: String): Flow<List<LocationItem>> = flow {
        if (userId.isBlank()) return@flow
        val path = "locations"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snap -> snap.toObjects(LocationItem::class.java) }
                .logFirestoreErrors(userId, OperationType.LIST, path)
        )
    }
}
