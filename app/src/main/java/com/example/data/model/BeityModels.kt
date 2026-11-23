package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class AccountType(val key: String, val labelAr: String) {
    SEEKER("seeker", "باحث عن عقار"),
    OWNER("owner", "مالك / معلن");

    companion object {
        fun fromKey(key: String?): AccountType =
            entries.firstOrNull { it.key == key } ?: SEEKER
    }
}

enum class AccountStatus(val key: String, val labelAr: String) {
    ACTIVE("active", "نشط"),
    SUSPENDED("suspended", "موقوف");

    companion object {
        fun fromKey(key: String?): AccountStatus =
            entries.firstOrNull { it.key == key } ?: ACTIVE
    }
}

enum class PropertyType(val key: String, val labelAr: String) {
    APARTMENT("apartment", "شقق سكنية"),
    VILLA("villa", "فيلات"),
    LAND("land", "أراضي"),
    SHOP("shop", "محلات تجارية"),
    OFFICE("office", "مكاتب إدارية"),
    CHALET("chalet", "شاليهات"),
    INVESTMENT("investment", "عقارات استثمارية");

    companion object {
        fun fromKey(key: String?): PropertyType =
            entries.firstOrNull { it.key == key } ?: APARTMENT
    }
}

enum class ListingType(val key: String, val labelAr: String) {
    SALE("sale", "للبيع"),
    RENT("rent", "للإيجار");

    companion object {
        fun fromKey(key: String?): ListingType =
            entries.firstOrNull { it.key == key } ?: SALE
    }
}

enum class FinishingType(val key: String, val labelAr: String) {
    SUPER_LUX("super_lux", "سوبر لوكس"),
    LUX("lux", "لوكس"),
    SEMI_FINISHED("semi_finished", "نصف تشطيب"),
    UNFINISHED("unfinished", "على الطوب"),
    FURNISHED("furnished", "تشطيب فندقي");

    companion object {
        fun fromKey(key: String?): FinishingType =
            entries.firstOrNull { it.key == key } ?: SUPER_LUX
    }
}

enum class PropertyStatus(val key: String, val labelAr: String) {
    PENDING("pending", "قيد المراجعة"),
    APPROVED("approved", "مقبول ومنشور"),
    REJECTED("rejected", "مرفوض"),
    PAUSED("paused", "متوقف مؤقتاً");

    companion object {
        fun fromKey(key: String?): PropertyStatus =
            entries.firstOrNull { it.key == key } ?: PENDING
    }
}

enum class SortOption(val labelAr: String) {
    NEWEST("الأحدث"),
    PRICE_LOW("الأقل سعراً"),
    PRICE_HIGH("الأعلى سعراً"),
    MOST_VIEWED("الأكثر مشاهدة"),
    AREA_LARGE("المساحة الأكبر")
}

data class PropertyFilter(
    val queryText: String = "",
    val governorate: String = "",
    val city: String = "",
    val district: String = "",
    val propertyType: PropertyType? = null,
    val listingType: ListingType? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minArea: Double? = null,
    val maxArea: Double? = null,
    val minBedrooms: Int? = null,
    val minBathrooms: Int? = null,
    val minFloor: Int? = null,
    val maxFloor: Int? = null,
    val finishingType: FinishingType? = null,
    val furnishedOnly: Boolean? = null,
    val elevatorOnly: Boolean = false,
    val garageOnly: Boolean = false,
    val balconyOnly: Boolean = false,
    val maxDaysOld: Int? = null,
    val sortBy: SortOption = SortOption.NEWEST
) {
    fun hasActiveFilters(): Boolean =
        queryText.isNotBlank() ||
            governorate.isNotBlank() ||
            city.isNotBlank() ||
            district.isNotBlank() ||
            propertyType != null ||
            listingType != null ||
            minPrice != null ||
            maxPrice != null ||
            minArea != null ||
            maxArea != null ||
            minBedrooms != null ||
            minBathrooms != null ||
            minFloor != null ||
            maxFloor != null ||
            finishingType != null ||
            furnishedOnly != null ||
            elevatorOnly ||
            garageOnly ||
            balconyOnly ||
            maxDaysOld != null

    fun summaryAr(): String {
        val parts = mutableListOf<String>()
        listingType?.let { parts.add(it.labelAr) }
        propertyType?.let { parts.add(it.labelAr) }
        if (governorate.isNotBlank()) parts.add(governorate)
        if (city.isNotBlank()) parts.add(city)
        if (district.isNotBlank()) parts.add(district)
        maxPrice?.let { parts.add("حتى ${formatPriceAr(it)}") }
        if (queryText.isNotBlank()) parts.add("\"$queryText\"")
        return if (parts.isEmpty()) "بحث عام في العقارات" else parts.joinToString(" • ")
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val accountType: String = AccountType.SEEKER.key,
    val status: String = AccountStatus.ACTIVE.key,
    val avatarUrl: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PublicProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val accountType: String = AccountType.SEEKER.key,
    val city: String? = null,
    val verified: Boolean = false,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PropertyItem(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val contactPhone: String = "",
    val title: String = "",
    val description: String = "",
    val propertyType: String = PropertyType.APARTMENT.key,
    val listingType: String = ListingType.SALE.key,
    val price: Double = 0.0,
    val areaSqm: Double = 0.0,
    val governorate: String = "",
    val city: String = "",
    val district: String = "",
    val address: String = "",
    val bedrooms: Long = 0L,
    val bathrooms: Long = 0L,
    val floor: Long = 0L,
    val finishingType: String = FinishingType.SUPER_LUX.key,
    val furnished: Boolean = false,
    val hasElevator: Boolean = false,
    val hasGarage: Boolean = false,
    val hasBalcony: Boolean = false,
    val features: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val videoUrl: String? = null,
    val latitude: Double = 30.0444,
    val longitude: Double = 31.2357,
    val status: String = PropertyStatus.PENDING.key,
    val featured: Boolean = false,
    val viewsCount: Long = 0L,
    val rejectionReason: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun propertyTypeEnum(): PropertyType = PropertyType.fromKey(propertyType)
    fun listingTypeEnum(): ListingType = ListingType.fromKey(listingType)
    fun finishingEnum(): FinishingType = FinishingType.fromKey(finishingType)
    fun statusEnum(): PropertyStatus = PropertyStatus.fromKey(status)
}

@Keep
@IgnoreExtraProperties
data class PropertyReview(
    @DocumentId val id: String = "",
    val propertyId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val rating: Long = 5L,
    val comment: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FavoriteItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val propertyId: String = "",
    val title: String? = null,
    val price: Double? = null,
    val city: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SearchHistoryItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val querySummary: String = "",
    val governorate: String? = null,
    val propertyType: String? = null,
    val listingType: String? = null,
    val maxPrice: Double? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class NotificationItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val senderId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "system",
    val targetId: String? = null,
    val read: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val participantIds: List<String> = emptyList(),
    val propertyId: String = "",
    val propertyTitle: String = "",
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastSenderId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val participantIds: List<String> = emptyList(),
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ReportItem(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val propertyId: String? = null,
    val propertyTitle: String? = null,
    val reportType: String = "property_report",
    val reason: String = "",
    val details: String = "",
    val status: String = "open",
    val adminNotes: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CategoryItem(
    @DocumentId val id: String = "",
    val key: String = "",
    val nameAr: String = "",
    val enabled: Boolean = true,
    val sortOrder: Long = 0L
)

@Keep
@IgnoreExtraProperties
data class LocationItem(
    @DocumentId val id: String = "",
    val nameAr: String = "",
    val cities: List<String> = emptyList(),
    val enabled: Boolean = true
)

fun formatPriceAr(price: Double): String {
    return when {
        price >= 1_000_000 -> {
            val millions = price / 1_000_000.0
            val formatted = if (millions % 1.0 == 0.0) {
                "${millions.toLong()}"
            } else {
                String.format(java.util.Locale.US, "%.2f", millions).trimEnd('0').trimEnd('.')
            }
            "$formatted مليون ج.م"
        }
        price >= 1_000 -> "${String.format(java.util.Locale.US, "%,.0f", price)} ج.م"
        else -> "${price.toLong()} ج.م"
    }
}
