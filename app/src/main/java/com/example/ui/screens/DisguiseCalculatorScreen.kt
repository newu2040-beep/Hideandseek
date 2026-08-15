package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VaultViewModel

@Composable
fun DisguiseCalculatorScreen(
    viewModel: VaultViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val rows = listOf(
        listOf("C", "±", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "=")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        // Display Text
        Text(
            text = uiState.calculatorExpression,
            color = Color.White,
            fontSize = 64.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Calculator Buttons Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { btnText ->
                        val isOrange = btnText in listOf("÷", "×", "-", "+", "=")
                        val isTopGray = btnText in listOf("C", "±", "%")
                        val btnBg = when {
                            isOrange -> Color(0xFFFF9F0A)
                            isTopGray -> Color(0xFFA5A5A5)
                            else -> Color(0xFF333333)
                        }
                        val btnTextColor = if (isTopGray) Color.Black else Color.White

                        val isZero = btnText == "0"
                        val btnModifier = if (isZero) {
                            Modifier
                                .size(width = 165.dp, height = 75.dp)
                                .clip(CircleShape)
                        } else {
                            Modifier
                                .size(75.dp)
                                .clip(CircleShape)
                        }

                        Surface(
                            modifier = btnModifier.clickable { viewModel.onCalculatorKey(btnText) },
                            color = btnBg,
                            shape = CircleShape
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = if (isZero) Alignment.CenterStart else Alignment.Center
                            ) {
                                Text(
                                    text = btnText,
                                    color = btnTextColor,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Normal,
                                    modifier = if (isZero) Modifier.padding(start = 28.dp) else Modifier
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
