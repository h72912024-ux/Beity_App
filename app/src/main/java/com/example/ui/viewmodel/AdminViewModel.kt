package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AccountStatus
import com.example.data.model.AccountType
import com.example.data.model.CategoryItem
import com.example.data.model.PropertyItem
import com.example.data.model.ReportItem
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.repository.AdminRepository
import com.example.data.repository.BeityRepository
import com.example.data.repository.friendlyFirestoreMessage
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    private val adminRepository: AdminRepository,
    private val beityRepository: BeityRepository,
    private val currentUserId: String,
    val isAdmin: Boolean
) : ViewModel() {

    private val _feedback = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val feedback: SharedFlow<String> = _feedback.asSharedFlow()

    private fun <T> Flow<T>.toAdminState(fallback: String): StateFlow<UiState<T>> =
        map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                emit(UiState.Error(friendlyFirestoreMessage(fsErr ?: error, fallback)))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val allProperties: StateFlow<UiState<List<PropertyItem>>> =
        if (!isAdmin) MutableStateFlowEmpty<List<PropertyItem>>()
        else adminRepository.observeAllProperties(currentUserId).toAdminState("تعذر تحميل العقارات")

    val allUsers: StateFlow<UiState<List<UserProfile>>> =
        if (!isAdmin) MutableStateFlowEmpty<List<UserProfile>>()
        else adminRepository.observeAllUsers(currentUserId).toAdminState("تعذر تحميل المستخدمين")

    val allReports: StateFlow<UiState<List<ReportItem>>> =
        if (!isAdmin) MutableStateFlowEmpty<List<ReportItem>>()
        else adminRepository.observeAllReports(currentUserId).toAdminState("تعذر تحميل البلاغات")

    private fun <T> MutableStateFlowEmpty(): StateFlow<UiState<T>> =
        flowOf<UiState<T>>(UiState.Error("غير مصرح"))
            .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading)

    fun approveProperty(property: PropertyItem) {
        viewModelScope.launch {
            adminRepository.reviewProperty(property, approve = true)
                .onSuccess { _feedback.tryEmit("تمت الموافقة على العقار ونشره") }
                .onFailure { _feedback.tryEmit(friendlyFirestoreMessage(it, "تعذر الموافقة")) }
        }
    }

    fun rejectProperty(property: PropertyItem, reason: String) {
        viewModelScope.launch {
            adminRepository.reviewProperty(property, approve = false, rejectionReason = reason)
                .onSuccess { _feedback.tryEmit("تم رفض الإعلان وإخطار المعلن") }
                .onFailure { _feedback.tryEmit(friendlyFirestoreMessage(it, "تعذر رفض الإعلان")) }
        }
    }

    fun toggleFeatured(propertyId: String, featured: Boolean) {
        viewModelScope.launch {
            adminRepository.toggleFeatured(propertyId, featured)
                .onSuccess { _feedback.tryEmit(if (featured) "تم تمييز العقار" else "تم إلغاء تمييز العقار") }
                .onFailure { _feedback.tryEmit(friendlyFirestoreMessage(it, "تعذر تحديث التمييز")) }
        }
    }

    fun deleteProperty(propertyId: String) {
        viewModelScope.launch {
            beityRepository.deleteProperty(propertyId)
                .onSuccess { _feedback.tryEmit("تم حذف العقار نهائياً") }
                .onFailure { _feedback.tryEmit(friendlyFirestoreMessage(it, "تعذر الحذف")) }
        }
    }

    fun updateUserAccount(userId: String, type: AccountType, status: AccountStatus) {
        viewModelScope.launch {
            adminRepository.updateUserAccount(userId, type, status)
                .onSuccess { _feedback.tryEmit("تم تحديث صلاحيات وحالة الحساب") }
                .onFailure { _feedback.tryEmit(friendlyFirestoreMessage(it, "تعذر تحديث الحساب")) }
        }
    }

    fun resolveReport(reportId: String, notes: String) {
        viewModelScope.launch {
            adminRepository.resolveReport(reportId, notes)
                .onSuccess { _feedback.tryEmit("تم إغلاق البلاغ ومعالجته") }
                .onFailure { _feedback.tryEmit(friendlyFirestoreMessage(it, "تعذر معالجة البلاغ")) }
        }
    }

    fun upsertCategory(category: CategoryItem) {
        viewModelScope.launch {
            adminRepository.upsertCategory(category)
                .onSuccess { _feedback.tryEmit("تم حفظ القسم بنجاح") }
                .onFailure { _feedback.tryEmit(friendlyFirestoreMessage(it, "تعذر حفظ القسم")) }
        }
    }

    fun upsertLocation(id: String, nameAr: String, cities: List<String>, enabled: Boolean) {
        viewModelScope.launch {
            adminRepository.upsertLocation(id, nameAr, cities, enabled)
                .onSuccess { _feedback.tryEmit("تم حفظ المحافظة بنجاح") }
                .onFailure { _feedback.tryEmit(friendlyFirestoreMessage(it, "تعذر حفظ المحافظة")) }
        }
    }

    fun seedDemoProperties() {
        viewModelScope.launch {
            adminRepository.seedDemoCatalogAndProperties(beityRepository)
                .onSuccess { count ->
                    _feedback.tryEmit("تم إدراج $count عقارات نموذجية تجريبية والأقسام والمحافظات في Firestore")
                }
                .onFailure {
                    _feedback.tryEmit(friendlyFirestoreMessage(it, "تعذر إدراج البيانات النموذجية"))
                }
        }
    }
}
