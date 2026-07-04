package com.mjland.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.mjland.ui.icons.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mjland.ui.components.SmoothAsyncImage
import com.mjland.model.AnimeMedia
import com.mjland.viewmodel.SearchViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlin.math.roundToInt

data class ActiveFilter(
    val id: String,
    val label: String,
    val onCancel: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onAnimeClick: (AnimeMedia) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    
    val selectedFormat by viewModel.selectedFormat.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val fromYear by viewModel.fromYear.collectAsState()
    val toYear by viewModel.toYear.collectAsState()
    val episodesFrom by viewModel.episodesFrom.collectAsState()
    val episodesTo by viewModel.episodesTo.collectAsState()
    val durationFrom by viewModel.durationFrom.collectAsState()
    val durationTo by viewModel.durationTo.collectAsState()
    val selectedGenres by viewModel.selectedGenres.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()

    val activeFiltersList = remember(
        selectedFormat, selectedStatus, selectedCountry, selectedSeason,
        fromYear, toYear, episodesFrom, episodesTo, durationFrom, durationTo,
        selectedGenres, selectedTags, selectedSort
    ) {
        val list = mutableListOf<ActiveFilter>()
        
        selectedFormat?.let { fmt ->
            list.add(
                ActiveFilter(
                    id = "format",
                    label = "Format: " + when (fmt) {
                        "TV_SHORT" -> "TV Short"
                        "MOVIE" -> "Movie"
                        "SPECIAL" -> "Special"
                        else -> fmt
                    },
                    onCancel = { viewModel.setFormat(null) }
                )
            )
        }
        
        selectedStatus?.let { stat ->
            list.add(
                ActiveFilter(
                    id = "status",
                    label = "Status: " + when (stat) {
                        "NOT_YET_RELEASED" -> "Not Released"
                        "RELEASING" -> "Releasing"
                        "FINISHED" -> "Finished"
                        "CANCELLED" -> "Canceled"
                        else -> stat
                    },
                    onCancel = { viewModel.setStatus(null) }
                )
            )
        }
        
        selectedCountry?.let { country ->
            list.add(
                ActiveFilter(
                    id = "country",
                    label = "Country: " + when (country) {
                        "JP" -> "Japan"
                        "KR" -> "S. Korea"
                        "CN" -> "China"
                        "TW" -> "Taiwan"
                        else -> country
                    },
                    onCancel = { viewModel.setCountry(null) }
                )
            )
        }
        
        selectedSeason?.let { season ->
            list.add(
                ActiveFilter(
                    id = "season",
                    label = "Season: " + season.lowercase().replaceFirstChar { it.uppercase() },
                    onCancel = { viewModel.setSeason(null) }
                )
            )
        }
        
        if (fromYear != null || toYear != null) {
            val fYear = fromYear ?: 1970
            val tYear = toYear ?: 2026
            list.add(
                ActiveFilter(
                    id = "years",
                    label = "Years: $fYear - $tYear",
                    onCancel = { viewModel.setYearRange(null, null) }
                )
            )
        }
        
        if (episodesFrom != null || episodesTo != null) {
            val fEp = episodesFrom ?: 1
            val tEp = episodesTo?.toString() ?: "100+"
            list.add(
                ActiveFilter(
                    id = "episodes",
                    label = "Episodes: $fEp - $tEp",
                    onCancel = { viewModel.setEpisodesRange(null, null) }
                )
            )
        }
        
        if (durationFrom != null || durationTo != null) {
            val fDur = durationFrom ?: 1
            val tDur = durationTo?.let { "${it}m" } ?: "180m+"
            list.add(
                ActiveFilter(
                    id = "duration",
                    label = "Duration: $fDur - $tDur",
                    onCancel = { viewModel.setDurationRange(null, null) }
                )
            )
        }
        
        selectedGenres.forEach { genre ->
            list.add(
                ActiveFilter(
                    id = "genre_$genre",
                    label = genre,
                    onCancel = { viewModel.toggleGenre(genre) }
                )
            )
        }
        
        selectedTags.forEach { tag ->
            list.add(
                ActiveFilter(
                    id = "tag_$tag",
                    label = tag,
                    onCancel = { viewModel.toggleTag(tag) }
                )
            )
        }
        
        if (selectedSort != "POPULARITY_DESC") {
            val sortLabel = when (selectedSort) {
                "SCORE_DESC" -> "Score"
                "TRENDING_DESC" -> "Trending"
                "START_DATE_DESC" -> "Date"
                "EPISODES_DESC" -> "Episodes"
                else -> selectedSort
            }
            list.add(
                ActiveFilter(
                    id = "sort",
                    label = "Sort: $sortLabel",
                    onCancel = { viewModel.setSort("POPULARITY_DESC") }
                )
            )
        }
        
        list
    }

    val searchHazeState = remember { HazeState() }
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val gridState = rememberLazyGridState()

    var isFiltersExpanded by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(false) }

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerContentHeight = if (activeFiltersList.isNotEmpty()) 144.dp else 104.dp
    val totalHeaderHeight = statusBarHeight + headerContentHeight

    
    LaunchedEffect(isLoading, searchResults) {
        if (!isLoading && searchResults.isNotEmpty()) {
            keyboardController?.hide()
        }
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf false
            lastVisibleItem.index >= gridState.layoutInfo.totalItemsCount - 8
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !isLoading && searchResults.isNotEmpty()) {
            viewModel.searchAnime(loadMore = true)
        }
    }
    
    LaunchedEffect(
        searchQuery, selectedFormat, selectedStatus, selectedCountry, selectedSeason,
        fromYear, toYear, episodesFrom, episodesTo, durationFrom, durationTo,
        selectedGenres, selectedTags, selectedSort
    ) {
        gridState.scrollToItem(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyVerticalGrid(
            state = gridState,
            columns = if (isGridView) GridCells.Adaptive(minSize = 100.dp) else GridCells.Fixed(1),
            contentPadding = PaddingValues(
                start = 16.dp, 
                end = 16.dp, 
                bottom = 24.dp,
                top = totalHeaderHeight
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(if (isGridView) 18.dp else 10.dp),
            modifier = Modifier
                .fillMaxSize()
                .haze(state = searchHazeState)
        ) {
            
            if (searchResults.isEmpty() && isLoading) {
                items(6) {
                    if (isGridView) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            com.mjland.ui.components.PosterSkeleton(
                                modifier = Modifier.fillMaxWidth().aspectRatio(112f / 155f)
                            )
                            com.mjland.ui.components.TextSkeleton(width = 80, height = 12)
                            com.mjland.ui.components.TextSkeleton(width = 50, height = 10)
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.02f))
                                .border(
                                    width = 0.5.dp,
                                    color = Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.mjland.ui.components.PosterSkeleton(
                                modifier = Modifier.width(70.dp).aspectRatio(0.7f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                com.mjland.ui.components.TextSkeleton(width = 140, height = 14)
                                com.mjland.ui.components.TextSkeleton(width = 80, height = 10)
                                com.mjland.ui.components.TextSkeleton(width = 120, height = 10)
                            }
                        }
                    }
                }
            } else if (searchResults.isEmpty() && error != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    com.mjland.ui.components.GlassmorphicErrorView(
                        message = error ?: "Unknown error",
                        onRetry = { viewModel.searchAnime() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp)
                    )
                }
            } else if (searchResults.isEmpty() && !isLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val isQueryEmpty = searchQuery.isEmpty()
                    val hasFilters = activeFiltersList.isNotEmpty()
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(450.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = 40.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(36.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.1f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = RoundedCornerShape(36.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (hasFilters || !isQueryEmpty) FluentuiSystemIconsSearchSparkleOutline else FluentuiSystemIconsSearchSparkleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Color.White.copy(alpha = 0.3f)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = if (!isQueryEmpty || hasFilters) "No results found" else "What are you looking for?",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (!isQueryEmpty || hasFilters) {
                                    "We couldn't find anything matching your search and filters.\nTry making search inputs broader or clearing filters."
                                } else {
                                    "Find anime, movies, specials and more."
                                },
                                color = Color.White.copy(alpha = 0.45f),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else {
                items(searchResults, key = { it.id }) { anime ->
                    if (isGridView) {
                        SearchAnimeCard(anime = anime, onClick = { onAnimeClick(anime) })
                    } else {
                        SearchAnimeListRow(anime = anime, onClick = { onAnimeClick(anime) })
                    }
                }
                
                if (isLoading) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(bottom = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White.copy(alpha = 0.5f),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }

        
        val containerColor = Color.Black.copy(alpha = 0.2f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .hazeChild(
                    state = searchHazeState,
                    shape = RoundedCornerShape(0.dp),
                    style = HazeStyle(blurRadius = 32.dp, tint = Color.Black.copy(alpha = 0.55f))
                )
                .background(containerColor)
                .statusBarsPadding()
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable { isGridView = !isGridView }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
                                contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isFiltersExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                .border(
                                    width = 1.dp,
                                    color = if (isFiltersExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f),
                                    shape = CircleShape
                                )
                                .clickable { isFiltersExpanded = !isFiltersExpanded }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FluentuiSystemIconsFilter,
                                contentDescription = "Filters",
                                tint = if (isFiltersExpanded) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            viewModel.onSearchQueryChanged(query)
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { 
                            viewModel.searchAnime() 
                            keyboardController?.hide()
                        }),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.15f),
                                                Color.White.copy(alpha = 0.03f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(19.dp)
                                    )
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = FluentuiSystemIconsSearchSparkleOutline,
                                    contentDescription = "Search",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Titles, genres, specials...",
                                            color = Color.White.copy(alpha = 0.35f),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                                        )
                                    }
                                    innerTextField()
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { 
                                            viewModel.onSearchQueryChanged("")
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = FluentuiSystemIconsDismissCircleFilled,
                                            contentDescription = "Clear search",
                                            tint = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }

                if (activeFiltersList.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        items(activeFiltersList) { activeFilter ->
                            ActiveFilterPill(activeFilter = activeFilter)
                        }
                    }
                }

                if (isFiltersExpanded) {
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ModalBottomSheet(
                        onDismissRequest = { isFiltersExpanded = false },
                        sheetState = sheetState,
                        containerColor = Color.Transparent,
                        dragHandle = null,
                        scrimColor = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .background(Color(0xFF09090B)) 
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                                )
                                .verticalScroll(rememberScrollState())
                                .padding(start = 0.dp, end = 0.dp, bottom = 24.dp, top = 6.dp) 
                                .navigationBarsPadding()
                        ) {
                            BottomSheetDefaults.DragHandle(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .align(Alignment.CenterHorizontally),
                                color = Color.White.copy(alpha = 0.3f)
                            )
                            FiltersPanel(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveFilterPill(
    activeFilter: ActiveFilter
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { activeFilter.onCancel() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = activeFilter.label,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
            Icon(
                imageVector = FluentuiSystemIconsDismissCircleFilled,
                contentDescription = "Cancel filter ${activeFilter.label}",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun FiltersPanel(viewModel: SearchViewModel) {
    val selectedFormat by viewModel.selectedFormat.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val fromYear by viewModel.fromYear.collectAsState()
    val toYear by viewModel.toYear.collectAsState()
    val episodesFrom by viewModel.episodesFrom.collectAsState()
    val episodesTo by viewModel.episodesTo.collectAsState()
    val durationFrom by viewModel.durationFrom.collectAsState()
    val durationTo by viewModel.durationTo.collectAsState()
    val selectedGenres by viewModel.selectedGenres.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val availableGenres by viewModel.availableGenres.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()

    
    var localYearRange by remember { mutableStateOf(1970f..2026f) }
    LaunchedEffect(fromYear, toYear) {
        localYearRange = (fromYear?.toFloat() ?: 1970f)..(toYear?.toFloat() ?: 2026f)
    }

    var localEpisodesRange by remember { mutableStateOf(1f..100f) }
    LaunchedEffect(episodesFrom, episodesTo) {
        localEpisodesRange = (episodesFrom?.toFloat() ?: 1f)..(episodesTo?.toFloat() ?: 100f)
    }

    var localDurationRange by remember { mutableStateOf(1f..180f) }
    LaunchedEffect(durationFrom, durationTo) {
        localDurationRange = (durationFrom?.toFloat() ?: 1f)..(durationTo?.toFloat() ?: 180f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "FORMAT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            val formats = listOf("TV", "TV_SHORT", "MOVIE", "SPECIAL", "OVA", "ONA")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChipLite(
                        text = "Any",
                        selected = selectedFormat == null,
                        onClick = { viewModel.setFormat(null) }
                    )
                }
                items(formats, key = { it }) { fmt ->
                    FilterChipLite(
                        text = when (fmt) {
                            "TV_SHORT" -> "TV Short"
                            "MOVIE" -> "Movie"
                            "SPECIAL" -> "Special"
                            else -> fmt
                        },
                        selected = selectedFormat == fmt,
                        onClick = { viewModel.setFormat(fmt) }
                    )
                }
            }
        }

        
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "STATUS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            val statuses = listOf("RELEASING", "NOT_YET_RELEASED", "FINISHED", "CANCELLED")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChipLite(
                        text = "Any",
                        selected = selectedStatus == null,
                        onClick = { viewModel.setStatus(null) }
                    )
                }
                items(statuses, key = { it }) { stat ->
                    FilterChipLite(
                        text = when (stat) {
                            "NOT_YET_RELEASED" -> "Not Released"
                            "RELEASING" -> "Releasing"
                            "FINISHED" -> "Finished"
                            "CANCELLED" -> "Canceled"
                            else -> stat
                        },
                        selected = selectedStatus == stat,
                        onClick = { viewModel.setStatus(stat) }
                    )
                }
            }
        }

        
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "COUNTRY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            val countries = listOf("JP" to "Japan", "KR" to "S. Korea", "CN" to "China", "TW" to "Taiwan")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChipLite(
                        text = "Any",
                        selected = selectedCountry == null,
                        onClick = { viewModel.setCountry(null) }
                    )
                }
                items(countries, key = { it.first }) { (code, name) ->
                    FilterChipLite(
                        text = name,
                        selected = selectedCountry == code,
                        onClick = { viewModel.setCountry(code) }
                    )
                }
            }
        }

        
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "SEASON",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            val seasons = listOf("WINTER", "SPRING", "SUMMER", "FALL")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChipLite(
                        text = "Any",
                        selected = selectedSeason == null,
                        onClick = { viewModel.setSeason(null) }
                    )
                }
                items(seasons, key = { it }) { seas ->
                    FilterChipLite(
                        text = seas.lowercase().replaceFirstChar { it.uppercase() },
                        selected = selectedSeason == seas,
                        onClick = { viewModel.setSeason(seas) }
                    )
                }
            }
        }

        
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YEARS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "${localYearRange.start.roundToInt()} - ${localYearRange.endInclusive.roundToInt()}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            RangeSlider(
                value = localYearRange,
                onValueChange = { localYearRange = it },
                valueRange = 1970f..2026f,
                steps = 55,
                onValueChangeFinished = {
                    viewModel.setYearRange(
                        localYearRange.start.roundToInt(),
                        localYearRange.endInclusive.roundToInt()
                    )
                },
                modifier = Modifier.fillMaxWidth().height(18.dp)
            )
        }

        
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EPISODES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "${localEpisodesRange.start.roundToInt()} - ${if (localEpisodesRange.endInclusive >= 100f) "100+" else localEpisodesRange.endInclusive.roundToInt()}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            RangeSlider(
                value = localEpisodesRange,
                onValueChange = { localEpisodesRange = it },
                valueRange = 1f..100f,
                steps = 98,
                onValueChangeFinished = {
                    val endVal = if (localEpisodesRange.endInclusive >= 100f) null else localEpisodesRange.endInclusive.roundToInt()
                    viewModel.setEpisodesRange(
                        localEpisodesRange.start.roundToInt(),
                        endVal
                    )
                },
                modifier = Modifier.fillMaxWidth().height(18.dp)
            )
        }

        
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DURATION (MINS)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "${localDurationRange.start.roundToInt()} - ${if (localDurationRange.endInclusive >= 180f) "180+ mins" else "${localDurationRange.endInclusive.roundToInt()} mins"}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            RangeSlider(
                value = localDurationRange,
                onValueChange = { localDurationRange = it },
                valueRange = 1f..180f,
                steps = 178,
                onValueChangeFinished = {
                    val endVal = if (localDurationRange.endInclusive >= 180f) null else localDurationRange.endInclusive.roundToInt()
                    viewModel.setDurationRange(
                        localDurationRange.start.roundToInt(),
                        endVal
                    )
                },
                modifier = Modifier.fillMaxWidth().height(18.dp)
            )
        }

        
        var genreSearch by remember { mutableStateOf("") }
        val filteredGenres = remember(availableGenres, genreSearch) {
            availableGenres.filter { it.contains(genreSearch, ignoreCase = true) }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GENRES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )
                )
                BasicTextField(
                    value = genreSearch,
                    onValueChange = { genreSearch = it },
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                    cursorBrush = Brush.verticalGradient(listOf(Color.White, Color.White)),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (genreSearch.isEmpty()) {
                                Text("Search", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.3f))
                            }
                            innerTextField()
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredGenres) { genre ->
                    val isSelected = selectedGenres.contains(genre)
                    FilterChipLite(
                        text = genre,
                        selected = isSelected,
                        onClick = { viewModel.toggleGenre(genre) }
                    )
                }
            }
        }

        var tagSearch by remember { mutableStateOf("") }
        val filteredTags = remember(availableTags, tagSearch) {
            availableTags.filter { it.contains(tagSearch, ignoreCase = true) }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TAGS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )
                )
                BasicTextField(
                    value = tagSearch,
                    onValueChange = { tagSearch = it },
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                    cursorBrush = Brush.verticalGradient(listOf(Color.White, Color.White)),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (tagSearch.isEmpty()) {
                                Text("Search", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.3f))
                            }
                            innerTextField()
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredTags) { tag ->
                    val isSelected = selectedTags.contains(tag)
                    FilterChipLite(
                        text = tag,
                        selected = isSelected,
                        onClick = { viewModel.toggleTag(tag) }
                    )
                }
            }
        }

        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color.White.copy(alpha = 0.08f))
        )

        
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "SORT BY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            val sortOptions = listOf(
                "POPULARITY_DESC" to "Popular",
                "SCORE_DESC" to "Score",
                "TRENDING_DESC" to "Trending",
                "START_DATE_DESC" to "Date",
                "EPISODES_DESC" to "Episodes"
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sortOptions) { (id, label) ->
                    FilterChipLite(
                        text = label,
                        selected = selectedSort == id,
                        onClick = { viewModel.setSort(id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        
        Button(
            onClick = { viewModel.clearAllFilters() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            ),
            contentPadding = PaddingValues(vertical = 14.dp),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(
                imageVector = FluentuiSystemIconsDismissCircleOutline,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Clear All Filters",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Composable
fun FilterChipLite(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else Color.White.copy(alpha = 0.04f)
            )
            .border(
                width = 0.5.dp,
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = text,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun SearchAnimeCard(anime: AnimeMedia, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Box {
            SmoothAsyncImage(
                model = anime.coverImage?.large,
                contentDescription = anime.title?.english ?: anime.title?.romaji,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Color(0xFF1E1E1E))
            )

            
            if (anime.averageScore != null) {
                val scoreColor = if (anime.averageScore!! > 75) Color(0xFF4CAF50) else if (anime.averageScore!! > 50) Color(0xFFFFC107) else Color.White
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(
                            width = 0.5.dp,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = FluentuiSystemIconsStarFilled, 
                            contentDescription = "Score", 
                            tint = scoreColor, 
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${anime.averageScore}%",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = anime.title?.english ?: anime.title?.romaji ?: "",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.1).sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        val detailsParts = listOfNotNull(
            when (anime.format) {
                "MOVIE" -> "Movie"
                "TV" -> "TV"
                "TV_SHORT" -> "Short"
                "OVA" -> "OVA"
                "ONA" -> "ONA"
                "SPECIAL" -> "Special"
                "MUSIC" -> "Music"
                else -> anime.format
            },
            anime.seasonYear?.toString(),
            anime.episodes?.let { "$it Eps" }
        )
        val formatText = detailsParts.joinToString(" • ")
        
        if (formatText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = formatText,
                color = Color.White.copy(alpha = 0.45f),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 0.1.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SearchAnimeListRow(anime: AnimeMedia, onClick: () -> Unit) {
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
        
        if (!anime.bannerImage.isNullOrEmpty()) {
            SmoothAsyncImage(
                model = anime.bannerImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer(alpha = 0.18f)
            )
            
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.94f),
                                Color.Black.copy(alpha = 0.55f)
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
                SmoothAsyncImage(
                    model = anime.coverImage?.large ?: anime.coverImage?.extraLarge,
                    contentDescription = anime.title?.english ?: anime.title?.romaji,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (anime.averageScore != null) {
                    val scoreColor = if (anime.averageScore!! > 75) Color(0xFF4CAF50) else if (anime.averageScore!! > 50) Color(0xFFFFC107) else Color.White
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
                                text = "${anime.averageScore}%",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold)
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
                    text = anime.title?.english ?: anime.title?.romaji ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = (-0.1).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                val detailsParts = listOfNotNull(
                    when (anime.format) {
                        "MOVIE" -> "Movie"
                        "TV" -> "TV"
                        "TV_SHORT" -> "Short"
                        "OVA" -> "OVA"
                        "ONA" -> "ONA"
                        "SPECIAL" -> "Special"
                        "MUSIC" -> "Music"
                        else -> anime.format
                    },
                    anime.seasonYear?.toString(),
                    if (anime.format != "MOVIE" && anime.format != "Movie") anime.episodes?.let { "$it Eps" } else null
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

                if (!anime.genres.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        anime.genres.take(3).forEach { genre ->
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
                
                if (!anime.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val cleanDesc = anime.description.replace(Regex("<[^>]*>"), "")
                    Text(
                        text = cleanDesc,
                        color = Color.White.copy(alpha = 0.45f),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
