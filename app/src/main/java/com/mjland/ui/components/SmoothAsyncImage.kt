package com.mjland.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.mjland.ui.utils.shimmerEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@Composable
fun SmoothAsyncImage(
    model: Any? = null,
    contentDescription: String? = null,
    imageUrl: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    crossfadeDuration: Int = 500,
    shape: RoundedCornerShape? = null
) {
    val context = LocalContext.current
    
    val imageModifier = if (shape != null) {
        modifier.clip(shape)
    } else {
        modifier
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(model ?: imageUrl)
            .crossfade(crossfadeDuration)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = imageModifier.shimmerEffect()
    )
}
