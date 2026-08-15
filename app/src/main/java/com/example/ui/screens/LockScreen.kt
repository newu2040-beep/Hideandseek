package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.security.BiometricHelper
import com.example.security.IntruderSelfieCapture
import com.example.ui.VaultViewModel
import com.example.ui.components.GlowingBiometricRing
import com.example.ui.components.PinDotsRow
import com.example.ui.components.PinKeypad
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleLight

@Composable
fun LockScreen(
    viewModel: VaultViewModel,
    isDark: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Trigger biometric on first launch if enabled
    LaunchedEffect(Unit) {
        if (uiState.isBiometricEnabled && !uiState.isPinMode) {
            val activity = context as? FragmentActivity
            activity?.let {
                val biometricHelper = BiometricHelper(it)
                if (biometricHelper.isBiometricAvailable()) {
                    biometricHelper.showBiometricPrompt(
                        title = "Unlock HIDEANDSEEK",
                        subtitle = "Verify with Fingerprint or Face Unlock",
                        description = "Touch the fingerprint sensor or look at screen for Face Unlock",
                        negativeButtonText = "Use App PIN",
                        onSuccess = { viewModel.unlockSuccess() },
                        onError = { errorCode, _ ->
                            if (errorCode != androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED &&
                                errorCode != androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                viewModel.togglePinOrBiometricMode(true)
                            }
                        },
                        onFailed = {
                            viewModel.unlockFailed()
                            if (uiState.isIntruderSelfieEnabled && uiState.failedAttempts >= 2) {
                                IntruderSelfieCapture(context).captureIntruderSelfie(
                                    lifecycleOwner,
                                    attemptedPin = "Biometric Failed"
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    // Trigger intruder selfie on multiple failed PIN attempts
    LaunchedEffect(uiState.pinError) {
        if (uiState.pinError && uiState.isIntruderSelfieEnabled && uiState.failedAttempts >= 2) {
            IntruderSelfieCapture(context).captureIntruderSelfie(
                lifecycleOwner,
                attemptedPin = "Wrong PIN #${uiState.failedAttempts}"
            )
        }
    }

    val preset = com.example.ui.theme.VaultThemePreset.fromId(uiState.themeMode)

    val bgModifier = if (preset.isAmoled) {
        Modifier.background(Color(0xFF000000))
    } else if (isDark) {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    preset.background,
                    preset.surface,
                    Color(0xFF05060A)
                )
            )
        )
    } else {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    preset.background,
                    preset.surfaceVariant,
                    Color(0xFFE4E7F2)
                )
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(bgModifier)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Brand Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Glowing Hexagon Vault Emblem
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(AccentPurple, Color(0xFF5B2EEB))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Vault Emblem",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "HIDEANDSEEK",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color = if (isDark) Color.White else Color(0xFF131522)
                )
            }

            // 2. Middle Content (Biometric Ring or PIN Keypad)
            if (!uiState.isPinMode) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    GlowingBiometricRing(
                        isDark = isDark,
                        onClick = {
                            val activity = context as? FragmentActivity
                            activity?.let {
                                val biometricHelper = BiometricHelper(it)
                                val status = biometricHelper.checkBiometricStatus()
                                if (status == BiometricHelper.BiometricStatus.AVAILABLE) {
                                    biometricHelper.showBiometricPrompt(
                                        title = "Unlock HIDEANDSEEK",
                                        subtitle = "Verify with Fingerprint or Face Unlock",
                                        description = "Touch fingerprint sensor or look at screen for Face Unlock",
                                        negativeButtonText = "Use App PIN",
                                        onSuccess = { viewModel.unlockSuccess() },
                                        onError = { errorCode, _ ->
                                            if (errorCode != androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED &&
                                                errorCode != androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                                viewModel.togglePinOrBiometricMode(true)
                                            }
                                        },
                                        onFailed = { viewModel.unlockFailed() }
                                    )
                                } else if (status == BiometricHelper.BiometricStatus.NONE_ENROLLED) {
                                    viewModel.setToastMessage("No biometrics enrolled. Opening system settings...")
                                    biometricHelper.openEnrollmentSettings()
                                    viewModel.togglePinOrBiometricMode(true)
                                } else {
                                    viewModel.setToastMessage("Biometrics unavailable. Please use PIN.")
                                    viewModel.togglePinOrBiometricMode(true)
                                }
                            } ?: viewModel.unlockSuccess()
                        }
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = stringResource(R.string.lock_screen_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Animated PIN Dots
                    PinDotsRow(
                        enteredPinLength = uiState.enteredPin.length,
                        maxDigits = 4,
                        hasError = uiState.pinError,
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    TextButton(
                        onClick = { viewModel.togglePinOrBiometricMode(true) },
                        modifier = Modifier.testTag("use_pin_button")
                    ) {
                        Text(
                            text = stringResource(R.string.use_pin_instead),
                            color = AccentPurpleLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Enter Vault PIN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF131522)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (uiState.pinError) "Incorrect PIN, try again" else "Default PIN is 1234",
                        fontSize = 13.sp,
                        color = if (uiState.pinError) MaterialTheme.colorScheme.error else if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)
                    )

                    PinDotsRow(
                        enteredPinLength = uiState.enteredPin.length,
                        maxDigits = 4,
                        hasError = uiState.pinError,
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PinKeypad(
                        isDark = isDark,
                        onDigitClick = { digit -> viewModel.onPinDigitEntered(digit) },
                        onBackspaceClick = { viewModel.onPinBackspace() },
                        onBiometricClick = { viewModel.togglePinOrBiometricMode(false) }
                    )
                }
            }

            // 3. Bottom Utility Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Device Admin / Protection Indicator
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF161826) else Color(0xFFE4E7F2))
                        .clickable { viewModel.setShowAdminInfoDialog(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Device Protection",
                        tint = if (uiState.isDeviceAdminEnabled) AccentCyan else if (isDark) Color(0xFF656A88) else Color(0xFF9498AC),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // PIN / Biometric Switcher Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF161826) else Color(0xFFE4E7F2))
                        .clickable { viewModel.togglePinOrBiometricMode(!uiState.isPinMode) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isPinMode) Icons.Default.Fingerprint else Icons.Default.Dialpad,
                        contentDescription = "Switch Input Mode",
                        tint = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF2C2D3A),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
