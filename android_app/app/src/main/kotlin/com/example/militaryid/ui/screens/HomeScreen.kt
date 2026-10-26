package com.example.militaryid.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.militaryid.data.LearningStatus
import com.example.militaryid.data.ProgressManager
import com.example.militaryid.data.WeaponsRepository

@Composable
fun HomeScreen(
    progressManager: ProgressManager,
    onNavigate: (String) -> Unit
) {
    val counts = remember(progressManager) { progressManager.getCounts() }
    val total = WeaponsRepository.weapons.size
    val mastered = counts[LearningStatus.MASTERED] ?: 0
    val progressRate = if (total > 0) mastered.toFloat() / total.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ヘッダー情報（TopAppBarはノッチ干渉回避のため廃止し、コンテンツ内で端正に表示）
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "現代兵器 識別暗記",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "ロシア・中国の主要戦闘機・火器・火砲・ミサイル (全${total}種)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 進捗状況サマリーカード
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "全体の習得状況",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${(progressRate * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = { progressRate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    trackColor = MaterialTheme.colorScheme.surface,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusCountChip(label = "◯ 習得", count = mastered, color = MaterialTheme.colorScheme.primary)
                    StatusCountChip(label = "△ 復習", count = counts[LearningStatus.REVIEWING] ?: 0, color = MaterialTheme.colorScheme.tertiary)
                    StatusCountChip(label = "✕ 未習得", count = counts[LearningStatus.LEARNING] ?: 0, color = MaterialTheme.colorScheme.error)
                    StatusCountChip(label = "未着手", count = counts[LearningStatus.UNSEEN] ?: 0, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        // 各モードへのショートカット
        Text(
            text = "学習モード",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        NavigationActionCard(
            title = "暗記カード",
            description = "画像を見て特徴と名称をテンポよく暗記・確認",
            icon = Icons.Default.Style,
            onClick = { onNavigate("flashcard") }
        )

        NavigationActionCard(
            title = "4択識別テスト",
            description = "写真と外観の特徴から正しい名称を判定する実力測定",
            icon = Icons.Default.Quiz,
            onClick = { onNavigate("quiz") }
        )

        NavigationActionCard(
            title = "兵器図鑑",
            description = "全54種類のスペック・識別のコツ・解説・ライセンスを閲覧",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            onClick = { onNavigate("gallery") }
        )

        NavigationActionCard(
            title = "学習統計",
            description = "カテゴリ別・国別の暗記進捗とデータの管理",
            icon = Icons.Default.BarChart,
            onClick = { onNavigate("stats") }
        )
    }
}

@Composable
private fun StatusCountChip(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun NavigationActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
