package com.example.militaryid.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.militaryid.data.LearningStatus
import com.example.militaryid.data.ProgressManager
import com.example.militaryid.data.Weapon
import com.example.militaryid.data.WeaponsRepository

private enum class FlashcardState {
    SETUP,
    LEARNING,
    RESULT
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlashcardScreen(
    progressManager: ProgressManager
) {
    var currentState by remember { mutableStateOf(FlashcardState.SETUP) }
    
    // 設定項目
    var selectedCountry by remember { mutableStateOf("all") }
    var selectedCategory by remember { mutableStateOf("all") }
    var selectedCount by remember { mutableStateOf("10") }

    // 学習セッション状態
    var sessionWeapons by remember { mutableStateOf<List<Weapon>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    // セッション内での判定カウント
    var countMastered by remember { mutableIntStateOf(0) }
    var countReviewing by remember { mutableIntStateOf(0) }
    var countLearning by remember { mutableIntStateOf(0) }

    // フィルタリング計算
    val allWeapons = WeaponsRepository.weapons
    val filteredWeapons = remember(selectedCountry, selectedCategory) {
        allWeapons.filter { weapon ->
            (selectedCountry == "all" || weapon.country == selectedCountry) &&
            (selectedCategory == "all" || weapon.category == selectedCategory)
        }
    }

    when (currentState) {
        FlashcardState.SETUP -> {
            FlashcardSetupView(
                selectedCountry = selectedCountry,
                onCountrySelect = { selectedCountry = it },
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it },
                selectedCount = selectedCount,
                onCountSelect = { selectedCount = it },
                matchingCount = filteredWeapons.size,
                onStart = {
                    val pool = filteredWeapons.shuffled()
                    val count = when (selectedCount) {
                        "5" -> 5
                        "10" -> 10
                        "20" -> 20
                        else -> pool.size
                    }
                    sessionWeapons = pool.take(count)
                    currentIndex = 0
                    isFlipped = false
                    countMastered = 0
                    countReviewing = 0
                    countLearning = 0
                    currentState = FlashcardState.LEARNING
                }
            )
        }
        FlashcardState.LEARNING -> {
            if (sessionWeapons.isNotEmpty() && currentIndex < sessionWeapons.size) {
                val currentWeapon = sessionWeapons[currentIndex]
                FlashcardSessionView(
                    weapon = currentWeapon,
                    currentIndex = currentIndex,
                    totalCount = sessionWeapons.size,
                    isFlipped = isFlipped,
                    onToggleFlip = { isFlipped = !isFlipped },
                    onQuit = { currentState = FlashcardState.SETUP },
                    onJudge = { status ->
                        progressManager.setStatus(currentWeapon.id, status)
                        when (status) {
                            LearningStatus.MASTERED -> countMastered++
                            LearningStatus.REVIEWING -> countReviewing++
                            LearningStatus.LEARNING -> countLearning++
                            else -> {}
                        }
                        if (currentIndex + 1 < sessionWeapons.size) {
                            currentIndex++
                            isFlipped = false
                        } else {
                            currentState = FlashcardState.RESULT
                        }
                    }
                )
            } else {
                currentState = FlashcardState.SETUP
            }
        }
        FlashcardState.RESULT -> {
            FlashcardResultView(
                totalCount = sessionWeapons.size,
                mastered = countMastered,
                reviewing = countReviewing,
                learning = countLearning,
                onRetry = {
                    val pool = filteredWeapons.shuffled()
                    val count = when (selectedCount) {
                        "5" -> 5
                        "10" -> 10
                        "20" -> 20
                        else -> pool.size
                    }
                    sessionWeapons = pool.take(count)
                    currentIndex = 0
                    isFlipped = false
                    countMastered = 0
                    countReviewing = 0
                    countLearning = 0
                    currentState = FlashcardState.LEARNING
                },
                onBackToSetup = { currentState = FlashcardState.SETUP }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlashcardSetupView(
    selectedCountry: String,
    onCountrySelect: (String) -> Unit,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    selectedCount: String,
    onCountSelect: (String) -> Unit,
    matchingCount: Int,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "暗記カード設定",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "出題条件を選択して暗記セッションを開始します",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 国の選択
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "国・地域",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("all" to "すべて", "russia" to "ロシア", "china" to "中国").forEach { (code, label) ->
                    FilterChip(
                        selected = selectedCountry == code,
                        onClick = { onCountrySelect(code) },
                        label = { Text(label) }
                    )
                }
            }
        }

        // 類別の選択
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "兵器類別",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "all" to "すべて",
                    "aircraft" to "戦闘機・航空機",
                    "firearm" to "小火器",
                    "artillery" to "火砲・防空システム",
                    "missile" to "ミサイル",
                    "bomb" to "誘導爆弾"
                ).forEach { (code, label) ->
                    FilterChip(
                        selected = selectedCategory == code,
                        onClick = { onCategorySelect(code) },
                        label = { Text(label) }
                    )
                }
            }
        }

        // 出題数の選択
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "出題数",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("5" to "5問", "10" to "10問", "20" to "20問", "all" to "すべて").forEach { (code, label) ->
                    FilterChip(
                        selected = selectedCount == code,
                        onClick = { onCountSelect(code) },
                        label = { Text(label) }
                    )
                }
            }
        }

        // 該当件数カード
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "条件に合致する兵器数",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "${matchingCount} 件",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = matchingCount > 0,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "暗記カードを開始",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FlashcardSessionView(
    weapon: Weapon,
    currentIndex: Int,
    totalCount: Int,
    isFlipped: Boolean,
    onToggleFlip: () -> Unit,
    onQuit: () -> Unit,
    onJudge: (LearningStatus) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // トップバー（進捗状況と終了ボタン）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onQuit) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "終了",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${currentIndex + 1} / $totalCount",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / totalCount.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onToggleFlip) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.RotateRight,
                    contentDescription = "表裏反転",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 暗記カード本体（タップで表裏切り替え）
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable(onClick = onToggleFlip),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (!isFlipped) {
                // 表面: 写真と識別質問
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(weapon.countryLabel) }
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text(weapon.categoryLabel) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(weapon.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "兵器外観画像",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "タップして名称と特徴を確認",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // 裏面: 名称・識別ポイント・特徴・スペック
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(weapon.countryLabel) }
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text(weapon.categoryLabel) }
                        )
                    }

                    // 名称
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = weapon.nameJa,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${weapon.nameEn} / NATO: ${weapon.natoOrAlt}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = weapon.type,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider()

                    // 外観・識別ポイント
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "外観の識別ポイント",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = weapon.appearance,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // 主な特徴
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "主な特徴",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        weapon.features.forEach { feat ->
                            Text(
                                text = "• $feat",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 主要スペック
                    if (weapon.specs.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "主要スペック",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            weapon.specs.forEach { (k, v) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = k,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = v,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 操作ボタン: 「◯」「△」「✕」の3つのみ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ✕ 未習得
            FilledTonalButton(
                onClick = { onJudge(LearningStatus.LEARNING) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "✕ 未習得",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // △ 要復習
            FilledTonalButton(
                onClick = { onJudge(LearningStatus.REVIEWING) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "△ 要復習",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // ◯ 習得
            Button(
                onClick = { onJudge(LearningStatus.MASTERED) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "◯ 習得",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FlashcardResultView(
    totalCount: Int,
    mastered: Int,
    reviewing: Int,
    learning: Int,
    onRetry: () -> Unit,
    onBackToSetup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "暗記セッション完了",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "今回の出題数: $totalCount 件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "◯ 習得",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$mastered",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "△ 要復習",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "$reviewing",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "✕ 未習得",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "$learning",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("同じ条件で再挑戦", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackToSetup,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("条件設定に戻る")
        }
    }
}
