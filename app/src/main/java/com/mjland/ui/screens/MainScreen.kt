package com.mjland.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mjland.viewmodel.MainViewModel
import com.mjland.model.AnimeMedia
import com.mjland.ui.icons.*
import com.mjland.viewmodel.ScheduleViewModel
import com.mjland.viewmodel.SearchViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import androidx.compose.runtime.collectAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    searchViewModel: SearchViewModel,
    scheduleViewModel: ScheduleViewModel,
    onAnimeClick: (AnimeMedia) -> Unit
) {
    val navController = rememberNavController()
    val hazeState = remember { HazeState() }
    val selectedTab by mainViewModel.selectedTab.collectAsState()

    LaunchedEffect(selectedTab) {
        if (navController.currentBackStackEntry?.destination?.route != selectedTab) {
            navController.navigate(selectedTab) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .haze(state = hazeState)
        ) {
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.fillMaxSize(),
                enterTransition = { slideInHorizontally(initialOffsetX = { 100 }, animationSpec = tween(300, easing = LinearOutSlowInEasing)) + fadeIn(animationSpec = tween(300)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -100 }, animationSpec = tween(300, easing = LinearOutSlowInEasing)) + fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -100 }, animationSpec = tween(300, easing = LinearOutSlowInEasing)) + fadeIn(animationSpec = tween(300)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { 100 }, animationSpec = tween(300, easing = LinearOutSlowInEasing)) + fadeOut(animationSpec = tween(300)) }
            ) {
                composable("home") {
                    HomeScreen(
                        viewModel = mainViewModel,
                        onAnimeClick = onAnimeClick,
                        onGenreClick = { genre ->
                            searchViewModel.selectSingleGenre(genre)
                            mainViewModel.setSelectedTab("search")
                        },
                        onTagClick = { tag ->
                            searchViewModel.selectSingleTag(tag)
                            mainViewModel.setSelectedTab("search")
                        }
                    )
                }
                composable("search") {
                    SearchScreen(
                        viewModel = searchViewModel,
                        onAnimeClick = onAnimeClick
                    )
                }
                composable("schedule") {
                    ScheduleScreen(
                        viewModel = scheduleViewModel,
                        onAnimeClick = onAnimeClick
                    )
                }
                composable("myspace") {
                    MySpaceScreen(onAnimeClick = onAnimeClick)
                }
            }
        }

        BottomNavigationBar(navController = navController, hazeState = hazeState, onTabSelected = { mainViewModel.setSelectedTab(it) })
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, hazeState: HazeState, onTabSelected: (String) -> Unit) {
    val items = listOf(
        BottomNavItem("Home", "home", FluentuiSystemIconsHomeFilled, FluentuiSystemIconsHomeOutline),
        BottomNavItem("Search", "search", FluentuiSystemIconsSearchSparkleOutline, FluentuiSystemIconsSearchSparkleOutline),
        BottomNavItem("Schedule", "schedule", FluentuiSystemIconsCalendarClockFilled, FluentuiSystemIconsCalendarClockOutline),
        BottomNavItem("My Space", "myspace", FluentuiSystemIconsPersonFilled, FluentuiSystemIconsPersonOutline)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF08080C))
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .navigationBarsPadding()
            .height(50.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            if (currentRoute != item.route) {
                                onTabSelected(item.route)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.05f else 1.0f,
                            animationSpec = tween(150)
                        )
                        Box(
                            modifier = Modifier
                                .graphicsLayer(scaleX = iconScale, scaleY = iconScale),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                                contentDescription = item.title,
                                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.45f),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = 0.05.sp
                            ),
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

data class BottomNavItem(
    val title: String,
    val route: String,
    val activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector
)
