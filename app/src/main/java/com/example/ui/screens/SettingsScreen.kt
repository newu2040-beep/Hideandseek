package com.example.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.security.VaultDeviceAdminReceiver
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

import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ColorLens
import com.example.ui.theme.VaultThemePreset

@Composable
fun SettingsScreen(
    viewModel: VaultViewModel,
    activePreset: VaultThemePreset = VaultThemePreset.SUPER_AMOLED_BLACK,
    isDark: Boolean = activePreset.isDark,
    onOpenThemeDrawer: () -> Unit = {},
    onNavigateToDisguisePicker: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Device Admin Activation Launcher
    val deviceAdminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, VaultDeviceAdminReceiver::class.java)
        viewModel.securityManager.isDeviceAdminFrictionEnabled = dpm.isAdminActive(adminComponent)
    }

    val bgColor = activePreset.background
    val cardBg = activePreset.surface
    val borderColor = activePreset.border
    val textColor = activePreset.textPrimary
    val subtitleColor = activePreset.textSecondary
    val sectionHeaderColor = activePreset.textTertiary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(scrollState)
        ) {
            // 1. Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.testTag("settings_header")
                )

                IconButton(
                    onClick = { viewModel.lockVault() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Vault",
                        tint = textColor
                    )
                }
            }

            // 2. SECURITY SECTION
            Text(
                text = stringResource(R.string.security_section),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = sectionHeaderColor,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 4.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                shadowElevation = if (isDark) 0.dp else 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                ) {
                    // Biometric Lock Toggle
                    SettingsToggleRow(
                        icon = Icons.Default.Fingerprint,
                        title = stringResource(R.string.biometric_lock),
                        subtitle = stringResource(R.string.biometric_lock_desc),
                        checked = uiState.isBiometricEnabled,
                        onCheckedChange = { viewModel.setBiometricEnabled(it) },
                        isDark = isDark
                    )

                    HorizontalDivider(color = borderColor.copy(alpha = 0.4f), thickness = 0.6.dp)

                    // Change PIN Row
                    SettingsActionRow(
                        icon = Icons.Default.Dialpad,
                        title = stringResource(R.string.change_pin),
                        subtitle = stringResource(R.string.change_pin_desc),
                        isDark = isDark,
                        onClick = { viewModel.setShowPinChangeDialog(true) }
                    )

                    HorizontalDivider(color = borderColor.copy(alpha = 0.4f), thickness = 0.6.dp)

                    // Intruder Selfie Toggle
                    SettingsToggleRow(
                        icon = Icons.Default.CameraAlt,
                        title = stringResource(R.string.intruder_selfie),
                        subtitle = stringResource(R.string.intruder_selfie_desc),
                        checked = uiState.isIntruderSelfieEnabled,
                        onCheckedChange = { viewModel.setIntruderSelfieEnabled(it) },
                        isDark = isDark
                    )

                    // Intruder Logs Row if any exist
                    if (uiState.intruderLogs.isNotEmpty()) {
                        HorizontalDivider(color = borderColor.copy(alpha = 0.4f), thickness = 0.6.dp)
                        SettingsActionRow(
                            icon = Icons.Default.Visibility,
                            title = "Intruder Captures (${uiState.intruderLogs.size})",
                            subtitle = "View photos of unauthorized unlock attempts",
                            isDark = isDark,
                            onClick = { viewModel.setShowIntruderLogs(true) }
                        )
                    }

                    HorizontalDivider(color = borderColor.copy(alpha = 0.4f), thickness = 0.6.dp)

                    // Prevent Uninstall (Device Admin Friction)
                    SettingsToggleRow(
                        icon = Icons.Default.Shield,
                        title = "Prevent Quick Uninstall",
                        subtitle = "Device Admin friction to prevent accidental vault removal",
                        checked = uiState.isDeviceAdminEnabled,
                        onCheckedChange = { enable ->
                            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                            val adminComponent = ComponentName(context, VaultDeviceAdminReceiver::class.java)
                            if (enable && !dpm.isAdminActive(adminComponent)) {
                                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                                    putExtra(
                                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                        "Activate ONELOCK protection to require an admin step before removing the vault."
                                    )
                                }
                                deviceAdminLauncher.launch(intent)
                            } else if (!enable && dpm.isAdminActive(adminComponent)) {
                                dpm.removeActiveAdmin(adminComponent)
                                viewModel.securityManager.isDeviceAdminFrictionEnabled = false
                            }
                        },
                        isDark = isDark
                    )

                    HorizontalDivider(color = borderColor.copy(alpha = 0.4f), thickness = 0.6.dp)

                    // System Permissions & Full Access Hub
                    SettingsActionRow(
                        icon = Icons.Default.Shield,
                        title = "System Permissions & Access",
                        subtitle = "Push notifications, Full Gallery access & Camera status",
                        isDark = isDark,
                        onClick = { viewModel.setShowPermissionsDialog(true) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. CUSTOMIZATION SECTION
            Text(
                text = stringResource(R.string.customization_section),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = sectionHeaderColor,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                shadowElevation = if (isDark) 0.dp else 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                ) {
                    // Disguise Mode Row
                    SettingsActionRow(
                        icon = Icons.Default.Visibility,
                        title = stringResource(R.string.disguise_mode),
                        subtitle = "Currently: ${uiState.activeDisguise?.name ?: "ONELOCK"}",
                        isDark = isDark,
                        onClick = onNavigateToDisguisePicker
                    )

                    HorizontalDivider(color = borderColor.copy(alpha = 0.4f), thickness = 0.6.dp)

                    // Themes & Super AMOLED Studio Action Row
                    SettingsActionRow(
                        icon = Icons.Default.ColorLens,
                        title = "Modern & Super AMOLED Themes",
                        subtitle = "${activePreset.title} • ${activePreset.accentBadge}",
                        isDark = isDark,
                        onClick = onOpenThemeDrawer
                    )

                    HorizontalDivider(color = borderColor.copy(alpha = 0.4f), thickness = 0.6.dp)

                    // Quick Theme Selector Swatch Pills
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quick Switcher",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = subtitleColor
                            )
                            Text(
                                text = "Open menu for all 9 themes",
                                fontSize = 11.sp,
                                color = activePreset.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { onOpenThemeDrawer() }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VaultThemePreset.entries.forEach { preset ->
                                val isSelected = preset.id == activePreset.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) preset.primary.copy(alpha = 0.18f) else cardBg,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 0.8.dp,
                                        color = if (isSelected) preset.primary else borderColor.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.setThemeMode(preset.id)
                                            viewModel.setToastMessage("Applied ${preset.title}")
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(preset.primary)
                                        )
                                        Text(
                                            text = if (preset.isAmoled) "⚡ ${preset.title.removePrefix("Super AMOLED ")}" else preset.title.split(" ").first(),
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) preset.primary else textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. ABOUT SECTION
            Text(
                text = stringResource(R.string.about_section),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = sectionHeaderColor,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                shadowElevation = if (isDark) 0.dp else 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                ) {
                    SettingsActionRow(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.about_app),
                        subtitle = stringResource(R.string.app_version),
                        isDark = isDark,
                        onClick = { viewModel.setShowAboutDialog(true) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // 5. FOOTER
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.credits),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = subtitleColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Encrypted Scoped Vault • Self-Disguise Engine",
                    fontSize = 11.sp,
                    color = subtitleColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDark: Boolean
) {
    val textColor = if (isDark) Color.White else Color(0xFF131522)
    val subtitleColor = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = AccentPurpleLight,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = subtitleColor
                )
            }
        }

        IosStyleSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isDark) Color.White else Color(0xFF131522)
    val subtitleColor = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = AccentPurpleLight,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = subtitleColor
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = subtitleColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
