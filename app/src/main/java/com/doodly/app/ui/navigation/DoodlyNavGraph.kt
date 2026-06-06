package com.doodly.app.ui.navigation

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.doodly.app.di.AppContainer
import com.doodly.app.ui.calendar.CalendarScreen
import com.doodly.app.ui.calendar.CalendarViewModel
import com.doodly.app.ui.component.DoodlyBottomBar
import com.doodly.app.ui.detail.DetailScreen
import com.doodly.app.ui.detail.DetailViewModel
import com.doodly.app.ui.gallery.GalleryScreen
import com.doodly.app.ui.gallery.GalleryViewModel
import com.doodly.app.ui.settings.SettingsScreen
import com.doodly.app.ui.settings.SettingsViewModel
import com.doodly.app.ui.splash.SplashScreen
import com.doodly.app.ui.write.WriteScreen
import com.doodly.app.ui.write.WriteViewModel
import kotlinx.coroutines.delay

@Composable
fun DoodlyNavGraph(
    navController: NavHostController,
    container: AppContainer
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != Routes.SPLASH &&
        currentRoute?.startsWith("diary/") != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                DoodlyBottomBar(
                    destinations = bottomDestinations,
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            backStackEntry?.destination?.id?.let { currentId ->
                                popUpTo(currentId) { inclusive = true }
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen()
                LaunchedEffect(Unit) {
                    delay(1_250)
                    if (navController.currentDestination?.route == Routes.SPLASH) {
                        navController.navigate(Routes.WRITE) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
            composable(Routes.GALLERY) {
                val vm: GalleryViewModel = viewModel(
                    factory = GalleryViewModel.Factory(container.diaryRepository)
                )
                GalleryScreen(vm) { navController.navigate(Routes.detail(it)) }
            }
            composable(Routes.CALENDAR) {
                val vm: CalendarViewModel = viewModel(
                    factory = CalendarViewModel.Factory(
                        container.diaryRepository,
                        container.aiRepository
                    )
                )
                CalendarScreen(
                    viewModel = vm,
                    onEntryClick = { navController.navigate(Routes.detail(it)) },
                    onEmptyDateClick = { navController.navigate(Routes.write(it)) }
                )
            }
            composable(
                route = Routes.WRITE_ROUTE,
                arguments = listOf(
                    navArgument("date") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "doodly://write"
                        action = Intent.ACTION_VIEW
                    }
                )
            ) { entry ->
                val date = entry.arguments?.getLong("date")?.takeIf { it >= 0L }
                val vm: WriteViewModel = viewModel(
                    key = "write_${date ?: "today"}",
                    factory = WriteViewModel.Factory(
                        container.diaryRepository,
                        container.aiRepository,
                        container.backupRepository,
                        date
                    )
                )
                val navigateToCalendar = {
                    if (!navController.popBackStack(Routes.CALENDAR, inclusive = false)) {
                        navController.navigate(Routes.CALENDAR) {
                            popUpTo(entry.destination.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
                if (date != null) {
                    BackHandler(onBack = navigateToCalendar)
                }
                WriteScreen(
                    viewModel = vm,
                    showCalendarBack = date != null,
                    onBackToCalendar = navigateToCalendar,
                    onSaved = { id ->
                        navController.navigate(Routes.detail(id)) {
                            popUpTo(entry.destination.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Routes.SETTINGS) {
                val vm: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(container.settingsRepository)
                )
                SettingsScreen(vm)
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "doodly://diary/{id}"
                        action = Intent.ACTION_VIEW
                    }
                )
            ) { entry ->
                val id = entry.arguments?.getInt("id") ?: return@composable
                val vm: DetailViewModel = viewModel(
                    key = "detail_$id",
                    factory = DetailViewModel.Factory(container.diaryRepository, id)
                )
                val navigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.GALLERY) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
                BackHandler(onBack = navigateBack)
                DetailScreen(vm, onBack = navigateBack)
            }
        }
    }
}
