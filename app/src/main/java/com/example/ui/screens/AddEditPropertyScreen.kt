package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.EgyptCatalog
import com.example.data.model.FinishingType
import com.example.data.model.ListingType
import com.example.data.model.LocationItem
import com.example.data.model.PropertyItem
import com.example.data.model.PropertyType
import com.example.ui.components.BeityPropertyImage
import com.example.ui.components.compressImageUriToDataUrl
import com.example.ui.theme.BeityError
import com.example.ui.theme.BeityGold
import com.example.ui.theme.BeityNavy
import com.example.ui.theme.BeityWhite

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditPropertyScreen(
    existingProperty: PropertyItem? = null,
    defaultOwnerName: String,
    defaultPhone: String,
    locations: List<LocationItem>,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onSubmit: (PropertyItem) -> Unit
) {
    val context = LocalContext.current
    val isEditing = existingProperty != null

    var title by remember(existingProperty) { mutableStateOf(existingProperty?.title.orEmpty()) }
    var propertyType by remember(existingProperty) {
        mutableStateOf(existingProperty?.propertyTypeEnum() ?: PropertyType.APARTMENT)
    }
    var listingType by remember(existingProperty) {
        mutableStateOf(existingProperty?.listingTypeEnum() ?: ListingType.SALE)
    }
    var priceText by remember(existingProperty) {
        mutableStateOf(existingProperty?.price?.toLong()?.toString().orEmpty())
    }
    var areaText by remember(existingProperty) {
        mutableStateOf(existingProperty?.areaSqm?.toLong()?.toString().orEmpty())
    }
    var governorate by remember(existingProperty) {
        mutableStateOf(existingProperty?.governorate?.ifBlank { null } ?: locations.firstOrNull()?.nameAr ?: "القاهرة")
    }
    var city by remember(existingProperty) {
        mutableStateOf(existingProperty?.city?.ifBlank { null } ?: "القاهرة الجديدة")
    }
    var district by remember(existingProperty) { mutableStateOf(existingProperty?.district.orEmpty()) }
    var address by remember(existingProperty) { mutableStateOf(existingProperty?.address.orEmpty()) }
    var bedroomsText by remember(existingProperty) { mutableStateOf((existingProperty?.bedrooms ?: 3L).toString()) }
    var bathroomsText by remember(existingProperty) { mutableStateOf((existingProperty?.bathrooms ?: 2L).toString()) }
    var floorText by remember(existingProperty) { mutableStateOf((existingProperty?.floor ?: 1L).toString()) }
    var finishing by remember(existingProperty) {
        mutableStateOf(existingProperty?.finishingEnum() ?: FinishingType.SUPER_LUX)
    }
    var furnished by remember(existingProperty) { mutableStateOf(existingProperty?.furnished ?: false) }
    var hasElevator by remember(existingProperty) { mutableStateOf(existingProperty?.hasElevator ?: true) }
    var hasGarage by remember(existingProperty) { mutableStateOf(existingProperty?.hasGarage ?: false) }
    var hasBalcony by remember(existingProperty) { mutableStateOf(existingProperty?.hasBalcony ?: true) }
    var description by remember(existingProperty) { mutableStateOf(existingProperty?.description.orEmpty()) }
    var videoUrl by remember(existingProperty) { mutableStateOf(existingProperty?.videoUrl.orEmpty()) }
    var ownerName by remember(existingProperty) { mutableStateOf(existingProperty?.ownerName ?: defaultOwnerName) }
    var contactPhone by remember(existingProperty) { mutableStateOf(existingProperty?.contactPhone ?: defaultPhone) }

    val coords = EgyptCatalog.coordinatesFor(governorate, city)
    var latText by remember(existingProperty, governorate, city) {
        mutableStateOf((existingProperty?.latitude ?: coords.first).toString())
    }
    var lngText by remember(existingProperty, governorate, city) {
        mutableStateOf((existingProperty?.longitude ?: coords.second).toString())
    }

    val selectedFeatures = remember(existingProperty) {
        mutableStateListOf<String>().apply { addAll(existingProperty?.features.orEmpty()) }
    }
    val selectedAmenities = remember(existingProperty) {
        mutableStateListOf<String>().apply { addAll(existingProperty?.amenities.orEmpty()) }
    }
    val selectedImages = remember(existingProperty) {
        mutableStateListOf<String>().apply {
            addAll(existingProperty?.imageUrls?.ifEmpty { listOf("preset:villa") } ?: listOf("preset:villa"))
        }
    }

    var validationError by remember { mutableStateOf<String?>(null) }
    var showGovMenu by remember { mutableStateOf(false) }
    var showCityMenu by remember { mutableStateOf(false) }

    val multiPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        uris.forEach { uri ->
            if (selectedImages.size < 8) {
                compressImageUriToDataUrl(context, uri)?.let { dataUrl ->
                    selectedImages.add(0, dataUrl)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "تعديل العقار" else "أضف عقارك الآن") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BeityNavy,
                    titleContentColor = BeityWhite,
                    navigationIconContentColor = BeityWhite
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!isAdmin) {
                Surface(
                    color = BeityGold.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ملاحظة: تمر جميع الإعلانات الجديدة بمراجعة الإدارة لضمان الجودة والمصداقية قبل النشر للعامة.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان الإعلان *") },
                placeholder = { Text("مثال: شقة فاخرة بالزمالك إطلالة نيلية") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_prop_title")
            )

            Text("نوع العملية *", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ListingType.entries.forEach { lt ->
                    FilterChip(
                        selected = listingType == lt,
                        onClick = { listingType = lt },
                        label = { Text(lt.labelAr) }
                    )
                }
            }

            Text("نوع العقار *", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PropertyType.entries.forEach { pt ->
                    FilterChip(
                        selected = propertyType == pt,
                        onClick = { propertyType = pt },
                        label = { Text(pt.labelAr) }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter(Char::isDigit) },
                    label = { Text("السعر (ج.م) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_prop_price")
                )
                OutlinedTextField(
                    value = areaText,
                    onValueChange = { areaText = it.filter(Char::isDigit) },
                    label = { Text("المساحة (م²) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_prop_area")
                )
            }

            // Governorate & City
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { showGovMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("المحافظة: $governorate", maxLines = 1)
                    }
                    DropdownMenu(expanded = showGovMenu, onDismissRequest = { showGovMenu = false }) {
                        locations.forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc.nameAr) },
                                onClick = {
                                    governorate = loc.nameAr
                                    city = loc.cities.firstOrNull().orEmpty()
                                    showGovMenu = false
                                }
                            )
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    val cities = locations.firstOrNull { it.nameAr == governorate }?.cities ?: emptyList()
                    OutlinedButton(onClick = { showCityMenu = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("المدينة: $city", maxLines = 1)
                    }
                    DropdownMenu(expanded = showCityMenu, onDismissRequest = { showCityMenu = false }) {
                        cities.forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = { city = c; showCityMenu = false })
                        }
                    }
                }
            }

            OutlinedTextField(
                value = district,
                onValueChange = { district = it },
                label = { Text("المنطقة / الحي *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("العنوان التفصيلي *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = bedroomsText,
                    onValueChange = { bedroomsText = it.filter(Char::isDigit) },
                    label = { Text("الغرف") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bathroomsText,
                    onValueChange = { bathroomsText = it.filter(Char::isDigit) },
                    label = { Text("الحمامات") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = floorText,
                    onValueChange = { floorText = it.filter { c -> c.isDigit() || c == '-' } },
                    label = { Text("الطابق") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Text("نوع التشطيب", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinishingType.entries.forEach { ft ->
                    FilterChip(
                        selected = finishing == ft,
                        onClick = { finishing = ft },
                        label = { Text(ft.labelAr) }
                    )
                }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = furnished, onCheckedChange = { furnished = it })
                    Text("مفروش")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasElevator, onCheckedChange = { hasElevator = it })
                    Text("مصعد")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasGarage, onCheckedChange = { hasGarage = it })
                    Text("جراج")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasBalcony, onCheckedChange = { hasBalcony = it })
                    Text("بلكونة")
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("وصف العقار (10 أحرف على الأقل) *") },
                minLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_prop_description")
            )

            // Photos Section via Android Photo Picker + Architectural Presets
            Text("صور العقار", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        multiPhotoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.height(84.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null)
                        Text("رفع صور", style = MaterialTheme.typography.labelSmall)
                    }
                }
                selectedImages.forEachIndexed { idx, img ->
                    Box(modifier = Modifier.size(84.dp)) {
                        BeityPropertyImage(
                            imageSource = img,
                            contentDescription = "صورة عقار ${idx + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                        if (selectedImages.size > 1) {
                            IconButton(
                                onClick = { selectedImages.removeAt(idx) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(BeityNavy)
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "حذف الصورة", tint = BeityWhite, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EgyptCatalog.presetImages.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedImages.contains(key),
                        onClick = {
                            if (selectedImages.contains(key)) {
                                if (selectedImages.size > 1) selectedImages.remove(key)
                            } else {
                                selectedImages.add(key)
                            }
                        },
                        label = { Text(label) }
                    )
                }
            }

            OutlinedTextField(
                value = videoUrl,
                onValueChange = { videoUrl = it },
                label = { Text("رابط فيديو للعقار (اختياري)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Features & Amenities Chips
            Text("مميزات إضافية", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                EgyptCatalog.commonFeatures.forEach { feat ->
                    FilterChip(
                        selected = selectedFeatures.contains(feat),
                        onClick = {
                            if (selectedFeatures.contains(feat)) selectedFeatures.remove(feat)
                            else if (selectedFeatures.size < 15) selectedFeatures.add(feat)
                        },
                        label = { Text(feat) }
                    )
                }
            }

            Text("الخدمات المحيطة", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                EgyptCatalog.commonAmenities.forEach { am ->
                    FilterChip(
                        selected = selectedAmenities.contains(am),
                        onClick = {
                            if (selectedAmenities.contains(am)) selectedAmenities.remove(am)
                            else if (selectedAmenities.size < 15) selectedAmenities.add(am)
                        },
                        label = { Text(am) }
                    )
                }
            }

            // Map Coordinates
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = latText,
                    onValueChange = { latText = it },
                    label = { Text("خط العرض (Lat)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = lngText,
                    onValueChange = { lngText = it },
                    label = { Text("خط الطول (Lng)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Contact Info
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("اسم المعلن *") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("رقم التواصل *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_prop_phone")
                )
            }

            if (!validationError.isNullOrBlank()) {
                Text(validationError.orEmpty(), color = BeityError, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    val area = areaText.toDoubleOrNull()
                    when {
                        title.trim().length < 3 -> validationError = "يرجى إدخال عنوان واضح (3 أحرف على الأقل)"
                        price == null || price <= 0 -> validationError = "يرجى إدخال سعر صحيح أكبر من صفر"
                        area == null || area <= 0 -> validationError = "يرجى إدخال مساحة صحيحة أكبر من صفر"
                        description.trim().length < 10 -> validationError = "يرجى كتابة وصف تفصيلي (10 أحرف على الأقل)"
                        contactPhone.trim().length < 6 -> validationError = "يرجى إدخال رقم تواصل صحيح"
                        else -> {
                            validationError = null
                            val base = existingProperty ?: PropertyItem()
                            onSubmit(
                                base.copy(
                                    ownerName = ownerName.trim().ifBlank { "معلن بيتي" },
                                    contactPhone = contactPhone.trim(),
                                    title = title.trim(),
                                    description = description.trim(),
                                    propertyType = propertyType.key,
                                    listingType = listingType.key,
                                    price = price,
                                    areaSqm = area,
                                    governorate = governorate,
                                    city = city.ifBlank { governorate },
                                    district = district.trim().ifBlank { city.ifBlank { governorate } },
                                    address = address.trim().ifBlank { district.trim().ifBlank { city } },
                                    bedrooms = bedroomsText.toLongOrNull() ?: 0L,
                                    bathrooms = bathroomsText.toLongOrNull() ?: 0L,
                                    floor = floorText.toLongOrNull() ?: 0L,
                                    finishingType = finishing.key,
                                    furnished = furnished,
                                    hasElevator = hasElevator,
                                    hasGarage = hasGarage,
                                    hasBalcony = hasBalcony,
                                    features = selectedFeatures.toList(),
                                    amenities = selectedAmenities.toList(),
                                    imageUrls = selectedImages.toList(),
                                    videoUrl = videoUrl.trim().takeIf { it.isNotEmpty() },
                                    latitude = latText.toDoubleOrNull() ?: coords.first,
                                    longitude = lngText.toDoubleOrNull() ?: coords.second
                                )
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_property_button")
            ) {
                Text(
                    text = if (isEditing) "حفظ التعديلات وإعادة الإرسال" else "نشر العقار الآن",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
