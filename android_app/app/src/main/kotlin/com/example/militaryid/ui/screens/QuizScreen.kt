package com.example.militaryid.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.militaryid.data.ProgressManager
import com.example.militaryid.data.Weapon
import com.example.militaryid.data.WeaponsRepository

private enum class QuizState {
    SETUP,
    QUESTION,
    RESULT
}

private data class QuizQuestion(
    val target: Weapon,
    val options: List<Weapon>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizScreen(
    progressManager: ProgressManager
) {
    var currentState by remember { mutableStateOf(QuizState.SETUP) }

    // 設定項目
    var selectedCountry by remember { mutableStateOf("all") }
    var selectedCategory by remember { mutableStateOf("all") }
    var selectedCount by remember { mutableStateOf("10") }

    // クイズセッション状態
    var questions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }

    val allWeapons = WeaponsRepository.weapons
    val filteredWeapons = remember(selectedCountry, selectedCategory) {
        allWeapons.filter { weapon ->
            (selectedCountry == "all" || weapon.country == selectedCountry) &&
            (selectedCategory == "all" || weapon.category == selectedCategory)
        }
    }

    when (currentState) {
        QuizState.SETUP -> {
            QuizSetupView(
                selectedCountry = selectedCountry,
                onCountrySelect = { selectedCountry = it },
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it },
                selectedCount = selectedCount,
                onCountSelect = { selectedCount = it },
                matchingCount = filteredWeapons.size,
                onStart = {
                    val count = when (selectedCount) {
                        "5" -> 5
                        "10" -> 10
                        "20" -> 20
                        else -> filteredWeapons.size
                    }
                    val targets = filteredWeapons.shuffled().take(count)
                    questions = targets.map { target ->
                        // ダミー選択肢3つを抽出（同じ国・カテゴリを優先、足りなければ全体から）
                        val candidatesSameCat = allWeapons.filter { it.id != target.id && it.category == target.category }
                        val candidatesOther = allWeapons.filter { it.id != target.id && it.category != target.category }
                        val dummies = (candidatesSameCat.shuffled() + candidatesOther.shuffled()).take(3)
                        val opts = (dummies + target).shuffled()
                        QuizQuestion(target, opts)
                    }
                    currentIndex = 0
                    selectedOptionIndex = null
                    score = 0
                    currentState = QuizState.QUESTION
                }
            )
        }
        QuizState.QUESTION -> {
            if (questions.isNotEmpty() && currentIndex < questions.size) {
                val q = questions[currentIndex]
                QuizQuestionView(
                    question = q,
                    currentIndex = currentIndex,
                    totalCount = questions.size,
                    selectedOptionIndex = selectedOptionIndex,
                    onSelectOption = { index ->
                        if (selectedOptionIndex == null) {
                            selectedOptionIndex = index
                            if (q.options[index].id == q.target.id) {
                                score++
                            }
                        }
                    },
                    onNext = {
                        if (currentIndex + 1 < questions.size) {
                            currentIndex++
                            selectedOptionIndex = null
                        } else {
                            currentState = QuizState.RESULT
                        }
                    },
                    onQuit = { currentState = QuizState.SETUP }
                )
            } else {
                currentState = QuizState.SETUP
            }
        }
        QuizState.RESULT -> {
            QuizResultView(
                score = score,
                totalCount = questions.size,
                onRetry = {
                    val count = when (selectedCount) {
                        "5" -> 5
                        "10" -> 10
                        "20" -> 20
                        else -> filteredWeapons.size
                    }
                    val targets = filteredWeapons.shuffled().take(count)
                    questions = targets.map { target ->
                        val candidatesSameCat = allWeapons.filter { it.id != target.id && it.category == target.category }
                        val candidatesOther = allWeapons.filter { it.id != target.id && it.category != target.category }
                        val dummies = (candidatesSameCat.shuffled() + candidatesOther.shuffled()).take(3)
                        val opts = (dummies + target).shuffled()
                        QuizQuestion(target, opts)
                    }
                    currentIndex = 0
                    selectedOptionIndex = null
                    score = 0
                    currentState = QuizState.QUESTION
                },
                onBackToSetup = { currentState = QuizState.SETUP }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuizSetupView(
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
                text = "4択識別テスト設定",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "写真と外観から兵器名称を識別するテストです",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 国
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "国・地域", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
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

        // 類別
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "兵器類別", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
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

        // 出題数
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "出題数", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
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

        // 件数表示
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "出題対象の兵器数",
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
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = matchingCount >= 4,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("テストを開始", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuizQuestionView(
    question: QuizQuestion,
    currentIndex: Int,
    totalCount: Int,
    selectedOptionIndex: Int?,
    onSelectOption: (Int) -> Unit,
    onNext: () -> Unit,
    onQuit: () -> Unit
) {
    val context = LocalContext.current
    val isAnswered = selectedOptionIndex != null
    val isCorrect = selectedOptionIndex?.let { question.options[it].id == question.target.id } == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ヘッダーバー
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
            Text(
                text = "第 ${currentIndex + 1} / $totalCount 問",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            // スペーサー代わりの透明幅
            Spacer(modifier = Modifier.size(48.dp))
        }

        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / totalCount.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary
        )

        // 画像エリア
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(question.target.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "識別対象画像",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                // バッジ
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(question.target.countryLabel, style = MaterialTheme.typography.labelSmall) }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text(question.target.categoryLabel, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        Text(
            text = "この兵器の名称として正しいものを選択してください",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 4択選択肢
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            question.options.forEachIndexed { index, option ->
                val isTarget = option.id == question.target.id
                val isSelected = selectedOptionIndex == index

                val (containerColor, contentColor, borderColor) = when {
                    !isAnswered -> Triple(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.onSurface,
                        MaterialTheme.colorScheme.outlineVariant
                    )
                    isTarget -> Triple(
                        Color(0xFFE8F5E9), // 薄い緑
                        Color(0xFF1B5E20), // 濃い緑
                        Color(0xFF4CAF50)
                    )
                    isSelected -> Triple(
                        Color(0xFFFFEBEE), // 薄い赤
                        Color(0xFFB71C1C), // 濃い赤
                        Color(0xFFF44336)
                    )
                    else -> Triple(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.colorScheme.outlineVariant
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isAnswered) { onSelectOption(index) },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, borderColor),
                    colors = CardDefaults.cardColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(24.dp)
                        )
                        Column {
                            Text(
                                text = option.nameJa,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "NATO/別名: ${option.natoOrAlt}",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // 回答後の解説およびWikipedia遷移ボタン
        if (isAnswered) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isCorrect) "正解" else "不正解",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                    )
                    Text(
                        text = "正解: ${question.target.nameJa} (${question.target.nameEn})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "識別ポイント: ${question.target.appearance}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    // Wikipediaで開くボタン
                    OutlinedButton(
                        onClick = {
                            val url = "https://en.wikipedia.org/wiki/${question.target.wikiTitle}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.padding(top = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Wikipediaで詳細を見る",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (currentIndex + 1 < totalCount) "次の問題へ" else "結果を見る",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuizResultView(
    score: Int,
    totalCount: Int,
    onRetry: () -> Unit,
    onBackToSetup: () -> Unit
) {
    val rate = if (totalCount > 0) (score.toFloat() / totalCount.toFloat() * 100).toInt() else 0

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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "テスト結果",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$score / $totalCount 問 正解",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "正答率: $rate%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("同じ条件で再挑戦", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackToSetup,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("条件設定に戻る")
        }
    }
}
