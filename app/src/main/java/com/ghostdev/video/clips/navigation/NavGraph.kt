package com.ghostdev.video.clips.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ghostdev.video.clips.ui.presentation.home.HomeComponent
import com.ghostdev.video.clips.ui.presentation.login.LoginComponent
import com.ghostdev.video.clips.ui.presentation.profile.ProfileComponent
import com.ghostdev.video.clips.ui.presentation.register.RegisterComponent
import com.ghostdev.video.clips.ui.presentation.uploadandpost.DescriptionComponent
import com.ghostdev.video.clips.ui.presentation.uploadandpost.ReviewComponent
import com.ghostdev.video.clips.ui.presentation.uploadandpost.UploadComponent

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = NavDestinations.Login.toString()) {
        composable(NavDestinations.Login.toString()) {
            LoginComponent(
                modifier = modifier,
                isDarkTheme = isDarkTheme,
                navigateToRegister = {
                    navController.navigate(NavDestinations.Register.toString())
                },
                navigateToHome = {
                    navController.navigate(NavDestinations.Home.toString())
                }
            )
        }

        composable(NavDestinations.Register.toString()) {
            RegisterComponent(
                modifier = modifier,
                isDarkTheme = isDarkTheme,
                navigateToLogin = {
                    navController.navigate(NavDestinations.Login.toString())
                }
            )
        }

        composable(
            route = "${NavDestinations.Home}?isPreview={isPreview}&videoId={videoId}",
            arguments = listOf(
                navArgument("isPreview") {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("videoId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val isPreview = backStackEntry.arguments?.getBoolean("isPreview") ?: false
            val videoId = backStackEntry.arguments?.getString("videoId")

            HomeComponent(
                isPreview = isPreview,
                previewVideoId = videoId,
                modifier = modifier,
                navigateToProfile = {
                    navController.navigate(NavDestinations.Profile.toString())
                },
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavDestinations.Profile.toString()) {
            ProfileComponent(
                modifier = modifier,
                navigateToHome = { isPreview ->
                    navController.navigate("${NavDestinations.Home}?isPreview=$isPreview")
                },
                navigateToVideoPreview = { videoId ->
                    // Navigate to home with the video ID to play a single video
                    navController.navigate("${NavDestinations.Home}?isPreview=true&videoId=$videoId")
                },
                isDarkTheme = isDarkTheme,
                navigateToUpload = {
                    navController.navigate(NavDestinations.Upload.toString())
                }
            )
        }

        composable(NavDestinations.Upload.toString()) {
            UploadComponent(
                modifier = modifier,
                navigateToHome = {
                    navController.popBackStack(NavDestinations.Profile.toString(), inclusive = false)
                },
                navigateToReview = { mediaPath ->
                    navController.navigate("${NavDestinations.Review}?mediaPath=$mediaPath")
                }
            )
        }

        composable(
            route = "${NavDestinations.Review}?mediaPath={mediaPath}",
            arguments = listOf(navArgument("mediaPath") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaPath = backStackEntry.arguments?.getString("mediaPath") ?: ""
            ReviewComponent(
                mediaPath = mediaPath,
                navigateToUploadScreen = { navController.popBackStack() },
                navigateToDescriptionScreen = {
                    navController.navigate("${NavDestinations.Description}?mediaPath=$mediaPath")
                }
            )
        }

        composable(
            route = "${NavDestinations.Description}?mediaPath={mediaPath}",
            arguments = listOf(navArgument("mediaPath") { type = NavType.StringType })
        ) { backStackEntry ->
            val mediaPath = backStackEntry.arguments?.getString("mediaPath") ?: ""
            DescriptionComponent(
                modifier = modifier,
                mediaPath = mediaPath,
                navigateToUploadScreen = { navController.popBackStack(NavDestinations.Upload.toString(), inclusive = false) },
                navigateToFeed = {
                    navController.popBackStack(NavDestinations.Home.toString(), inclusive = false)
                }
            )
        }
    }
}