package com.nakibul.android.appscheduler.navigation

sealed class Screen(val route: String) {
    data object AppListScreenScreen : Screen("app_list_screen")
    data object ScheduleListScreen : Screen("schedule_list_screen")
}