package com.example.pilahin.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pilahin.ui.common.pressScale
import com.example.pilahin.ui.dashboard.DashboardScreen
import com.example.pilahin.ui.dashboard.DashboardViewModel
import com.example.pilahin.ui.dashboard.FullMapScreen
import com.example.pilahin.ui.menu.AboutScreen
import com.example.pilahin.ui.menu.GuideScreen
import com.example.pilahin.ui.menu.HistoryScreen
import com.example.pilahin.ui.menu.OilScreen
import com.example.pilahin.ui.menu.ProfileScreen
import com.example.pilahin.ui.menu.ScheduleScreen
import com.example.pilahin.ui.scan.ResultScreen
import com.example.pilahin.ui.scan.ScanScreen
import com.example.pilahin.ui.scan.ScanViewModel
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.SplashBackground
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted

object MainRoutes {
    const val HOME = "home"
    const val SCHEDULE = "schedule"
    const val SCAN = "scan"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val GUIDE = "guide"
    const val OIL = "oil"
    const val RESULT = "result"
    const val ABOUT = "about"
    const val MAP = "map"
}

@Composable
fun MainShell(
    userName: String,
    username: String,
    onLogout: () -> Unit
) {
    val nav = rememberNavController()
    val scanViewModel: ScanViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()
    val backEntry by nav.currentBackStackEntryAsState()
    val route = backEntry?.destination?.route

    // Sub-halaman ikut menyalakan tab induknya
    val activeTab = when (route) {
        MainRoutes.GUIDE, MainRoutes.OIL, MainRoutes.MAP -> MainRoutes.HOME
        MainRoutes.ABOUT -> MainRoutes.PROFILE
        else -> route
    }

    fun openTab(target: String) {
        nav.navigate(target) {
            popUpTo(MainRoutes.HOME)
            launchSingleTop = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground)
    ) {
        NavHost(
            navController = nav,
            startDestination = MainRoutes.HOME,
            modifier = Modifier.weight(1f),
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(120)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(120)) }
        ) {
            composable(MainRoutes.HOME) {
                DashboardScreen(
                    userName = userName,
                    onScan = { openTab(MainRoutes.SCAN) },
                    onSchedule = { openTab(MainRoutes.SCHEDULE) },
                    onGuide = { nav.navigate(MainRoutes.GUIDE) },
                    onHistory = { openTab(MainRoutes.HISTORY) },
                    onOil = { nav.navigate(MainRoutes.OIL) },
                    onOpenMap = { nav.navigate(MainRoutes.MAP) },
                    viewModel = dashboardViewModel
                )
            }
            composable(MainRoutes.MAP) {
                FullMapScreen(
                    viewModel = dashboardViewModel,
                    onBack = { nav.popBackStack() }
                )
            }
            composable(MainRoutes.SCHEDULE) {
                ScheduleScreen(onBack = { openTab(MainRoutes.HOME) })
            }
            composable(MainRoutes.SCAN) {
                ScanScreen(
                    viewModel = scanViewModel,
                    onResult = { nav.navigate(MainRoutes.RESULT) }
                )
            }
            composable(MainRoutes.RESULT) {
                ResultScreen(
                    viewModel = scanViewModel,
                    onBack = { nav.popBackStack() },
                    onRescan = { openTab(MainRoutes.SCAN) }
                )
            }
            composable(MainRoutes.HISTORY) {
                HistoryScreen(
                    viewModel = scanViewModel,
                    onOpen = {
                        scanViewModel.openFromHistory(it)
                        nav.navigate(MainRoutes.RESULT)
                    },
                    onBack = { openTab(MainRoutes.HOME) }
                )
            }
            composable(MainRoutes.PROFILE) {
                ProfileScreen(
                    name = userName,
                    username = username,
                    onHistory = { openTab(MainRoutes.HISTORY) },
                    onAbout = { nav.navigate(MainRoutes.ABOUT) },
                    onLogout = onLogout
                )
            }
            composable(MainRoutes.GUIDE) {
                GuideScreen(
                    onBack = { nav.popBackStack() },
                    onScan = { openTab(MainRoutes.SCAN) }
                )
            }
            composable(MainRoutes.OIL) {
                OilScreen(onBack = { nav.popBackStack() })
            }
            composable(MainRoutes.ABOUT) {
                AboutScreen(onBack = { nav.popBackStack() })
            }
        }

        if (route != MainRoutes.RESULT && route != MainRoutes.MAP) {
            MainBottomBar(
                selected = activeTab,
                onSelect = { openTab(it) }
            )
        }
    }
}

@Composable
private fun MainBottomBar(selected: String?, onSelect: (String) -> Unit) {
    val scanSelected = selected == MainRoutes.SCAN

    val ringWidth by animateDpAsState(
        targetValue = if (scanSelected) 3.dp else 0.dp,
        animationSpec = tween(250),
        label = "scanRing"
    )
    val scanScale by animateFloatAsState(
        targetValue = if (scanSelected) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scanScale"
    )
    val scanLabelColor by animateColorAsState(
        targetValue = if (scanSelected) GreenDark else TextMuted,
        animationSpec = tween(250),
        label = "scanLabel"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(92.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(70.dp)
                .background(Color.White, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BarItem("Beranda", Icons.Rounded.Home, selected == MainRoutes.HOME, { onSelect(MainRoutes.HOME) }, Modifier.weight(1f))
            BarItem("Jadwal", Icons.Rounded.CalendarMonth, selected == MainRoutes.SCHEDULE, { onSelect(MainRoutes.SCHEDULE) }, Modifier.weight(1f))
            Spacer(Modifier.weight(1f))
            BarItem("Riwayat", Icons.Rounded.History, selected == MainRoutes.HISTORY, { onSelect(MainRoutes.HISTORY) }, Modifier.weight(1f))
            BarItem("Profil", Icons.Rounded.Person, selected == MainRoutes.PROFILE, { onSelect(MainRoutes.PROFILE) }, Modifier.weight(1f))
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(60.dp)
                .graphicsLayer {
                    scaleX = scanScale
                    scaleY = scanScale
                }
                .pressScale { onSelect(MainRoutes.SCAN) }
                .border(ringWidth, GreenDark, CircleShape)
                .clip(CircleShape)
                .background(Lime),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.DocumentScanner,
                contentDescription = "Scan sampah",
                tint = TextDark,
                modifier = Modifier.size(30.dp)
            )
        }
        Text(
            text = "Scan",
            fontSize = 11.sp,
            fontWeight = if (scanSelected) FontWeight.Bold else FontWeight.Medium,
            color = scanLabelColor,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun BarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background by animateColorAsState(
        targetValue = if (selected) Lime.copy(alpha = 0.55f) else Color.Transparent,
        animationSpec = tween(250),
        label = "itemBg"
    )
    val tint by animateColorAsState(
        targetValue = if (selected) GreenDark else TextMuted,
        animationSpec = tween(250),
        label = "itemTint"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "itemScale"
    )

    Column(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                }
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = tint
        )
    }
}