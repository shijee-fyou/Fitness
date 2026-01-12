package com.example.fitness_demo.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import com.example.fitness_demo.data.AppRepository
import com.example.fitness_demo.data.SetEntry
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitness_demo.ui.viewmodel.SessionViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: Int,
    repository: AppRepository,
    onBack: () -> Unit
) {
    val vm: SessionViewModel = viewModel(factory = SessionViewModel.factory(repository, sessionId))
    val sets by vm.sets.collectAsState()
    val exercises by vm.exercises.collectAsState()
    val repsText by vm.repsText.collectAsState()
    val weightText by vm.weightText.collectAsState()
    val selectedExerciseId by vm.selectedExerciseId.collectAsState()
    val haptics = LocalHapticFeedback.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val restSeconds by vm.restSeconds.collectAsState()
    var pickerOpen by remember { mutableStateOf(false) } // exercise picker
    var query by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    var repsPickerOpen by remember { mutableStateOf(false) }
    var weightPickerOpen by remember { mutableStateOf(false) }
    var customReps by remember { mutableStateOf("") }
    var customWeight by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("训练内容 #$sessionId") },
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
                .padding(16.dp)
        ) {
            if (restSeconds != null) {
                ElevatedCard(
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val total by vm.restTotalSeconds.collectAsState()
                        val remaining = restSeconds ?: 0
                        val fractionTarget = if (total != null && (total ?: 0) > 0) {
                            (remaining.toFloat() / (total ?: 1)).coerceIn(0f, 1f)
                        } else 0f
                        val fraction by animateFloatAsState(targetValue = fractionTarget, label = "restProgress")

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            val progressColor = MaterialTheme.colorScheme.primary
                            Canvas(modifier = Modifier
                                .padding(end = 0.dp)
                                .let { it }
                                .then(Modifier)
                                .size(24.dp)) {
                                // 背景轨道
                                drawArc(
                                    color = trackColor,
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 4f)
                                )
                                // 进度
                                drawArc(
                                    color = progressColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f * fraction,
                                    useCenter = false,
                                    style = Stroke(width = 4f)
                                )
                            }
                            Text(text = "休息 ${remaining}s")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AssistChip(onClick = { vm.addRest(30) }, label = { Text("+30s") })
                            SuggestionChip(onClick = { vm.stopRest() }, label = { Text("结束") })
                        }
                    }
                }
            }

            val currentName = exercises.firstOrNull { it.id == selectedExerciseId }?.name ?: "选择练习"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(currentName)
                TextButton(onClick = { pickerOpen = true }) { Text("选择") }
            }
            if (pickerOpen) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(onDismissRequest = { pickerOpen = false }, sheetState = sheetState) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { q -> query = q },
                            label = { Text("搜索动作") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val groups = listOf("All", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Full Body", "Other")
                            groups.forEach { g ->
                                val sel = if (g == "All") null else g
                                FilterChip(selected = (selectedGroup == sel), onClick = { selectedGroup = sel }, label = { Text(g) })
                            }
                        }
                        val filtered = exercises.filter { ex ->
                            (selectedGroup == null || ex.muscleGroup.equals(selectedGroup, true)) &&
                                    (query.isBlank() || ex.name.contains(query, true))
                        }
                        LazyColumn {
                            items(filtered) { ex ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(ex.name)
                                    TextButton(onClick = {
                                        vm.selectExercise(ex.id)
                                        pickerOpen = false
                                    }) { Text("选择") }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = repsText,
                    onValueChange = vm::setRepsText,
                    label = { Text("次数") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { repsPickerOpen = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "选择次数")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { repsPickerOpen = true }
                )
                OutlinedTextField(
                    value = weightText,
                    onValueChange = vm::setWeightText,
                    label = { Text("重量(kg)") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { weightPickerOpen = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "选择重量")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { weightPickerOpen = true }
                )
                Button(
                    onClick = {
                        vm.addSet()
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    enabled = selectedExerciseId != null &&
                        (repsText.toIntOrNull() != null) &&
                        (weightText.isBlank() || weightText.toFloatOrNull() != null)
                ) { Text("添加") }
            }

            // 次数选择器底部弹层
            if (repsPickerOpen) {
                val rSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(onDismissRequest = { repsPickerOpen = false }, sheetState = rSheet) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("选择次数", style = MaterialTheme.typography.titleMedium)
                        val rOptions = listOf(5, 6, 8, 10, 12, 15, 20)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rOptions.forEach { n ->
                                AssistChip(onClick = {
                                    vm.setRepsText(n.toString())
                                    repsPickerOpen = false
                                }, label = { Text("$n") })
                            }
                        }
                        OutlinedTextField(
                            value = customReps,
                            onValueChange = { customReps = it },
                            label = { Text("自定义次数") }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { repsPickerOpen = false }) { Text("取消") }
                            Button(enabled = customReps.toIntOrNull() != null, onClick = {
                                vm.setRepsText(customReps)
                                repsPickerOpen = false
                                customReps = ""
                            }) { Text("确定") }
                        }
                    }
                }
            }

            // 重量选择器底部弹层
            if (weightPickerOpen) {
                val wSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(onDismissRequest = { weightPickerOpen = false }, sheetState = wSheet) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("选择重量(kg)", style = MaterialTheme.typography.titleMedium)
                        val wOptions = listOf("自重","2.5","5","7.5","10","12.5","15","20","25","30","40","50")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            wOptions.forEach { w ->
                                AssistChip(onClick = {
                                    if (w == "自重") vm.setWeightText("") else vm.setWeightText(w)
                                    weightPickerOpen = false
                                }, label = { Text(w) })
                            }
                        }
                        OutlinedTextField(
                            value = customWeight,
                            onValueChange = { customWeight = it },
                            label = { Text("自定义重量(kg)") }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { weightPickerOpen = false }) { Text("取消") }
                            Button(enabled = customWeight.toFloatOrNull() != null, onClick = {
                                vm.setWeightText(customWeight)
                                weightPickerOpen = false
                                customWeight = ""
                            }) { Text("确定") }
                        }
                    }
                }
            }
            HorizontalDivider()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
            ) {
                items(sets) { set ->
                    SetRow(set, onDelete = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        vm.deleteSet(set.id)
                    })
                }
            }
        }
    }
}

@Composable
private fun SetRow(set: SetEntry, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "组 ${set.setNumber}")
            Text(text = "次数 ${set.reps}，" + (set.weightKg?.let { "重量 ${it}kg" } ?: "自重"))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
    HorizontalDivider()
}


