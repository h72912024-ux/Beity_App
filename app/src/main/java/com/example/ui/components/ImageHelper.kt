package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import java.io.ByteArrayOutputStream

@Composable
fun BeityPropertyImage(
    imageSource: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val source = imageSource.orEmpty()

    when {
        source.startsWith("preset:") || source.isBlank() -> {
            val resId = when (source) {
                "preset:villa" -> R.drawable.img_prop_villa
                "preset:apartment" -> R.drawable.img_prop_apartment
                "preset:chalet" -> R.drawable.img_prop_chalet
                "preset:hero" -> R.drawable.img_hero_luxury
                else -> R.drawable.img_prop_villa
            }
            Image(
                painter = painterResource(id = resId),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
        source.startsWith("data:image/") -> {
            val bytes = remember(source) {
                try {
                    val base64Part = source.substringAfter("base64,", "")
                    if (base64Part.isNotEmpty()) Base64.decode(base64Part, Base64.DEFAULT) else null
                } catch (_: Exception) {
                    null
                }
            }
            if (bytes != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(bytes).crossfade(true).build(),
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = contentScale
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_prop_villa),
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = contentScale
                )
            }
        }
        else -> {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(source)
                    .crossfade(true)
                    .error(R.drawable.img_prop_villa)
                    .placeholder(R.drawable.img_hero_luxury)
                    .build(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
    }
}

fun compressImageUriToDataUrl(context: Context, uri: Uri, maxDimension: Int = 520): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val original = inputStream.use { BitmapFactory.decodeStream(it) } ?: return null
        val ratio = minOf(
            maxDimension.toFloat() / original.width.coerceAtLeast(1),
            maxDimension.toFloat() / original.height.coerceAtLeast(1),
            1f
        )
        val targetW = (original.width * ratio).toInt().coerceAtLeast(1)
        val targetH = (original.height * ratio).toInt().coerceAtLeast(1)
        val scaled = if (ratio < 1f) Bitmap.createScaledBitmap(original, targetW, targetH, true) else original

        var quality = 70
        var bytes: ByteArray
        do {
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            bytes = baos.toByteArray()
            quality -= 15
        } while (bytes.size > 58_000 && quality >= 25)

        if (scaled != original) scaled.recycle()
        original.recycle()

        val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
        "data:image/jpeg;base64,$encoded"
    } catch (_: Exception) {
        null
    }
}
