package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.components.RoleBadge
import com.example.ui.components.RoleSwitcherDialog
import com.example.ui.screens.BalanceDashboardScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InputReceiveScreen
import com.example.ui.screens.LineBalancingScreen
import com.example.ui.screens.ManpowerScreen
import com.example.ui.screens.ProductionOutputScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StylePOScreen
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PolishBackground
import com.example.ui.theme.PolishOutline
import com.example.ui.theme.PolishOutlineVariant
import com.example.ui.theme.PolishSurfaceVariant
import com.example.ui.theme.PolishTextPrimary
import com.example.ui.theme.PolishTextSecondary
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurpleOnContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSecondaryContainer
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.GarmentsViewModel
import com.example.ui.viewmodel.NavScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: GarmentsViewModel = viewModel()
                GarmentsMainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarmentsMainApp(viewModel: GarmentsViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showRoleSwitcherDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(310.dp),
                drawerContainerColor = PolishBackground
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PurplePrimary)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(PurpleContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = PurpleOnContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Garments Pro",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = "Line 04 • Morning Shift",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PurpleSecondaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // User profile card inside drawer
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PurpleOnContainer.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch { drawerState.close() }
                                    showRoleSwitcherDialog = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = currentUser.fullName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Tap to switch role",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = PurpleSecondaryContainer
                                    )
                                }
                                RoleBadge(role = currentRole)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Menu items grouped
                DrawerSectionHeader("FLOOR OPERATIONS")
                DrawerMenuItem(
                    navScreen = NavScreen.DASHBOARD,
                    currentScreen = currentScreen,
                    icon = Icons.Default.Dashboard,
                    onClick = {
                        viewModel.navigateTo(NavScreen.DASHBOARD)
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    navScreen = NavScreen.INPUT_RECEIVE,
                    currentScreen = currentScreen,
                    icon = Icons.Default.Input,
                    onClick = {
                        viewModel.navigateTo(NavScreen.INPUT_RECEIVE)
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    navScreen = NavScreen.PRODUCTION_OUTPUT,
                    currentScreen = currentScreen,
                    icon = Icons.Default.PrecisionManufacturing,
                    onClick = {
                        viewModel.navigateTo(NavScreen.PRODUCTION_OUTPUT)
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    navScreen = NavScreen.BALANCE_DASHBOARD,
                    currentScreen = currentScreen,
                    icon = Icons.Default.Balance,
                    onClick = {
                        viewModel.navigateTo(NavScreen.BALANCE_DASHBOARD)
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    navScreen = NavScreen.LINE_BALANCING,
                    currentScreen = currentScreen,
                    icon = Icons.Default.Tune,
                    onClick = {
                        viewModel.navigateTo(NavScreen.LINE_BALANCING)
                        scope.launch { drawerState.close() }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp), color = PolishOutlineVariant)

                DrawerSectionHeader("MASTER DATA")
                DrawerMenuItem(
                    navScreen = NavScreen.STYLE_PO,
                    currentScreen = currentScreen,
                    icon = Icons.Default.Inventory2,
                    onClick = {
                        viewModel.navigateTo(NavScreen.STYLE_PO)
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    navScreen = NavScreen.MANPOWER,
                    currentScreen = currentScreen,
                    icon = Icons.Default.People,
                    onClick = {
                        viewModel.navigateTo(NavScreen.MANPOWER)
                        scope.launch { drawerState.close() }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp), color = PolishOutlineVariant)

                DrawerSectionHeader("MANAGEMENT & TOOLS")
                DrawerMenuItem(
                    navScreen = NavScreen.REPORTS,
                    currentScreen = currentScreen,
                    icon = Icons.Default.Assessment,
                    onClick = {
                        viewModel.navigateTo(NavScreen.REPORTS)
                        scope.launch { drawerState.close() }
                    }
                )
                DrawerMenuItem(
                    navScreen = NavScreen.SETTINGS,
                    currentScreen = currentScreen,
                    icon = Icons.Default.Settings,
                    onClick = {
                        viewModel.navigateTo(NavScreen.SETTINGS)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(PurplePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Garments Pro",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PolishTextPrimary
                                )
                                Text(
                                    text = "Line 04 • Morning Shift",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PolishTextSecondary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("nav_menu_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = PolishTextPrimary)
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PurpleContainer)
                                .clickable {
                                    showRoleSwitcherDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = PurpleOnContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        RoleBadge(
                            role = currentRole,
                            onClick = { showRoleSwitcherDialog = true },
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PolishBackground
                    )
                )
            },
            bottomBar = {
                Column {
                    HorizontalDivider(color = PolishOutline, thickness = 1.dp)
                    NavigationBar(
                        containerColor = PolishSurfaceVariant,
                        tonalElevation = 0.dp
                    ) {
                        val bottomItems = listOf(
                            NavScreen.DASHBOARD to Icons.Default.Dashboard,
                            NavScreen.INPUT_RECEIVE to Icons.Default.Input,
                            NavScreen.PRODUCTION_OUTPUT to Icons.Default.PrecisionManufacturing,
                            NavScreen.LINE_BALANCING to Icons.Default.Tune
                        )

                        bottomItems.forEach { (screen, icon) ->
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(screen) },
                                icon = { Icon(icon, contentDescription = screen.title) },
                                label = { Text(screen.title.take(8), fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PurplePrimary,
                                    selectedTextColor = PurplePrimary,
                                    indicatorColor = PurpleSecondaryContainer,
                                    unselectedIconColor = PolishTextSecondary,
                                    unselectedTextColor = PolishTextSecondary
                                ),
                                modifier = Modifier.testTag("bottom_nav_${screen.name.lowercase()}")
                            )
                        }

                        // 5th item: More / Menu
                        NavigationBarItem(
                            selected = currentScreen !in bottomItems.map { it.first },
                            onClick = { scope.launch { drawerState.open() } },
                            icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                            label = { Text("Menu", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PurplePrimary,
                                selectedTextColor = PurplePrimary,
                                indicatorColor = PurpleSecondaryContainer,
                                unselectedIconColor = PolishTextSecondary,
                                unselectedTextColor = PolishTextSecondary
                            ),
                            modifier = Modifier.testTag("bottom_nav_more")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PolishBackground)
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        NavScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                        NavScreen.INPUT_RECEIVE -> InputReceiveScreen(viewModel = viewModel)
                        NavScreen.PRODUCTION_OUTPUT -> ProductionOutputScreen(viewModel = viewModel)
                        NavScreen.BALANCE_DASHBOARD -> BalanceDashboardScreen(viewModel = viewModel)
                        NavScreen.LINE_BALANCING -> LineBalancingScreen(viewModel = viewModel)
                        NavScreen.STYLE_PO -> StylePOScreen(viewModel = viewModel)
                        NavScreen.MANPOWER -> ManpowerScreen(viewModel = viewModel)
                        NavScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
                        NavScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    if (showRoleSwitcherDialog) {
        RoleSwitcherDialog(
            currentRole = currentRole,
            onRoleSelected = { role ->
                viewModel.switchUserRole(role)
                showRoleSwitcherDialog = false
            },
            onDismiss = { showRoleSwitcherDialog = false }
        )
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = PolishTextSecondary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
    )
}

@Composable
private fun DrawerMenuItem(
    navScreen: NavScreen,
    currentScreen: NavScreen,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val isSelected = navScreen == currentScreen
    NavigationDrawerItem(
        label = {
            Text(
                text = navScreen.title,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        },
        selected = isSelected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = navScreen.title,
                tint = if (isSelected) PurplePrimary else PolishTextSecondary
            )
        },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = PurpleSecondaryContainer,
            selectedTextColor = PurplePrimary,
            unselectedTextColor = PolishTextSecondary
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag("drawer_item_${navScreen.name.lowercase()}")
    )
}
