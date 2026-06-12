package com.mjland.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mjland.model.AnimeMedia
import com.mjland.viewmodel.ScheduleViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onAnimeClick: (AnimeMedia) -> Unit
) {
    val schedules by viewModel.schedules.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val currentDate = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.time
    }
    
    var selectedDate by remember { mutableStateOf(currentDate) }

    val fiveDays = remember {
        (0..4).map { offset ->
            val cal = Calendar.getInstance()
            cal.time = currentDate
            cal.add(Calendar.DAY_OF_YEAR, offset)
            cal.time
        }
    }

    val scheduleHazeState = remember { HazeState() }
    val listState = rememberLazyListState()

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerContentHeight = 112.dp
    val totalHeaderHeight = statusBarHeight + headerContentHeight

    
    val filteredSchedules = remember(schedules, selectedDate) {
        val targetCal = Calendar.getInstance()
        targetCal.time = selectedDate
        val targetYear = targetCal.get(Calendar.YEAR)
        val targetDayOfYear = targetCal.get(Calendar.DAY_OF_YEAR)

        schedules.filter { schedule ->
            if (schedule.airingAt != null) {
                val itemCal = Calendar.getInstance()
                itemCal.timeInMillis = schedule.airingAt * 1000
                val itemYear = itemCal.get(Calendar.YEAR)
                val itemDayOfYear = itemCal.get(Calendar.DAY_OF_YEAR)
                itemYear == targetYear && itemDayOfYear == targetDayOfYear
            } else false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .haze(state = scheduleHazeState),
            contentPadding = PaddingValues(
                bottom = 24.dp,
                top = totalHeaderHeight
            )
        ) {
            
            if (isLoading) {
                items(4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.03f))
                                .border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.12f),
                                            Color.White.copy(alpha = 0.02f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            
                            Box(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(68.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .height(16.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(14.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color.White.copy(alpha = 0.04f))
                                )
                            }
                        }
                    }
                }
            } else if (error != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillParentMaxHeight(0.7f),
                        contentAlignment = Alignment.Center
                    ) {
                        com.mjland.ui.components.GlassmorphicErrorView(
                            message = error ?: "Unknown error",
                            onRetry = { viewModel.fetchSchedule() }
                        )
                    }
                }
            } else if (filteredSchedules.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillParentMaxHeight(0.7f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No schedule on this date", color = Color.Gray, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            val displayFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
                            Text(
                                displayFormatter.format(selectedDate),
                                color = Color.DarkGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                items(filteredSchedules.size) { index ->
                    val schedule = filteredSchedules[index]
                    schedule.media?.let { media ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                        ) {
                            ScheduleItemCard(
                                anime = media,
                                episode = schedule.episode,
                                airingAt = schedule.airingAt,
                                isLast = index == filteredSchedules.lastIndex,
                                onClick = { onAnimeClick(media) }
                            )
                        }
                    }
                }
            }
        }

        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .hazeChild(
                    state = scheduleHazeState,
                    shape = RoundedCornerShape(0.dp),
                    style = HazeStyle(blurRadius = 32.dp, tint = Color.Black.copy(alpha = 0.55f))
                )
                .background(Color.Black.copy(alpha = 0.2f))
                .statusBarsPadding()
                .padding(bottom = 10.dp, start = 16.dp, end = 16.dp, top = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Airing Schedule",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color.White
                    )
                }

                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.White.copy(alpha = 0.03f)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        fiveDays.forEach { date ->
                            DayContent(
                                date = date,
                                isSelected = selectedDate == date,
                                onClick = { selectedDate = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayContent(date: Date, isSelected: Boolean, modifier: Modifier = Modifier, onClick: (Date) -> Unit) {
    val dateFormatter = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val dayName = dateFormatter.format(date).uppercase()
    val dayOfMonthString = remember(date) {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.get(Calendar.DAY_OF_MONTH).toString()
    }
    
    Box(
        modifier = modifier
            .padding(2.dp)
            .height(54.dp) 
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick(date) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                ),
                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = dayOfMonthString,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = if (isSelected) Color.Black else Color.White
            )
        }
    }
}

@Composable
fun ScheduleItemCard(
    anime: AnimeMedia,
    episode: Int?,
    airingAt: Long?,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    val timeString = remember(airingAt) {
        if (airingAt != null) {
            val date = Date(airingAt * 1000)
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            sdf.format(date)
        } else {
            "Unknown"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.03f))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            AsyncImage(
                model = anime.coverImage?.large,
                contentDescription = anime.title?.english,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(48.dp)
                    .height(68.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = anime.title?.english ?: anime.title?.romaji ?: "",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            lineHeight = 19.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    
                    Text(
                        text = timeString,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50)) 
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EPISODE $episode",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }

                    
                    val formatText = anime.format ?: "TV"
                    Text(
                        text = formatText,
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
