package com.example.militaryid.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.militaryid.data.LearningStatus
import com.example.militaryid.data.ProgressManager
import com.example.militaryid.data.WeaponsRepository

@Composable
fun StatsScreen(
    progressManager: ProgressManager
) {
    var counts by remember { mutableStateOf(progressManager.getCounts()) }
    var showResetDialog by remember { mutableStateOf(false) }

    val allWeapons = WeaponsRepository.weapons
    val total = allWeapons.size
    val mastered = counts[LearningStatus.MASTERED] ?: 0
    val reviewing = counts[LearningStatus.REVIEWING] ?: 0
    val learning = counts[LearningStatus.LEARNING] ?: 0
    val unseen = counts[LearningStatus.UNSEEN] ?: 0

    val progressRate = if (total > 0) mastered.toFloat() / total.toFloat() else 0f

    // 国別の習得数
    val ruWeapons = remember { allWeapons.filter { it.country == "russia" } }
    val ruMastered = remember(counts) { ruWeapons.count { progressManager.getStatus(it.id) == LearningStatus.MASTERED } }

    val cnWeapons = remember { allWeapons.filter { it.country == "china" } }
    val cnMastered = remember(counts) { cnWeapons.count { progressManager.getStatus(it.id) == LearningStatus.MASTERED } }

    // 類別別の習得数
    val categories = listOf(
        "aircraft" to "戦闘機・航空機",
        "firearm" to "小火器",
        "artillery" to "火砲・防空システム",
        "missile" to "ミサイル",
        "bomb" to "誘導爆弾"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // タイトル
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "学習統計",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "暗記状況とカテゴリー別の習得度一覧",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 総合習得サマリー
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "全体習得率",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(progressRate * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = { progressRate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatMetricColumn("◯ 習得", "$mastered", MaterialTheme.colorScheme.primary)
                    StatMetricColumn("△ 要復習", "$reviewing", MaterialTheme.colorScheme.tertiary)
                    StatMetricColumn("✕ 未習得", "$learning", MaterialTheme.colorScheme.error)
                    StatMetricColumn("未着手", "$unseen", MaterialTheme.colorScheme.outline)
                }
            }
        }

        // 国別進捗
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "国別習得状況",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // ロシア
                CountryProgressRow(
                    name = "ロシア",
                    mastered = ruMastered,
                    total = ruWeapons.size
                )

                // 中国
                CountryProgressRow(
                    name = "中国",
                    mastered = cnMastered,
                    total = cnWeapons.size
                )
            }
        }

        // 類別別進捗
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "兵器類別ごとの習得状況",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                categories.forEach { (catCode, catName) ->
                    val catWeapons = allWeapons.filter { it.category == catCode }
                    val catMastered = catWeapons.count { progressManager.getStatus(it.id) == LearningStatus.MASTERED }
                    CountryProgressRow(
                        name = catName,
                        mastered = catMastered,
                        total = catWeapons.size
                    )
                }
            }
        }

        // リセットボタン
        OutlinedButton(
            onClick = { showResetDialog = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("学習記録をすべて初期化")
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("学習記録の初期化") },
            text = { Text("これまでに記録したすべての暗記状況（◯, △, ✕）をクリアします。よろしいですか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        progressManager.resetAll()
                        counts = progressManager.getCounts()
                        showResetDialog = false
                    }
                ) {
                    Text("初期化する", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Composable
private fun StatMetricColumn(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun CountryProgressRow(
    name: String,
    mastered: Int,
    total: Int
) {
    val rate = if (total > 0) mastered.toFloat() / total.toFloat() else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text(
                text = "$mastered / $total (${(rate * 100).toInt()}%)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { rate },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
