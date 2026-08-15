package com.example.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.IntruderLogEntity
import com.example.ui.VaultViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PinChangeDialog(
    viewModel: VaultViewModel,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val cardBg = if (isDark) DarkSurface else LightSurface
    val textColor = if (isDark) Color.White else Color(0xFF131522)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Change Vault PIN",
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = oldPin,
                    onValueChange = { oldPin = it },
                    label = { Text("Current PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = it },
                    label = { Text("New 4-digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it },
                    label = { Text("Confirm New PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFFFF453A),
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!viewModel.securityManager.verifyPin(oldPin)) {
                        errorMessage = "Current PIN is incorrect"
                    } else if (newPin.length < 4) {
                        errorMessage = "New PIN must be at least 4 digits"
                    } else if (newPin != confirmPin) {
                        errorMessage = "New PINs do not match"
                    } else {
                        viewModel.updatePin(newPin)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text("Save PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AccentPurpleLight)
            }
        },
        containerColor = cardBg
    )
}

@Composable
fun IntruderLogsDialog(
    viewModel: VaultViewModel,
    logs: List<IntruderLogEntity>,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val cardBg = if (isDark) DarkSurface else LightSurface
    val textColor = if (isDark) Color.White else Color(0xFF131522)
    val subtitleColor = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Intruder Captures",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                if (logs.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearAllIntruderLogs() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear All",
                            tint = Color(0xFFFF453A)
                        )
                    }
                }
            }
        },
        text = {
            if (logs.isEmpty()) {
                Text("No intruder attempts detected.", color = subtitleColor)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        val dateFormatted = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            .format(Date(log.attemptTimestamp))
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color(0xFF1C1F32) else Color(0xFFE8EAF4)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = File(log.photoPath),
                                    contentDescription = "Intruder Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Failed Attempt",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = textColor
                                    )
                                    Text(
                                        text = dateFormatted,
                                        fontSize = 12.sp,
                                        color = subtitleColor
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteIntruderLog(log.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Log",
                                        tint = Color(0xFFFF453A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AccentPurple)
            }
        },
        containerColor = cardBg
    )
}

@Composable
fun AboutDialog(
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val cardBg = if (isDark) DarkSurface else LightSurface
    val textColor = if (isDark) Color.White else Color(0xFF131522)
    val subtitleColor = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.app_vault_icon),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "HIDEANDSEEK",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "A private vault space to keep photos, videos, and apps secure with AES-256 GCM encryption, biometric unlock, intruder selfie alerts, and live alias disguise modes.",
                    color = subtitleColor,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Version 1.0.0",
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    fontSize = 13.sp
                )
                Text(
                    text = "Made with ❤️ by Rahul Shah",
                    fontWeight = FontWeight.Bold,
                    color = AccentPurpleLight,
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = AccentPurple)
            }
        },
        containerColor = cardBg
    )
}

@Composable
fun AdminInfoDialog(
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val cardBg = if (isDark) DarkSurface else LightSurface
    val textColor = if (isDark) Color.White else Color(0xFF131522)
    val subtitleColor = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Vault Protection Status",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        },
        text = {
            Text(
                text = "Device Admin Protection adds friction against unauthorized uninstallation. On Android devices, this requires disabling administrator rights in System Settings before removing the vault.",
                color = subtitleColor,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = AccentPurple)
            }
        },
        containerColor = cardBg
    )
}
