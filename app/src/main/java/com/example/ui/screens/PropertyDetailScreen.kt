package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Bathtub
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Elevator
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Garage
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SquareFoot
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PropertyItem
import com.example.data.model.PropertyReview
import com.example.data.model.formatPriceAr
import com.example.ui.components.BeityPropertyImage
import com.example.ui.components.PropertyCard
import com.example.ui.theme.BeityError
import com.example.ui.theme.BeityGold
import com.example.ui.theme.BeityNavy
import com.example.ui.theme.BeitySuccess
import com.example.ui.theme.BeityWhite
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PropertyDetailScreen(
    property: PropertyItem,
    currentUserId: String,
    reviews: List<PropertyReview>,
    similarProperties: List<PropertyItem>,
    isFavorite: Boolean,
    isCompared: Boolean,
    favoriteIds: Set<String>,
    compareIds: List<String>,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleCompare: () -> Unit,
    onEditOwnProperty: () -> Unit,
    onSendMessage: (String) -> Unit,
    onAddReview: (Int, String) -> Unit,
    onSubmitReport: (String, String) -> Unit,
    onOpenProperty: (PropertyItem) -> Unit
) {
    val context = LocalContext.current
    val images = property.imageUrls.ifEmpty { listOf("preset:villa") }
    val pagerState = rememberPagerState(pageCount = { images.size })

    var zoomImageIndex by remember { mutableStateOf<Int?>(null) }
    var showMessageDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

    val dateLabel = remember(property.createdAt) {
        val date = property.createdAt?.toDate()
        if (date != null) SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("ar")).format(date) else "حديثاً"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(property.propertyTypeEnum().labelAr, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    if (property.ownerId == currentUserId) {
                        IconButton(onClick = onEditOwnProperty) {
                            Icon(Icons.Rounded.Edit, contentDescription = "تعديل العقار", tint = BeityGold)
                        }
                    }
                    IconButton(onClick = onToggleCompare) {
                        Icon(
                            Icons.Outlined.Balance,
                            contentDescription = "مقارنة",
                            tint = if (isCompared) BeityGold else BeityWhite
                        )
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (isFavorite) BeityGold else BeityWhite
                        )
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Image Gallery Pager with Zoom Lightbox
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        BeityPropertyImage(
                            imageSource = images[page],
                            contentDescription = property.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { zoomImageIndex = page }
                        )
                    }

                    // Zoom hint pill
                    Surface(
                        color = BeityNavy.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .clickable { zoomImageIndex = pagerState.currentPage }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Outlined.ZoomIn, contentDescription = null, tint = BeityGold, modifier = Modifier.size(16.dp))
                            Text("${pagerState.currentPage + 1}/${images.size} • تكبير", color = BeityWhite, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Video CTA if provided
                    if (!property.videoUrl.isNullOrBlank()) {
                        Surface(
                            color = BeityGold,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                                .clickable {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(property.videoUrl)))
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Outlined.PlayCircle, contentDescription = null, tint = BeityNavy, modifier = Modifier.size(16.dp))
                                Text("فيديو العقار", color = BeityNavy, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Title, Price, Location, Date & Views
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(color = BeityNavy, shape = RoundedCornerShape(10.dp)) {
                            Text(
                                text = "${property.listingTypeEnum().labelAr} • ${property.propertyTypeEnum().labelAr}",
                                color = BeityGold,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${property.viewsCount} مشاهدة", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(dateLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Text(
                        text = formatPriceAr(property.price) + if (property.listingType == "rent") " شهرياً" else "",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = BeityGold
                    )

                    Text(
                        text = property.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = BeityGold, modifier = Modifier.size(18.dp))
                        Text(
                            text = "${property.governorate} - ${property.city} - ${property.district} (${property.address})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 3. Key Specs Grid
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailSpecBadge(Icons.Outlined.SquareFoot, "المساحة", "${property.areaSqm.toInt()} م²")
                        DetailSpecBadge(Icons.Outlined.Bed, "الغرف", "${property.bedrooms}")
                        DetailSpecBadge(Icons.Outlined.Bathtub, "الحمامات", "${property.bathrooms}")
                        DetailSpecBadge(Icons.Outlined.Layers, "الطابق", if (property.floor == 0L) "أرضي" else "${property.floor}")
                        DetailSpecBadge(Icons.Outlined.CheckCircle, "التشطيب", property.finishingEnum().labelAr)
                        DetailSpecBadge(Icons.Outlined.CheckCircle, "الفرش", if (property.furnished) "مفروش" else "غير مفروش")
                        if (property.hasElevator) DetailSpecBadge(Icons.Outlined.Elevator, "مصعد", "متوفر")
                        if (property.hasGarage) DetailSpecBadge(Icons.Outlined.Garage, "جراج", "متوفر")
                        if (property.hasBalcony) DetailSpecBadge(Icons.Outlined.CheckCircle, "بلكونة", "متوفر")
                    }
                }
            }

            // 4. Description
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("الوصف التفصيلي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(property.description, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            // 5. Features & Amenities
            if (property.features.isNotEmpty() || property.amenities.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (property.features.isNotEmpty()) {
                                Text("مميزات العقار", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    property.features.forEach { feat ->
                                        Surface(color = BeityGold.copy(alpha = 0.16f), shape = RoundedCornerShape(10.dp)) {
                                            Text(feat, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                        }
                                    }
                                }
                            }
                            if (property.amenities.isNotEmpty()) {
                                Text("الخدمات المتاحة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    property.amenities.forEach { am ->
                                        Surface(color = BeityNavy.copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp)) {
                                            Text(am, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Map Location Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("موقع العقار على الخريطة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "الإحداثيات: ${String.format(Locale.US, "%.4f", property.latitude)}, ${String.format(Locale.US, "%.4f", property.longitude)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                val geoUri = Uri.parse("geo:${property.latitude},${property.longitude}?q=${property.latitude},${property.longitude}(${Uri.encode(property.title)})")
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, geoUri)) }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("فتح الخريطة")
                        }
                    }
                }
            }

            // 7. Advertiser Card & All Contact/Action Buttons
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = BeityNavy)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(BeityGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Person, contentDescription = null, tint = BeityNavy)
                            }
                            Column {
                                Text("المعلن", color = BeityGold, style = MaterialTheme.typography.labelMedium)
                                Text(property.ownerName, color = BeityWhite, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(property.contactPhone, color = BeityWhite.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${property.contactPhone}")))
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BeityGold, contentColor = BeityNavy),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("call_advertiser_button")
                            ) {
                                Icon(Icons.Outlined.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("اتصل بالمعلن", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    val cleanPhone = property.contactPhone.filter { it.isDigit() }
                                    val msg = Uri.encode("مرحباً، أستفسر عن عقار: ${property.title}")
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanPhone?text=$msg")))
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BeitySuccess, contentColor = BeityWhite),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("whatsapp_advertiser_button")
                            ) {
                                Text("واتساب", fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showMessageDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BeityWhite),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("inapp_message_button")
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.Chat, contentDescription = null, tint = BeityGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إرسال رسالة")
                            }
                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "${property.title}\nالسعر: ${formatPriceAr(property.price)}\nالموقع: ${property.governorate} - ${property.city}\nعبر تطبيق بيتي | Beity"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة العقار"))
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BeityWhite),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Outlined.Share, contentDescription = null, tint = BeityGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مشاركة")
                            }
                        }

                        TextButton(
                            onClick = { showReportDialog = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Outlined.Flag, contentDescription = null, tint = BeityError, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الإبلاغ عن الإعلان", color = Color(0xFFFF9A9A), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // 8. Reviews Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("تقييمات العقار (${reviews.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { showReviewDialog = true }) {
                                Text("أضف تقييمك", color = BeityGold)
                            }
                        }
                        if (reviews.isEmpty()) {
                            Text("لا توجد تقييمات بعد لهذا العقار.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            reviews.forEach { rev ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(rev.authorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                        Row {
                                            repeat(rev.rating.toInt().coerceIn(1, 5)) {
                                                Icon(Icons.Rounded.Star, contentDescription = null, tint = BeityGold, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                    Text(rev.comment, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            // 9. Similar Properties
            if (similarProperties.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "عقارات مشابهة",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(similarProperties, key = { it.id }) { sim ->
                                PropertyCard(
                                    property = sim,
                                    isFavorite = favoriteIds.contains(sim.id),
                                    isCompared = compareIds.contains(sim.id),
                                    onClick = { onOpenProperty(sim) },
                                    onToggleFavorite = {},
                                    onToggleCompare = {},
                                    modifier = Modifier.width(275.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Fullscreen Image Zoom Lightbox
    val zoomedIdx = zoomImageIndex
    if (zoomedIdx != null) {
        Dialog(
            onDismissRequest = { zoomImageIndex = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var scale by remember { mutableFloatStateOf(1f) }
            val lightboxPager = rememberPagerState(initialPage = zoomedIdx, pageCount = { images.size })
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                        }
                    }
            ) {
                HorizontalPager(state = lightboxPager, modifier = Modifier.fillMaxSize()) { page ->
                    BeityPropertyImage(
                        imageSource = images[page],
                        contentDescription = property.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                    )
                }
                IconButton(
                    onClick = { zoomImageIndex = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "إغلاق", tint = Color.White)
                }
            }
        }
    }

    // Send Message Dialog
    if (showMessageDialog) {
        var msgText by remember { mutableStateOf("مرحباً، أود الاستفسار عن تفاصيل عقار: ${property.title}") }
        AlertDialog(
            onDismissRequest = { showMessageDialog = false },
            title = { Text("مراسلة المعلن") },
            text = {
                OutlinedTextField(
                    value = msgText,
                    onValueChange = { msgText = it },
                    label = { Text("رسالتك") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (msgText.trim().isNotEmpty()) {
                            onSendMessage(msgText.trim())
                            showMessageDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold)
                ) { Text("إرسال") }
            },
            dismissButton = {
                TextButton(onClick = { showMessageDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Add Review Dialog
    if (showReviewDialog) {
        var rating by remember { mutableIntStateOf(5) }
        var comment by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("إضافة تقييم") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { rating = star }) {
                                Icon(
                                    Icons.Rounded.Star,
                                    contentDescription = "$star نجوم",
                                    tint = if (star <= rating) BeityGold else Color.Gray
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("اكتب رأيك حول العقار") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (comment.trim().length >= 2) {
                            onAddReview(rating, comment.trim())
                            showReviewDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold)
                ) { Text("نشر التقييم") }
            },
            dismissButton = { TextButton(onClick = { showReviewDialog = false }) { Text("إلغاء") } }
        )
    }

    // Report Listing Dialog
    if (showReportDialog) {
        var reason by remember { mutableStateOf("بيانات غير مطابقة") }
        var details by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("الإبلاغ عن الإعلان") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("السبب") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = details, onValueChange = { details = it }, label = { Text("تفاصيل البلاغ") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reason.trim().length >= 2 && details.trim().length >= 5) {
                            onSubmitReport(reason.trim(), details.trim())
                            showReportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BeityError, contentColor = BeityWhite)
                ) { Text("إرسال البلاغ") }
            },
            dismissButton = { TextButton(onClick = { showReportDialog = false }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun DetailSpecBadge(icon: ImageVector, label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = BeityGold, modifier = Modifier.size(18.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}
