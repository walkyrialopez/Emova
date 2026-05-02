package com.emotionalintelligence.emova.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.emotionalintelligence.emova.R
import com.emotionalintelligence.emova.ui.checkin.CheckInScreen
import com.emotionalintelligence.emova.ui.growth.GrowthScreen
import com.emotionalintelligence.emova.ui.insights.InsightsScreen
import com.emotionalintelligence.emova.ui.journal.JournalScreen
import com.emotionalintelligence.emova.ui.onboarding.OnboardingScreen
import com.emotionalintelligence.emova.ui.settings.SettingsScreen
import com.emotionalintelligence.emova.ui.thermometer.ThoughtThermometerScreen
import com.emotionalintelligence.emova.ui.today.TodayScreen
import com.emotionalintelligence.emova.ui.wellness.WellnessScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emotionalintelligence.emova.viewmodel.JournalViewModel

data class BottomNavItem(
    val labelResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem(R.string.nav_today,    Icons.Filled.Home,        Icons.Outlined.Home,           "today"),
    BottomNavItem(R.string.nav_checkin,  Icons.Filled.Favorite,    Icons.Outlined.FavoriteBorder, "checkin"),
    BottomNavItem(R.string.nav_insights, Icons.Filled.DateRange,   Icons.Outlined.DateRange,      "insights"),
    BottomNavItem(R.string.nav_wellness, Icons.Filled.Healing,     Icons.Outlined.Healing,        "wellness/0"),
    BottomNavItem(R.string.nav_growth,   Icons.Filled.AutoStories, Icons.Outlined.AutoStories,    "growth")
)

object Routes {
    const val ONBOARDING  = "onboarding"
    const val TODAY       = "today"
    const val CHECK_IN    = "checkin"
    const val INSIGHTS    = "insights"
    const val WELLNESS    = "wellness/{startTab}"
    const val GROWTH      = "growth"
    const val SETTINGS    = "settings"

    // 🎯 NUEVO: Agregamos /{intensity} a la ruta principal del Journal
    const val JOURNAL     = "journal/{checkInId}/{emotionName}/{emotionEmoji}/{emotionFamily}/{intensity}"

    const val THERMOMETER = "thermometer/{emotionName}"

    fun wellnessRoute(startTab: Int = 0) = "wellness/$startTab"
    fun thermometerRoute(emotionName: String) = "thermometer/$emotionName"

    // 🎯 NUEVO: Agregamos intensity al helper de la ruta
    fun journalRoute(checkInId: Int, emotionName: String, emotionEmoji: String, emotionFamily: String, intensity: Int) =
        "journal/$checkInId/$emotionName/$emotionEmoji/$emotionFamily/$intensity"
}

@Composable
fun EmováNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("emova_prefs", Context.MODE_PRIVATE) }
    val hasOnboarded = remember { prefs.getBoolean("onboarded", false) }
    val startDestination = if (hasOnboarded) Routes.TODAY else Routes.ONBOARDING

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    var todayRefreshKey by remember { mutableIntStateOf(0) }

    val showBottomBar = currentRoute != Routes.ONBOARDING && currentRoute != Routes.THERMOMETER

    fun isSelected(itemRoute: String): Boolean = when {
        itemRoute.startsWith("wellness") -> currentRoute?.startsWith("wellness") == true
        else -> currentRoute == itemRoute
    }

    Scaffold(
        containerColor = Color(0xFFF7F6FB),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color(0xFFFFFFFF), tonalElevation = 0.dp) {
                    bottomNavItems.forEach { item ->
                        val selected = isSelected(item.route)
                        val label = stringResource(item.labelResId)
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                val route = if (item.route == "wellness/0") Routes.wellnessRoute(0) else item.route
                                if (item.route == "today") {
                                    todayRefreshKey++
                                    navController.navigate(route) {
                                        popUpTo(Routes.TODAY) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.navigate(route) {
                                        popUpTo(Routes.TODAY) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(if (selected) item.selectedIcon else item.unselectedIcon, contentDescription = label) },
                            label = { Text(text = label, maxLines = 1, fontSize = 10.sp, softWrap = false) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = Color(0xFF6C63FF),
                                selectedTextColor   = Color(0xFF6C63FF),
                                unselectedIconColor = Color(0xFF666688),
                                unselectedTextColor = Color(0xFF666688),
                                indicatorColor      = Color(0xFF6C63FF).copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = startDestination, modifier = Modifier.padding(innerPadding)) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(onComplete = { name ->
                    prefs.edit().putString("user_name", name).putBoolean("onboarded", true).apply()
                    navController.navigate(Routes.TODAY) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                })
            }
            composable(Routes.TODAY) {
                TodayScreen(
                    refreshKey            = todayRefreshKey,
                    onNavigateToCheckIn   = { navController.navigate(Routes.CHECK_IN) { launchSingleTop = true } },
                    onNavigateToGratitude = { navController.navigate(Routes.wellnessRoute(1)) { launchSingleTop = true } },
                    onNavigateToBreathe   = { navController.navigate(Routes.wellnessRoute(0)) { launchSingleTop = true } },
                    onNavigateToLesson    = { navController.navigate(Routes.GROWTH) { launchSingleTop = true } },
                    onNavigateToHabits    = { navController.navigate(Routes.wellnessRoute(2)) { launchSingleTop = true } },
                    onNavigateToSettings  = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } }
                )
            }
            composable(Routes.CHECK_IN) {
                // 🎯 NUEVO: Agregamos el parámetro 'intensity' que devuelve el CheckInScreen
                CheckInScreen(onCheckInSaved = { checkInId, emotionName, emotionEmoji, emotionFamily, intensity ->
                    navController.navigate(Routes.journalRoute(checkInId, emotionName, emotionEmoji, emotionFamily, intensity))
                })
            }
            composable(route = Routes.WELLNESS, arguments = listOf(navArgument("startTab") { type = NavType.IntType })) { backStackEntry ->
                WellnessScreen(startTab = backStackEntry.arguments?.getInt("startTab") ?: 0)
            }
            composable(Routes.INSIGHTS) { InsightsScreen() }
            composable(Routes.GROWTH)   { GrowthScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }

            // 🎯 EL BLOQUE DE JOURNAL QUE ESTÁBAMOS BUSCANDO
            composable(route = Routes.JOURNAL,
                arguments = listOf(
                    navArgument("checkInId")     { type = NavType.IntType },
                    navArgument("emotionName")   { type = NavType.StringType },
                    navArgument("emotionEmoji")  { type = NavType.StringType },
                    navArgument("emotionFamily") { type = NavType.StringType },
                    // 🎯 NUEVO: Le decimos a Compose que espere un entero de la URL
                    navArgument("intensity")     { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val journalViewModel: JournalViewModel = viewModel()

                JournalScreen(
                    checkInId     = backStackEntry.arguments?.getInt("checkInId") ?: 0,
                    emotionName   = backStackEntry.arguments?.getString("emotionName") ?: "",
                    emotionEmoji  = backStackEntry.arguments?.getString("emotionEmoji") ?: "",
                    emotionFamily = backStackEntry.arguments?.getString("emotionFamily") ?: "",

                    // 🎯 NUEVO: Extraemos la intensidad de los argumentos
                    initialIntensity = backStackEntry.arguments?.getInt("intensity") ?: 0,

                    viewModel     = journalViewModel,

                    // 🎯 NUEVO: Cambiado a onFinish para el nuevo flujo
                    onFinish = {
                        navController.navigate(Routes.TODAY) { popUpTo(Routes.TODAY) { inclusive = true } }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(route = Routes.THERMOMETER, arguments = listOf(navArgument("emotionName") { type = NavType.StringType })) { backStackEntry ->
                ThoughtThermometerScreen(
                    emotionName = backStackEntry.arguments?.getString("emotionName") ?: "",
                    onFinish = { navController.navigate(Routes.TODAY) { popUpTo(Routes.TODAY) { inclusive = true } } },
                    onSkip   = { navController.navigate(Routes.TODAY) { popUpTo(Routes.TODAY) { inclusive = true } } }
                )
            }
        }
    }
}