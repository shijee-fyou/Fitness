package com.example.fitness_demo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.fitness_demo.ui.theme.Dimens
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import com.example.fitness_demo.data.SettingsStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var restSeconds by remember { mutableStateOf(SettingsStore.getDefaultRestSeconds(context)) }
    var restPickerOpen by remember { mutableStateOf(false) }
    var weightUnit by remember { mutableStateOf(SettingsStore.getDefaultWeightUnit(context)) }
    var weightPickerOpen by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            item {
                SectionCard(title = "训练相关") {
                    SettingsRow(
                        title = "默认休息时长",
                        subtitle = "每组结束后的自动休息时间",
                        trailing = {
                            TextButton(onClick = { restPickerOpen = true }) {
                                Text("${restSeconds} 秒", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                    SettingsRow(
                        title = "重量单位",
                        subtitle = "训练记录与统计的显示单位",
                        trailing = {
                            TextButton(onClick = { weightPickerOpen = true }) {
                                Text(weightUnit, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                    SettingsRow(
                        title = "自动开始计时",
                        subtitle = "新增一组后自动开始计时",
                        trailing = { Text("开启", color = MaterialTheme.colorScheme.primary) },
                    )
                    SettingsRow(
                        title = "训练中自动休息",
                        subtitle = "完成本组后自动进入休息",
                        trailing = { Text("开启", color = MaterialTheme.colorScheme.primary) }
                    )
                    SettingsRow(
                        title = "组内计时显示",
                        subtitle = "灵动岛中显示本组计时",
                        trailing = { Text("开启", color = MaterialTheme.colorScheme.primary) },
                        showDivider = false
                    )
                }
            }
            item {
                SectionCard(title = "记录与统计") {
                    SettingsRow(
                        title = "日历显示项",
                        subtitle = "日期卡片显示的统计内容",
                        trailing = { Text("容量", color = MaterialTheme.colorScheme.primary) }
                    )
                    SettingsRow(
                        title = "日历周起始日",
                        subtitle = "周一 / 周日",
                        trailing = { Text("周一", color = MaterialTheme.colorScheme.primary) }
                    )
                    SettingsRow(
                        title = "容量单位显示",
                        subtitle = "在统计中显示单位",
                        trailing = { Text("关闭", color = MaterialTheme.colorScheme.primary) },
                    )
                    SettingsRow(
                        title = "训练时长统计",
                        subtitle = "统计并显示训练时长",
                        trailing = { Text("开启", color = MaterialTheme.colorScheme.primary) },
                        showDivider = false
                    )
                }
            }
            item {
                SectionCard(title = "通知与提醒") {
                    SettingsRow(
                        title = "休息结束提醒",
                        subtitle = "休息计时结束时提醒",
                        trailing = { Text("开启", color = MaterialTheme.colorScheme.primary) }
                    )
                    SettingsRow(
                        title = "训练完成提醒",
                        subtitle = "完成训练后显示提醒",
                        trailing = { Text("开启", color = MaterialTheme.colorScheme.primary) },
                        showDivider = false
                    )
                }
            }
            item {
                SectionCard(title = "账户与同步") {
                    SettingsRow(
                        title = "云端同步",
                        subtitle = "训练记录多设备同步",
                        trailing = { Text("关闭", color = MaterialTheme.colorScheme.primary) }
                    )
                    SettingsRow(
                        title = "自动备份",
                        subtitle = "Wi‑Fi 下自动备份",
                        trailing = { Text("开启", color = MaterialTheme.colorScheme.primary) }
                    )
                    SettingsRow(
                        title = "同步频率",
                        subtitle = "每次训练后 / 每日",
                        trailing = { Text("每次训练后", color = MaterialTheme.colorScheme.primary) },
                        showDivider = false
                    )
                }
            }
            item {
                SectionCard(title = "设备与传感") {
                    SettingsRow(
                        title = "健康平台",
                        subtitle = "连接健康数据平台",
                        trailing = { Text("未连接", color = MaterialTheme.colorScheme.primary) }
                    )
                    SettingsRow(
                        title = "动作计时震动",
                        subtitle = "倒计时结束时震动",
                        trailing = { Text("开启", color = MaterialTheme.colorScheme.primary) },
                        showDivider = false
                    )
                }
            }
            item {
                SectionCard(title = "外观与无障碍") {
                    SettingsRow(
                        title = "主题模式",
                        subtitle = "浅色 / 深色 / 跟随系统",
                        trailing = { Text("跟随系统", color = MaterialTheme.colorScheme.primary) }
                    )
                    SettingsRow(
                        title = "字体大小",
                        subtitle = "影响训练记录与详情显示",
                        trailing = { Text("标准", color = MaterialTheme.colorScheme.primary) }
                    )
                    SettingsRow(
                        title = "高对比度",
                        subtitle = "提升文字与背景对比",
                        trailing = { Text("关闭", color = MaterialTheme.colorScheme.primary) },
                        showDivider = false
                    )
                }
            }
            item {
                SectionCard(title = "数据与隐私") {
                    SettingsRow(
                        title = "导出训练数据",
                        subtitle = "CSV 格式导出",
                        trailing = { TextButton(onClick = {}) { Text("导出") } }
                    )
                    SettingsRow(
                        title = "清空训练数据",
                        subtitle = "不可恢复，请谨慎操作",
                        trailing = { TextButton(onClick = {}) { Text("清空") } }
                    )
                    SettingsRow(
                        title = "数据加密",
                        subtitle = "本地数据加密存储",
                        trailing = { Text("关闭", color = MaterialTheme.colorScheme.primary) },
                        showDivider = false
                    )
                }
            }
            item {
                SectionCard(title = "关于") {
                    SettingsRow(
                        title = "当前版本",
                        subtitle = "版本号与更新信息",
                        trailing = { Text("v1.0", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    )
                    SettingsRow(
                        title = "反馈与建议",
                        subtitle = "提交问题或功能建议",
                        trailing = { TextButton(onClick = {}) { Text("联系") } }
                    )
                    SettingsRow(
                        title = "隐私政策",
                        subtitle = "了解数据使用方式",
                        trailing = { TextButton(onClick = {}) { Text("查看") } },
                        showDivider = false
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    "设置项仅展示 UI 框架，后续可接入实际功能",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (restPickerOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        val options = remember { (10..300 step 5).toList() }
        val itemHeight = 42.dp
        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = (options.indexOf(restSeconds).coerceAtLeast(0))
        )
        val density = LocalDensity.current
        val itemPx = with(density) { itemHeight.toPx() }
        val centeredIndex by remember {
            derivedStateOf {
                val idx = listState.firstVisibleItemIndex
                val offset = listState.firstVisibleItemScrollOffset
                val delta = if (offset > itemPx / 2f) 1 else 0
                (idx + delta).coerceIn(0, options.lastIndex)
            }
        }
        ModalBottomSheet(
            onDismissRequest = { restPickerOpen = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 260.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { restPickerOpen = false }) { Text("取消") }
                    TextButton(onClick = {
                        restSeconds = options[centeredIndex]
                        SettingsStore.setDefaultRestSeconds(context, restSeconds)
                        restPickerOpen = false
                    }) { Text("完成") }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = itemHeight * 5)
                ) {
                    LazyColumn(
                        state = listState,
                        flingBehavior = rememberSnapFlingBehavior(listState),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = itemHeight * 2)
                    ) {
                        items(options.size) { i ->
                            val value = options[i]
                            val dist = kotlin.math.abs(i - centeredIndex)
                            val scale = if (dist == 0) 1.06f else 0.92f
                            val alpha = if (dist == 0) 1.0f else 0.45f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(itemHeight)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        this.alpha = alpha
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${value} 秒",
                                    style = if (dist == 0)
                                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    else
                                        MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .height(itemHeight)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                RoundedCornerShape(12.dp)
                            )
                    )
                }
            }
        }
    }

    if (weightPickerOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        val options = remember { listOf("kg", "lb") }
        val itemHeight = 42.dp
        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = (options.indexOf(weightUnit).coerceAtLeast(0))
        )
        val density = LocalDensity.current
        val itemPx = with(density) { itemHeight.toPx() }
        val centeredIndex by remember {
            derivedStateOf {
                val idx = listState.firstVisibleItemIndex
                val offset = listState.firstVisibleItemScrollOffset
                val delta = if (offset > itemPx / 2f) 1 else 0
                (idx + delta).coerceIn(0, options.lastIndex)
            }
        }
        ModalBottomSheet(
            onDismissRequest = { weightPickerOpen = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 240.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { weightPickerOpen = false }) { Text("取消") }
                    TextButton(onClick = {
                        weightUnit = options[centeredIndex]
                        SettingsStore.setDefaultWeightUnit(context, weightUnit)
                        weightPickerOpen = false
                    }) { Text("完成") }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = itemHeight * 5)
                ) {
                    LazyColumn(
                        state = listState,
                        flingBehavior = rememberSnapFlingBehavior(listState),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = itemHeight * 2)
                    ) {
                        items(options.size) { i ->
                            val value = options[i]
                            val dist = kotlin.math.abs(i - centeredIndex)
                            val scale = if (dist == 0) 1.06f else 0.92f
                            val alpha = if (dist == 0) 1.0f else 0.45f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(itemHeight)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        this.alpha = alpha
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    value,
                                    style = if (dist == 0)
                                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    else
                                        MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .height(itemHeight)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                RoundedCornerShape(12.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = Dimens.CardElevationMed),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding + 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.size(12.dp))
        trailing()
    }
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp),
            thickness = Dimens.DividerThickness,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

