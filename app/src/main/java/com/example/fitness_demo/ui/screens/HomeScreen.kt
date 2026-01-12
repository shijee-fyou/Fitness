package com.example.fitness_demo.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitness_demo.ui.theme.Dimens

@Composable
fun HomeScreen(
    onStartSessionClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { show = true }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "开始你的训练",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = "记录每一次进步，保持持续增肌与减脂。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AnimatedVisibility(visible = show, enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 })) {
                ElevatedCard(
                    onClick = onStartSessionClick,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = Dimens.CardElevationMed)
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.CardPadding),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Column {
                            Text("开始训练", style = MaterialTheme.typography.titleMedium)
                            Text("创建新会话并记录组数", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            AnimatedVisibility(visible = show, enter = fadeIn() + slideInVertically(initialOffsetY = { it / 8 })) {
                ElevatedCard(
                    onClick = onHistoryClick,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = Dimens.CardElevationMed)
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.CardPadding),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Column {
                            Text("训练记录", style = MaterialTheme.typography.titleMedium)
                            Text("回顾统计与历史", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

