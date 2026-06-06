package com.example.adfeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import com.example.adfeed.ui.ai.AiChatScreen
import com.example.adfeed.ui.detail.DetailScreen
import com.example.adfeed.ui.feed.FeedScreen
import com.example.adfeed.ui.theme.AdFeedTheme
import com.example.adfeed.viewmodel.FeedViewModel

import com.example.adfeed.ui.splash.SplashScreen
import androidx.compose.animation.fadeOut

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
        // 启动页
        composable(
            route = "splash",
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            SplashScreen(
                onFinished = {
                    navController.navigate("feed") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 信息流主页
        composable("feed") {
            FeedScreen(
                viewModel = feedViewModel,
                onAdClick = { ad -> navController.navigate("detail/${ad.id}") },
                onAiClick = { navController.navigate("ai") }
            )
        }

        // 广告详情页
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
                onBack = { navController.popBackStack() }
            )
        }

        // AI对话页
        composable(
            route = "ai",
            enterTransition = {
                slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
            }
        ) {
            AiChatScreen(
                onClose = { navController.popBackStack() },
                onSettings = { }
            )
        }
    }
}