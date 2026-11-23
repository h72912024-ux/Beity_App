package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.FavoriteItem
import com.example.data.model.PropertyItem
import com.example.data.model.formatPriceAr
import com.example.ui.components.BeityPropertyImage
import com.example.ui.components.PropertyCard
import com.example.ui.theme.BeityGold
import com.example.ui.theme.BeityNavy
import com.example.ui.theme.BeityWhite

@Composable
fun FavoritesCompareScreen(
    allApprovedProperties: List<PropertyItem>,
    favorites: List<FavoriteItem>,
    compareIds: List<String>,
    onPropertyClick: (PropertyItem) -> Unit,
    onToggleFavorite: (PropertyItem) -> Unit,
    onToggleCompare: (String) -> Unit,
    onExploreClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val favIdSet = remember(favorites) { favorites.map { it.propertyId }.toSet() }
    val favProperties = remember(allApprovedProperties, favIdSet) {
        allApprovedProperties.filter { favIdSet.contains(it.id) }
    }
    val comparedProperties = remember(allApprovedProperties, compareIds) {
        compareIds.mapNotNull { id -> allApprovedProperties.firstOrNull { it.id == id } }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = BeityNavy, contentColor = BeityWhite) {
            Column {
                Text(
                    text = "المفضلة والمقارنة",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = BeityWhite,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BeityNavy,
                    contentColor = BeityGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = BeityGold
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("المفضلة (${favProperties.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("مقارنة العقارات (${comparedProperties.size}/3)") }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            if (favProperties.isEmpty()) {
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
                        Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = BeityGold, modifier = Modifier.size(56.dp))
                        Text("لا توجد عقارات في المفضلة بعد", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("اضغط على أيقونة القلب في أي عقار لحفظه والرجوع إليه لاحقاً.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(
                            onClick = onExploreClick,
                            colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold)
                        ) { Text("استكشف العقارات") }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(favProperties, key = { it.id }) { prop ->
                        PropertyCard(
                            property = prop,
                            isFavorite = true,
                            isCompared = compareIds.contains(prop.id),
                            onClick = { onPropertyClick(prop) },
                            onToggleFavorite = { onToggleFavorite(prop) },
                            onToggleCompare = { onToggleCompare(prop.id) }
                        )
                    }
                }
            }
        } else {
            if (comparedProperties.isEmpty()) {
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
                        Icon(Icons.Outlined.Balance, contentDescription = null, tint = BeityGold, modifier = Modifier.size(56.dp))
                        Text("لم تختر عقارات للمقارنة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("اضغط على أيقونة الميزان في البطاقات العقارية لمقارنة حتى 3 عقارات جنباً إلى جنب.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(
                            onClick = onExploreClick,
                            colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold)
                        ) { Text("اختيار عقارات") }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        comparedProperties.forEach { prop ->
                            Card(
                                modifier = Modifier.width(190.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column {
                                    Box(modifier = Modifier.height(105.dp)) {
                                        BeityPropertyImage(
                                            imageSource = prop.imageUrls.firstOrNull(),
                                            contentDescription = prop.title,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        IconButton(
                                            onClick = { onToggleCompare(prop.id) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(BeityNavy.copy(alpha = 0.8f))
                                        ) {
                                            Icon(Icons.Rounded.Close, contentDescription = "إزالة", tint = BeityWhite, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = prop.title,
                                            style = MaterialTheme.typography.labelLarge,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = formatPriceAr(prop.price),
                                            color = BeityGold,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Side-by-side Spec Rows
                    ComparisonRow("نوع العملية", comparedProperties.map { it.listingTypeEnum().labelAr })
                    ComparisonRow("نوع العقار", comparedProperties.map { it.propertyTypeEnum().labelAr })
                    ComparisonRow("السعر", comparedProperties.map { formatPriceAr(it.price) })
                    ComparisonRow("المساحة", comparedProperties.map { "${it.areaSqm.toInt()} م²" })
                    ComparisonRow(
                        "سعر المتر",
                        comparedProperties.map {
                            "${(it.price / it.areaSqm.coerceAtLeast(1.0)).toLong()} ج.م/م²"
                        }
                    )
                    ComparisonRow("الغرف", comparedProperties.map { "${it.bedrooms}" })
                    ComparisonRow("الحمامات", comparedProperties.map { "${it.bathrooms}" })
                    ComparisonRow("الطابق", comparedProperties.map { if (it.floor == 0L) "أرضي" else "${it.floor}" })
                    ComparisonRow("الموقع", comparedProperties.map { "${it.governorate} - ${it.city}" })
                    ComparisonRow("التشطيب", comparedProperties.map { it.finishingEnum().labelAr })
                    ComparisonRow("مفروش", comparedProperties.map { if (it.furnished) "نعم" else "لا" })
                    ComparisonRow("مصعد", comparedProperties.map { if (it.hasElevator) "متوفر" else "غير متوفر" })
                    ComparisonRow("جراج", comparedProperties.map { if (it.hasGarage) "متوفر" else "غير متوفر" })
                    ComparisonRow("بلكونة", comparedProperties.map { if (it.hasBalcony) "متوفر" else "غير متوفر" })
                }
            }
        }
    }
}

@Composable
private fun ComparisonRow(label: String, values: List<String>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = BeityGold, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                values.forEach { v ->
                    Text(
                        text = v,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
