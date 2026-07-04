package com.mjland

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.Coil
import coil.ImageLoader
import coil.request.CachePolicy
import com.mjland.ui.screens.SplashScreen
import com.mjland.ui.screens.DetailsScreen
import com.mjland.ui.screens.MainScreen
import com.mjland.ui.screens.PlayerScreen
import com.mjland.ui.theme.CornCastleAnimeTheme
import com.mjland.viewmodel.ScheduleViewModel
import com.mjland.viewmodel.SearchViewModel
import com.mjland.viewmodel.MainViewModel

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut

import android.content.Intent
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private val mainViewModel: MainViewModel by viewModels()
  private val searchViewModel: SearchViewModel by viewModels()
  private val scheduleViewModel: ScheduleViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleIntent(intent)
    enableEdgeToEdge()
    
    val imageLoader = ImageLoader.Builder(this)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .build()
    Coil.setImageLoader(imageLoader)
    
    setContent {
      CornCastleAnimeTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            val rootNavController = rememberNavController()

            NavHost(
                navController = rootNavController,
                startDestination = "splash",
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { 200 }, animationSpec = tween(400, easing = LinearOutSlowInEasing)) + fadeIn(animationSpec = tween(400))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -200 }, animationSpec = tween(400, easing = LinearOutSlowInEasing)) + fadeOut(animationSpec = tween(400))
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -200 }, animationSpec = tween(400, easing = LinearOutSlowInEasing)) + fadeIn(animationSpec = tween(400))
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { 200 }, animationSpec = tween(400, easing = LinearOutSlowInEasing)) + fadeOut(animationSpec = tween(400))
                }
            ) {
                composable("splash") {
                    SplashScreen(onFinished = {
                        rootNavController.navigate("main") {
                            popUpTo("splash") { inclusive = true }
                        }
                    })
                }
                composable("main") {
                    MainScreen(
                        mainViewModel = mainViewModel,
                        searchViewModel = searchViewModel,
                        scheduleViewModel = scheduleViewModel,
                        onAnimeClick = { anime ->
                            rootNavController.navigate("details/${anime.id}")
                        }
                    )
                }
                composable(
                    route = "details/{animeId}",
                    arguments = listOf(
                        navArgument("animeId") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val animeId = backStackEntry.arguments?.getInt("animeId") ?: 0
                    DetailsScreen(
                        animeId = animeId,
                        onBackClick = { rootNavController.popBackStack() },
                        onPlayClick = { id, title, ep ->
                            val epPart = if (ep != null) "&episode=$ep" else ""
                            rootNavController.navigate("player/$id?title=$title$epPart")
                        },
                        onAnimeClick = { anime ->
                            rootNavController.navigate("details/${anime.id}")
                        },
                        onGenreClick = { genre ->
                            searchViewModel.selectSingleGenre(genre)
                            mainViewModel.setSelectedTab("search")
                            rootNavController.popBackStack("main", false)
                        },
                        onTagClick = { tag ->
                            searchViewModel.selectSingleTag(tag)
                            mainViewModel.setSelectedTab("search")
                            rootNavController.popBackStack("main", false)
                        }
                    )
                }
                composable(
                    route = "player/{animeId}?title={title}&episode={episode}",
                    arguments = listOf(
                        navArgument("animeId") { type = NavType.IntType },
                        navArgument("title") { 
                            type = NavType.StringType
                            defaultValue = "Playing..." 
                        },
                        navArgument("episode") {
                            type = NavType.IntType
                            defaultValue = -1
                        }
                    )
                ) { backStackEntry ->
                    val animeId = backStackEntry.arguments?.getInt("animeId") ?: 0
                    val title = backStackEntry.arguments?.getString("title") ?: "Playing..."
                    val episode = backStackEntry.arguments?.getInt("episode") ?: -1
                    PlayerScreen(
                        animeTitle = title,
                        animeId = animeId,
                        initialEpisode = if (episode == -1) null else episode,
                        onBackClick = { rootNavController.popBackStack() }
                    )
                }
            }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
      super.onNewIntent(intent)
      handleIntent(intent)
  }

  private fun handleIntent(intent: Intent?) {
      val data = intent?.data
      if (data?.scheme == "corncastle" && data.host == "anilist-auth") {
          
          val code = data.getQueryParameter("code")
          if (!code.isNullOrEmpty()) {
              val tokenManager = com.mjland.database.TokenManager.getInstance(this)
              kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                  val token = com.mjland.api.AniListAuthHelper.exchangeCodeForToken(code)
                  if (token != null) {
                      tokenManager.saveAnilistToken(token)
                  }
              }
              return
          }

          
          val fragment = data.fragment
          if (fragment != null) {
              val tokenMap = fragment.split("&").associate { 
                  val parts = it.split("=")
                  parts[0] to (parts.getOrNull(1) ?: "")
              }
              val token = tokenMap["access_token"]
              if (token != null) {
                  val tokenManager = com.mjland.database.TokenManager.getInstance(this)
                  kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                      tokenManager.saveAnilistToken(token)
                  }
              }
          }
      }
  }
}

