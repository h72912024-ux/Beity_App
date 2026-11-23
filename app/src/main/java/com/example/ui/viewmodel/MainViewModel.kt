package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AccountType
import com.example.data.model.CategoryItem
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.EgyptCatalog
import com.example.data.model.FavoriteItem
import com.example.data.model.LocationItem
import com.example.data.model.NotificationItem
import com.example.data.model.PropertyFilter
import com.example.data.model.PropertyItem
import com.example.data.model.PropertyReview
import com.example.data.model.SearchHistoryItem
import com.example.data.model.SortOption
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.repository.BeityRepository
import com.example.data.repository.friendlyFirestoreMessage
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: BeityRepository,
    val currentUserId: String,
    private val initialDisplayName: String,
    private val initialEmail: String?,
    private val initialPhotoUrl: String?,
    val isAdmin: Boolean
) : ViewModel() {

    private val _feedback = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val feedback: SharedFlow<String> = _feedback.asSharedFlow()

    private val _filter = MutableStateFlow(PropertyFilter())
    val filter: StateFlow<PropertyFilter> = _filter.asStateFlow()

    private val _compareIds = MutableStateFlow<List<String>>(emptyList())
    val compareIds: StateFlow<List<String>> = _compareIds.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(null)
    private val _activePropertyIdForReviews = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.ensureUserProfile(
                displayName = initialDisplayName,
                email = initialEmail,
                avatarUrl = initialPhotoUrl
            )
        }
    }

    private fun <T> Flow<T>.toUiStateFlow(errorFallback: String): StateFlow<UiState<T>> =
        map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                emit(UiState.Error(friendlyFirestoreMessage(rootError, errorFallback)))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile(currentUserId)
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            emit(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val approvedPropertiesState: StateFlow<UiState<List<PropertyItem>>> =
        repository.observeApprovedProperties(currentUserId)
            .toUiStateFlow("تعذر تحميل قائمة العقارات")

    val myPropertiesState: StateFlow<UiState<List<PropertyItem>>> =
        repository.observeMyProperties(currentUserId)
            .toUiStateFlow("تعذر تحميل عقاراتك")

    val favorites: StateFlow<List<FavoriteItem>> =
        repository.observeFavorites(currentUserId)
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val notifications: StateFlow<List<NotificationItem>> =
        repository.observeNotifications(currentUserId)
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val conversations: StateFlow<List<Conversation>> =
        repository.observeConversations(currentUserId)
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val searchHistory: StateFlow<List<SearchHistoryItem>> =
        repository.observeSearchHistory(currentUserId)
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val categories: StateFlow<List<CategoryItem>> =
        repository.observeCategories(currentUserId)
            .map { list -> if (list.isEmpty()) EgyptCatalog.defaultCategories else list.filter { it.enabled } }
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                emit(EgyptCatalog.defaultCategories)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), EgyptCatalog.defaultCategories)

    val locations: StateFlow<List<LocationItem>> =
        repository.observeLocations(currentUserId)
            .map { list ->
                if (list.isEmpty()) {
                    EgyptCatalog.governorates.map { LocationItem(id = it.id, nameAr = it.nameAr, cities = it.cities, enabled = true) }
                } else {
                    list.filter { it.enabled }
                }
            }
            .catch { error ->
                val fsErr = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                if (fsErr == null && error is CancellationException) throw error
                emit(EgyptCatalog.governorates.map { LocationItem(id = it.id, nameAr = it.nameAr, cities = it.cities, enabled = true) })
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                EgyptCatalog.governorates.map { LocationItem(id = it.id, nameAr = it.nameAr, cities = it.cities, enabled = true) }
            )

    @Suppress("OPT_IN_USAGE")
    val activeMessages: StateFlow<List<ChatMessage>> =
        _activeConversationId.flatMapLatest { convId ->
            if (convId.isNullOrBlank()) flowOf(emptyList())
            else repository.observeMessages(currentUserId, convId)
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    emit(emptyList())
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    @Suppress("OPT_IN_USAGE")
    val activePropertyReviews: StateFlow<List<PropertyReview>> =
        _activePropertyIdForReviews.flatMapLatest { propId ->
            if (propId.isNullOrBlank()) flowOf(emptyList())
            else repository.observeReviews(currentUserId, propId)
                .catch { error ->
                    val fsErr = generateSequence(error) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()
                    if (fsErr == null && error is CancellationException) throw error
                    emit(emptyList())
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val filteredProperties: StateFlow<List<PropertyItem>> =
        combine(approvedPropertiesState, _filter) { state, filter ->
            val items = (state as? UiState.Success)?.data ?: emptyList()
            applyPropertyFilter(items, filter)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun updateFilter(transform: (PropertyFilter) -> PropertyFilter) {
        _filter.value = transform(_filter.value)
    }

    fun resetFilter() {
        _filter.value = PropertyFilter()
    }

    fun saveCurrentSearch() {
        val current = _filter.value
        if (!current.hasActiveFilters()) return
        viewModelScope.launch {
            repository.saveSearchHistory(current)
        }
    }

    fun applySearchHistoryItem(item: SearchHistoryItem) {
        _filter.value = PropertyFilter(
            governorate = item.governorate.orEmpty(),
            propertyType = item.propertyType?.let { com.example.data.model.PropertyType.fromKey(it) },
            listingType = item.listingType?.let { com.example.data.model.ListingType.fromKey(it) },
            maxPrice = item.maxPrice
        )
    }

    fun setActiveConversation(conversationId: String?) {
        _activeConversationId.value = conversationId
    }

    fun setActivePropertyForDetails(propertyId: String) {
        _activePropertyIdForReviews.value = propertyId
        viewModelScope.launch {
            repository.incrementPropertyViews(propertyId)
        }
    }

    fun toggleCompare(propertyId: String) {
        val current = _compareIds.value.toMutableList()
        if (current.contains(propertyId)) {
            current.remove(propertyId)
            _compareIds.value = current
            _feedback.tryEmit("تمت إزالة العقار من المقارنة")
        } else {
            if (current.size >= 3) {
                _feedback.tryEmit("يمكنك مقارنة حتى 3 عقارات كحد أقصى")
            } else {
                current.add(propertyId)
                _compareIds.value = current
                _feedback.tryEmit("تمت إضافة العقار للمقارنة (${current.size}/3)")
            }
        }
    }

    fun toggleFavorite(property: PropertyItem) {
        val isFav = favorites.value.any { it.propertyId == property.id }
        viewModelScope.launch {
            repository.toggleFavorite(property, isFav)
                .onSuccess {
                    _feedback.tryEmit(if (isFav) "تمت الإزالة من المفضلة" else "تمت الإضافة إلى المفضلة")
                }
                .onFailure { err ->
                    _feedback.tryEmit(friendlyFirestoreMessage(err, "تعذر تحديث المفضلة"))
                }
        }
    }

    fun createProperty(property: PropertyItem, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            repository.createProperty(property, isAdminUser = isAdmin)
                .onSuccess { newId ->
                    val msg = if (isAdmin) {
                        "تم نشر العقار بنجاح"
                    } else {
                        "تم إرسال إعلانك بنجاح وهو قيد مراجعة الإدارة"
                    }
                    _feedback.tryEmit(msg)
                    onSuccess(newId)
                }
                .onFailure { err ->
                    _feedback.tryEmit(friendlyFirestoreMessage(err, err.message ?: "تعذر حفظ العقار"))
                }
        }
    }

    fun updateOwnProperty(property: PropertyItem, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateOwnProperty(property)
                .onSuccess {
                    _feedback.tryEmit("تم تحديث العقار وإرساله للمراجعة")
                    onSuccess()
                }
                .onFailure { err ->
                    _feedback.tryEmit(friendlyFirestoreMessage(err, err.message ?: "تعذر تعديل العقار"))
                }
        }
    }

    fun togglePropertyPause(propertyId: String, pause: Boolean) {
        viewModelScope.launch {
            repository.toggleOwnPropertyPause(propertyId, pause)
                .onSuccess {
                    _feedback.tryEmit(if (pause) "تم إيقاف الإعلان مؤقتاً" else "تمت إعادة نشر الإعلان")
                }
                .onFailure { err ->
                    _feedback.tryEmit(friendlyFirestoreMessage(err, "تعذر تغيير حالة الإعلان"))
                }
        }
    }

    fun deleteProperty(propertyId: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteProperty(propertyId)
                .onSuccess {
                    _feedback.tryEmit("تم حذف الإعلان بنجاح")
                    onDeleted()
                }
                .onFailure { err ->
                    _feedback.tryEmit(friendlyFirestoreMessage(err, "تعذر حذف الإعلان"))
                }
        }
    }

    fun sendMessage(
        propertyId: String,
        propertyTitle: String,
        recipientId: String,
        recipientName: String,
        text: String,
        onSent: (String) -> Unit = {}
    ) {
        val senderName = userProfile.value?.displayName ?: initialDisplayName.ifBlank { "مستخدم بيتي" }
        viewModelScope.launch {
            repository.startOrSendMessage(
                propertyId = propertyId,
                propertyTitle = propertyTitle,
                recipientId = recipientId,
                recipientName = recipientName,
                senderName = senderName,
                text = text
            ).onSuccess { convId ->
                _activeConversationId.value = convId
                onSent(convId)
            }.onFailure { err ->
                _feedback.tryEmit(friendlyFirestoreMessage(err, "تعذر إرسال الرسالة"))
            }
        }
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(notificationId)
        }
    }

    fun addReview(propertyId: String, rating: Int, comment: String, onDone: () -> Unit) {
        val authorName = userProfile.value?.displayName ?: initialDisplayName.ifBlank { "مستخدم بيتي" }
        viewModelScope.launch {
            repository.addReview(propertyId, authorName, rating, comment)
                .onSuccess {
                    _feedback.tryEmit("تم إضافة تقييمك بنجاح")
                    onDone()
                }
                .onFailure { err ->
                    _feedback.tryEmit(friendlyFirestoreMessage(err, "تعذر إضافة التقييم"))
                }
        }
    }

    fun submitReport(
        propertyId: String?,
        propertyTitle: String?,
        reportType: String,
        reason: String,
        details: String,
        onDone: () -> Unit
    ) {
        val name = userProfile.value?.displayName ?: initialDisplayName.ifBlank { "مستخدم بيتي" }
        viewModelScope.launch {
            repository.submitReport(name, propertyId, propertyTitle, reportType, reason, details)
                .onSuccess {
                    _feedback.tryEmit("تم استلام رسالتك بنجاح وسيتابعها فريقنا")
                    onDone()
                }
                .onFailure { err ->
                    _feedback.tryEmit(friendlyFirestoreMessage(err, "تعذر إرسال البلاغ"))
                }
        }
    }

    fun updateProfile(
        displayName: String,
        phone: String?,
        city: String?,
        accountType: AccountType,
        avatarUrl: String?,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.updateUserProfile(displayName, phone, city, accountType, avatarUrl)
                .onSuccess {
                    _feedback.tryEmit("تم تحديث الملف الشخصي بنجاح")
                    onDone()
                }
                .onFailure { err ->
                    _feedback.tryEmit(friendlyFirestoreMessage(err, "تعذر حفظ البيانات"))
                }
        }
    }

    private fun applyPropertyFilter(items: List<PropertyItem>, f: PropertyFilter): List<PropertyItem> {
        val nowSeconds = System.currentTimeMillis() / 1000L
        val filtered = items.filter { p ->
            val matchesQuery = f.queryText.isBlank() || listOf(
                p.title, p.description, p.governorate, p.city, p.district, p.address
            ).any { it.contains(f.queryText.trim(), ignoreCase = true) }

            val matchesGov = f.governorate.isBlank() || p.governorate == f.governorate
            val matchesCity = f.city.isBlank() || p.city == f.city
            val matchesDistrict = f.district.isBlank() || p.district.contains(f.district.trim(), ignoreCase = true)
            val matchesType = f.propertyType == null || p.propertyType == f.propertyType.key
            val matchesListing = f.listingType == null || p.listingType == f.listingType.key
            val matchesMinPrice = f.minPrice == null || p.price >= f.minPrice
            val matchesMaxPrice = f.maxPrice == null || p.price <= f.maxPrice
            val matchesMinArea = f.minArea == null || p.areaSqm >= f.minArea
            val matchesMaxArea = f.maxArea == null || p.areaSqm <= f.maxArea
            val matchesBeds = f.minBedrooms == null || p.bedrooms >= f.minBedrooms
            val matchesBaths = f.minBathrooms == null || p.bathrooms >= f.minBathrooms
            val matchesMinFloor = f.minFloor == null || p.floor >= f.minFloor
            val matchesMaxFloor = f.maxFloor == null || p.floor <= f.maxFloor
            val matchesFinish = f.finishingType == null || p.finishingType == f.finishingType.key
            val matchesFurnished = f.furnishedOnly == null || p.furnished == f.furnishedOnly
            val matchesElevator = !f.elevatorOnly || p.hasElevator
            val matchesGarage = !f.garageOnly || p.hasGarage
            val matchesBalcony = !f.balconyOnly || p.hasBalcony
            val matchesAge = f.maxDaysOld == null || ((nowSeconds - (p.createdAt?.seconds ?: nowSeconds)) <= f.maxDaysOld * 86400L)

            matchesQuery && matchesGov && matchesCity && matchesDistrict &&
                matchesType && matchesListing && matchesMinPrice && matchesMaxPrice &&
                matchesMinArea && matchesMaxArea && matchesBeds && matchesBaths &&
                matchesMinFloor && matchesMaxFloor && matchesFinish && matchesFurnished &&
                matchesElevator && matchesGarage && matchesBalcony && matchesAge
        }

        return when (f.sortBy) {
            SortOption.NEWEST -> filtered.sortedByDescending { it.createdAt?.seconds ?: 0L }
            SortOption.PRICE_LOW -> filtered.sortedBy { it.price }
            SortOption.PRICE_HIGH -> filtered.sortedByDescending { it.price }
            SortOption.MOST_VIEWED -> filtered.sortedByDescending { it.viewsCount }
            SortOption.AREA_LARGE -> filtered.sortedByDescending { it.areaSqm }
        }
    }
}
