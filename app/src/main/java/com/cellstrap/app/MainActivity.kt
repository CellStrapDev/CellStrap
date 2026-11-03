package com.cellstrap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cellstrap.app.ui.CellstrapViewModel
import com.cellstrap.app.ui.screens.*
import com.cellstrap.app.ui.theme.CellBlack
import com.cellstrap.app.ui.theme.CellCard
import com.cellstrap.app.ui.theme.CellCardBorder
import com.cellstrap.app.ui.theme.CellGreen
import com.cellstrap.app.ui.theme.CellGreenDark
import com.cellstrap.app.ui.theme.CellTextMuted
import com.cellstrap.app.ui.theme.CellTextSecondary
import com.cellstrap.app.ui.theme.CellstrapTheme

sealed class NavRoute(val route: String, val label: String, val icon: ImageVector) {
    object Home : NavRoute("home", "Home", Icons.Default.Home)
    object FFlags : NavRoute("fflags", "FFlags", Icons.Default.Code)
    object Presets : NavRoute("presets", "Presets", Icons.Default.FlashOn)
    object Profiles : NavRoute("profiles", "Profiles", Icons.Default.Folder)
    object Settings : NavRoute("settings", "Settings", Icons.Default.Settings)
}

val navItems = listOf(
    NavRoute.Home,
    NavRoute.FFlags,
    NavRoute.Presets,
    NavRoute.Profiles,
    NavRoute.Settings
)

class MainActivity : ComponentActivity() {
    private val viewModel: CellstrapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CellstrapTheme {
                CellstrapApp(viewModel)
            }
        }
    }
}

@Composable
fun CellstrapApp(viewModel: CellstrapViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CellBlack),
        containerColor = CellBlack,
        bottomBar = {
            CellstrapBottomBar(
                navItems = navItems,
                currentDestination = currentDestination,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = remember { SnackbarHostState() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CellBlack)
        ) {
            NavHost(
                navController = navController,
                startDestination = NavRoute.Home.route
            ) {
                composable(NavRoute.Home.route) { HomeScreen(viewModel) }
                composable(NavRoute.FFlags.route) { FFlagsScreen(viewModel) }
                composable(NavRoute.Presets.route) { PresetsScreen(viewModel) }
                composable(NavRoute.Profiles.route) { ProfilesScreen(viewModel) }
                composable(NavRoute.Settings.route) { SettingsScreen(viewModel) }
            }

            // Snackbar overlay
            uiState.snackbarMessage?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    CellstrapSnackbar(
                        message = message,
                        isSuccess = uiState.injectSuccess,
                        onDismiss = { viewModel.dismissSnackbar() }
                    )
                }
            }
        }
    }
}

@Composable
fun CellstrapBottomBar(
    navItems: List<NavRoute>,
    currentDestination: androidx.navigation.NavDestination?,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CellCard)
                .border(1.dp, CellCardBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    BottomNavItem(
                        item = item,
                        selected = selected,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(item: NavRoute, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) CellGreenDark.copy(alpha = 0.35f) else Color.Transparent
    val iconColor = if (selected) CellGreen else CellTextMuted
    val textColor = if (selected) CellGreen else CellTextMuted

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .then(
                if (selected) Modifier.border(
                    1.dp,
                    CellGreen.copy(alpha = 0.3f),
                    RoundedCornerShape(14.dp)
                ) else Modifier
            )
    ) {
        NavigationBarItem(
            selected = selected,
            onClick = onClick,
            icon = {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    modifier = Modifier.size(22.dp),
                    tint = iconColor
                )
            },
            label = {
                Text(
                    text = item.label,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = textColor
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CellGreen,
                selectedTextColor = CellGreen,
                unselectedIconColor = CellTextMuted,
                unselectedTextColor = CellTextMuted,
                indicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
fun CellstrapSnackbar(
    message: String,
    isSuccess: Boolean,
    onDismiss: () -> Unit
) {
    LaunchedEffect(message) {
        kotlinx.coroutines.delay(3000)
        onDismiss()
    }

    val color = if (isSuccess) CellGreen else CellTextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CellCard)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = com.cellstrap.app.ui.theme.CellTextPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = CellTextMuted, modifier = Modifier.size(14.dp))
            }
        }
    }
}
