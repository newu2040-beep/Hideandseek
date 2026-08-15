package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface

@Composable
fun FrostedCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    isDark: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val backgroundColor = if (isDark) DarkSurface else LightSurface
    val borderColor = if (isDark) DarkBorder else LightBorder

    val cardModifier = modifier
        .clip(shape)
        .background(backgroundColor)
        .border(1.dp, borderColor.copy(alpha = 0.6f), shape)
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        )

    Box(modifier = cardModifier) {
        content()
    }
}

@Composable
fun GlowingBiometricRing(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_anim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(),
        label = "pressScale"
    )

    Box(
        modifier = modifier
            .size(170.dp)
            .scale(pulseScale * pressScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("biometric_scanner_ring"),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing gradient ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ringBrush = Brush.sweepGradient(
                colors = listOf(
                    AccentPurple,
                    AccentCyan,
                    AccentPurpleLight,
                    AccentCyan,
                    AccentPurple
                ),
                center = Offset(size.width / 2, size.height / 2)
            )

            // Outer soft glow
            drawCircle(
                brush = ringBrush,
                radius = size.minDimension / 2 - 8.dp.toPx(),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                alpha = if (isDark) 0.85f else 0.6f
            )

            // Inner orbit ring
            drawCircle(
                color = if (isDark) Color(0xFF2A2E44) else Color(0xFFD8DCED),
                radius = size.minDimension / 2 - 18.dp.toPx(),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Inner dark circular core
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = if (isDark) Color(0xFF141624) else Color(0xFFFFFFFF),
            shadowElevation = if (isDark) 0.dp else 6.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        1.5.dp,
                        if (isDark) Color(0xFF323754) else Color(0xFFE4E7F2),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometric Scanner",
                    modifier = Modifier.size(68.dp),
                    tint = if (isDark) AccentCyan else AccentPurple
                )
            }
        }
    }
}

@Composable
fun PinDotsRow(
    enteredPinLength: Int,
    maxDigits: Int = 4,
    hasError: Boolean = false,
    isDark: Boolean = true
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(hasError) {
        if (hasError) {
            shakeOffset.animateTo(
                targetValue = 12f,
                animationSpec = tween(60, easing = LinearEasing)
            )
            shakeOffset.animateTo(
                targetValue = -12f,
                animationSpec = tween(60, easing = LinearEasing)
            )
            shakeOffset.animateTo(
                targetValue = 6f,
                animationSpec = tween(60, easing = LinearEasing)
            )
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(60, easing = LinearEasing)
            )
        }
    }

    Row(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .drawBehind {
                // Shake transform via translation
            },
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until maxDigits) {
            val isFilled = i < enteredPinLength
            val dotColor = when {
                hasError -> MaterialTheme.colorScheme.error
                isFilled -> AccentPurpleLight
                isDark -> Color(0xFF2E334D)
                else -> Color(0xFFD4D8E8)
            }

            val scale by animateFloatAsState(
                targetValue = if (isFilled) 1.25f else 1f,
                animationSpec = spring(),
                label = "dot_scale"
            )

            Box(
                modifier = Modifier
                    .size(14.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(dotColor)
                    .border(
                        1.dp,
                        if (isFilled) AccentPurple else Color.Transparent,
                        CircleShape
                    )
            )
        }
    }
}

@Composable
fun PinKeypad(
    isDark: Boolean = true,
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onBiometricClick: () -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("BIO", "0", "DEL")
    )

    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    when (key) {
                        "BIO" -> {
                            PinKeyButton(
                                isDark = isDark,
                                onClick = onBiometricClick
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Use Biometrics",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        "DEL" -> {
                            PinKeyButton(
                                isDark = isDark,
                                onClick = onBackspaceClick
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Backspace",
                                    tint = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF2C2D3A),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        else -> {
                            PinKeyButton(
                                isDark = isDark,
                                onClick = { onDigitClick(key) }
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Color.White else Color(0xFF131522)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinKeyButton(
    isDark: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(),
        label = "keyPress"
    )

    val buttonBg = if (isDark) {
        if (isPressed) Color(0xFF2B304A) else Color(0xFF191B29)
    } else {
        if (isPressed) Color(0xFFE2E5F2) else Color(0xFFFFFFFF)
    }

    val buttonBorder = if (isDark) Color(0xFF2C324B) else Color(0xFFE0E3EE)

    Surface(
        modifier = Modifier
            .size(72.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = CircleShape,
        color = buttonBg,
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, buttonBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun PillTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    isDark: Boolean = true
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(if (isDark) Color(0xFF161826) else Color(0xFFE9EBF4))
            .border(1.dp, if (isDark) Color(0xFF2A2E44) else Color(0xFFDCE0ED), RoundedCornerShape(30.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            val tabBg = if (isSelected) {
                Brush.horizontalGradient(listOf(AccentPurple, Color(0xFF5B2EEB)))
            } else {
                Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(26.dp))
                    .background(tabBg)
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 22.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)
                )
            }
        }
    }
}

@Composable
fun IosStyleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = AccentPurple,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFF393E58),
            uncheckedBorderColor = Color.Transparent
        )
    )
}
