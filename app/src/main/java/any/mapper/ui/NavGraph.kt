package any.mapper.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import any.mapper.R
import any.mapper.ui.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Mappings : Screen("mappings")
    object MappingEditor : Screen("mapping_editor")
    object Test : Screen("test")
    object Profiles : Screen("profiles")
    object Settings : Screen("settings")
}

@Composable
fun AnyMapperNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        Triple(Screen.Home, Icons.Default.Home, R.string.nav_home),
        Triple(Screen.Mappings, Icons.Default.List, R.string.nav_mappings),
        Triple(Screen.Test, Icons.Default.Gamepad, R.string.nav_test),
        Triple(Screen.Profiles, Icons.Default.AccountTree, R.string.nav_profiles),
        Triple(Screen.Settings, Icons.Default.Settings, R.string.nav_settings),
    )

    val showBottomBar = currentDestination?.route != Screen.MappingEditor.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { (screen, icon, labelRes) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(stringResource(labelRes)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Mappings.route) {
                MappingListScreen(onAddMapping = { navController.navigate(Screen.MappingEditor.route) })
            }
            composable(Screen.MappingEditor.route) {
                MappingEditorScreen(
                    onSave = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(Screen.Test.route) { ControllerTestScreen() }
            composable(Screen.Profiles.route) { ProfileScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
