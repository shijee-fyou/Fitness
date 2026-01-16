package com.example.fitness_demo.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.background
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.FilledTonalButton
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.LinearProgressIndicator
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
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import com.example.fitness_demo.ui.components.GradientPrimaryButton
import androidx.compose.ui.unit.sp

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
    var addSheetOpen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val uiScope = rememberCoroutineScope()
    // 灵动岛展开/折叠（统一控制“待完成组/休息”两态）
    var islandExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(restSeconds) {
        if (restSeconds != null) {
            islandExpanded = false
        }
    }
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶部为灵动岛预留空间，避免拥挤
                val islandVisible = (vm.pendingSetId.collectAsState().value != null) || (restSeconds != null)
                val islandTarget = if (islandVisible) {
                    if (restSeconds != null) 64.dp else if (islandExpanded) 168.dp else 60.dp
                } else 0.dp
                val islandSpacer by animateDpAsState(targetValue = islandTarget + 12.dp, label = "islandSpacer")
                Spacer(modifier = Modifier.height(islandSpacer))
            // 顶部休息区域已移除，统一放到“灵动岛”中

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
            // Hero 渐变区块：强化主次层次
            ElevatedCard(
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = Dimens.CardElevationMed),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .padding(Dimens.CardPadding)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("最近动作", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(currentName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            if (pickerOpen) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(onDismissRequest = { pickerOpen = false }, sheetState = sheetState, tonalElevation = Dimens.SheetElevation) {
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
                                HorizontalDivider(thickness = Dimens.DividerThickness, color = MaterialTheme.colorScheme.outlineVariant)
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

            // 移除常驻设置卡片，改为“新增时弹出”
            // 复制上一组操作（已移除常驻“训练填充”，改为面板内提供）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { addSheetOpen = true },
                    enabled = selectedExerciseId != null
                ) { Text("新增一组") }
            }

            // 次数选择器底部弹层
            if (repsPickerOpen) {
                val rSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(onDismissRequest = { repsPickerOpen = false }, sheetState = rSheet, tonalElevation = Dimens.SheetElevation) {
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

            // 新增一组：设置面板弹层
            if (addSheetOpen) {
                val addSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(onDismissRequest = { addSheetOpen = false }, sheetState = addSheet, tonalElevation = Dimens.SheetElevation) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("本组设置", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AssistChip(onClick = {
                                vm.presetFromLastOfSelectedExercise()
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }, label = { Text("训练填充") })
                        }
                        // 读取单位
                        val unit by vm.unitSystem.collectAsState()
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.ChipSpacing),
                            verticalArrangement = Arrangement.spacedBy(Dimens.ChipSpacing),
                            maxItemsInEachRow = 3
            ) {
                            // 次数
                            Box(modifier = Modifier.weight(1f, fill = true)) {
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = vm::setRepsText,
                        label = { Text("次数") },
                        readOnly = true,
                        singleLine = true,
                                    trailingIcon = {
                                        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                                            RepeatIconButton(onStep = {
                                                val current = repsText.toIntOrNull() ?: 0
                                                val next = (current - 1).coerceAtLeast(0)
                                                vm.setRepsText(if (next == 0) "0" else next.toString())
                                            }) { Icon(Icons.Filled.Remove, contentDescription = "减少次数") }
                                            RepeatIconButton(onStep = {
                                                val current = repsText.toIntOrNull() ?: 0
                                                vm.setRepsText((current + 1).toString())
                                            }) { Icon(Icons.Filled.Add, contentDescription = "增加次数") }
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
                                        .padding(end = 96.dp)
                            .clickable { repsPickerOpen = true }
                    )
                }
                            // 重量
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
                                            RepeatIconButton(onStep = {
                                                val base = weightText.toFloatOrNull() ?: 0f
                                                val next = (base - step).coerceAtLeast(0f)
                                                vm.setWeightText(if (next == 0f) "0" else "%.1f".format(next))
                                            }) { Icon(Icons.Filled.Remove, contentDescription = "减少重量") }
                                            RepeatIconButton(onStep = {
                                                val base = weightText.toFloatOrNull() ?: 0f
                                                val next = base + step
                                                vm.setWeightText("%.1f".format(next))
                                            }) { Icon(Icons.Filled.Add, contentDescription = "增加重量") }
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
                                        .padding(end = 96.dp)
                            .clickable { weightPickerOpen = true }
                    )
                }
                            // RPE
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
            Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("0", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                    Text("5", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                    Text("10", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { addSheetOpen = false }) { Text("取消") }
                Button(
                    onClick = {
                        vm.addSet()
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    vm.setRepsText("")
                                    vm.setWeightText("")
                                    vm.setRpeText("")
                                    addSheetOpen = false
                    },
                    enabled = selectedExerciseId != null &&
                        (repsText.toIntOrNull() != null) &&
                        (weightText.isBlank() || weightText.toFloatOrNull() != null) &&
                        (vm.rpeText.collectAsState().value.isBlank() || vm.rpeText.collectAsState().value.toFloatOrNull() != null)
                ) { Text("添加") }
            }
                    }
                }
            }
            // 重量选择器底部弹层
            if (weightPickerOpen) {
                val wSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(onDismissRequest = { weightPickerOpen = false }, sheetState = wSheet, tonalElevation = Dimens.SheetElevation) {
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
            HorizontalDivider(thickness = Dimens.DividerThickness, color = MaterialTheme.colorScheme.outlineVariant)
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
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = Dimens.SectionSpacing)
            ) {
                items(
                    items = sets.sortedBy { it.setNumber },
                    key = { it.id }
                ) { set ->
                    val dismissState = rememberDismissState(
                        confirmStateChange = { value ->
                            if (value == DismissValue.DismissedToStart) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.deleteSetWithRemember(set.id)
                                uiScope.launch {
                                    val res = snackbarHostState.showSnackbar(
                                        message = "已删除一组",
                                        actionLabel = "撤销",
                                        duration = SnackbarDuration.Short,
                                        withDismissAction = true
                                    )
                                    if (res == SnackbarResult.ActionPerformed) {
                                        vm.undoLastDelete()
                                    }
                                }
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
            // 浮窗（灵动岛风格）：当前“待完成组”
            val pendingId by vm.pendingSetId.collectAsState()
            if (pendingId != null && restSeconds == null) {
                val shape = RoundedCornerShape(28.dp)
                val targetHeight = if (islandExpanded) 168.dp else 60.dp
                val h by animateDpAsState(targetValue = targetHeight, label = "islandHeight")
                val pendingSet = sets.firstOrNull { it.id == pendingId }
                Surface(
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp,
                    shape = shape,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .widthIn(min = 220.dp, max = 520.dp)
                        .heightIn(min = 52.dp)
                        .height(h)
                ) {
                    Column(
                        modifier = Modifier
                            .clip(shape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.96f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.92f)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                Text("本组进行中", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium)
                            }
                            IconButton(onClick = { islandExpanded = !islandExpanded }) {
                                Icon(if (islandExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        if (islandExpanded) {
                            Spacer(modifier = Modifier.size(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val ex = exercises.firstOrNull { it.id == pendingSet?.exerciseId }?.name?.let { Localization.exercise(it) } ?: "动作"
                                    Text(ex, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleSmall)
                                    val meta = pendingSet?.let { set ->
                                        val w = set.weightKg?.let { "${"%.1f".format(it)}kg" } ?: "自重"
                                        "${set.reps} 次 × $w${set.rpe?.let { " · RPE ${"%.1f".format(it)}" } ?: ""}"
                                    } ?: "进行中…"
                                    Text(meta, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f), style = MaterialTheme.typography.labelMedium)
                                }
                                // 组计时（mm:ss）
                                val gMs by vm.groupElapsedMs.collectAsState()
                                val gSec = (gMs / 1000).coerceAtLeast(0)
                                val gmm = (gSec / 60).toInt()
                                val gss = (gSec % 60).toInt()
                                Surface(
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "${"%02d:%02d".format(gmm, gss)}",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 0.5.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.size(10.dp))
                            FilledTonalButton(
                                onClick = { vm.completePendingSetAndStartRest(60) },
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("完成本组", style = MaterialTheme.typography.labelLarge)
                            }
                        } else {
                            // 折叠态点击整体即可快速完成
                            Spacer(modifier = Modifier.size(0.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.dp)
                                    .clickable { vm.completePendingSetAndStartRest(60) }
                            )
                        }
                    }
                }
            }
            // 休息态灵动岛
            if (restSeconds != null) {
                val shape = RoundedCornerShape(28.dp)
                val remaining = restSeconds ?: 0
                val targetHeight = 64.dp
                val h by animateDpAsState(targetValue = targetHeight, label = "restIslandHeight")
                Surface(
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp,
                    shape = shape,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .widthIn(min = 220.dp, max = 520.dp)
                        .heightIn(min = 52.dp)
                        .height(h)
                ) {
                    Column(
                        modifier = Modifier
                            .clip(shape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.96f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.92f)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${"%02d:%02d".format((remaining / 60), (remaining % 60))}",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp,
                                maxLines = 1
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalButton(
                                    onClick = { vm.addRest(30) },
                                    shape = shape,
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f),
                                        contentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) { Text("+30s", style = MaterialTheme.typography.labelMedium) }
                                FilledTonalButton(
                                    onClick = { vm.stopRest() },
                                    shape = shape,
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f),
                                        contentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) { Text("结束", style = MaterialTheme.typography.labelMedium) }
                            }
                        }
                    }
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
        ModalBottomSheet(onDismissRequest = { summaryOpen = false }, sheetState = sheetState, tonalElevation = Dimens.SheetElevation) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("训练总结", style = MaterialTheme.typography.titleMedium)
                ElevatedCard(elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        summary.theme?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                    Text("${summary.totalSets}", style = MaterialTheme.typography.titleLarge)
                                    Text("组数", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                    Text("${summary.totalReps}", style = MaterialTheme.typography.titleLarge)
                                    Text("总次数", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                    Text("${"%.1f".format(summary.totalVolumeKg)}", style = MaterialTheme.typography.titleLarge)
                                    Text("训练量(kg)", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                        session?.let { s ->
                            val end = s.endTimeMillis ?: System.currentTimeMillis()
                            val durMin = ((end - s.startTimeMillis) / 60000.0).toInt()
                            val formatter = java.text.SimpleDateFormat("MM.dd HH:mm", java.util.Locale.getDefault())
                            Text(
                                "时长 ${durMin} 分钟 · ${formatter.format(java.util.Date(s.startTimeMillis))} - ${formatter.format(java.util.Date(end))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                // 完成训练并返回日历/记录页
                // 主操作按钮：渐变 + 轻微按压动效
                GradientPrimaryButton(
                    text = "完成训练！",
                    leadingIcon = Icons.Filled.Check,
                    onClick = {
                        vm.completeSession {
                            summaryOpen = false
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("分享", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledTonalButton(
                        onClick = {
                        val text = buildString {
                            appendLine("训练总结")
                            appendLine("主题：${summary.theme ?: "训练"}")
                            appendLine("组数：${summary.totalSets}")
                            appendLine("总次数：${summary.totalReps}")
                            appendLine("总训练量：${"%.1f".format(summary.totalVolumeKg)} kg")
                        }
                        clipboard.setText(AnnotatedString(text))
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("复制摘要")
                    }
                    FilledTonalButton(
                        onClick = {
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
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("系统分享")
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledTonalButton(
                        onClick = {
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
                        paint.color = 0xFFFFFFFF.toInt()
                        c.drawRoundRect(card, 32f, 32f, paint)
                        paint.color = onSurface
                        paint.textSize = 64f
                        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        c.drawText("训练总结", card.left + 48f, card.top + 120f, paint)
                        val chipRect = RectF(card.left + 48f, card.top + 150f, card.left + 48f + 260f, card.top + 210f)
                        paint.color = primary
                        c.drawRoundRect(chipRect, 24f, 24f, paint)
                        paint.color = 0xFFFFFFFF.toInt()
                        paint.textSize = 36f
                        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                        val themeText = (summary.theme ?: "训练")
                        c.drawText(themeText, chipRect.left + 28f, chipRect.bottom - 18f, paint)
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
                        paint.textSize = 40f
                        val dateStr = java.text.SimpleDateFormat("yyyy.MM.dd HH:mm").format(java.util.Date())
                        c.drawText(dateStr, card.left + 48f, card.bottom - 60f, paint)
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
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("图片分享")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    }
                    TextButton(onClick = {
                        summaryOpen = false
                        onBack()
                }, modifier = Modifier.fillMaxWidth()) { Text("返回记录") }
            }
        }
    }
}

@Composable
private fun SetRowContent(set: SetEntry) {
    val volume = ((set.weightKg ?: 0f) * set.reps).toInt()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "组 ${set.setNumber}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        val weightPart = set.weightKg?.let { "${"%.1f".format(it)} kg" } ?: "自重"
        Text(
            text = "${set.reps} 次 × $weightPart",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        set.rpe?.let {
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "RPE ${"%.1f".format(it)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${volume} kg·rep",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    HorizontalDivider(thickness = com.example.fitness_demo.ui.theme.Dimens.DividerThickness, color = MaterialTheme.colorScheme.outlineVariant)
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

@Composable
private fun RepeatIconButton(
    onStep: () -> Unit,
    enabled: Boolean = true,
    initialDelayMs: Long = 450,
    repeatDelayMs: Long = 90,
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    LaunchedEffect(isPressed, enabled) {
        if (enabled && isPressed) {
            delay(initialDelayMs)
            while (enabled && isPressed) {
                onStep()
                delay(repeatDelayMs)
            }
        }
    }
    IconButton(onClick = onStep, enabled = enabled, interactionSource = interaction) {
        content()
    }
}


