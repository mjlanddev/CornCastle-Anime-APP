package com.mjland.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.delay
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.mjland.MainViewModel
import com.mjland.model.AnimeMedia
import com.mjland.ui.theme.interFontFamily
import com.mjland.ui.icons.*

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow

import com.mjland.viewmodel.MySpaceViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mjland.ui.screens.ContinueWatchingCompactItem

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onAnimeClick: (AnimeMedia) -> Unit,
    onGenreClick: (String) -> Unit = {},
    onTagClick: (String) -> Unit = {}
) {
    val mySpaceViewModel: MySpaceViewModel = viewModel()
    val continueWatching by mySpaceViewModel.continueWatching.collectAsState()
    val anilistFavorites by viewModel.anilistFavorites.collectAsState()
    val anilistLists by viewModel.anilistMediaLists.collectAsState()
    val trending by viewModel.trendingAnime.collectAsState()
    val popular by viewModel.popularAnime.collectAsState()
    val topRated by viewModel.topRatedAnime.collectAsState()
    val currentlyAiring by viewModel.currentlyAiring.collectAsState()
    val finishedAiring by viewModel.finishedAiring.collectAsState()
    val upcoming by viewModel.upcomingAnime.collectAsState()
    val movies by viewModel.movies.collectAsState()
    val actionAnime by viewModel.actionAnime.collectAsState()
    val romanceAnime by viewModel.romanceAnime.collectAsState()
    val fantasyAnime by viewModel.fantasyAnime.collectAsState()
    val sciFiAnime by viewModel.sciFiAnime.collectAsState()
    val mostFavorited by viewModel.mostFavorited.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val allLoadedAnime = remember(trending, popular, topRated, currentlyAiring, finishedAiring, upcoming, movies, mostFavorited, actionAnime, romanceAnime, fantasyAnime, sciFiAnime) {
        (trending + popular + topRated + currentlyAiring + finishedAiring + upcoming + movies + mostFavorited + actionAnime + romanceAnime + fantasyAnime + sciFiAnime).filter { !it.bannerImage.isNullOrEmpty() }
    }

    val genresToDisplay = remember {
        listOf("Action", "Romance", "Fantasy", "Sci-Fi", "Comedy", "Adventure", "Drama", "Supernatural", "Slice of Life", "Mystery", "Sports")
    }

    
    val availableHomeTabs = remember(anilistFavorites, anilistLists) {
        val list = mutableListOf<String>()
        if (anilistFavorites.isNotEmpty()) {
            list.add("Favorites")
        }
        anilistLists.forEach { group ->
            val entries = group.entries
            if (!entries.isNullOrEmpty() && group.name != null) {
                val listMedia = entries.mapNotNull { it.media }
                if (listMedia.isNotEmpty()) {
                    list.add(group.name)
                }
            }
        }
        list
    }

    val genreBackdrops = remember(allLoadedAnime, genresToDisplay) {
        val result = mutableMapOf<String, String>()
        val usedBanners = mutableSetOf<String>()
        
        
        for (genre in genresToDisplay) {
            val matchingAnime = allLoadedAnime.firstOrNull { anime ->
                anime.genres?.contains(genre) == true && !anime.bannerImage.isNullOrEmpty() && !usedBanners.contains(anime.bannerImage)
            }
            if (matchingAnime?.bannerImage != null) {
                result[genre] = matchingAnime.bannerImage
                usedBanners.add(matchingAnime.bannerImage)
            }
        }
        
        
        for (genre in genresToDisplay) {
            if (!result.containsKey(genre)) {
                val fallbackAnime = allLoadedAnime.firstOrNull { anime ->
                    anime.genres?.contains(genre) == true && !anime.bannerImage.isNullOrEmpty()
                }
                if (fallbackAnime?.bannerImage != null) {
                    result[genre] = fallbackAnime.bannerImage
                } else {
                    val coverAnime = allLoadedAnime.firstOrNull { anime ->
                        anime.genres?.contains(genre) == true && !anime.coverImage?.large.isNullOrEmpty()
                    }
                    if (coverAnime?.coverImage?.large != null) {
                        result[genre] = coverAnime.coverImage.large
                    }
                }
            }
        }
        result
    }

    val tagsToDisplay = remember {
        listOf("Isekai", "Magic", "School", "Super Power", "Military", "Historical", "Martial Arts", "Space", "Post-Apocalyptic", "Tragedy")
    }

    val tagBackdrops = remember(allLoadedAnime, tagsToDisplay) {
        val result = mutableMapOf<String, String>()
        val usedBanners = mutableSetOf<String>()
        
        
        for (tag in tagsToDisplay) {
            val matchingAnime = allLoadedAnime.firstOrNull { anime ->
                anime.tags?.any { it.name == tag } == true && !anime.bannerImage.isNullOrEmpty() && !usedBanners.contains(anime.bannerImage)
            }
            if (matchingAnime?.bannerImage != null) {
                result[tag] = matchingAnime.bannerImage
                usedBanners.add(matchingAnime.bannerImage)
            }
        }
        
        
        for (tag in tagsToDisplay) {
            if (!result.containsKey(tag)) {
                val fallbackAnime = allLoadedAnime.firstOrNull { anime ->
                    anime.tags?.any { it.name == tag } == true && !anime.bannerImage.isNullOrEmpty()
                }
                if (fallbackAnime?.bannerImage != null) {
                    result[tag] = fallbackAnime.bannerImage
                } else {
                    val coverAnime = allLoadedAnime.firstOrNull { anime ->
                        anime.tags?.any { it.name == tag } == true && !anime.coverImage?.large.isNullOrEmpty()
                    }
                    if (coverAnime?.coverImage?.large != null) {
                        result[tag] = coverAnime.coverImage.large
                    }
                }
            }
        }
        result
    }

    
    val pagerState = rememberPagerState(pageCount = { if (trending.isNotEmpty()) trending.take(5).size else 0 })
    var dominantColor by remember { mutableStateOf(Color.Transparent) }
    val animatedDominantColor by animateColorAsState(
        targetValue = dominantColor,
        animationSpec = tween(1000)
    )

    val currentAnime = if (trending.isNotEmpty()) trending.getOrNull(pagerState.currentPage) else null
    val currentImage = currentAnime?.bannerImage ?: currentAnime?.coverImage?.extraLarge ?: currentAnime?.coverImage?.large

    val context = LocalContext.current
    LaunchedEffect(currentImage) {
        if (currentImage != null) {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(currentImage)
                .allowHardware(false)
                .build()
            
            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    androidx.palette.graphics.Palette.from(bitmap).generate { palette ->
                        palette?.dominantSwatch?.rgb?.let { colorInt ->
                            dominantColor = Color(colorInt)
                        } ?: palette?.mutedSwatch?.rgb?.let { colorInt ->
                            dominantColor = Color(colorInt)
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (error != null && trending.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                com.mjland.ui.components.GlassmorphicErrorView(
                    message = error ?: "Unknown error",
                    onRetry = { viewModel.refresh() }
                )
            }
        } else if (isLoading && trending.isEmpty()) {
            HomeSkeletonView()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 0.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                if (animatedDominantColor != Color.Transparent) {
                                    drawRect(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                animatedDominantColor.copy(alpha = 0.4f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "CornCastle",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                    text = "ANIME",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                }
                            }
                            Text(
                                text = "Discover trending series & movies",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.45f)
                            )
                        }

                        
                        if (trending.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HeroCompactPager(
                                animes = trending.take(5),
                                pagerState = pagerState,
                                onPlayClick = { onAnimeClick(it) }
                            )
                        }
                    }
                }

                if (continueWatching.isNotEmpty()) {
                    item {
                        ContinueWatchingSectionRow(
                            animes = continueWatching,
                            onAnimeClick = onAnimeClick
                        )
                    }
                }

                if (availableHomeTabs.isNotEmpty()) {
                    item {
                        var selectedHomeTab by remember { mutableStateOf("") }
                        LaunchedEffect(availableHomeTabs) {
                            if (selectedHomeTab.isEmpty() || !availableHomeTabs.contains(selectedHomeTab)) {
                                selectedHomeTab = availableHomeTabs.firstOrNull() ?: ""
                            }
                        }

                        val activeAnimes = remember(selectedHomeTab, anilistFavorites, anilistLists) {
                            if (selectedHomeTab == "Favorites") {
                                anilistFavorites
                            } else {
                                val group = anilistLists.firstOrNull { it.name == selectedHomeTab }
                                group?.entries?.mapNotNull { it.media } ?: emptyList()
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = com.mjland.R.drawable.ic_anilist),
                                        contentDescription = "AniList Icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "My AniList: $selectedHomeTab",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.5).sp
                                        ),
                                        color = Color.White
                                    )
                                }
                            }

                            
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                items(availableHomeTabs) { tab ->
                                    val isTabSelected = selectedHomeTab == tab
                                    val count = remember(tab, anilistFavorites, anilistLists) {
                                        if (tab == "Favorites") {
                                            anilistFavorites.size
                                        } else {
                                            anilistLists.firstOrNull { it.name == tab }?.entries?.size ?: 0
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (isTabSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                else Color.White.copy(alpha = 0.05f)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isTabSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                                else Color.White.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable { selectedHomeTab = tab }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "$tab ($count)",
                                            color = if (isTabSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                                        )
                                    }
                                }
                            }

                            
                            if (activeAnimes.isNotEmpty()) {
                                val rowState = rememberLazyListState()
                                LazyRow(
                                    state = rowState,
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(activeAnimes, key = { "home_anilist_${selectedHomeTab}_${it.id}" }) { anime ->
                                        AnimeCompactCard(
                                            anime = anime,
                                            onClick = { onAnimeClick(anime) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    AnimeSectionRow(
                        title = "Trending Now",
                        icon = FluentuiSystemIconsArrowTrending,
                        animes = trending.drop(5),
                        onAnimeClick = onAnimeClick,
                        onLoadMore = { viewModel.loadNextPage(com.mjland.AnimeSection.TRENDING) }
                    )
                }
                
                if (popular.isNotEmpty()) {
                    item {
                        AnimeSectionRow(
                            title = "Popular Absolute",
                            icon = FluentuiSystemIconsHeartFilled,
                            animes = popular,
                            onAnimeClick = onAnimeClick,
                            onLoadMore = { viewModel.loadNextPage(com.mjland.AnimeSection.POPULAR) }
                        )
                    }
                }
                
                if (topRated.isNotEmpty()) {
                    item {
                        AnimeSectionRow(
                            title = "Top Rated",
                            icon = FluentuiSystemIconsStarFilled,
                            animes = topRated,
                            onAnimeClick = onAnimeClick,
                            onLoadMore = { viewModel.loadNextPage(com.mjland.AnimeSection.TOP_RATED) }
                        )
                    }
                }
                
                if (currentlyAiring.isNotEmpty()) {
                    item {
                        AnimeSectionRow(
                            title = "Currently Airing",
                            icon = FluentuiSystemIconsTvFilled,
                            animes = currentlyAiring,
                            onAnimeClick = onAnimeClick,
                            onLoadMore = { viewModel.loadNextPage(com.mjland.AnimeSection.CURRENTLY_AIRING) }
                        )
                    }
                }
                
                if (finishedAiring.isNotEmpty()) {
                    item {
                        AnimeSectionRow(
                            title = "Finished Airing",
                            icon = FluentuiSystemIconsTvFilled,
                            animes = finishedAiring,
                            onAnimeClick = onAnimeClick,
                            onLoadMore = { viewModel.loadNextPage(com.mjland.AnimeSection.FINISHED_AIRING) }
                        )
                    }
                }
                
                if (genreBackdrops.isNotEmpty()) {
                    item {
                        PopularGenresRow(
                            genreBackdrops = genreBackdrops,
                            onGenreClick = onGenreClick
                        )
                    }
                }
                
                if (upcoming.isNotEmpty()) {
                    item {
                        AnimeSectionRow(
                            title = "Anticipated Upcoming",
                            icon = FluentuiSystemIconsArrowTrending,
                            animes = upcoming,
                            onAnimeClick = onAnimeClick,
                            onLoadMore = { viewModel.loadNextPage(com.mjland.AnimeSection.UPCOMING) }
                        )
                    }
                }
                
                if (movies.isNotEmpty()) {
                    item {
                        AnimeSectionRow(
                            title = "Movies & Specials",
                            icon = FluentuiSystemIconsMoviesAndTvFilled,
                            animes = movies,
                            onAnimeClick = onAnimeClick,
                            onLoadMore = { viewModel.loadNextPage(com.mjland.AnimeSection.MOVIES) }
                        )
                    }
                }
                
                if (mostFavorited.isNotEmpty()) {
                    item {
                        AnimeSectionRow(
                            title = "Most Favorited",
                            icon = FluentuiSystemIconsHeartFilled,
                            animes = mostFavorited,
                            onAnimeClick = onAnimeClick,
                            onLoadMore = { viewModel.loadNextPage(com.mjland.AnimeSection.MOST_FAVORITED) }
                        )
                    }
                }
                
                if (tagBackdrops.isNotEmpty()) {
                    item {
                        PopularTagsRow(
                            tagBackdrops = tagBackdrops,
                            onTagClick = onTagClick
                        )
                    }
                }
                
                if (actionAnime.isNotEmpty()) {
                    item {
                        AnimeSectionRow(
                            title = "Action Packed",
                            icon = FluentuiSystemIconsStarFilled,
                            animes = actionAnime,
                            onAnimeClick = onAnimeClick,
                            onLoadMore = { viewModel.loadNextPage(com.mjland.AnimeSection.ACTION) }
                        )
                    }
                }
                
                if (romanceAnime.isNotEmpty()) {
                    item {
                        AnimeSectionRow(
                            title = "Romance & Drama",
                            icon = FluentuiSystemIconsHeartFilled,
                            animes = romanceAnime,
                            onAnimeClick = onAnimeClick,
                            onLoadMore = { viewModel.loadNextPage(com.mjland.AnimeSection.ROMANCE) }
                        )
                    }
                }
                
                if (fantasyAnime.isNotEmpty()) {
                    item {
                        AnimeSectionRow(
                            title = "Fantasy Worlds",
                            icon = FluentuiSystemIconsArrowTrending,
                            animes = fantasyAnime,
                            onAnimeClick = onAnimeClick,
                            onLoadMore = { viewModel.loadNextPage(com.mjland.AnimeSection.FANTASY) }
                        )
                    }
                }
                
                if (sciFiAnime.isNotEmpty()) {
                    item {
                        AnimeSectionRow(
                            title = "Sci-Fi & Cyberpunk",
                            icon = FluentuiSystemIconsMoviesAndTvFilled,
                            animes = sciFiAnime,
                            onAnimeClick = onAnimeClick,
                            onLoadMore = { viewModel.loadNextPage(com.mjland.AnimeSection.SCI_FI) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroCompactPager(
    animes: List<AnimeMedia>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onPlayClick: (AnimeMedia) -> Unit
) {
    
    LaunchedEffect(animes) {
        if (animes.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(5000) 
            if (!pagerState.isScrollInProgress) {
                val nextPage = if (pagerState.currentPage < animes.size - 1) {
                    pagerState.currentPage + 1
                } else {
                    0
                }
                
                if (nextPage == 0) {
                    
                    pagerState.animateScrollToPage(0, animationSpec = tween(600))
                } else {
                    pagerState.animateScrollToPage(
                        nextPage, 
                        animationSpec = tween(800, easing = LinearOutSlowInEasing)
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) { page ->
            val anime = animes[page]
            HeroGlassCard(anime = anime, onPlayClick = { onPlayClick(anime) })
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(animes.size) { index ->
                val isSelected = pagerState.currentPage == index
                val itemWidth by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 6.dp,
                    animationSpec = tween(400), label = "tickWidth"
                )
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(itemWidth)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color.White else Color.White.copy(alpha = 0.2f)
                        )
                )
            }
        }
    }
}

@Composable
fun HeroGlassCard(anime: AnimeMedia, onPlayClick: () -> Unit) {
    val scoreText = remember(anime) {
        if (anime.averageScore != null && anime.averageScore > 0) {
            val scoreDouble = anime.averageScore / 10.0
            String.format("%.1f", scoreDouble)
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp) 
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF0F0F14).copy(alpha = 0.8f))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        
        AsyncImage(
            model = anime.bannerImage ?: anime.coverImage?.extraLarge,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.35f }
        )
        
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.9f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 700f
                    )
                )
        )
        
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = (anime.format ?: "TV").uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                fontSize = 9.sp
                            ),
                            color = Color.White
                        )
                        if (scoreText != null) {
                            Box(modifier = Modifier.size(2.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = FluentuiSystemIconsStarFilled,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = scoreText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = anime.title?.english ?: anime.title?.romaji ?: "Unknown",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            letterSpacing = (-0.4).sp,
                            lineHeight = 24.sp
                        ),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = anime.genres?.take(2)?.joinToString(" • ") ?: "",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color.White.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                
                Box(
                    modifier = Modifier
                        .clickable { onPlayClick() }
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = FluentuiSystemIconsPlayFilled,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Watch Now",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            ),
                            color = Color.Black
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            
            AsyncImage(
                model = anime.coverImage?.large,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(115.dp) 
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    )
            )
        }
    }
}

@Composable
fun ContinueWatchingSectionRow(
    animes: List<com.mjland.database.ContinueWatchingEntity>,
    onAnimeClick: (AnimeMedia) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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
                    text = "Continue Watching",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    ),
                    color = Color.White
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(animes, key = { it.animeId }) { watch ->
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

@Composable
fun AnimeSectionRow(
    title: String,
    icon: ImageVector,
    animes: List<AnimeMedia>,
    onAnimeClick: (AnimeMedia) -> Unit,
    onLoadMore: () -> Unit = {}
) {
    if (animes.isEmpty()) return

    val listState = rememberLazyListState()
    val shouldLoadMore = remember(listState) {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            onLoadMore()
        }
    }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                ),
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }
        
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(animes, key = { it.id }) { anime ->
                AnimeCompactCard(anime = anime, onClick = { onAnimeClick(anime) })
            }
        }
    }
}

@Composable
fun AnimeCompactCard(anime: AnimeMedia, onClick: () -> Unit) {
    val scoreText = remember(anime) {
        if (anime.averageScore != null && anime.averageScore > 0) {
            val scoreDouble = anime.averageScore / 10.0
            String.format("%.1f", scoreDouble)
        } else null
    }

    Column(
        modifier = Modifier
            .width(112.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(112.dp)
                .height(155.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            AsyncImage(
                model = anime.coverImage?.large,
                contentDescription = anime.title?.english,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            
            if (scoreText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = FluentuiSystemIconsStarFilled,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(8.dp)
                        )
                        Text(
                            text = scoreText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = anime.title?.english ?: anime.title?.romaji ?: "",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 14.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        
        val formatText = when (anime.format) {
            "MOVIE" -> "Movie" + (if (anime.seasonYear != null) " • ${anime.seasonYear}" else "")
            "TV" -> "Series" + (if (anime.episodes != null) " • ${anime.episodes} EP" else "")
            else -> anime.format ?: ""
        }
        
        if (formatText.isNotEmpty()) {
            Text(
                text = formatText,
                color = Color.White.copy(alpha = 0.45f),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
fun HomeSkeletonView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                )
                
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                )
            }
            
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.04f))
            )
        }

        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF0F0F14).copy(alpha = 0.55f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                        )
                        
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                        )
                        
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                
                Box(
                    modifier = Modifier
                        .width(105.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }
        }

        
        repeat(2) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .width(130.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(3) {
                        Column(
                            modifier = Modifier.width(112.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(112.dp)
                                    .height(155.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.06f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                            )
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PopularGenresRow(
    genreBackdrops: Map<String, String>,
    onGenreClick: (String) -> Unit
) {
    val genresList = genreBackdrops.keys.toList()
    if (genresList.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Popular Genres",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.2.sp
                ),
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(genresList) { genre ->
                val imageUrl = genreBackdrops[genre]
                Box(
                    modifier = Modifier
                        .width(136.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onGenreClick(genre) }
                        .background(Color(0xFF0F0F14))
                        .border(
                            width = 0.5.dp,
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    if (!imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = genre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.15f),
                                        Color.Black.copy(alpha = 0.75f)
                                    )
                                )
                            )
                    )
                    
                    Text(
                        text = genre,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = (-0.1).sp
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PopularTagsRow(
    tagBackdrops: Map<String, String>,
    onTagClick: (String) -> Unit
) {
    val tagsList = tagBackdrops.keys.toList()
    if (tagsList.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Popular Tags",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.2.sp
                ),
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(tagsList) { tag ->
                val imageUrl = tagBackdrops[tag]
                Box(
                    modifier = Modifier
                        .width(136.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTagClick(tag) }
                        .background(Color(0xFF0F0F14))
                        .border(
                            width = 0.5.dp,
                            color = Color.White.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    if (!imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = tag,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.15f),
                                        Color.Black.copy(alpha = 0.75f)
                                    )
                                )
                            )
                    )
                    
                    Text(
                        text = tag,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = (-0.1).sp
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}


