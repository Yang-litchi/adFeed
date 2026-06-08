package com.example.adfeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.adfeed.ui.detail.DetailScreen
import com.example.adfeed.ui.feed.FeedScreen
import com.example.adfeed.ui.splash.SplashScreen
import com.example.adfeed.ui.statistics.StatisticsScreen
import com.example.adfeed.ui.theme.AdFeedTheme
import com.example.adfeed.viewmodel.FeedViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdFeedTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val feedViewModel: FeedViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable(
            route = "splash",
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            SplashScreen(
                onFinished = {
                    navController.navigate("feed") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("feed") {
            FeedScreen(
                viewModel = feedViewModel,
                onAdClick = { ad -> navController.navigate("detail/${ad.id}") }
            )
        }

        composable(
            route = "detail/{adId}",
            arguments = listOf(navArgument("adId") { type = NavType.StringType }),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val adId = backStackEntry.arguments?.getString("adId") ?: ""
            DetailScreen(
                adId = adId,
                viewModel = feedViewModel,
                onBack = { navController.popBackStack() },
                onViewStatistics = { id -> navController.navigate("statistics/$id") }
            )
        }

        composable(
            route = "statistics/{adId}",
            arguments = listOf(navArgument("adId") { type = NavType.StringType }),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
            }
        ) { backStackEntry ->
            val statAdId = backStackEntry.arguments?.getString("adId") ?: ""
            StatisticsScreen(
                adId = statAdId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}