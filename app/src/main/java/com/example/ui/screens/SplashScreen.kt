package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.BeityLogo
import com.example.ui.components.BeityLogoVariant
import com.example.ui.theme.BeityGold
import com.example.ui.theme.BeityGoldLight
import com.example.ui.theme.BeityNavy
import com.example.ui.theme.BeityNavyDark
import com.example.ui.theme.BeityWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.82f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_gold")
    val shimmerShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, animationSpec = tween(750))
        logoScale.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        delay(1200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BeityNavyDark, BeityNavy, Color(0xFF0B2552))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animated subtle golden architectural accents
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = BeityGold.copy(alpha = 0.08f + shimmerShift * 0.07f),
                radius = w * 0.55f,
                center = Offset(w * 0.5f, h * 0.44f)
            )
            drawLine(
                color = BeityGold.copy(alpha = 0.25f),
                start = Offset(w * 0.12f, h * (0.20f + shimmerShift * 0.02f)),
                end = Offset(w * 0.88f, h * (0.20f + shimmerShift * 0.02f)),
                strokeWidth = 2f
            )
            drawLine(
                color = BeityGold.copy(alpha = 0.25f),
                start = Offset(w * 0.18f, h * (0.78f - shimmerShift * 0.02f)),
                end = Offset(w * 0.82f, h * (0.78f - shimmerShift * 0.02f)),
                strokeWidth = 2f
            )
        }

        Column(
            modifier = Modifier
                .padding(32.dp)
                .alpha(logoAlpha.value)
                .scale(logoScale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BeityLogo(variant = BeityLogoVariant.ON_DARK, compact = false)

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "كل ما تحتاجه في عالم العقارات.",
                color = BeityGoldLight,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "منصة عقارية فاخرة للسوق المصري والعربي",
                color = BeityWhite.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .width(140.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = BeityGold,
                trackColor = BeityWhite.copy(alpha = 0.15f)
            )
        }
    }
}
