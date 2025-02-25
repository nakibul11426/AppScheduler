package com.nakibul.android.appscheduler.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nakibul.android.appscheduler.screens.AppListScreen
import com.nakibul.android.appscheduler.screens.ScheduleListScreen

@Composable
fun Navigation(navController: NavController) {
    NavHost(
        navController = navController as NavHostController,
        startDestination = Screen.ScheduleListScreen.route
    ) {
        composable(route = Screen.AppListScreenScreen.route) {
            AppListScreen(navController = navController)
        }
        composable(route = Screen.ScheduleListScreen.route) {
            ScheduleListScreen(navController = navController)
        }
    }
}