package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VaultViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.AccentRed
import com.example.ui.theme.VaultTheme
import com.example.ui.theme.VaultThemePreset

@Composable
fun VaultHamburgerDrawer(
    viewModel: VaultViewModel,
    onClose: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val activePreset = VaultThemePreset.fromId(uiState.themeMode)

    var selectedThemeCategory by remember { mutableStateOf("ALL") }

    val filteredThemes = remember(selectedThemeCategory) {
        when (selectedThemeCategory) {
            "AMOLED" -> VaultThemePreset.entries.filter { it.isAmoled }
            "DARK" -> VaultThemePreset.entries.filter { it.category == "DARK" }
            "LIGHT" -> VaultThemePreset.entries.filter { it.category == "LIGHT" }
            else -> VaultThemePreset.entries
        }
    }

    val photosCount = uiState.allMedia.count { it.mediaType == "IMAGE" }
    val videosCount = uiState.allMedia.count { it.mediaType == "VIDEO" }

    ModalDrawerSheet(
        drawerContainerColor = activePreset.surface,
        drawerContentColor = activePreset.textPrimary,
        modifier = Modifier
            .width(340.dp)
            .fillMaxHeight()
            .testTag("hamburger_drawer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. Drawer Header with Security Branding
            DrawerHeader(
                activePreset = activePreset,
                onClose = onClose
            )

            HorizontalDivider(
                color = activePreset.border.copy(alpha = 0.6f),
                thickness = 0.8.dp
            )

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // AMOLED Super Highlight Card
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    AmoledShowcaseBanner(activePreset = activePreset)
                }

                // 2. THEME SELECTOR SECTION
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ColorLens,
                                    contentDescription = null,
                                    tint = activePreset.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "DISPLAY THEMES",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp,
                                    color = activePreset.primary
                                )
                            }

                            Text(
                                text = "${VaultThemePreset.entries.size} Available",
                                fontSize = 11.sp,
                                color = activePreset.textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ThemeFilterPill(
                                title = "All (${VaultThemePreset.entries.size})",
                                selected = selectedThemeCategory == "ALL",
                                onClick = { selectedThemeCategory = "ALL" },
                                activePreset = activePreset
                            )
                            ThemeFilterPill(
                                title = "⚡ Super AMOLED (4)",
                                selected = selectedThemeCategory == "AMOLED",
                                onClick = { selectedThemeCategory = "AMOLED" },
                                activePreset = activePreset
                            )
                            ThemeFilterPill(
                                title = "🌙 Dark Slate (3)",
                                selected = selectedThemeCategory == "DARK",
                                onClick = { selectedThemeCategory = "DARK" },
                                activePreset = activePreset
                            )
                            ThemeFilterPill(
                                title = "☀️ Crisp Light (2)",
                                selected = selectedThemeCategory == "LIGHT",
                                onClick = { selectedThemeCategory = "LIGHT" },
                                activePreset = activePreset
                            )
                        }
                    }
                }

                // Theme Presets List Cards
                items(filteredThemes, key = { it.id }) { preset ->
                    ThemePresetCard(
                        preset = preset,
                        isSelected = preset.id == activePreset.id,
                        activePreset = activePreset,
                        onSelect = {
                            viewModel.setThemeMode(preset.id)
                            viewModel.setToastMessage("Switched to ${preset.title}")
                        }
                    )
                }

                // 3. QUICK VAULT NAVIGATION SECTION
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "VAULT UTILITIES & NAVIGATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        color = activePreset.textSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = activePreset.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            0.8.dp,
                            activePreset.border.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            DrawerNavRow(
                                icon = Icons.Default.PhotoLibrary,
                                title = "Encrypted Photos",
                                badgeText = "$photosCount items",
                                activePreset = activePreset,
                                onClick = {
                                    viewModel.selectTab(0)
                                    onNavigate("vault")
                                    onClose()
                                }
                            )

                            HorizontalDivider(color = activePreset.border.copy(alpha = 0.4f), thickness = 0.6.dp)

                            DrawerNavRow(
                                icon = Icons.Default.Videocam,
                                title = "Encrypted Videos",
                                badgeText = "$videosCount items",
                                activePreset = activePreset,
                                onClick = {
                                    viewModel.selectTab(1)
                                    onNavigate("vault")
                                    onClose()
                                }
                            )

                            HorizontalDivider(color = activePreset.border.copy(alpha = 0.4f), thickness = 0.6.dp)

                            DrawerNavRow(
                                icon = Icons.Default.Apps,
                                title = "Stealth App Hider",
                                badgeText = "Launcher",
                                activePreset = activePreset,
                                onClick = {
                                    onNavigate("apps")
                                    onClose()
                                }
                            )

                            HorizontalDivider(color = activePreset.border.copy(alpha = 0.4f), thickness = 0.6.dp)

                            DrawerNavRow(
                                icon = Icons.Default.DeleteSweep,
                                title = "Trash / Recycle Bin",
                                badgeText = if (uiState.trashCount > 0) "${uiState.trashCount} items" else "Empty",
                                badgeColor = if (uiState.trashCount > 0) AccentRed else activePreset.textSecondary,
                                activePreset = activePreset,
                                onClick = {
                                    onNavigate("trash")
                                    onClose()
                                }
                            )

                            HorizontalDivider(color = activePreset.border.copy(alpha = 0.4f), thickness = 0.6.dp)

                            DrawerNavRow(
                                icon = Icons.Default.CameraAlt,
                                title = "Intruder Captures",
                                badgeText = "${uiState.intruderLogs.size} logs",
                                activePreset = activePreset,
                                onClick = {
                                    viewModel.setShowIntruderLogs(true)
                                    onClose()
                                }
                            )

                            HorizontalDivider(color = activePreset.border.copy(alpha = 0.4f), thickness = 0.6.dp)

                            DrawerNavRow(
                                icon = Icons.Default.Visibility,
                                title = "Disguise Mode",
                                badgeText = uiState.activeDisguise?.name ?: "Default",
                                activePreset = activePreset,
                                onClick = {
                                    onNavigate("disguise_picker")
                                    onClose()
                                }
                            )

                            HorizontalDivider(color = activePreset.border.copy(alpha = 0.4f), thickness = 0.6.dp)

                            DrawerNavRow(
                                icon = Icons.Default.Shield,
                                title = "Permissions & System Hub",
                                badgeText = "Security",
                                activePreset = activePreset,
                                onClick = {
                                    viewModel.setShowPermissionsDialog(true)
                                    onClose()
                                }
                            )

                            HorizontalDivider(color = activePreset.border.copy(alpha = 0.4f), thickness = 0.6.dp)

                            DrawerNavRow(
                                icon = Icons.Default.Settings,
                                title = "Vault Settings",
                                badgeText = "Config",
                                activePreset = activePreset,
                                onClick = {
                                    onNavigate("settings")
                                    onClose()
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // 4. Drawer Footer with Quick Lock
            HorizontalDivider(
                color = activePreset.border.copy(alpha = 0.6f),
                thickness = 0.8.dp
            )

            DrawerFooter(
                activePreset = activePreset,
                onLockVault = {
                    onClose()
                    viewModel.lockVault()
                }
            )
        }
    }
}

@Composable
private fun DrawerHeader(
    activePreset: VaultThemePreset,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = activePreset.primary.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, activePreset.primary.copy(alpha = 0.4f)),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.onelock_icon_1786844769394),
                        contentDescription = "App Icon",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            Column {
                Text(
                    text = "ONELOCK",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = activePreset.textPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF34C759))
                    )
                    Text(
                        text = "AES-256 GCM SECURED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34C759),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("drawer_close_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Menu",
                tint = activePreset.textSecondary
            )
        }
    }
}

@Composable
private fun AmoledShowcaseBanner(activePreset: VaultThemePreset) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF000000),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    Color(0xFF00E5FF).copy(alpha = 0.6f),
                    Color(0xFF9E67FF).copy(alpha = 0.6f),
                    Color(0xFFFF007F).copy(alpha = 0.6f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "AMOLED",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "SUPER AMOLED ADVANTAGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color(0xFF00E5FF)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF161A26),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "0.00% PIXEL POWER",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Pure 0x000000 black turns individual OLED pixels completely OFF for true infinite contrast and maximum battery preservation.",
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
                color = Color(0xFFB0B6CE)
            )
        }
    }
}

@Composable
private fun ThemeFilterPill(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    activePreset: VaultThemePreset
) {
    val bgColor = if (selected) activePreset.primary.copy(alpha = 0.18f) else activePreset.surfaceVariant.copy(alpha = 0.6f)
    val borderColor = if (selected) activePreset.primary else activePreset.border.copy(alpha = 0.5f)
    val textColor = if (selected) activePreset.primary else activePreset.textSecondary

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ThemePresetCard(
    preset: VaultThemePreset,
    isSelected: Boolean,
    activePreset: VaultThemePreset,
    onSelect: () -> Unit
) {
    val cardBorder = if (isSelected) {
        androidx.compose.foundation.BorderStroke(1.5.dp, preset.primary)
    } else {
        androidx.compose.foundation.BorderStroke(0.8.dp, activePreset.border.copy(alpha = 0.5f))
    }

    val cardBg = if (isSelected) {
        activePreset.surfaceVariant.copy(alpha = 0.8f)
    } else {
        activePreset.surfaceVariant.copy(alpha = 0.35f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSelect() }
            .testTag("theme_card_${preset.id}"),
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Theme Name & AMOLED / Style Badge
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = preset.title,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = activePreset.textPrimary
                        )

                        if (preset.isAmoled) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF000000),
                                border = androidx.compose.foundation.BorderStroke(0.6.dp, preset.primary)
                            ) {
                                Text(
                                    text = "OLED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = preset.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = preset.subtitle,
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp,
                        color = activePreset.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Active checkmark or radio indicator
                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = preset.primary,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = if (preset.isDark) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(1.5.dp, activePreset.textTertiary, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Color Palette Swatches Preview Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(preset.background)
                    .border(0.6.dp, preset.border, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Background & Surface chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColorDot(color = preset.background, label = "BG", borderColor = preset.border)
                    ColorDot(color = preset.surface, label = "SURFACE", borderColor = preset.border)
                    ColorDot(color = preset.surfaceVariant, label = "CARD", borderColor = preset.border)
                }

                // Accent colors chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColorDot(color = preset.primary, label = "PRI", glow = true)
                    ColorDot(color = preset.secondary, label = "SEC", glow = true)
                    ColorDot(color = preset.tertiary, label = "TER", glow = true)
                }
            }
        }
    }
}

@Composable
private fun ColorDot(
    color: Color,
    label: String,
    borderColor: Color = Color.Transparent,
    glow: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color)
            .border(0.6.dp, if (borderColor != Color.Transparent) borderColor else Color.White.copy(alpha = 0.3f), CircleShape)
    )
}

@Composable
private fun DrawerNavRow(
    icon: ImageVector,
    title: String,
    badgeText: String,
    badgeColor: Color = activePreset.textSecondary,
    activePreset: VaultThemePreset,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = activePreset.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = activePreset.textPrimary
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = activePreset.surface,
            border = androidx.compose.foundation.BorderStroke(0.6.dp, activePreset.border.copy(alpha = 0.5f))
        ) {
            Text(
                text = badgeText,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = badgeColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun DrawerFooter(
    activePreset: VaultThemePreset,
    onLockVault: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Instant Lock Button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onLockVault() }
                .testTag("drawer_quick_lock_button"),
            shape = RoundedCornerShape(12.dp),
            color = AccentRed.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = AccentRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lock Vault & Clear Memory",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentRed
                )
            }
        }

        Text(
            text = "OneLock Vault • v2.4.0 (Super AMOLED)",
            fontSize = 10.sp,
            color = activePreset.textTertiary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
