package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.data.model.EgyptCatalog
import com.example.data.model.PropertyItem
import com.example.data.model.formatPriceAr
import com.example.ui.theme.BeityGold
import com.example.ui.theme.BeityNavy
import com.example.ui.theme.BeityNavyDark
import com.example.ui.theme.BeityNavyLight
import com.example.ui.theme.BeityWhite
import kotlin.math.roundToInt

@Composable
fun InteractivePropertyMap(
    properties: List<PropertyItem>,
    onPropertyClick: (PropertyItem) -> Unit,
    onFilterByGovernorate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Map viewport bounded across Egypt real estate corridors (lat 26.5..31.8, lng 28.0..34.5)
    var zoom by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(IntSize(1, 1)) }
    var selectedProperty by remember(properties) { mutableStateOf(properties.firstOrNull()) }

    val minLat = 26.8
    val maxLat = 31.6
    val minLng = 28.2
    val maxLng = 34.2

    fun latLngToScreen(lat: Double, lng: Double, size: IntSize): Offset {
        val normX = ((lng - minLng) / (maxLng - minLng)).toFloat().coerceIn(0.05f, 0.95f)
        val normY = (1.0 - (lat - minLat) / (maxLat - minLat)).toFloat().coerceIn(0.05f, 0.95f)
        val cx = size.width / 2f
        val cy = size.height / 2f
        val x = cx + (normX * size.width - cx) * zoom + panOffset.x
        val y = cy + (normY * size.height - cy) * zoom + panOffset.y
        return Offset(x, y)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0A2044))
            .onSizeChanged { canvasSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    zoom = (zoom * gestureZoom).coerceIn(0.85f, 4.5f)
                    panOffset = Offset(
                        (panOffset.x + pan.x).coerceIn(-600f, 600f),
                        (panOffset.y + pan.y).coerceIn(-600f, 600f)
                    )
                }
            }
    ) {
        // Stylized luxury architectural map canvas with Mediterranean/Red Sea & Nile geography
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Subtle coordinate grid
            val step = 64f * zoom.coerceAtMost(2f)
            var gx = (panOffset.x % step)
            while (gx < w) {
                drawLine(Color.White.copy(alpha = 0.06f), Offset(gx, 0f), Offset(gx, h), 1f)
                gx += step
            }
            var gy = (panOffset.y % step)
            while (gy < h) {
                drawLine(Color.White.copy(alpha = 0.06f), Offset(0f, gy), Offset(w, gy), 1f)
                gy += step
            }

            // Mediterranean coast curve at top
            val medPath = Path().apply {
                val p1 = latLngToScreen(31.4, 28.2, IntSize(w.toInt(), h.toInt()))
                val p2 = latLngToScreen(31.2, 29.9, IntSize(w.toInt(), h.toInt()))
                val p3 = latLngToScreen(31.5, 32.3, IntSize(w.toInt(), h.toInt()))
                val p4 = latLngToScreen(31.2, 34.2, IntSize(w.toInt(), h.toInt()))
                moveTo(p1.x, 0f)
                lineTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                lineTo(p4.x, p4.y)
                lineTo(w, 0f)
                close()
            }
            drawPath(medPath, color = Color(0xFF123B70).copy(alpha = 0.7f))

            // Nile river ribbon + Delta
            val nilePath = Path().apply {
                val n1 = latLngToScreen(26.9, 31.3, IntSize(w.toInt(), h.toInt()))
                val n2 = latLngToScreen(29.0, 31.1, IntSize(w.toInt(), h.toInt()))
                val n3 = latLngToScreen(30.05, 31.23, IntSize(w.toInt(), h.toInt()))
                val n4 = latLngToScreen(31.35, 30.3, IntSize(w.toInt(), h.toInt()))
                val n5 = latLngToScreen(31.4, 31.8, IntSize(w.toInt(), h.toInt()))
                moveTo(n1.x, n1.y)
                lineTo(n2.x, n2.y)
                lineTo(n3.x, n3.y)
                lineTo(n4.x, n4.y)
                moveTo(n3.x, n3.y)
                lineTo(n5.x, n5.y)
            }
            drawPath(
                nilePath,
                color = Color(0xFF2B6CB0).copy(alpha = 0.65f),
                style = Stroke(width = 8f * zoom.coerceAtMost(2f), cap = StrokeCap.Round)
            )
        }

        // Governorate region quick-labels on the map
        EgyptCatalog.governorates.take(5).forEach { gov ->
            val pos = latLngToScreen(gov.lat, gov.lng, canvasSize)
            if (pos.x in 20f..(canvasSize.width - 80f) && pos.y in 20f..(canvasSize.height - 80f)) {
                Text(
                    text = gov.nameAr,
                    color = BeityWhite.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .offset { IntOffset(pos.x.roundToInt(), (pos.y + 18).roundToInt()) }
                        .clickable { onFilterByGovernorate(gov.nameAr) }
                )
            }
        }

        // Property interactive gold price markers
        properties.forEach { prop ->
            val pos = latLngToScreen(prop.latitude, prop.longitude, canvasSize)
            val isSelected = selectedProperty?.id == prop.id
            if (pos.x in -40f..(canvasSize.width + 40f) && pos.y in -40f..(canvasSize.height + 40f)) {
                Surface(
                    color = if (isSelected) BeityGold else BeityWhite,
                    contentColor = BeityNavy,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = if (isSelected) 8.dp else 4.dp,
                    modifier = Modifier
                        .offset { IntOffset((pos.x - 45).roundToInt(), (pos.y - 24).roundToInt()) }
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedProperty = prop }
                        .testTag("map_pin_${prop.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = if (isSelected) BeityNavy else BeityGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = formatPriceAr(prop.price),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Map zoom & recenter controls
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(shape = CircleShape, color = BeityNavyDark.copy(alpha = 0.85f), shadowElevation = 4.dp) {
                IconButton(onClick = { zoom = (zoom * 1.25f).coerceAtMost(4.5f) }) {
                    Icon(Icons.Rounded.Add, contentDescription = "تكبير الخريطة", tint = BeityGold)
                }
            }
            Surface(shape = CircleShape, color = BeityNavyDark.copy(alpha = 0.85f), shadowElevation = 4.dp) {
                IconButton(onClick = { zoom = (zoom / 1.25f).coerceAtLeast(0.85f) }) {
                    Icon(Icons.Rounded.Remove, contentDescription = "تصغير الخريطة", tint = BeityGold)
                }
            }
            Surface(shape = CircleShape, color = BeityNavyDark.copy(alpha = 0.85f), shadowElevation = 4.dp) {
                IconButton(onClick = {
                    zoom = 1f
                    panOffset = Offset.Zero
                }) {
                    Icon(Icons.Rounded.MyLocation, contentDescription = "إعادة ضبط الخريطة", tint = BeityWhite)
                }
            }
        }

        // Selected property floating bottom card
        val current = selectedProperty
        if (current != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BeityPropertyImage(
                        imageSource = current.imageUrls.firstOrNull(),
                        contentDescription = current.title,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formatPriceAr(current.price),
                            color = BeityGold,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = current.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${current.city} • ${current.areaSqm.toInt()} م²",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { onPropertyClick(current) },
                        colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("التفاصيل", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        } else {
            Surface(
                color = BeityNavyDark.copy(alpha = 0.85f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(
                    text = "لا توجد عقارات مطابقة على الخريطة حالياً",
                    color = BeityWhite,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}
