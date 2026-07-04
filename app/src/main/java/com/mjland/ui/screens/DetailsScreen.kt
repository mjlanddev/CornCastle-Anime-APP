package com.mjland.ui.screens

import android.os.Build
import android.text.Html
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
// import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.mjland.model.AnimeDetails
import com.mjland.model.AnimeMedia
import com.mjland.model.CharacterEdge
import com.mjland.viewmodel.DetailsViewModel
import com.mjland.ui.theme.interFontFamily
import com.mjland.ui.icons.*
import com.mjland.ui.utils.shimmerEffect
import com.mjland.ui.components.SmoothAsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            content()
        }
    }
}

@Composable
fun DetailsScreen(
    animeId: Int,
    onBackClick: () -> Unit,
    onPlayClick: (Int, String, Int?) -> Unit,
    onAnimeClick: (AnimeMedia) -> Unit,
    onGenreClick: (String) -> Unit = {},
    onTagClick: (String) -> Unit = {},
    viewModel: DetailsViewModel = viewModel()
) {
    val details by viewModel.animeDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isBookmarked by viewModel.isBookmarked.collectAsState()
    val lastWatchedEp by viewModel.lastWatchedEpisode.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(animeId) {
        viewModel.fetchDetails(context, animeId)
        viewModel.checkBookmarkedStatus(context, animeId)
    }

    var targetBgColor by remember { mutableStateOf(Color.Transparent) }
    val bgColor by animateColorAsState(targetValue = targetBgColor, animationSpec = tween(1000), label = "heroBgColor")
    val detailsHazeState = remember { HazeState() }

    LaunchedEffect(details) {
        val imageUrl = details?.bannerImage ?: details?.coverImage?.extraLarge
        if (imageUrl != null) {
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) {
                val bitmap = result.drawable.toBitmap()
                Palette.from(bitmap).generate { palette ->
                    val colorInt = palette?.dominantSwatch?.rgb 
                        ?: palette?.darkVibrantSwatch?.rgb 
                        ?: palette?.darkMutedSwatch?.rgb
                    
                    if (colorInt != null) {
                        targetBgColor = Color(colorInt)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .haze(detailsHazeState)
    ) {
        
        if (error != null && details == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                com.mjland.ui.components.GlassmorphicErrorView(
                    message = error ?: "Unknown error",
                    onRetry = { viewModel.fetchDetails(context, animeId) }
                )
            }
        } else if (isLoading) {
            DetailsSkeletonView(detailsHazeState)
        } else if (details != null) {
            val anime = details!!
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp) 
                    ) {
                        SmoothAsyncImage(
                            imageUrl = anime.bannerImage ?: anime.coverImage?.extraLarge,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            bgColor.copy(alpha = 0.6f),
                                            bgColor.copy(alpha = 0.2f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.5f),
                                            Color.Black
                                        )
                                    )
                                )
                        )
                    }
                }

                
                item {
                    CinematicHeroSection(
                        modifier = Modifier.offset(y = (-45).dp), 
                        anime = anime,
                        onPlayClick = { id, title, ep ->
                            viewModel.addToContinueWatching(context, anime)
                            onPlayClick(id, title, ep)
                        },
                        isBookmarked = isBookmarked,
                        lastWatchedEp = lastWatchedEp,
                        onBookmarkClick = {
                            viewModel.toggleBookmark(context, anime)
                        }
                    )
                }

                
                item {
                    ModernStatsRow(anime, Modifier.offset(y = (-20).dp))
                }

                
                item {
                    ModernOverviewSection(anime, Modifier.offset(y = (-5).dp))
                }

                
                item {
                    ModernCategoriesSection(anime, onGenreClick, onTagClick)
                }

                
                anime.trailer?.let { trailer ->
                    if (!trailer.id.isNullOrEmpty()) {
                        item {
                            ModernTrailerSection(trailer)
                        }
                    }
                }

                
                anime.relations?.edges?.let { relations ->
                    if (relations.isNotEmpty()) {
                        item {
                            ModernHorizontalSection(title = "Related Content", items = relations.mapNotNull { it.node }) { onAnimeClick(it) }
                        }
                    }
                }

                
                anime.characters?.edges?.let { characters ->
                    if (characters.isNotEmpty()) {
                        item {
                            ModernCharactersSection(characters)
                        }
                    }
                }

                
                anime.staff?.edges?.let { staff ->
                    if (staff.isNotEmpty()) {
                        item {
                            ModernStaffSection(staff)
                        }
                    }
                }

                
                anime.recommendations?.nodes?.mapNotNull { it.mediaRecommendation }?.let { recs ->
                    if (recs.isNotEmpty()) {
                        item {
                            ModernHorizontalSection(title = "More Like This", items = recs) { onAnimeClick(it) }
                        }
                    }
                }
            }
        }

        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = FluentuiSystemIconsChevronLeft,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CinematicHeroSection(
    modifier: Modifier = Modifier,
    anime: AnimeDetails,
    onPlayClick: (Int, String, Int?) -> Unit,
    isBookmarked: Boolean,
    lastWatchedEp: Int? = null,
    onBookmarkClick: () -> Unit
) {
    val title = anime.title?.english ?: anime.title?.romaji ?: "Unknown"
    val mainStudio = anime.studios?.edges?.firstOrNull { it.isMain == true }?.node?.name
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            SmoothAsyncImage(
                imageUrl = anime.coverImage?.large ?: anime.coverImage?.extraLarge,
                modifier = Modifier
                    .width(110.dp) 
                    .aspectRatio(2f/3f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                
                anime.nextAiringEpisode?.let { next ->
                    val timeUntil = next.timeUntilAiring ?: 0L
                    if (timeUntil > 0) {
                        val days = timeUntil / 86400
                        val hours = (timeUntil % 86400) / 3600
                        val countdown = if (days > 0) "${days}d ${hours}h" else "${hours}h"
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = FluentuiSystemIconsCalendarClockFilled,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "EP ${next.episode}: $countdown",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }

                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val formatText = when (anime.format) {
                        "TV" -> "TV"
                        "MOVIE" -> "Movie"
                        else -> anime.format ?: ""
                    }
                    if (formatText.isNotEmpty()) {
                        Text(
                            text = formatText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Box(modifier = Modifier.size(2.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)))
                    }

                    anime.status?.let { status ->
                        val statusLabel = when (status) {
                            "RELEASING" -> "Airing"
                            "FINISHED" -> "Completed"
                            else -> status.lowercase().replaceFirstChar { it.uppercase() }
                        }
                        val statusColor = if (status == "RELEASING") MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f)
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                    }
                }

                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        lineHeight = 26.sp
                    ),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                
                if (!mainStudio.isNullOrEmpty()) {
                    Text(
                        text = mainStudio,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.4f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onPlayClick(anime.id, title, lastWatchedEp) },
                enabled = anime.status != "NOT_YET_RELEASED",
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.White.copy(alpha = 0.1f),
                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = FluentuiSystemIconsPlayFilled,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        anime.status == "NOT_YET_RELEASED" -> "Upcoming"
                        anime.format == "MOVIE" && lastWatchedEp != null -> "Resume Movie"
                        lastWatchedEp != null -> "Play Ep. $lastWatchedEp"
                        else -> "Play Now"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = interFontFamily
                    )
                )
            }

            IconButton(
                onClick = onBookmarkClick,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isBookmarked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                    .border(1.dp, if (isBookmarked) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            ) {
                Icon(
                    imageVector = if (isBookmarked) FluentuiSystemIconsHeartFilled else FluentuiSystemIconsHeartOutline,
                    contentDescription = "Favorite",
                    tint = if (isBookmarked) Color(0xFFFF4081) else Color.White, 
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ModernStatsRow(anime: AnimeDetails, modifier: Modifier = Modifier) {
    val score = anime.averageScore
    val ratingColor = if (score != null && score > 75) Color(0xFF4CAF50) 
                     else if (score != null && score > 50) Color(0xFFFFC107) 
                     else Color.White

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(
            value = if (anime.averageScore != null) "${anime.averageScore}%" else "--",
            label = "Score",
            icon = FluentuiSystemIconsStarFilled,
            color = ratingColor
        )
        
        VerticalDivider()

        StatItem(
            value = if (anime.format == "MOVIE") "${anime.duration ?: "--"}m" else "${anime.episodes ?: "--"} EP",
            label = if (anime.format == "MOVIE") "Duration" else "Episodes",
            icon = if (anime.format == "MOVIE") FluentuiSystemIconsMoviesAndTvFilled else FluentuiSystemIconsListBarFilled
        )

        VerticalDivider()

        StatItem(
            value = if (anime.seasonYear != null) "${anime.seasonYear}" else "--",
            label = "Year",
            icon = FluentuiSystemIconsCalendarClockFilled
        )

        VerticalDivider()

        StatItem(
            value = anime.format ?: "TV",
            label = "Format",
            icon = FluentuiSystemIconsInfoFilled
        )
    }
}

@Composable
fun StatItem(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color = Color.White) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
        Text(text = value, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = color)
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium), color = Color.White.copy(alpha = 0.3f))
    }
}

@Composable
fun VerticalDivider() {
    Box(modifier = Modifier.height(24.dp).width(1.dp).background(Color.White.copy(alpha = 0.1f)))
}

@Composable
fun ModernOverviewSection(anime: AnimeDetails, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }
    val cleanDescription = remember(anime.description) {
        if (anime.description.isNullOrEmpty()) "No information available."
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(anime.description, Html.FROM_HTML_MODE_COMPACT).toString().trim()
            } else {
                Html.fromHtml(anime.description).toString().trim()
            }.replace(Regex("\n{3,}"), "\n\n")
        }
    }

    Column(modifier = modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(
            text = "STORYLINE",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
            color = Color.White.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .clickable { isExpanded = !isExpanded }
        ) {
            Text(
                text = cleanDescription,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = Color.White.copy(alpha = 0.7f),
                maxLines = if (isExpanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis
            )
            
            if (!isExpanded && cleanDescription.length > 200) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 150f
                            )
                        )
                )
                Text(
                    text = "Read More",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
fun ModernCategoriesSection(anime: AnimeDetails, onGenreClick: (String) -> Unit, onTagClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(anime.genres ?: emptyList()) { genre ->
                Text(
                    text = genre,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .clickable { onGenreClick(genre) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(anime.tags?.mapNotNull { it.name } ?: emptyList()) { tag ->
                Text(
                    text = tag,
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .clickable { onTagClick(tag) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
fun ModernTrailerSection(trailer: com.mjland.model.Trailer) {
    var isPlaying by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = "TRAILER",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
            color = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .shimmerEffect()
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
        ) {
            if (isPlaying) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewClient = WebViewClient()
                            webChromeClient = WebChromeClient()
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            setBackgroundColor(android.graphics.Color.BLACK)
                            loadUrl("https://www.youtube.com/embed/${trailer.id}?autoplay=1")
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val thumbnailUrl = trailer.thumbnail ?: "https://img.youtube.com/vi/${trailer.id}/maxresdefault.jpg"
                SmoothAsyncImage(
                    model = thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { isPlaying = true },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FluentuiSystemIconsPlayFilled,
                            contentDescription = "Play Trailer",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernHorizontalSection(title: String, items: List<AnimeMedia>, onClick: (AnimeMedia) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 20.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
            color = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(items, key = { it.id }) { anime ->
                Column(modifier = Modifier.width(110.dp).clickable { onClick(anime) }) {
                    SmoothAsyncImage(
                        imageUrl = anime.coverImage?.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = anime.title?.english ?: anime.title?.romaji ?: "Unknown",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, lineHeight = 14.sp),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}






@Composable
fun ModernCharactersSection(characters: List<CharacterEdge>) {
    Column(modifier = Modifier.padding(vertical = 20.dp)) {
        Text(
            text = "CHARACTERS",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
            color = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(characters) { edge ->
                val character = edge.node
                val imageUrl = character?.image?.large
                val isDefaultImage = imageUrl?.contains("default.jpg") == true
                Column(modifier = Modifier.width(90.dp)) {
                    if (isDefaultImage || imageUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shimmerEffect()
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(
                                imageVector = FluentuiSystemIconsPersonFilled,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        SmoothAsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shimmerEffect()
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = character?.name?.full ?: "Unknown",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = edge.role ?: "",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.4f),
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ModernStaffSection(staff: List<com.mjland.model.StaffEdge>) {
    Column(modifier = Modifier.padding(vertical = 20.dp)) {
        Text(
            text = "STAFF",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
            color = Color.White.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(staff) { edge ->
                val person = edge.node
                val imageUrl = person?.image?.large
                val isDefaultImage = imageUrl?.contains("default.jpg") == true
                Column(modifier = Modifier.width(90.dp)) {
                    if (isDefaultImage || imageUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shimmerEffect()
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(
                                imageVector = FluentuiSystemIconsPersonFilled,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        SmoothAsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shimmerEffect()
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = person?.name?.full ?: "Unknown",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = edge.role ?: "",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.4f),
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}


fun Modifier.fillModifierHeightLine(): Modifier = this.wrapContentWidth().height(IntrinsicSize.Min)





@Composable
fun DetailsSkeletonView(hazeState: HazeState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(top = 56.dp, bottom = 40.dp)
    ) {
        
        item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                hazeState = hazeState
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    Box(
                        modifier = Modifier
                            .width(88.dp)
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .shimmerEffect()
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    )

                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(45.dp)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .shimmerEffect()
                            )
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .shimmerEffect()
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(180.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerEffect()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .shimmerEffect()
                    )
                }
            }
        }

        
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                hazeState = hazeState
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { i ->
                        if (i > 0) {
                            Box(
                                modifier = Modifier
                                    .width(0.5.dp)
                                    .height(32.dp)
                                    .background(Color.White.copy(alpha = 0.1f))
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.06f))
                            )
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                            )
                        }
                    }
                }
            }
        }

        
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                hazeState = hazeState
            ) {
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                    )
                }
            }
        }
    }
}


