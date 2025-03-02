package com.ghostdev.video.clips.navigation

sealed class NavDestinations(route: String) {

    data object Login: NavDestinations("login")
    data object Register: NavDestinations("register")
    data object Home: NavDestinations("home")
    data object Profile: NavDestinations("profile")
    data object Upload: NavDestinations("upload")
    data object Review: NavDestinations("review")
    data object Description: NavDestinations("description")

}