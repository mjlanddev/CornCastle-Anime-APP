package com.mjland.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mjland.model.AnimeMedia

@Composable
fun AnimePosterView(
    anime: AnimeMedia,
    onClick: (AnimeMedia) -> Unit,
    modifier: Modifier = Modifier,
    width: Int = 110,
    imageHeight: Int = 160
) {
    Column(modifier = modifier
        .width(width.dp)
        .clickable { onClick(anime) }
    ) {
        SmoothAsyncImage(
            imageUrl = anime.coverImage?.large,
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight.dp),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = anime.title?.english ?: anime.title?.romaji ?: "Unknown",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 14.sp
            ),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
