package com.example.ui.dialogs

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface

@Composable
fun PermissionsAccessDialog(
    isDark: Boolean = true,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Check permission helper
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
            } else true
        )
    }

    var hasMediaPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isPermissionGranted(Manifest.permission.READ_MEDIA_IMAGES) &&
                isPermissionGranted(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        )
    }

    var hasCameraPermission by remember {
        mutableStateOf(isPermissionGranted(Manifest.permission.CAMERA))
    }

    // Permission launcher
    val multiplePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = results[Manifest.permission.POST_NOTIFICATIONS] ?: isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
            hasMediaPermission = (results[Manifest.permission.READ_MEDIA_IMAGES] ?: isPermissionGranted(Manifest.permission.READ_MEDIA_IMAGES)) &&
                                 (results[Manifest.permission.READ_MEDIA_VIDEO] ?: isPermissionGranted(Manifest.permission.READ_MEDIA_VIDEO))
        } else {
            hasNotificationPermission = true
            hasMediaPermission = results[Manifest.permission.READ_EXTERNAL_STORAGE] ?: isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        hasCameraPermission = results[Manifest.permission.CAMERA] ?: isPermissionGranted(Manifest.permission.CAMERA)
    }

    val requestAllPermissions = {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        permissionsToRequest.add(Manifest.permission.CAMERA)
        multiplePermissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    val allGranted = hasNotificationPermission && hasMediaPermission && hasCameraPermission
    val dialogBg = if (isDark) DarkSurface else LightSurface
    val textColor = if (isDark) Color.White else Color(0xFF131522)
    val subtitleColor = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("permissions_access_dialog"),
        shape = RoundedCornerShape(28.dp),
        containerColor = dialogBg,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(AccentPurple, AccentCyan))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "System Permissions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Full Access Security Hub",
                        fontSize = 12.sp,
                        color = AccentCyan
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ONELOCK requires system privileges to secure your media vault, take intruder snapshots, and send security alerts.",
                    fontSize = 13.sp,
                    color = subtitleColor,
                    lineHeight = 18.sp
                )

                // 1. Notifications Permission
                PermissionCardItem(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    description = "Instant break-in alerts & vault status updates",
                    isGranted = hasNotificationPermission,
                    isDark = isDark,
                    onGrantClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            multiplePermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                        }
                    }
                )

                // 2. Gallery & Media Access
                PermissionCardItem(
                    icon = Icons.Default.PhotoLibrary,
                    title = "Full Gallery & Media Access",
                    description = "Encrypt, hide, and restore photos & videos safely",
                    isGranted = hasMediaPermission,
                    isDark = isDark,
                    onGrantClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            multiplePermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_MEDIA_IMAGES,
                                    Manifest.permission.READ_MEDIA_VIDEO,
                                    Manifest.permission.READ_MEDIA_AUDIO
                                )
                            )
                        } else {
                            multiplePermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                            )
                        }
                    }
                )

                // 3. Camera Access
                PermissionCardItem(
                    icon = Icons.Default.CameraAlt,
                    title = "Camera (Intruder Catch)",
                    description = "Silent front-facing selfie capture on wrong PIN",
                    isGranted = hasCameraPermission,
                    isDark = isDark,
                    onGrantClick = {
                        multiplePermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    }
                )

                // 4. Biometric & Face Lock Note
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF141724) else Color(0xFFECEFF8)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = AccentPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Native Android Biometrics & Face Unlock are active and hardware-secured.",
                            fontSize = 12.sp,
                            color = subtitleColor
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!allGranted) {
                Button(
                    onClick = requestAllPermissions,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("grant_all_permissions_button")
                ) {
                    Text("Grant All Permissions", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("All Granted (Done)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = subtitleColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Settings", color = subtitleColor)
                }
                TextButton(onClick = onDismiss) {
                    Text("Close", color = subtitleColor)
                }
            }
        }
    )
}

@Composable
fun PermissionCardItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    isDark: Boolean,
    onGrantClick: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF161928) else Color(0xFFF1F3FA)
    val textColor = if (isDark) Color.White else Color(0xFF131522)
    val subtitleColor = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) Color(0xFF10B981).copy(alpha = 0.3f) else (if (isDark) DarkBorder else LightBorder)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isGranted) Color(0xFF10B981).copy(alpha = 0.15f)
                            else AccentPurple.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) Color(0xFF10B981) else AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = subtitleColor,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            } else {
                Button(
                    onClick = onGrantClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Allow", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
