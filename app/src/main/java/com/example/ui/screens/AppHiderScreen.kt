package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.security.AppCategory
import com.example.security.InstalledAppItem
import com.example.ui.VaultViewModel
import com.example.ui.components.IosStyleSwitch
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHiderScreen(
    viewModel: VaultViewModel,
    isDark: Boolean = true,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var showExplanationDialog by remember { mutableStateOf(false) }
    var selectedAppForDetail by remember { mutableStateOf<InstalledAppItem?>(null) }

    // Realtime filtering by Category and Search Query
    val filteredApps = remember(uiState.installedApps, uiState.selectedAppCategory, uiState.appSearchQuery) {
        uiState.installedApps.filter { app ->
            val matchesCategory = when (uiState.selectedAppCategory) {
                AppCategory.ALL -> true
                AppCategory.PROTECTED -> app.isHidden
                else -> app.category == uiState.selectedAppCategory
            }
            val matchesQuery = if (uiState.appSearchQuery.isBlank()) true else {
                app.appName.contains(uiState.appSearchQuery, ignoreCase = true) ||
                app.packageName.contains(uiState.appSearchQuery, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
    }

    // Dynamic counts per category
    val categoryCounts = remember(uiState.installedApps) {
        AppCategory.values().associateWith { cat ->
            when (cat) {
                AppCategory.ALL -> uiState.installedApps.size
                AppCategory.PROTECTED -> uiState.installedApps.count { it.isHidden }
                else -> uiState.installedApps.count { it.category == cat }
            }
        }
    }

    val bgColor = if (isDark) DarkBackground else LightBackground
    val cardBg = if (isDark) DarkSurface else LightSurface
    val textColor = if (isDark) Color.White else Color(0xFF131522)
    val subtitleColor = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1. Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "App & Game Hider",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "${uiState.installedApps.size} Installed • ${uiState.installedApps.count { it.isHidden }} Protected",
                        fontSize = 11.sp,
                        color = AccentCyan,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row {
                    IconButton(onClick = { viewModel.refreshInstalledApps() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Apps",
                            tint = textColor
                        )
                    }
                    IconButton(onClick = { showExplanationDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "App Hider Info",
                            tint = textColor
                        )
                    }
                }
            }

            // 2. Search Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0xFF181B2A) else Color(0xFFE8EAF3)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = subtitleColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = uiState.appSearchQuery,
                        onValueChange = { viewModel.onAppSearchQueryChanged(it) },
                        placeholder = {
                            Text(
                                text = "Search games & applications...",
                                color = subtitleColor,
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("app_search_field")
                    )
                    if (uiState.appSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onAppSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = subtitleColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 3. Realtime Category Chips (Horizontal Scroll)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AppCategory.values()) { category ->
                    val isSelected = uiState.selectedAppCategory == category
                    val count = categoryCounts[category] ?: 0

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.onAppCategorySelected(category) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) AccentPurple else (if (isDark) Color(0xFF1B1E2E) else Color(0xFFECEFF8)),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AccentCyan else (if (isDark) DarkBorder else LightBorder)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = category.emoji,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${category.displayName} ($count)",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else textColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 4. Apps List
            if (uiState.isLoadingApps) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentPurple)
                }
            } else if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${uiState.selectedAppCategory.emoji} No ${uiState.selectedAppCategory.displayName} Found",
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.appSearchQuery.isNotEmpty()) "Try a different search query" else "No apps registered under this category",
                            color = subtitleColor,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { appItem ->
                        AppHiderRowItem(
                            app = appItem,
                            isDark = isDark,
                            onToggle = { viewModel.toggleAppHidden(appItem) },
                            onLaunch = {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(appItem.packageName)
                                if (launchIntent != null) {
                                    context.startActivity(launchIntent)
                                } else {
                                    Toast.makeText(context, "Cannot launch this application", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onInfo = { selectedAppForDetail = appItem }
                        )
                    }
                }
            }
        }

        // Explanation Dialog
        if (showExplanationDialog) {
            AlertDialog(
                onDismissRequest = { showExplanationDialog = false },
                title = {
                    Text(
                        text = "App & Game Isolation Engine",
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "HIDEANDSEEK categorizes all installed games, social messengers, media apps, and system tools in real-time.",
                            color = textColor,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "• Hiding an app isolates it inside the secret vault.\n• Games and social apps can be toggled individually.\n• Direct launch is available securely from within the vault.",
                            color = subtitleColor,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showExplanationDialog = false }) {
                        Text("Understood", color = AccentPurple, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = cardBg
            )
        }

        // App Detail Dialog
        selectedAppForDetail?.let { app ->
            AlertDialog(
                onDismissRequest = { selectedAppForDetail = null },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (app.iconBitmap != null) {
                            Image(
                                bitmap = app.iconBitmap.asImageBitmap(),
                                contentDescription = app.appName,
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                            )
                        }
                        Text(text = app.appName, fontWeight = FontWeight.Bold, color = textColor, fontSize = 17.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Package: ${app.packageName}", color = subtitleColor, fontSize = 12.sp)
                        Text("Category: ${app.category.emoji} ${app.category.displayName}", color = AccentCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        app.versionName?.let {
                            Text("Version: $it", color = subtitleColor, fontSize = 12.sp)
                        }
                        Text("Protection Status: ${if (app.isHidden) "🔒 Vault Isolated" else "📱 Visible in System"}", color = if (app.isHidden) Color(0xFF10B981) else subtitleColor, fontSize = 13.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                                selectedAppForDetail = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open App")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedAppForDetail = null }) {
                        Text("Close", color = subtitleColor)
                    }
                },
                containerColor = cardBg
            )
        }
    }
}

@Composable
fun AppHiderRowItem(
    app: InstalledAppItem,
    isDark: Boolean,
    onToggle: () -> Unit,
    onLaunch: () -> Unit,
    onInfo: () -> Unit
) {
    val cardBg = if (isDark) DarkSurface else LightSurface
    val borderColor = if (isDark) DarkBorder else LightBorder
    val textColor = if (isDark) Color.White else Color(0xFF131522)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        shadowElevation = if (isDark) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onInfo() }
            ) {
                // App Icon
                if (app.iconBitmap != null) {
                    Image(
                        bitmap = app.iconBitmap.asImageBitmap(),
                        contentDescription = app.appName,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = app.appName,
                            tint = AccentPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = app.appName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${app.category.emoji} ${app.category.displayName}",
                            fontSize = 11.sp,
                            color = AccentCyan,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF686D88) else Color(0xFF9AA0B8)
                        )
                        Text(
                            text = if (app.isHidden) "🔒 Isolated" else "Visible",
                            fontSize = 11.sp,
                            color = if (app.isHidden) Color(0xFF10B981) else (if (isDark) Color(0xFF7B809B) else Color(0xFF8C92AA)),
                            fontWeight = if (app.isHidden) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onLaunch,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Launch App",
                        tint = AccentPurpleLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IosStyleSwitch(
                    checked = app.isHidden,
                    onCheckedChange = { onToggle() }
                )
            }
        }
    }
}

