package com.mjland.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mjland.database.BookmarkEntity
import com.mjland.database.ContinueWatchingEntity
import com.mjland.model.AnimeMedia
import com.mjland.ui.icons.*
import com.mjland.viewmodel.MySpaceViewModel

@Composable
fun MySpaceScreen(
    onAnimeClick: (AnimeMedia) -> Unit,
    viewModel: MySpaceViewModel = viewModel()
) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My Space",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Your favorites & history.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.45f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            
            item {
                val anilistToken by viewModel.anilistToken.collectAsState()
                val viewerProfile by viewModel.viewerProfile.collectAsState()
                val anilistLists by viewModel.anilistMediaLists.collectAsState()
                val anilistFavorites by viewModel.anilistFavorites.collectAsState()
                val isSyncing by viewModel.isSyncing.collectAsState()

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "CLOUDSYNC (ANILIST)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.0.sp
                                ),
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            if (anilistToken != null) {
                                val infiniteTransition = rememberInfiniteTransition(label = "sync")
                                val syncRotation by if (isSyncing) {
                                    infiniteTransition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = 360f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1200, easing = LinearEasing),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "rotation"
                                    )
                                } else {
                                    remember { mutableStateOf(0f) }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .clickable(enabled = !isSyncing) { viewModel.refreshAnilist() }
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = "Sync AniList",
                                        tint = if (isSyncing) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .graphicsLayer {
                                                rotationZ = syncRotation
                                            }
                                    )
                                }
                            }
                        }

                        if (anilistToken != null) {
                            Text(
                                text = "Logout",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                modifier = Modifier.clickable { viewModel.logoutAnilist() }
                            )
                        }
                    }

                    if (anilistToken == null) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF141416))
                                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .clickable {
                                    val clientId = com.mjland.BuildConfig.ANILIST_API_KEY
                                    val clientSecret = com.mjland.BuildConfig.ANILIST_CLIENT_SECRET
                                    val isCodeFlow = clientSecret.isNotEmpty() && clientSecret != "YOUR_CLIENT_SECRET_HERE"
                                    
                                    val responseType = if (isCodeFlow) "code" else "token"
                                    val url = "https://anilist.co/api/v2/oauth/authorize?client_id=$clientId&redirect_uri=corncastle://anilist-auth&response_type=$responseType"
                                    
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    context.startActivity(intent)
                                }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = com.mjland.R.drawable.ic_anilist),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Connect AniList",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    } else if (isSyncing && viewerProfile == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White.copy(alpha = 0.5f),
                                strokeWidth = 2.dp
                            )
                        }
                    } else if (viewerProfile != null) {
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = viewerProfile?.avatar?.large,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E1E20))
                            )
                            Column {
                                Text(
                                    text = viewerProfile?.name ?: "User",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = if (isSyncing) "Syncing..." else "Connected",
                                    color = if (isSyncing) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        
                        var selectedTab by remember { mutableStateOf("") }
                        val availableTabs = remember(anilistFavorites, anilistLists) {
                            val list = mutableListOf<String>()
                            if (!anilistFavorites.isNullOrEmpty()) {
                                list.add("Favorites")
                            }
                            anilistLists?.forEach { group ->
                                if (!group.entries.isNullOrEmpty() && group.name != null) {
                                    list.add(group.name)
                                }
                            }
                            list
                        }

                        LaunchedEffect(availableTabs) {
                            if (selectedTab.isEmpty() || !availableTabs.contains(selectedTab)) {
                                selectedTab = availableTabs.firstOrNull() ?: ""
                            }
                        }

                        if (availableTabs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(availableTabs) { tab ->
                                    val isSelected = selectedTab == tab
                                    val count = remember(tab, anilistFavorites, anilistLists) {
                                        if (tab == "Favorites") {
                                            anilistFavorites?.size ?: 0
                                        } else {
                                            anilistLists?.firstOrNull { it.name == tab }?.entries?.size ?: 0
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                else Color.White.copy(alpha = 0.05f)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                                else Color.White.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable { selectedTab = tab }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "$tab ($count)",
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                                        )
                                    }
                                }
                            }
                        }

                        
                        Spacer(modifier = Modifier.height(8.dp))
                        if (selectedTab == "Favorites" && !anilistFavorites.isNullOrEmpty()) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(anilistFavorites!!, key = { "fav_${it.id}" }) { anime ->
                                    ContinueWatchingCompactItem(
                                        watch = ContinueWatchingEntity(
                                            animeId = anime.id,
                                            title = anime.title?.english ?: anime.title?.romaji ?: "Anime",
                                            coverImage = anime.coverImage?.large,
                                            bannerImage = anime.bannerImage,
                                            genres = anime.genres?.joinToString(","),
                                            averageScore = anime.averageScore,
                                            format = anime.format,
                                            episodes = anime.episodes,
                                            lastProgress = 0f,
                                            lastLanguage = "sub",
                                            timestamp = 0L
                                        ),
                                        onClick = { onAnimeClick(anime) }
                                    )
                                }
                            }
                        } else if (anilistLists != null) {
                            val activeGroup = anilistLists!!.firstOrNull { it.name == selectedTab }
                            if (activeGroup != null && !activeGroup.entries.isNullOrEmpty()) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(activeGroup.entries, key = { "list_${activeGroup.name}_${it.id}" }) { entry ->
                                        val anime = entry.media
                                        if (anime != null) {
                                            ContinueWatchingCompactItem(
                                                watch = ContinueWatchingEntity(
                                                    animeId = anime.id,
                                                    title = anime.title?.english ?: anime.title?.romaji ?: "Anime",
                                                    coverImage = anime.coverImage?.large,
                                                    bannerImage = anime.bannerImage,
                                                    genres = anime.genres?.joinToString(","),
                                                    averageScore = anime.averageScore,
                                                    format = anime.format,
                                                    episodes = anime.episodes,
                                                    lastProgress = entry.progress?.toFloat() ?: 0f,
                                                    lastLanguage = "sub",
                                                    timestamp = entry.updatedAt ?: 0L
                                                ),
                                                onClick = { onAnimeClick(anime) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            
            item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "CONTINUE WATCHING",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.0.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            
                            if (continueWatching.isNotEmpty()) {
                                Text(
                                    text = "Clear History",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.clickable { viewModel.clearHistory() }
                                )
                            }
                        }

                        if (continueWatching.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.02f))
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = FluentuiSystemIconsMoviesAndTvFilled,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.15f),
                                        modifier = Modifier.size(42.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "No watch history",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = "Anime you watch will appear here",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        } else {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(continueWatching, key = { it.animeId }) { watch ->
                                    ContinueWatchingCompactItem(
                                        watch = watch,
                                        onClick = {
                                            val animeMedia = AnimeMedia(
                                                id = watch.animeId,
                                                title = com.mjland.model.MediaTitle(
                                                    romaji = watch.title,
                                                    english = watch.title,
                                                    native = watch.title
                                                ),
                                                coverImage = com.mjland.model.CoverImage(
                                                    extraLarge = watch.coverImage,
                                                    large = watch.coverImage
                                                ),
                                                bannerImage = watch.bannerImage,
                                                format = watch.format,
                                                episodes = watch.episodes,
                                                status = null,
                                                description = null,
                                                averageScore = watch.averageScore,
                                                genres = watch.genres?.split(",")?.filter { it.isNotBlank() },
                                                seasonYear = null
                                            )
                                            onAnimeClick(animeMedia)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "MY FAVORITES",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.0.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }

                            if (bookmarks.isNotEmpty()) {
                                Text(
                                    text = "Clear Favorites",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.clickable { viewModel.clearWatchlist() }
                                )
                            }
                        }

                        if (bookmarks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.02f))
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = FluentuiSystemIconsBookmarkOutline,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.15f),
                                        modifier = Modifier.size(42.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "No favorites yet",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = "Tap the bookmark icon to save anime",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                bookmarks.forEach { bookmark ->
                                    BookmarkListItem(
                                        bookmark = bookmark,
                                        onClick = {
                                            val animeMedia = AnimeMedia(
                                                id = bookmark.animeId,
                                                title = com.mjland.model.MediaTitle(
                                                    romaji = bookmark.title,
                                                    english = bookmark.title,
                                                    native = bookmark.title
                                                ),
                                                coverImage = com.mjland.model.CoverImage(
                                                    extraLarge = bookmark.coverImage,
                                                    large = bookmark.coverImage
                                                ),
                                                bannerImage = bookmark.bannerImage,
                                                format = bookmark.format,
                                                episodes = bookmark.episodes,
                                                status = null,
                                                description = null,
                                                averageScore = bookmark.averageScore,
                                                genres = bookmark.genres?.split(",")?.filter { it.isNotBlank() },
                                                seasonYear = null
                                            )
                                            onAnimeClick(animeMedia)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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
                AsyncImage(
                    model = watch.bannerImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = 0.25f)
                )
            }

            AsyncImage(
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

@Composable
fun BookmarkListItem(
    bookmark: BookmarkEntity,
    onClick: () -> Unit
) {
    val scoreText = remember(bookmark) {
        if (bookmark.averageScore != null && bookmark.averageScore > 0) {
            val scoreDouble = bookmark.averageScore / 10.0
            String.format("%.1f", scoreDouble)
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(Color(0xFF0C0C10))
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        
        if (!bookmark.bannerImage.isNullOrEmpty()) {
            AsyncImage(
                model = bookmark.bannerImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer(alpha = 0.15f)
            )
            
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.94f),
                                Color.Black.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .aspectRatio(0.7f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF141416))
            ) {
                AsyncImage(
                    model = bookmark.coverImage,
                    contentDescription = bookmark.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (scoreText != null) {
                    val scoreValue = bookmark.averageScore ?: 0
                    val scoreColor = if (scoreValue > 75) Color(0xFF4CAF50) else if (scoreValue > 50) Color(0xFFFFC107) else Color.White
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.8f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = FluentuiSystemIconsStarFilled,
                                contentDescription = "Score",
                                tint = scoreColor,
                                modifier = Modifier.size(8.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${bookmark.averageScore}%",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = bookmark.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        letterSpacing = (-0.1).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                val detailsParts = listOfNotNull(
                    when (bookmark.format) {
                        "MOVIE" -> "Movie"
                        "TV" -> "TV"
                        "TV_SHORT" -> "Short"
                        "OVA" -> "OVA"
                        "ONA" -> "ONA"
                        "SPECIAL" -> "Special"
                        "MUSIC" -> "Music"
                        else -> bookmark.format
                    },
                    if (bookmark.format != "MOVIE" && bookmark.format != "Movie") bookmark.episodes?.let { "$it Eps" } else null
                )
                val formatText = detailsParts.joinToString(" • ")
                
                if (formatText.isNotEmpty()) {
                    Text(
                        text = formatText,
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                
                val genreList = remember(bookmark.genres) {
                    bookmark.genres?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                }
                if (genreList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        genreList.take(3).forEach { genre ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = genre,
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
