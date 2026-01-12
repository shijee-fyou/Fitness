package com.example.fitness_demo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitness_demo.data.AppRepository
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartSessionScreen(
    repository: AppRepository,
    onBack: () -> Unit,
    onSessionStarted: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val haptics = LocalHapticFeedback.current
    var note by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    val groups = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Full Body")
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("开始训练") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(com.example.fitness_demo.ui.theme.Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("快速开始", modifier = Modifier.padding(bottom = 4.dp))
            ElevatedCard(
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = com.example.fitness_demo.ui.theme.Dimens.CardElevationMed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(com.example.fitness_demo.ui.theme.Dimens.CardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("选择目标肌群（可选）")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        groups.forEach { g ->
                            FilterChip(selected = selectedGroup == g, onClick = {
                                selectedGroup = if (selectedGroup == g) null else g
                            }, label = { Text(g) })
                        }
                    }
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注（可选）") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        scope.launch {
                            val session = repository.startNewSession(note = if (note.isBlank()) null else note)
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSessionStarted(session.id)
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("开始空白训练")
                    }
                }
            }
            Text("推荐模板（占位，后续可自定义）", modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ElevatedCard(onClick = {
                    scope.launch {
                        val session = repository.startNewSession(note = "Push/Pull/Legs - ${selectedGroup ?: "General"}")
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSessionStarted(session.id)
                    }
                }, elevation = CardDefaults.elevatedCardElevation(com.example.fitness_demo.ui.theme.Dimens.CardElevationLow), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(com.example.fitness_demo.ui.theme.Dimens.CardPadding)) { Text("PPL 快速开始") }
                }
                ElevatedCard(onClick = {
                    scope.launch {
                        val session = repository.startNewSession(note = "Full Body - ${selectedGroup ?: "General"}")
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSessionStarted(session.id)
                    }
                }, elevation = CardDefaults.elevatedCardElevation(com.example.fitness_demo.ui.theme.Dimens.CardElevationLow), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(com.example.fitness_demo.ui.theme.Dimens.CardPadding)) { Text("全身 快速开始") }
                }
            }
        }
    }
}

