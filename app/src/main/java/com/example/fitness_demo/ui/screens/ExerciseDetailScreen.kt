package com.example.fitness_demo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitness_demo.data.AppRepository
import com.example.fitness_demo.data.Exercise
import com.example.fitness_demo.ui.theme.Dimens
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: Int,
    repository: AppRepository,
    onBack: () -> Unit
) {
    val exerciseState = remember { mutableStateOf<Exercise?>(null) }
    LaunchedEffect(exerciseId) {
        exerciseState.value = repository.getExerciseById(exerciseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("动作详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        val ex = exerciseState.value
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
        ) {
            if (ex != null) {
                ElevatedCard {
                    Column(modifier = Modifier.padding(Dimens.CardPadding), verticalArrangement = Arrangement.spacedBy(Dimens.ChipSpacing)) {
                        Text(ex.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold))
                        Text(ex.muscleGroup, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                ElevatedCard {
                    Column(modifier = Modifier.padding(Dimens.CardPadding), verticalArrangement = Arrangement.spacedBy(Dimens.ChipSpacing)) {
                        Text("动作动画", style = MaterialTheme.typography.titleMedium)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("动画占位（稍后接入本地动画/插图）", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                ElevatedCard {
                    Column(
                        modifier = Modifier
                            .padding(Dimens.CardPadding)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(Dimens.ChipSpacing)
                    ) {
                        Text("动作说明", style = MaterialTheme.typography.titleMedium)
                        val intro = buildString {
                            appendLine("${ex.name} 是针对 ${ex.muscleGroup} 的常见训练动作，适合不同层级的训练者。")
                            appendLine("目标肌群：${ex.muscleGroup}；次要协同肌群可能包含胸、背、核心等。")
                            appendLine()
                            appendLine("规范要点：")
                            appendLine("1. 选择合适重量，确保全程控制与稳定。")
                            appendLine("2. 动作过程中保持核心收紧与脊柱中立。")
                            appendLine("3. 节奏建议：下放 2 秒、底部停顿 0–1 秒、上推/上拉 1–2 秒。")
                            appendLine("4. 全程呼吸，上力时吐气，下放时吸气。")
                            appendLine()
                            appendLine("常见错误：")
                            appendLine("• 借力过大或摆动，导致目标肌群刺激不足。")
                            appendLine("• 关节锁死或活动度不足，缩短有效运动范围。")
                            appendLine("• 肩/腰代偿明显，核心未稳定。")
                            appendLine()
                            appendLine("进阶与回归：")
                            appendLine("• 回归：降低重量、减少次数或采用器械/辅助。")
                            appendLine("• 进阶：缓慢离心、暂停重复、增加训练量或密度。")
                            appendLine()
                            appendLine("编排建议：")
                            appendLine("• 初学者：3×8–12，每周 2–3 次；组间休息 60–90 秒。")
                            appendLine("• 进阶者：4×6–10，可采用 RPE 7–9；组间休息 90–120 秒。")
                        }
                        Text(intro, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            } else {
                Text("加载中…")
            }
        }
    }
}
