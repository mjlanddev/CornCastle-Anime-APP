package com.mjland.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mjland.database.ContinueWatchingEntity

@Composable
fun ContinueWatchingCompactItem(
    watch: ContinueWatchingEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(102.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            
            if (!watch.bannerImage.isNullOrEmpty()) {
                SmoothAsyncImage(
                    model = watch.bannerImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = 0.25f)
                )
            }

            SmoothAsyncImage(
                model = watch.coverImage,
                contentDescription = watch.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )

            val lastProgress = watch.lastProgress ?: 0f
            if (lastProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.BottomStart)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(lastProgress)
                            .background(Color(0xFFE50914)) 
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = watch.title,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 14.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        val subtitleText = remember(watch) {
            val formatStr = if (watch.format == "MOVIE" || watch.format == "Movie") "Movie" else (watch.format ?: "TV")
            if (watch.format == "MOVIE" || watch.format == "Movie") {
                formatStr
            } else if (watch.episodes != null) {
                "EP ${watch.episodes} • $formatStr"
            } else {
                formatStr
            }
        }
        Text(
            text = subtitleText,
            color = Color.White.copy(alpha = 0.4f),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}
