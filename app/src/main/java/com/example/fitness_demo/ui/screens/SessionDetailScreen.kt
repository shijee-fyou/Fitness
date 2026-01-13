package com.example.fitness_demo.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitness_demo.ui.viewmodel.SessionViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.FileProvider
import androidx.compose.ui.graphics.toArgb
import kotlin.math.roundToInt
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.rememberDismissState
import androidx.compose.material.ExperimentalMaterialApi
import com.example.fitness_demo.ui.util.Localization

import com.example.fitness_demo.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
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
    var summaryOpen by remember { mutableStateOf(false) }

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
                actions = {
                    TextButton(onClick = {
                        vm.completeSession {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            summaryOpen = true
                        }
                    }) {
                        Text("完成")
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

            val currentName = exercises.firstOrNull { it.id == selectedExerciseId }?.name?.let { Localization.exercise(it) } ?: "选择训练"
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
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val groups = listOf("All", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Full Body", "Other")
                            items(groups) { g ->
                                val sel = if (g == "All") null else g
                                FilterChip(selected = (selectedGroup == sel), onClick = { selectedGroup = sel }, label = { Text(groupLabel(g)) })
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
                                    Text(Localization.exercise(ex.name))
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
                // 建议区：上次最佳/上次平均/趋势
                val selectedId by vm.selectedExerciseId.collectAsState()
                if (selectedId != null) {
                    val scope = rememberCoroutineScope()
                    var bestText by remember { mutableStateOf<String?>(null) }
                    var avgText by remember { mutableStateOf<String?>(null) }
                    LaunchedEffect(selectedId) {
                        val (best, avg) = vm.bestAndAverageForExercise(selectedId!!)
                        bestText = best?.let { "最佳 ${((it.weightKg ?: 0f) * it.reps).toInt()}kg·rep" }
                        avgText = avg?.let { (aw, ar) -> "平均 ${aw.toInt()}kg × ${ar.toInt()}" }
                    }
                    val (trendReps, trendW) = vm.trendSuggestionForCurrent()
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        bestText?.let { AssistChip(onClick = {
                            bestText = null
                        }, label = { Text(it) }) }
                        avgText?.let { AssistChip(onClick = {
                            avgText = null
                        }, label = { Text(it) }) }
                        if (trendReps != null || trendW != null) {
                            SuggestionChip(onClick = {
                                trendReps?.let { r -> vm.setRepsText((vm.repsText.value.toIntOrNull() ?: 0 + r).toString()) }
                                trendW?.let { dw ->
                                    val base = vm.weightText.value.toFloatOrNull() ?: 0f
                                    vm.setWeightText((base + dw).toString())
                                }
                            }, label = { Text("趋势${trendReps?.let { (if (it>=0) " +$it 次" else " $it 次") } ?: ""}${trendW?.let { if (it>=0) " +${"%.1f".format(it)}kg" else " ${"%.1f".format(it)}kg" } ?: ""}") })
                        }
                    }
                }
            }

            // 读取当前重量单位用于步长控制
            val unit by vm.unitSystem.collectAsState()

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(Dimens.ChipSpacing),
                verticalArrangement = Arrangement.spacedBy(Dimens.ChipSpacing),
                maxItemsInEachRow = 3
            ) {
                // 次数（只读，覆盖层触发弹层）
                Box(modifier = Modifier.weight(1f, fill = true)) {
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = vm::setRepsText,
                        label = { Text("次数") },
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = {
                            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                                IconButton(onClick = {
                                    val current = repsText.toIntOrNull() ?: 0
                                    val next = (current - 1).coerceAtLeast(0)
                                    vm.setRepsText(if (next == 0) "0" else next.toString())
                                }) {
                                    Icon(Icons.Filled.Remove, contentDescription = "减少次数")
                                }
                                IconButton(onClick = {
                                    val current = repsText.toIntOrNull() ?: 0
                                    vm.setRepsText((current + 1).toString())
                                }) {
                                    Icon(Icons.Filled.Add, contentDescription = "增加次数")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .widthIn(min = 96.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(end = 88.dp) // 预留尾部图标点击区域
                            .clickable { repsPickerOpen = true }
                    )
                }
                // 重量（只读，覆盖层触发弹层）
                Box(modifier = Modifier.weight(1f, fill = true)) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = vm::setWeightText,
                        label = { Text("重量(kg)") },
                        readOnly = true,
                        singleLine = true,
                        trailingIcon = {
                            val step = if (unit == com.example.fitness_demo.ui.viewmodel.SessionViewModel.UnitSystem.KG) 2.5f else 5f
                            Row {
                                IconButton(onClick = {
                                    val base = weightText.toFloatOrNull() ?: 0f
                                    val next = (base - step).coerceAtLeast(0f)
                                    vm.setWeightText(if (next == 0f) "0" else "%.1f".format(next))
                                }) {
                                    Icon(Icons.Filled.Remove, contentDescription = "减少重量")
                                }
                                IconButton(onClick = {
                                    val base = weightText.toFloatOrNull() ?: 0f
                                    val next = base + step
                                    vm.setWeightText("%.1f".format(next))
                                }) {
                                    Icon(Icons.Filled.Add, contentDescription = "增加重量")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .widthIn(min = 96.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(end = 88.dp)
                            .clickable { weightPickerOpen = true }
                    )
                }
                // RPE（滑块输入）
                Column(
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .widthIn(min = 96.dp)
                ) {
                    Text("RPE")
                    val rpeValue = vm.rpeText.collectAsState().value.toFloatOrNull()?.coerceIn(0f, 10f) ?: 0f
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            value = rpeValue,
                            onValueChange = { v ->
                                val rounded = ((v * 10f).roundToInt() / 10f)
                                vm.setRpeText("%.1f".format(rounded))
                            },
                            valueRange = 0f..10f,
                            steps = 0,
                            colors = SliderDefaults.colors(
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                thumbColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = "%.1f".format(rpeValue),
                            modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
            // 复制上一组操作
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(onClick = {
                        vm.presetFromLastOfSelectedExercise()
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }, label = { Text("训练填充") })
                }
                Button(
                    onClick = {
                        vm.addSet()
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        // 添加后清空输入
                        vm.setRepsText("")
                        vm.setWeightText("")
                        vm.setRpeText("")
                    },
                    enabled = selectedExerciseId != null &&
                        (repsText.toIntOrNull() != null) &&
                        (weightText.isBlank() || weightText.toFloatOrNull() != null) &&
                        (vm.rpeText.collectAsState().value.isBlank() || vm.rpeText.collectAsState().value.toFloatOrNull() != null)
                ) { Text("添加") }
            }

            // 次数选择器底部弹层
            if (repsPickerOpen) {
                val rSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(onDismissRequest = { repsPickerOpen = false }, sheetState = rSheet) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("选择次数", style = MaterialTheme.typography.titleMedium)
                        // 最近使用次数
                        val recentReps by vm.recentReps.collectAsState()
                        if (recentReps.isNotEmpty()) {
                            Text("最近使用", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(recentReps.take(12)) { n ->
                                    AssistChip(onClick = {
                                        vm.setRepsText(n.toString())
                                        repsPickerOpen = false
                                    }, label = { Text("$n") })
                                }
                            }
                        }
                        val rOptions = listOf(3, 5, 6, 8, 10, 12, 15, 20, 25, 30)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(rOptions) { n ->
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
                        // 单位切换
                        val unit by vm.unitSystem.collectAsState()
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("单位")
                            AssistChip(onClick = { vm.toggleUnit() }, label = { Text(if (unit == com.example.fitness_demo.ui.viewmodel.SessionViewModel.UnitSystem.KG) "kg" else "lb") })
                        }
                        // 最近使用重量
                        val recentW by vm.recentWeightsKg.collectAsState()
                        if (recentW.isNotEmpty()) {
                            Text("最近使用", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(recentW.take(12)) { kg ->
                                    val shown = if (unit == com.example.fitness_demo.ui.viewmodel.SessionViewModel.UnitSystem.KG) kg to (kg * 2.20462f) else (kg * 2.20462f) to kg
                                    val primary = if (unit == com.example.fitness_demo.ui.viewmodel.SessionViewModel.UnitSystem.KG) "${shown.first.toInt()}" else "${shown.first.toInt()}"
                                    val ghost = if (unit == com.example.fitness_demo.ui.viewmodel.SessionViewModel.UnitSystem.KG) "${shown.second.toInt()}lb" else "${shown.second.toInt()}kg"
                                    AssistChip(onClick = {
                                        vm.setWeightText(primary)
                                        weightPickerOpen = false
                                    }, label = { Text("$primary (${ghost})") })
                                }
                            }
                        }
                        val wOptions = if (unit == com.example.fitness_demo.ui.viewmodel.SessionViewModel.UnitSystem.KG)
                            listOf("自重","2.5","5","7.5","10","12.5","15","17.5","20","22.5","25","27.5","30","32.5","35","37.5","40","42.5","45","47.5","50","55","60","65","70","75","80","85","90","95","100")
                        else
                            listOf("自重","5","10","15","20","25","30","35","40","45","50","55","60","65","70","75","80","85","90","95","100","110","120","135","155","185","205","225")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(wOptions) { w ->
                                AssistChip(onClick = {
                                    if (w == "自重") vm.setWeightText("") else vm.setWeightText(w)
                                    weightPickerOpen = false
                                }, label = { Text(w) })
                            }
                        }
                        OutlinedTextField(
                            value = customWeight,
                            onValueChange = { customWeight = it },
                            label = { Text(if (unit == com.example.fitness_demo.ui.viewmodel.SessionViewModel.UnitSystem.KG) "自定义重量(kg)" else "自定义重量(lb)") }
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
            val listState = androidx.compose.foundation.lazy.rememberLazyListState()
            var prevSize by remember { mutableStateOf(sets.size) }
            LaunchedEffect(sets.size) {
                if (sets.size > prevSize && sets.isNotEmpty()) {
                    listState.animateScrollToItem(index = sets.size - 1)
                }
                prevSize = sets.size
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = sets.sortedBy { it.setNumber },
                    key = { it.id }
                ) { set ->
                    val dismissState = rememberDismissState(
                        confirmStateChange = { value ->
                            if (value == DismissValue.DismissedToStart) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.deleteSet(set.id)
                                true
                            } else {
                                false
                            }
                        }
                    )
                    SwipeToDismiss(
                        state = dismissState,
                        background = {
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp))
                        },
                        directions = setOf(DismissDirection.EndToStart),
                        dismissContent = {
                            SetRowContent(set)
                        }
                    )
                }
            }
        }
    }
    if (summaryOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val summary = vm.currentSummary()
        val clipboard = LocalClipboardManager.current
        val context = LocalContext.current
        val primaryColor = MaterialTheme.colorScheme.primary
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        val surfaceColor = MaterialTheme.colorScheme.surface
        val session by vm.session.collectAsState()
        ModalBottomSheet(onDismissRequest = { summaryOpen = false }, sheetState = sheetState) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("训练总结", style = MaterialTheme.typography.titleMedium)
                ElevatedCard(elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("主题：${summary.theme ?: "训练"}", style = MaterialTheme.typography.bodyMedium)
                        Text("组数：${summary.totalSets}", style = MaterialTheme.typography.bodyMedium)
                        Text("总次数：${summary.totalReps}", style = MaterialTheme.typography.bodyMedium)
                        Text("总训练量：${"%.1f".format(summary.totalVolumeKg)} kg", style = MaterialTheme.typography.bodyMedium)
                        session?.let { s ->
                            val end = s.endTimeMillis ?: System.currentTimeMillis()
                            val durMin = ((end - s.startTimeMillis) / 60000.0).toInt()
                            Text("时长：${durMin} min", style = MaterialTheme.typography.bodyMedium)
                            val formatter = java.text.SimpleDateFormat("MM.dd HH:mm", java.util.Locale.getDefault())
                            Text("时间：${formatter.format(java.util.Date(s.startTimeMillis))} - ${formatter.format(java.util.Date(end))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        val text = buildString {
                            appendLine("训练总结")
                            appendLine("主题：${summary.theme ?: "训练"}")
                            appendLine("组数：${summary.totalSets}")
                            appendLine("总次数：${summary.totalReps}")
                            appendLine("总训练量：${"%.1f".format(summary.totalVolumeKg)} kg")
                        }
                        clipboard.setText(AnnotatedString(text))
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }) {
                        Text("分享（复制）")
                    }
                    Button(onClick = {
                        val text = buildString {
                            appendLine("训练总结")
                            appendLine("主题：${summary.theme ?: "训练"}")
                            appendLine("组数：${summary.totalSets}")
                            appendLine("总次数：${summary.totalReps}")
                            appendLine("总训练量：${"%.1f".format(summary.totalVolumeKg)} kg")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(intent, "分享训练总结"))
                    }) {
                        Text("系统分享")
                    }
                    Button(onClick = {
                        // 生成图片分享卡片
                        val primary = primaryColor.toArgb()
                        val onSurface = onSurfaceColor.toArgb()
                        val surface = surfaceColor.toArgb()
                        val width = 1080
                        val height = 1350
                        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val c = AndroidCanvas(bmp)
                        c.drawColor(surface)
                        val card = RectF(60f, 120f, width - 60f, height - 160f)
                        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                        // 卡片背景
                        paint.color = 0xFFFFFFFF.toInt()
                        c.drawRoundRect(card, 32f, 32f, paint)
                        // 标题
                        paint.color = onSurface
                        paint.textSize = 64f
                        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        c.drawText("训练总结", card.left + 48f, card.top + 120f, paint)
                        // 主题芯片
                        val chipRect = RectF(card.left + 48f, card.top + 150f, card.left + 48f + 260f, card.top + 210f)
                        paint.color = primary
                        c.drawRoundRect(chipRect, 24f, 24f, paint)
                        paint.color = 0xFFFFFFFF.toInt()
                        paint.textSize = 36f
                        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                        val themeText = (summary.theme ?: "训练")
                        c.drawText(themeText, chipRect.left + 28f, chipRect.bottom - 18f, paint)
                        // 指标
                        paint.color = onSurface
                        paint.textSize = 48f
                        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        val y0 = chipRect.bottom + 120f
                        c.drawText("组数", card.left + 48f, y0, paint)
                        c.drawText("总次数", card.left + 48f, y0 + 120f, paint)
                        c.drawText("总训练量", card.left + 48f, y0 + 240f, paint)
                        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                        c.drawText("${summary.totalSets}", card.right - 48f - paint.measureText("${summary.totalSets}"), y0, paint)
                        c.drawText("${summary.totalReps}", card.right - 48f - paint.measureText("${summary.totalReps}"), y0 + 120f, paint)
                        val volStr = "%.1f kg".format(summary.totalVolumeKg)
                        c.drawText(volStr, card.right - 48f - paint.measureText(volStr), y0 + 240f, paint)
                        // 日期
                        paint.textSize = 40f
                        val dateStr = java.text.SimpleDateFormat("yyyy.MM.dd HH:mm").format(java.util.Date())
                        c.drawText(dateStr, card.left + 48f, card.bottom - 60f, paint)
                        // 保存到缓存并分享
                        val dir = File(context.cacheDir, "images").apply { mkdirs() }
                        val outFile = File(dir, "summary_${System.currentTimeMillis()}.png")
                        FileOutputStream(outFile).use { fos -> bmp.compress(Bitmap.CompressFormat.PNG, 100, fos) }
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "分享训练总结图片"))
                    }) {
                        Text("图片分享")
                    }
                    TextButton(onClick = {
                        summaryOpen = false
                        onBack()
                    }) { Text("返回记录") }
                }
            }
        }
    }
}

@Composable
private fun SetRowContent(set: SetEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "组 ${set.setNumber}")
            val weightPart = set.weightKg?.let { "重量 ${"%.1f".format(it)}kg" } ?: "自重"
            val rpePart = set.rpe?.let { "，RPE ${"%.1f".format(it)}" } ?: ""
            Text(text = "次数 ${set.reps}，$weightPart$rpePart")
        }
    }
    HorizontalDivider()
}

private fun groupLabel(en: String): String = when (en.lowercase()) {
    "all" -> "全部"
    "chest" -> "胸部"
    "back" -> "背部"
    "legs" -> "腿部"
    "shoulders" -> "肩部"
    "arms" -> "手臂"
    "core" -> "核心"
    "full body" -> "全身"
    "other" -> "其它"
    else -> en
}


