package com.example.data.model

data class GovernoratePreset(
    val id: String,
    val nameAr: String,
    val lat: Double,
    val lng: Double,
    val cities: List<String>
)

object EgyptCatalog {
    val governorates: List<GovernoratePreset> = listOf(
        GovernoratePreset(
            id = "cairo",
            nameAr = "القاهرة",
            lat = 30.0444,
            lng = 31.2357,
            cities = listOf("القاهرة الجديدة", "العاصمة الإدارية", "مدينة نصر", "الزمالك", "المعادي", "مصر الجديدة", "مدينتي", "الرحاب")
        ),
        GovernoratePreset(
            id = "giza",
            nameAr = "الجيزة",
            lat = 30.0131,
            lng = 31.2089,
            cities = listOf("الشيخ زايد", "6 أكتوبر", "المهندسين", "الدقي", "حدائق الأهرام", "الهرم")
        ),
        GovernoratePreset(
            id = "alexandria",
            nameAr = "الإسكندرية",
            lat = 31.2001,
            lng = 29.9187,
            cities = listOf("سموحة", "سيدي جابر", "كفر عبده", "ستانلي", "المنتزه", "برج العرب")
        ),
        GovernoratePreset(
            id = "matrouh",
            nameAr = "مطروح والساحل",
            lat = 30.8400,
            lng = 28.9550,
            cities = listOf("العلمين الجديدة", "سيدي عبد الرحمن", "رأس الحكمة", "الضبعة", "مرسى مطروح")
        ),
        GovernoratePreset(
            id = "red_sea",
            nameAr = "البحر الأحمر",
            lat = 27.2579,
            lng = 33.8116,
            cities = listOf("الجونة", "الغردقة", "سهل حشيش", "مكادي باي", "مرسى علم")
        ),
        GovernoratePreset(
            id = "suez",
            nameAr = "السويس والعين السخنة",
            lat = 29.5960,
            lng = 32.3450,
            cities = listOf("العين السخنة", "الجلالة", "بورتو السخنة", "السويس")
        ),
        GovernoratePreset(
            id = "dakahlia",
            nameAr = "الدقهلية",
            lat = 31.0409,
            lng = 31.3785,
            cities = listOf("المنصورة", "المنصورة الجديدة", "طلخا", "جمصة")
        ),
        GovernoratePreset(
            id = "sharqia",
            nameAr = "الشرقية",
            lat = 30.5877,
            lng = 31.5020,
            cities = listOf("العاشر من رمضان", "الزقازيق", "بلبيس")
        )
    )

    val defaultCategories: List<CategoryItem> = PropertyType.entries.mapIndexed { idx, type ->
        CategoryItem(
            id = type.key,
            key = type.key,
            nameAr = type.labelAr,
            enabled = true,
            sortOrder = idx.toLong()
        )
    }

    val commonFeatures = listOf(
        "حديقة خاصة", "حمام سباحة", "إطلالة على النيل", "إطلالة على البحر",
        "تراس واسع", "غرفة مربية", "تكييف مركزي", "مطبخ مجهز",
        "أرضيات رخام", "سمارت هوم", "واجهة بحري", "ناصية"
    )

    val commonAmenities = listOf(
        "أمن وحراسة 24 ساعة", "كاميرات مراقبة", "نادي صحي وجيم", "مساحات خضراء",
        "جراج مغطى", "مول تجاري قريب", "مدارس دولية قريبة", "بوابات إلكترونية"
    )

    val presetImages = listOf(
        "preset:villa" to "فيلا عصرية فاخرة",
        "preset:apartment" to "شقة وبنتهاوس حديث",
        "preset:chalet" to "شاليه ساحلي مطل على البحر",
        "preset:hero" to "كمبوند سكني راقٍ"
    )

    fun coordinatesFor(governorate: String, city: String): Pair<Double, Double> {
        val base = governorates.firstOrNull { it.nameAr == governorate } ?: governorates.first()
        val cityHash = (city.hashCode() % 100) / 1000.0
        return Pair(base.lat + cityHash, base.lng + cityHash)
    }

    fun sampleDemoPropertiesForAdmin(adminId: String, adminName: String): List<PropertyItem> = listOf(
        PropertyItem(
            ownerId = adminId,
            ownerName = adminName,
            contactPhone = "+201001234567",
            title = "فيلا مستقلة فاخرة في كمبوند ميفيدا بالتجمع الخامس",
            description = "فيلا مميزة بتصميم معماري حديث وتشطيب سوبر لوكس مع حديقة خاصة وحمام سباحة وتراس مطل على المساحات الخضراء، قريبة من الجامعة الأمريكية وكافة الخدمات.",
            propertyType = PropertyType.VILLA.key,
            listingType = ListingType.SALE.key,
            price = 18500000.0,
            areaSqm = 450.0,
            governorate = "القاهرة",
            city = "القاهرة الجديدة",
            district = "التجمع الخامس",
            address = "كمبوند ميفيدا، شارع التسعين الجنوبي",
            bedrooms = 5,
            bathrooms = 5,
            floor = 0,
            finishingType = FinishingType.SUPER_LUX.key,
            furnished = false,
            hasElevator = false,
            hasGarage = true,
            hasBalcony = true,
            features = listOf("حديقة خاصة", "حمام سباحة", "سمارت هوم", "غرفة مربية"),
            amenities = listOf("أمن وحراسة 24 ساعة", "نادي صحي وجيم", "مساحات خضراء"),
            imageUrls = listOf("preset:villa", "preset:hero"),
            latitude = 30.0155,
            longitude = 31.4910,
            status = PropertyStatus.APPROVED.key,
            featured = true,
            viewsCount = 148
        ),
        PropertyItem(
            ownerId = adminId,
            ownerName = adminName,
            contactPhone = "+201009876543",
            title = "بنتهاوس بإطلالة بانورامية على النيل في الزمالك",
            description = "بنتهاوس راقٍ مفروش بالكامل بأرقى التشطيبات الفندقية وأرضيات الرخام الإيطالي، يضم 3 غرف نوم ماستر وتراس مفتوح مباشرة على النيل.",
            propertyType = PropertyType.APARTMENT.key,
            listingType = ListingType.RENT.key,
            price = 95000.0,
            areaSqm = 285.0,
            governorate = "القاهرة",
            city = "الزمالك",
            district = "شارع أبو الفدا",
            address = "برج النيل الذهبي، الزمالك",
            bedrooms = 3,
            bathrooms = 3,
            floor = 11,
            finishingType = FinishingType.FURNISHED.key,
            furnished = true,
            hasElevator = true,
            hasGarage = true,
            hasBalcony = true,
            features = listOf("إطلالة على النيل", "أرضيات رخام", "تكييف مركزي"),
            amenities = listOf("أمن وحراسة 24 ساعة", "جراج مغطى", "كاميرات مراقبة"),
            imageUrls = listOf("preset:apartment", "preset:hero"),
            latitude = 30.0609,
            longitude = 31.2197,
            status = PropertyStatus.APPROVED.key,
            featured = true,
            viewsCount = 234
        ),
        PropertyItem(
            ownerId = adminId,
            ownerName = adminName,
            contactPhone = "+201112223344",
            title = "شاليه صف أول على البحر في سيدي عبد الرحمن",
            description = "شاليه صيفي فاخر بتشطيب كامل في الساحل الشمالي، يضم 3 غرف وتراس واسع يطل على اللاجون والبحر مباشرة جاهز للاستلام الفوري.",
            propertyType = PropertyType.CHALET.key,
            listingType = ListingType.SALE.key,
            price = 9200000.0,
            areaSqm = 165.0,
            governorate = "مطروح والساحل",
            city = "سيدي عبد الرحمن",
            district = "مراسي الساحل الشمالي",
            address = "كيلو 125 طريق الإسكندرية مطروح",
            bedrooms = 3,
            bathrooms = 2,
            floor = 1,
            finishingType = FinishingType.SUPER_LUX.key,
            furnished = true,
            hasElevator = false,
            hasGarage = true,
            hasBalcony = true,
            features = listOf("إطلالة على البحر", "تراس واسع", "مطبخ مجهز"),
            amenities = listOf("أمن وحراسة 24 ساعة", "مساحات خضراء", "بوابات إلكترونية"),
            imageUrls = listOf("preset:chalet", "preset:villa"),
            latitude = 30.9680,
            longitude = 28.7420,
            status = PropertyStatus.APPROVED.key,
            featured = true,
            viewsCount = 192
        ),
        PropertyItem(
            ownerId = adminId,
            ownerName = adminName,
            contactPhone = "+201223334455",
            title = "مقر إداري متميز في محور 26 يوليو بالشيخ زايد",
            description = "مكتب إداري بواجهة زجاجية حديثة في مول إداري متكامل الخدمات بالشيخ زايد، مناسب للشركات والمكاتب الاستشارية الكبرى.",
            propertyType = PropertyType.OFFICE.key,
            listingType = ListingType.SALE.key,
            price = 6800000.0,
            areaSqm = 140.0,
            governorate = "الجيزة",
            city = "الشيخ زايد",
            district = "أركان بلازا",
            address = "محور 26 يوليو، الشيخ زايد",
            bedrooms = 4,
            bathrooms = 2,
            floor = 4,
            finishingType = FinishingType.LUX.key,
            furnished = false,
            hasElevator = true,
            hasGarage = true,
            hasBalcony = false,
            features = listOf("تكييف مركزي", "واجهة بحري", "ناصية"),
            amenities = listOf("أمن وحراسة 24 ساعة", "كاميرات مراقبة", "جراج مغطى"),
            imageUrls = listOf("preset:hero", "preset:apartment"),
            latitude = 30.0221,
            longitude = 30.9990,
            status = PropertyStatus.APPROVED.key,
            featured = false,
            viewsCount = 87
        ),
        PropertyItem(
            ownerId = adminId,
            ownerName = adminName,
            contactPhone = "+201556667788",
            title = "شقة سكنية راقية في سموحة بالإسكندرية",
            description = "شقة واسعة بمساحة 210 متر في أرقى شوارع سموحة بالإسكندرية، مشمسة بالكامل وتطل على حدائق نادي سموحة.",
            propertyType = PropertyType.APARTMENT.key,
            listingType = ListingType.SALE.key,
            price = 5400000.0,
            areaSqm = 210.0,
            governorate = "الإسكندرية",
            city = "سموحة",
            district = "ميدان فيكتور عمانويل",
            address = "برج الأندلس، سموحة",
            bedrooms = 3,
            bathrooms = 2,
            floor = 6,
            finishingType = FinishingType.SUPER_LUX.key,
            furnished = false,
            hasElevator = true,
            hasGarage = true,
            hasBalcony = true,
            features = listOf("واجهة بحري", "أرضيات رخام"),
            amenities = listOf("أمن وحراسة 24 ساعة", "مول تجاري قريب"),
            imageUrls = listOf("preset:apartment", "preset:villa"),
            latitude = 31.2136,
            longitude = 29.9465,
            status = PropertyStatus.APPROVED.key,
            featured = false,
            viewsCount = 115
        )
    )
}
