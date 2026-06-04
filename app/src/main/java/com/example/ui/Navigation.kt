package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AddExpenseScreen
import com.example.ui.screens.AiCoachScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.viewmodel.MainViewModel

sealed class Screen(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Splash : Screen("splash", "Splash", {})
    object Onboarding : Screen("onboarding", "Onboarding", {})
    object Login : Screen("login", "Login", {})
    
    object Dashboard : Screen("dashboard", "Home", { Icon(Icons.Default.Home, contentDescription = "Dashboard") })
    object Analytics : Screen("analytics", "Analytics", { Icon(Icons.Default.PieChart, contentDescription = "Analytics") })
    object Add : Screen("add", "Add", { Icon(Icons.Default.Add, contentDescription = "Add") })
    object Coach : Screen("coach", "Coach", { Icon(Icons.Default.AutoGraph, contentDescription = "AI Coach") })
    object Profile : Screen("profile", "Profile", { Icon(Icons.Default.Person, contentDescription = "Profile") })
    object Settings : Screen("settings", "Settings", { Icon(Icons.Default.Settings, contentDescription = "Settings") })
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val bottomNavigationItems = listOf(
        Screen.Dashboard, Screen.Analytics, Screen.Add, Screen.Coach, Screen.Profile
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            // Only show bottom nav on main flow
            if (bottomNavigationItems.any { it.route == currentRoute }) {
                NavigationBar {
                    bottomNavigationItems.forEach { screen ->
                        NavigationBarItem(
                            icon = screen.icon,
                            label = { Text(screen.label) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(viewModel) { nextRoute ->
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(onFinish = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Login.route) {
                LoginScreen(viewModel, onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel)
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(viewModel)
            }
            composable(Screen.Add.route) {
                AddExpenseScreen(viewModel) {
                    navController.popBackStack()
                }
            }
            composable(Screen.Coach.route) {
                AiCoachScreen(viewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(viewModel, onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel) {
                    navController.popBackStack()
                }
            }
        }
    }
}
