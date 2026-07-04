package com.mjland.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mjland.ui.utils.shimmerEffect

@Composable
fun PosterSkeleton(
    modifier: Modifier = Modifier,
    width: Int = 110,
    height: Int = 160
) {
    Box(
        modifier = modifier
            .width(width.dp)
            .height(height.dp)
            .clip(RoundedCornerShape(12.dp))
            .shimmerEffect()
    )
}

@Composable
fun TextSkeleton(
    modifier: Modifier = Modifier,
    width: Int = 80,
    height: Int = 14
) {
    Box(
        modifier = modifier
            .width(width.dp)
            .height(height.dp)
            .clip(RoundedCornerShape(4.dp))
            .shimmerEffect()
    )
}
