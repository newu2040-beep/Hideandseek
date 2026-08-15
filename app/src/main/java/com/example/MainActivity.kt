package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.VaultViewModel
import com.example.ui.components.VaultHamburgerDrawer
import com.example.ui.dialogs.AboutDialog
import com.example.ui.dialogs.AdminInfoDialog
import com.example.ui.dialogs.IntruderLogsDialog
import com.example.ui.dialogs.PermissionsAccessDialog
import com.example.ui.dialogs.PinChangeDialog
import com.example.ui.screens.AppHiderScreen
import com.example.ui.screens.DisguiseCalculatorScreen
import com.example.ui.screens.DisguiseIconPickerScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.MediaDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrashScreen
import com.example.ui.screens.VaultHomeScreen
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.HideAndSeekTheme
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import com.example.ui.theme.VaultThemePreset
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val viewModel: VaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val preset = VaultThemePreset.fromId(uiState.themeMode)

            HideAndSeekTheme(themeMode = uiState.themeMode) {
                MainAppContent(viewModel = viewModel, activePreset = preset)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Auto-lock vault when app is minimized or backgrounded
        viewModel.lockVault()
    }
}

@Composable
fun MainAppContent(
    viewModel: VaultViewModel,
    activePreset: VaultThemePreset
) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf("vault") }
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val isDark = activePreset.isDark

    // Toast notification handler
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!uiState.isUnlocked) {
            // Check if active disguise is Calculator
            if (uiState.isDisguiseCalculatorActive) {
                DisguiseCalculatorScreen(viewModel = viewModel)
            } else {
                LockScreen(viewModel = viewModel, isDark = isDark)
            }
        } else {
            // Unlocked Vault wrapped in ModalNavigationDrawer for Hamburger Menu
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = currentRoute != "disguise_picker" && uiState.activeMediaDetail == null,
                drawerContent = {
                    VaultHamburgerDrawer(
                        viewModel = viewModel,
                        onClose = {
                            coroutineScope.launch { drawerState.close() }
                        },
                        onNavigate = { route ->
                            currentRoute = route
                            navController.navigate(route) {
                                popUpTo("vault") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = activePreset.background,
                    bottomBar = {
                        if (currentRoute in listOf("vault", "apps", "trash", "settings") && uiState.activeMediaDetail == null) {
                            VaultBottomNavigationBar(
                                currentRoute = currentRoute,
                                trashCount = uiState.trashCount,
                                activePreset = activePreset,
                                onNavigate = { route ->
                                    currentRoute = route
                                    navController.navigate(route) {
                                        popUpTo("vault") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "vault",
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable("vault") {
                                VaultHomeScreen(
                                    viewModel = viewModel,
                                    activePreset = activePreset,
                                    onOpenHamburgerMenu = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onNavigateToApps = {
                                        currentRoute = "apps"
                                        navController.navigate("apps")
                                    },
                                    onNavigateToTrash = {
                                        currentRoute = "trash"
                                        navController.navigate("trash")
                                    }
                                )
                            }
                            composable("apps") {
                                AppHiderScreen(
                                    viewModel = viewModel,
                                    isDark = isDark,
                                    onBack = {
                                        currentRoute = "vault"
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composable("trash") {
                                TrashScreen(
                                    viewModel = viewModel,
                                    isDark = isDark,
                                    onBack = {
                                        currentRoute = "vault"
                                        navController.popBackStack()
                                    }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    activePreset = activePreset,
                                    onOpenThemeDrawer = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onNavigateToDisguisePicker = {
                                        navController.navigate("disguise_picker")
                                    }
                                )
                            }
                            composable("disguise_picker") {
                                DisguiseIconPickerScreen(
                                    viewModel = viewModel,
                                    isDark = isDark,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }

                        // Fullscreen Media Detail & Video Player overlay
                        AnimatedVisibility(
                            visible = uiState.activeMediaDetail != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            uiState.activeMediaDetail?.let { media ->
                                MediaDetailScreen(
                                    viewModel = viewModel,
                                    media = media,
                                    decryptedFilePath = uiState.activeMediaDecryptedPath,
                                    onBack = { viewModel.closeMediaDetail() }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Global Dialogs
        if (uiState.showPinChangeDialog) {
            PinChangeDialog(
                viewModel = viewModel,
                isDark = isDark,
                onDismiss = { viewModel.setShowPinChangeDialog(false) }
            )
        }

        if (uiState.showIntruderLogs) {
            IntruderLogsDialog(
                viewModel = viewModel,
                logs = uiState.intruderLogs,
                isDark = isDark,
                onDismiss = { viewModel.setShowIntruderLogs(false) }
            )
        }

        if (uiState.showAboutDialog) {
            AboutDialog(
                isDark = isDark,
                onDismiss = { viewModel.setShowAboutDialog(false) }
            )
        }

        if (uiState.showAdminInfoDialog) {
            AdminInfoDialog(
                isDark = isDark,
                onDismiss = { viewModel.setShowAdminInfoDialog(false) }
            )
        }

        if (uiState.showPermissionsDialog) {
            PermissionsAccessDialog(
                isDark = isDark,
                onDismiss = { viewModel.setShowPermissionsDialog(false) }
            )
        }
    }
}

@Composable
fun VaultBottomNavigationBar(
    currentRoute: String,
    trashCount: Int,
    activePreset: VaultThemePreset,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = activePreset.surface,
        shadowElevation = if (activePreset.isDark) 0.dp else 4.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.8.dp, color = activePreset.border.copy(alpha = 0.6f))
                .height(68.dp)
        ) {
            // Vault Tab
            NavigationBarItem(
                selected = currentRoute == "vault",
                onClick = { onNavigate("vault") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Vault",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "Vault",
                        fontSize = 11.sp,
                        fontWeight = if (currentRoute == "vault") FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = activePreset.primary,
                    selectedTextColor = activePreset.primary,
                    indicatorColor = activePreset.primary.copy(alpha = 0.18f),
                    unselectedIconColor = activePreset.textSecondary,
                    unselectedTextColor = activePreset.textSecondary
                ),
                modifier = Modifier.testTag("tab_vault")
            )

            // App Hider Tab
            NavigationBarItem(
                selected = currentRoute == "apps",
                onClick = { onNavigate("apps") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "Apps",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "Apps",
                        fontSize = 11.sp,
                        fontWeight = if (currentRoute == "apps") FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = activePreset.primary,
                    selectedTextColor = activePreset.primary,
                    indicatorColor = activePreset.primary.copy(alpha = 0.18f),
                    unselectedIconColor = activePreset.textSecondary,
                    unselectedTextColor = activePreset.textSecondary
                ),
                modifier = Modifier.testTag("tab_apps")
            )

            // Trash Tab
            NavigationBarItem(
                selected = currentRoute == "trash",
                onClick = { onNavigate("trash") },
                icon = {
                    if (trashCount > 0) {
                        BadgedBox(
                            badge = {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFF453A),
                                    modifier = Modifier.padding(1.dp)
                                ) {
                                    Text(
                                        text = "$trashCount",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Trash",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Trash",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = "Trash",
                        fontSize = 11.sp,
                        fontWeight = if (currentRoute == "trash") FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = activePreset.secondary,
                    selectedTextColor = activePreset.secondary,
                    indicatorColor = activePreset.secondary.copy(alpha = 0.18f),
                    unselectedIconColor = activePreset.textSecondary,
                    unselectedTextColor = activePreset.textSecondary
                ),
                modifier = Modifier.testTag("tab_trash")
            )

            // Settings Tab
            NavigationBarItem(
                selected = currentRoute == "settings",
                onClick = { onNavigate("settings") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "Settings",
                        fontSize = 11.sp,
                        fontWeight = if (currentRoute == "settings") FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = activePreset.primary,
                    selectedTextColor = activePreset.primary,
                    indicatorColor = activePreset.primary.copy(alpha = 0.18f),
                    unselectedIconColor = activePreset.textSecondary,
                    unselectedTextColor = activePreset.textSecondary
                ),
                modifier = Modifier.testTag("tab_settings")
            )
        }
    }
}
