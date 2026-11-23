package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Villa
import androidx.compose.material.icons.rounded.AddHomeWork
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CategoryItem
import com.example.data.model.ListingType
import com.example.data.model.LocationItem
import com.example.data.model.PropertyItem
import com.example.data.model.PropertyType
import com.example.data.model.UiState
import com.example.ui.components.BeityTopHeader
import com.example.ui.components.PropertyCard
import com.example.ui.theme.BeityGold
import com.example.ui.theme.BeityGoldLight
import com.example.ui.theme.BeityNavy
import com.example.ui.theme.BeityNavyDark
import com.example.ui.theme.BeityWhite

@Composable
fun HomeScreen(
    propertiesState: UiState<List<PropertyItem>>,
    categories: List<CategoryItem>,
    locations: List<LocationItem>,
    favoriteIds: Set<String>,
    compareIds: List<String>,
    unreadNotifications: Int,
    isAdmin: Boolean,
    onQuickNav: (String) -> Unit,
    onSearchSubmit: (governorate: String, city: String, district: String, type: PropertyType?, listing: ListingType?, maxPrice: Double?) -> Unit,
    onSelectCategory: (PropertyType) -> Unit,
    onPropertyClick: (PropertyItem) -> Unit,
    onToggleFavorite: (PropertyItem) -> Unit,
    onToggleCompare: (String) -> Unit,
    onAddProperty: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    var selectedGov by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("") }
    var districtInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<PropertyType?>(null) }
    var selectedListing by remember { mutableStateOf<ListingType?>(null) }
    var maxPriceText by remember { mutableStateOf("") }

    var showGovMenu by remember { mutableStateOf(false) }
    var showCityMenu by remember { mutableStateOf(false) }
    var showTypeMenu by remember { mutableStateOf(false) }

    val suggestedList = remember(propertiesState) {
        (propertiesState as? UiState.Success)?.data?.shuffled()?.take(4) ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BeityTopHeader(
            unreadNotifications = unreadNotifications,
            isAdmin = isAdmin,
            onAddProperty = onAddProperty,
            onOpenNotifications = onOpenNotifications,
            onOpenAdmin = onOpenAdmin,
            onQuickNav = onQuickNav
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Hero Welcome Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_luxury),
                        contentDescription = "اعثر على بيت أحلامك",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        BeityNavyDark.copy(alpha = 0.65f),
                                        BeityNavy.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = BeityGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "كل ما تحتاجه في عالم العقارات",
                                color = BeityGoldLight,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Text(
                            text = "اعثر على بيت أحلامك بسهولة",
                            color = BeityWhite,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "اكتشف أفضل العقارات للبيع والإيجار في مكان واحد، واختر منزلك القادم بثقة.",
                            color = BeityWhite.copy(alpha = 0.86f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 2. Advanced Search Card on Home
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ابحث عن عقارك المثالي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Sale / Rent toggle pills
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedListing == null,
                                onClick = { selectedListing = null },
                                label = { Text("الكل") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BeityNavy,
                                    selectedLabelColor = BeityGold
                                )
                            )
                            ListingType.entries.forEach { lt ->
                                FilterChip(
                                    selected = selectedListing == lt,
                                    onClick = { selectedListing = lt },
                                    label = { Text(lt.labelAr) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BeityNavy,
                                        selectedLabelColor = BeityGold
                                    )
                                )
                            }
                        }

                        // Governorate & City Dropdowns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { showGovMenu = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = selectedGov.ifBlank { "المحافظة" },
                                        maxLines = 1
                                    )
                                }
                                DropdownMenu(
                                    expanded = showGovMenu,
                                    onDismissRequest = { showGovMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("كل المحافظات") },
                                        onClick = {
                                            selectedGov = ""
                                            selectedCity = ""
                                            showGovMenu = false
                                        }
                                    )
                                    locations.forEach { loc ->
                                        DropdownMenuItem(
                                            text = { Text(loc.nameAr) },
                                            onClick = {
                                                selectedGov = loc.nameAr
                                                selectedCity = ""
                                                showGovMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                val cities = locations.firstOrNull { it.nameAr == selectedGov }?.cities
                                    ?: locations.flatMap { it.cities }.distinct()
                                OutlinedButton(
                                    onClick = { showCityMenu = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = selectedCity.ifBlank { "المدينة" },
                                        maxLines = 1
                                    )
                                }
                                DropdownMenu(
                                    expanded = showCityMenu,
                                    onDismissRequest = { showCityMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("كل المدن") },
                                        onClick = {
                                            selectedCity = ""
                                            showCityMenu = false
                                        }
                                    )
                                    cities.forEach { city ->
                                        DropdownMenuItem(
                                            text = { Text(city) },
                                            onClick = {
                                                selectedCity = city
                                                showCityMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = districtInput,
                                onValueChange = { districtInput = it },
                                label = { Text("المنطقة / الحي") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { showTypeMenu = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                ) {
                                    Text(selectedType?.labelAr ?: "نوع العقار")
                                }
                                DropdownMenu(
                                    expanded = showTypeMenu,
                                    onDismissRequest = { showTypeMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("جميع الأنواع") },
                                        onClick = {
                                            selectedType = null
                                            showTypeMenu = false
                                        }
                                    )
                                    PropertyType.entries.forEach { pt ->
                                        DropdownMenuItem(
                                            text = { Text(pt.labelAr) },
                                            onClick = {
                                                selectedType = pt
                                                showTypeMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = maxPriceText,
                            onValueChange = { maxPriceText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("السعر الأقصى (ج.م)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                onSearchSubmit(
                                    selectedGov,
                                    selectedCity,
                                    districtInput,
                                    selectedType,
                                    selectedListing,
                                    maxPriceText.toDoubleOrNull()
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BeityGold,
                                contentColor = BeityNavy
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("home_search_submit")
                        ) {
                            Icon(Icons.Outlined.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ابحث الآن", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. Property Categories
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(title = "أقسام العقارات", onSeeAll = { onQuickNav("explore") })
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        categories.forEach { cat ->
                            val pt = PropertyType.fromKey(cat.key)
                            CategoryCard(
                                label = cat.nameAr,
                                icon = categoryIcon(pt),
                                onClick = { onSelectCategory(pt) }
                            )
                        }
                    }
                }
            }

            // 4-8. Real Firestore Property Sections
            when (propertiesState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = BeityGold)
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                text = propertiesState.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    val all = propertiesState.data
                    if (all.isEmpty()) {
                        item {
                            EmptyListingsCard(
                                isAdmin = isAdmin,
                                onAddProperty = onAddProperty,
                                onOpenAdmin = onOpenAdmin
                            )
                        }
                    } else {
                        val featured = all.filter { it.featured }.ifEmpty { all.take(4) }
                        val latest = all.sortedByDescending { it.createdAt?.seconds ?: 0L }.take(6)
                        val opportunities = all.sortedBy { it.price / it.areaSqm.coerceAtLeast(1.0) }.take(5)
                        val mostViewed = all.sortedByDescending { it.viewsCount }.take(5)
                        val suggested = suggestedList

                        item {
                            PropertyHorizontalSection(
                                title = "العقارات المميزة",
                                items = featured,
                                favoriteIds = favoriteIds,
                                compareIds = compareIds,
                                onSeeAll = { onQuickNav("explore") },
                                onPropertyClick = onPropertyClick,
                                onToggleFavorite = onToggleFavorite,
                                onToggleCompare = onToggleCompare
                            )
                        }
                        item {
                            PropertyHorizontalSection(
                                title = "أحدث العقارات",
                                items = latest,
                                favoriteIds = favoriteIds,
                                compareIds = compareIds,
                                onSeeAll = { onQuickNav("explore") },
                                onPropertyClick = onPropertyClick,
                                onToggleFavorite = onToggleFavorite,
                                onToggleCompare = onToggleCompare
                            )
                        }
                        item {
                            PropertyHorizontalSection(
                                title = "أفضل الفرص العقارية",
                                items = opportunities,
                                favoriteIds = favoriteIds,
                                compareIds = compareIds,
                                onSeeAll = { onQuickNav("explore") },
                                onPropertyClick = onPropertyClick,
                                onToggleFavorite = onToggleFavorite,
                                onToggleCompare = onToggleCompare
                            )
                        }
                        item {
                            PropertyHorizontalSection(
                                title = "عقارات مقترحة لك",
                                items = suggested,
                                favoriteIds = favoriteIds,
                                compareIds = compareIds,
                                onSeeAll = { onQuickNav("explore") },
                                onPropertyClick = onPropertyClick,
                                onToggleFavorite = onToggleFavorite,
                                onToggleCompare = onToggleCompare
                            )
                        }
                        item {
                            PropertyHorizontalSection(
                                title = "الأكثر مشاهدة",
                                items = mostViewed,
                                favoriteIds = favoriteIds,
                                compareIds = compareIds,
                                onSeeAll = { onQuickNav("explore") },
                                onPropertyClick = onPropertyClick,
                                onToggleFavorite = onToggleFavorite,
                                onToggleCompare = onToggleCompare
                            )
                        }
                    }
                }
            }

            // 10. Promotional CTA Banner to Add Property
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = BeityNavy)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "هل تمتلك عقاراً للبيع أو الإيجار؟",
                            color = BeityGold,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "انشر عقارك على منصة بيتي مجاناً وتواصل مباشرة مع آلاف الباحثين الجادين في مصر والعالم العربي.",
                            color = BeityWhite.copy(alpha = 0.88f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = onAddProperty,
                            colors = ButtonDefaults.buttonColors(containerColor = BeityGold, contentColor = BeityNavy),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("promo_add_property_button")
                        ) {
                            Icon(Icons.Rounded.AddHomeWork, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("أضف عقارك الآن", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onSeeAll)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "عرض الكل",
                style = MaterialTheme.typography.labelLarge,
                color = BeityGold
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                tint = BeityGold,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun PropertyHorizontalSection(
    title: String,
    items: List<PropertyItem>,
    favoriteIds: Set<String>,
    compareIds: List<String>,
    onSeeAll: () -> Unit,
    onPropertyClick: (PropertyItem) -> Unit,
    onToggleFavorite: (PropertyItem) -> Unit,
    onToggleCompare: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = title, onSeeAll = onSeeAll)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(items, key = { "${title}_${it.id}" }) { property ->
                PropertyCard(
                    property = property,
                    isFavorite = favoriteIds.contains(property.id),
                    isCompared = compareIds.contains(property.id),
                    onClick = { onPropertyClick(property) },
                    onToggleFavorite = { onToggleFavorite(property) },
                    onToggleCompare = { onToggleCompare(property.id) },
                    modifier = Modifier.width(290.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(106.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BeityNavy),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = BeityGold)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EmptyListingsCard(
    isAdmin: Boolean,
    onAddProperty: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.HomeWork,
                contentDescription = null,
                tint = BeityGold,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "لا توجد عقارات منشورة في قاعدة البيانات حتى الآن",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "ابدأ بإضافة أول عقار عبر النموذج الاحترافي، أو استخدم لوحة الإدارة لمراجعة العقارات.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onAddProperty,
                colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold)
            ) {
                Text("أضف أول عقار الآن")
            }
            if (isAdmin) {
                OutlinedButton(onClick = onOpenAdmin) {
                    Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فتح لوحة تحكم الإدارة")
                }
            }
        }
    }
}

private fun categoryIcon(type: PropertyType): ImageVector = when (type) {
    PropertyType.APARTMENT -> Icons.Outlined.Apartment
    PropertyType.VILLA -> Icons.Outlined.Villa
    PropertyType.LAND -> Icons.Outlined.Landscape
    PropertyType.SHOP -> Icons.Outlined.Storefront
    PropertyType.OFFICE -> Icons.Outlined.Business
    PropertyType.CHALET -> Icons.Outlined.HomeWork
    PropertyType.INVESTMENT -> Icons.AutoMirrored.Outlined.TrendingUp
}
