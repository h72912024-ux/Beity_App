package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.FinishingType
import com.example.data.model.ListingType
import com.example.data.model.LocationItem
import com.example.data.model.PropertyFilter
import com.example.data.model.PropertyItem
import com.example.data.model.PropertyType
import com.example.data.model.SearchHistoryItem
import com.example.data.model.SortOption
import com.example.ui.components.InteractivePropertyMap
import com.example.ui.components.PropertyCard
import com.example.ui.theme.BeityGold
import com.example.ui.theme.BeityNavy
import com.example.ui.theme.BeityWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    properties: List<PropertyItem>,
    filter: PropertyFilter,
    locations: List<LocationItem>,
    searchHistory: List<SearchHistoryItem>,
    favoriteIds: Set<String>,
    compareIds: List<String>,
    startOnMap: Boolean = false,
    onUpdateFilter: ((PropertyFilter) -> PropertyFilter) -> Unit,
    onResetFilter: () -> Unit,
    onSaveSearch: () -> Unit,
    onApplyHistory: (SearchHistoryItem) -> Unit,
    onPropertyClick: (PropertyItem) -> Unit,
    onToggleFavorite: (PropertyItem) -> Unit,
    onToggleCompare: (String) -> Unit
) {
    var showMap by remember(startOnMap) { mutableStateOf(startOnMap) }
    var showFiltersSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showHistoryMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Navy Search & Toolbar
        Surface(color = BeityNavy, contentColor = BeityWhite, shadowElevation = 4.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = filter.queryText,
                        onValueChange = { text -> onUpdateFilter { it.copy(queryText = text) } },
                        placeholder = { Text("ابحث بالمنطقة، اسم الكمبوند، أو العنوان...", color = BeityWhite.copy(alpha = 0.65f)) },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = BeityGold) },
                        trailingIcon = {
                            if (filter.queryText.isNotBlank()) {
                                IconButton(onClick = { onUpdateFilter { it.copy(queryText = "") } }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "مسح", tint = BeityWhite)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BeityWhite,
                            unfocusedTextColor = BeityWhite,
                            focusedBorderColor = BeityGold,
                            unfocusedBorderColor = BeityWhite.copy(alpha = 0.3f),
                            cursorColor = BeityGold
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("explore_search_input")
                    )

                    Surface(
                        color = if (showMap) BeityGold else BeityWhite.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showMap = !showMap }
                            .testTag("toggle_map_list_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (showMap) Icons.AutoMirrored.Outlined.ViewList else Icons.Outlined.Map,
                                contentDescription = if (showMap) "عرض القائمة" else "عرض الخريطة",
                                tint = if (showMap) BeityNavy else BeityWhite
                            )
                        }
                    }
                }

                // Action Pills: Filters, Sort, History, Result Count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = filter.hasActiveFilters(),
                        onClick = { showFiltersSheet = true },
                        leadingIcon = { Icon(Icons.Outlined.FilterList, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = { Text("فلترة متقدمة") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = BeityWhite.copy(alpha = 0.12f),
                            labelColor = BeityWhite,
                            iconColor = BeityGold,
                            selectedContainerColor = BeityGold,
                            selectedLabelColor = BeityNavy,
                            selectedLeadingIconColor = BeityNavy
                        ),
                        modifier = Modifier.testTag("open_advanced_filters_button")
                    )

                    Box {
                        FilterChip(
                            selected = false,
                            onClick = { showSortMenu = true },
                            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            label = { Text("الترتيب: ${filter.sortBy.labelAr}") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = BeityWhite.copy(alpha = 0.12f),
                                labelColor = BeityWhite,
                                iconColor = BeityGold
                            )
                        )
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.labelAr) },
                                    onClick = {
                                        onUpdateFilter { it.copy(sortBy = option) }
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    if (searchHistory.isNotEmpty()) {
                        Box {
                            FilterChip(
                                selected = false,
                                onClick = { showHistoryMenu = true },
                                leadingIcon = { Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                label = { Text("سجل البحث (${searchHistory.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = BeityWhite.copy(alpha = 0.12f),
                                    labelColor = BeityWhite,
                                    iconColor = BeityGold
                                )
                            )
                            DropdownMenu(expanded = showHistoryMenu, onDismissRequest = { showHistoryMenu = false }) {
                                searchHistory.forEach { hist ->
                                    DropdownMenuItem(
                                        text = { Text(hist.querySummary) },
                                        onClick = {
                                            onApplyHistory(hist)
                                            showHistoryMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (filter.hasActiveFilters()) {
                        TextButton(onClick = onResetFilter) {
                            Text("إعادة ضبط", color = BeityGold)
                        }
                    }
                }

                Text(
                    text = "تم العثور على ${properties.size} عقار",
                    color = BeityGold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        if (showMap) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                InteractivePropertyMap(
                    properties = properties,
                    onPropertyClick = onPropertyClick,
                    onFilterByGovernorate = { gov ->
                        onUpdateFilter { it.copy(governorate = gov) }
                    }
                )
            }
        } else {
            if (properties.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = BeityGold,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "لا توجد نتائج مطابقة للبحث الحالي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (filter.hasActiveFilters()) {
                            Button(
                                onClick = onResetFilter,
                                colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold)
                            ) {
                                Text("مسح جميع الفلاتر")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(properties, key = { it.id }) { prop ->
                        PropertyCard(
                            property = prop,
                            isFavorite = favoriteIds.contains(prop.id),
                            isCompared = compareIds.contains(prop.id),
                            onClick = { onPropertyClick(prop) },
                            onToggleFavorite = { onToggleFavorite(prop) },
                            onToggleCompare = { onToggleCompare(prop.id) }
                        )
                    }
                }
            }
        }
    }

    if (showFiltersSheet) {
        AdvancedFiltersSheet(
            filter = filter,
            locations = locations,
            onDismiss = { showFiltersSheet = false },
            onApply = { updated ->
                onUpdateFilter { updated }
                onSaveSearch()
                showFiltersSheet = false
            },
            onReset = {
                onResetFilter()
                showFiltersSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedFiltersSheet(
    filter: PropertyFilter,
    locations: List<LocationItem>,
    onDismiss: () -> Unit,
    onApply: (PropertyFilter) -> Unit,
    onReset: () -> Unit
) {
    var draft by remember(filter) { mutableStateOf(filter) }
    var minPriceTxt by remember(filter) { mutableStateOf(filter.minPrice?.toLong()?.toString().orEmpty()) }
    var maxPriceTxt by remember(filter) { mutableStateOf(filter.maxPrice?.toLong()?.toString().orEmpty()) }
    var minAreaTxt by remember(filter) { mutableStateOf(filter.minArea?.toLong()?.toString().orEmpty()) }
    var maxAreaTxt by remember(filter) { mutableStateOf(filter.maxArea?.toLong()?.toString().orEmpty()) }
    var minFloorTxt by remember(filter) { mutableStateOf(filter.minFloor?.toString().orEmpty()) }
    var maxFloorTxt by remember(filter) { mutableStateOf(filter.maxFloor?.toString().orEmpty()) }

    var showGovMenu by remember { mutableStateOf(false) }
    var showCityMenu by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "الفلاتر المتقدمة",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Listing Type
            Text("نوع العملية", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = draft.listingType == null, onClick = { draft = draft.copy(listingType = null) }, label = { Text("الكل") })
                ListingType.entries.forEach { lt ->
                    FilterChip(
                        selected = draft.listingType == lt,
                        onClick = { draft = draft.copy(listingType = lt) },
                        label = { Text(lt.labelAr) }
                    )
                }
            }

            // Property Type
            Text("نوع العقار", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = draft.propertyType == null, onClick = { draft = draft.copy(propertyType = null) }, label = { Text("الكل") })
                PropertyType.entries.forEach { pt ->
                    FilterChip(
                        selected = draft.propertyType == pt,
                        onClick = { draft = draft.copy(propertyType = pt) },
                        label = { Text(pt.labelAr) }
                    )
                }
            }

            // Governorate & City
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { showGovMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(draft.governorate.ifBlank { "المحافظة: الكل" }, maxLines = 1)
                    }
                    DropdownMenu(expanded = showGovMenu, onDismissRequest = { showGovMenu = false }) {
                        DropdownMenuItem(text = { Text("الكل") }, onClick = { draft = draft.copy(governorate = "", city = ""); showGovMenu = false })
                        locations.forEach { loc ->
                            DropdownMenuItem(text = { Text(loc.nameAr) }, onClick = { draft = draft.copy(governorate = loc.nameAr, city = ""); showGovMenu = false })
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    val cities = locations.firstOrNull { it.nameAr == draft.governorate }?.cities
                        ?: locations.flatMap { it.cities }.distinct()
                    OutlinedButton(onClick = { showCityMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(draft.city.ifBlank { "المدينة: الكل" }, maxLines = 1)
                    }
                    DropdownMenu(expanded = showCityMenu, onDismissRequest = { showCityMenu = false }) {
                        DropdownMenuItem(text = { Text("الكل") }, onClick = { draft = draft.copy(city = ""); showCityMenu = false })
                        cities.forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = { draft = draft.copy(city = c); showCityMenu = false })
                        }
                    }
                }
            }

            OutlinedTextField(
                value = draft.district,
                onValueChange = { draft = draft.copy(district = it) },
                label = { Text("المنطقة / الحي") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Price Range
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minPriceTxt,
                    onValueChange = { minPriceTxt = it.filter(Char::isDigit) },
                    label = { Text("أقل سعر") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = maxPriceTxt,
                    onValueChange = { maxPriceTxt = it.filter(Char::isDigit) },
                    label = { Text("أعلى سعر") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Area Range
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minAreaTxt,
                    onValueChange = { minAreaTxt = it.filter(Char::isDigit) },
                    label = { Text("أقل مساحة (م²)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = maxAreaTxt,
                    onValueChange = { maxAreaTxt = it.filter(Char::isDigit) },
                    label = { Text("أقصى مساحة (م²)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Bedrooms & Bathrooms minimum
            Text("الحد الأدنى للغرف", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null to "الكل", 1 to "1+", 2 to "2+", 3 to "3+", 4 to "4+", 5 to "5+").forEach { (count, label) ->
                    FilterChip(
                        selected = draft.minBedrooms == count,
                        onClick = { draft = draft.copy(minBedrooms = count) },
                        label = { Text(label) }
                    )
                }
            }

            Text("الحد الأدنى للحمامات", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null to "الكل", 1 to "1+", 2 to "2+", 3 to "3+", 4 to "4+").forEach { (count, label) ->
                    FilterChip(
                        selected = draft.minBathrooms == count,
                        onClick = { draft = draft.copy(minBathrooms = count) },
                        label = { Text(label) }
                    )
                }
            }

            // Floor Range
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minFloorTxt,
                    onValueChange = { minFloorTxt = it.filter { c -> c.isDigit() || c == '-' } },
                    label = { Text("من طابق") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = maxFloorTxt,
                    onValueChange = { maxFloorTxt = it.filter { c -> c.isDigit() || c == '-' } },
                    label = { Text("إلى طابق") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Finishing
            Text("نوع التشطيب", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = draft.finishingType == null, onClick = { draft = draft.copy(finishingType = null) }, label = { Text("الكل") })
                FinishingType.entries.forEach { ft ->
                    FilterChip(
                        selected = draft.finishingType == ft,
                        onClick = { draft = draft.copy(finishingType = ft) },
                        label = { Text(ft.labelAr) }
                    )
                }
            }

            // Furnished status
            Text("حالة الفرش", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = draft.furnishedOnly == null, onClick = { draft = draft.copy(furnishedOnly = null) }, label = { Text("الكل") })
                FilterChip(selected = draft.furnishedOnly == true, onClick = { draft = draft.copy(furnishedOnly = true) }, label = { Text("مفروش") })
                FilterChip(selected = draft.furnishedOnly == false, onClick = { draft = draft.copy(furnishedOnly = false) }, label = { Text("غير مفروش") })
            }

            // Amenities Checkboxes (Elevator, Garage, Balcony)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = draft.elevatorOnly, onCheckedChange = { draft = draft.copy(elevatorOnly = it) })
                    Text("مصعد")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = draft.garageOnly, onCheckedChange = { draft = draft.copy(garageOnly = it) })
                    Text("جراج")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = draft.balconyOnly, onCheckedChange = { draft = draft.copy(balconyOnly = it) })
                    Text("بلكونة")
                }
            }

            // Publication recency
            Text("تاريخ نشر الإعلان", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null to "أي وقت", 1 to "آخر 24 ساعة", 7 to "آخر أسبوع", 30 to "آخر شهر").forEach { (days, label) ->
                    FilterChip(
                        selected = draft.maxDaysOld == days,
                        onClick = { draft = draft.copy(maxDaysOld = days) },
                        label = { Text(label) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) {
                    Text("مسح الفلاتر")
                }
                Button(
                    onClick = {
                        onApply(
                            draft.copy(
                                minPrice = minPriceTxt.toDoubleOrNull(),
                                maxPrice = maxPriceTxt.toDoubleOrNull(),
                                minArea = minAreaTxt.toDoubleOrNull(),
                                maxArea = maxAreaTxt.toDoubleOrNull(),
                                minFloor = minFloorTxt.toIntOrNull(),
                                maxFloor = maxFloorTxt.toIntOrNull()
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Text("تطبيق النتائج", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
