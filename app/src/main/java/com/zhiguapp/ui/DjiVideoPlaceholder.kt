package com.zhiguapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zhiguapp.ui.theme.CyanGlow
import com.zhiguapp.ui.theme.InkBlue
import com.zhiguapp.ui.theme.OceanBlue

@Composable
fun DjiVideoPlaceholder(
    state: ZhiGuUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(InkBlue.copy(alpha = 0.98f), OceanBlue.copy(alpha = 0.92f), CyanGlow.copy(alpha = 0.88f))
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "实时图传区",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    if (state.djiConnected) "真机已连接" else "演示占位画面",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    "相机 ${if (state.djiAvailableCameras.isEmpty()) "--" else state.djiAvailableCameras.joinToString()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f)
                )
            }
        }
    }
}
