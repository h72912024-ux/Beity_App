package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.AddHome
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BeityGold
import com.example.ui.theme.BeityNavy
import com.example.ui.theme.BeityWhite

enum class BeityLogoVariant {
    ON_DARK,
    ON_LIGHT
}

@Composable
fun BeityLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    variant: BeityLogoVariant = BeityLogoVariant.ON_DARK
) {
    val lineColor = BeityGold
    val accentTowerColor = if (variant == BeityLogoVariant.ON_DARK) BeityWhite else BeityNavy

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeMain = w * 0.065f
        val strokeFine = w * 0.045f

        // Background subtle architectural tower silhouette on the right
        val towerPath = Path().apply {
            moveTo(w * 0.56f, h * 0.85f)
            lineTo(w * 0.56f, h * 0.24f)
            lineTo(w * 0.78f, h * 0.16f)
            lineTo(w * 0.78f, h * 0.85f)
        }
        drawPath(
            path = towerPath,
            color = accentTowerColor.copy(alpha = 0.85f),
            style = Stroke(width = strokeFine, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Secondary shorter building on the left
        val sideBuilding = Path().apply {
            moveTo(w * 0.22f, h * 0.85f)
            lineTo(w * 0.22f, h * 0.34f)
            lineTo(w * 0.36f, h * 0.28f)
        }
        drawPath(
            path = sideBuilding,
            color = accentTowerColor.copy(alpha = 0.65f),
            style = Stroke(width = strokeFine, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Foreground gold luxury house roofline + architectural walls
        val housePath = Path().apply {
            moveTo(w * 0.12f, h * 0.54f)
            lineTo(w * 0.48f, h * 0.22f)
            lineTo(w * 0.86f, h * 0.54f)
        }
        drawPath(
            path = housePath,
            color = lineColor,
            style = Stroke(width = strokeMain, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val houseBase = Path().apply {
            moveTo(w * 0.24f, h * 0.49f)
            lineTo(w * 0.24f, h * 0.85f)
            lineTo(w * 0.72f, h * 0.85f)
            lineTo(w * 0.72f, h * 0.49f)
        }
        drawPath(
            path = houseBase,
            color = lineColor,
            style = Stroke(width = strokeMain, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Golden doorway / architectural window accent
        drawLine(
            color = lineColor,
            start = Offset(w * 0.48f, h * 0.85f),
            end = Offset(w * 0.48f, h * 0.62f),
            strokeWidth = strokeFine,
            cap = StrokeCap.Round
        )
        drawLine(
            color = lineColor,
            start = Offset(w * 0.65f, h * 0.32f),
            end = Offset(w * 0.71f, h * 0.30f),
            strokeWidth = strokeFine,
            cap = StrokeCap.Round
        )
        drawLine(
            color = lineColor,
            start = Offset(w * 0.65f, h * 0.41f),
            end = Offset(w * 0.71f, h * 0.39f),
            strokeWidth = strokeFine,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun BeityLogo(
    modifier: Modifier = Modifier,
    variant: BeityLogoVariant = BeityLogoVariant.ON_DARK,
    compact: Boolean = false
) {
    val titleColor = if (variant == BeityLogoVariant.ON_DARK) BeityWhite else BeityNavy
    val subtitleColor = BeityGold

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)
    ) {
        BeityLogoMark(
            size = if (compact) 34.dp else 54.dp,
            variant = variant
        )
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = "بيتي",
                color = titleColor,
                fontSize = if (compact) 18.sp else 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = if (compact) 22.sp else 30.sp
            )
            Text(
                text = "BEITY",
                color = subtitleColor,
                fontSize = if (compact) 10.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                lineHeight = if (compact) 12.sp else 15.sp
            )
        }
    }
}

@Composable
fun BeityTopHeader(
    unreadNotifications: Int,
    isAdmin: Boolean,
    onAddProperty: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAdmin: () -> Unit,
    onQuickNav: (String) -> Unit
) {
    Surface(
        color = BeityNavy,
        contentColor = BeityWhite,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BeityLogo(variant = BeityLogoVariant.ON_DARK, compact = true)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isAdmin) {
                        IconButton(
                            onClick = onOpenAdmin,
                            modifier = Modifier.testTag("header_admin_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AdminPanelSettings,
                                contentDescription = "لوحة تحكم الإدارة",
                                tint = BeityGold
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier.testTag("header_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifications > 0) {
                                    Badge(containerColor = BeityGold, contentColor = BeityNavy) {
                                        Text(unreadNotifications.coerceAtMost(99).toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "الإشعارات والرسائل",
                                tint = BeityWhite
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BeityGold)
                            .clickable(onClick = onAddProperty)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("header_add_property_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AddHome,
                                contentDescription = "أضف عقارك",
                                tint = BeityNavy,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "أضف عقارك",
                                color = BeityNavy,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick horizontal header navigation bar matching the web-inspired sections
            val quickLinks = listOf(
                "home" to "الرئيسية",
                "explore" to "استكشف العقارات",
                "sell" to "بيع عقارك",
                "rent" to "تأجير عقارك",
                "map" to "الخريطة العقارية",
                "about" to "من نحن",
                "contact" to "تواصل معنا"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickLinks.forEach { (key, label) ->
                    Surface(
                        color = BeityWhite.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onQuickNav(key) }
                            .testTag("quick_nav_$key")
                    ) {
                        Text(
                            text = label,
                            color = BeityWhite.copy(alpha = 0.92f),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.size(4.dp))
        }
    }
}
