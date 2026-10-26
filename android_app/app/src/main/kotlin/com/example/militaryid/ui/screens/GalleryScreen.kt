package com.example.militaryid.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.militaryid.data.LearningStatus
import com.example.militaryid.data.ProgressManager
import com.example.militaryid.data.Weapon
import com.example.militaryid.data.WeaponsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    progressManager: ProgressManager
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("all") }
    var selectedCategory by remember { mutableStateOf("all") }

    var expandedCountryMenu by remember { mutableStateOf(false) }
    var expandedCategoryMenu by remember { mutableStateOf(false) }

    var selectedWeaponForDetail by remember { mutableStateOf<Weapon?>(null) }

    val allWeapons = WeaponsRepository.weapons
    val filteredWeapons = remember(searchQuery, selectedCountry, selectedCategory) {
        allWeapons.filter { weapon ->
            val matchCountry = selectedCountry == "all" || weapon.country == selectedCountry
            val matchCategory = selectedCategory == "all" || weapon.category == selectedCategory
            val matchQuery = searchQuery.isBlank() ||
                weapon.nameJa.contains(searchQuery, ignoreCase = true) ||
                weapon.nameEn.contains(searchQuery, ignoreCase = true) ||
                weapon.natoOrAlt.contains(searchQuery, ignoreCase = true) ||
                weapon.appearance.contains(searchQuery, ignoreCase = true)
            matchCountry && matchCategory && matchQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // タイトル
        Text(
            text = "兵器図鑑",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 検索窓
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("兵器名、NATO名、特徴で検索") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "クリア")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // 省スペースなプルダウン式フィルター行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 国フィルター
            val countryLabels = mapOf("all" to "国: すべて", "russia" to "国: ロシア", "china" to "国: 中国")
            ExposedDropdownMenuBox(
                expanded = expandedCountryMenu,
                onExpandedChange = { expandedCountryMenu = !expandedCountryMenu },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = countryLabels[selectedCountry] ?: "国: すべて",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCountryMenu) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    textStyle = MaterialTheme.typography.bodySmall,
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedCountryMenu,
                    onDismissRequest = { expandedCountryMenu = false }
                ) {
                    countryLabels.forEach { (code, label) ->
                        DropdownMenuItem(
                            text = { Text(label, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                selectedCountry = code
                                expandedCountryMenu = false
                            }
                        )
                    }
                }
            }

            // 類別フィルター
            val categoryLabels = mapOf(
                "all" to "類別: すべて",
                "aircraft" to "戦闘機・航空機",
                "firearm" to "小火器",
                "artillery" to "火砲・防空",
                "missile" to "ミサイル",
                "bomb" to "誘導爆弾"
            )
            ExposedDropdownMenuBox(
                expanded = expandedCategoryMenu,
                onExpandedChange = { expandedCategoryMenu = !expandedCategoryMenu },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = categoryLabels[selectedCategory] ?: "類別: すべて",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryMenu) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    textStyle = MaterialTheme.typography.bodySmall,
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedCategoryMenu,
                    onDismissRequest = { expandedCategoryMenu = false }
                ) {
                    categoryLabels.forEach { (code, label) ->
                        DropdownMenuItem(
                            text = { Text(label, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                selectedCategory = code
                                expandedCategoryMenu = false
                            }
                        )
                    }
                }
            }
        }

        // 件数表示
        Text(
            text = "該当: ${filteredWeapons.size} / ${allWeapons.size} 件",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 2列グリッド
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredWeapons, key = { it.id }) { weapon ->
                val status = progressManager.getStatus(weapon.id)
                WeaponGridCard(
                    weapon = weapon,
                    status = status,
                    onClick = { selectedWeaponForDetail = weapon }
                )
            }
        }
    }

    // 詳細モーダルダイアログ
    selectedWeaponForDetail?.let { weapon ->
        WeaponDetailDialog(
            weapon = weapon,
            progressManager = progressManager,
            onDismiss = { selectedWeaponForDetail = null }
        )
    }
}

@Composable
private fun WeaponGridCard(
    weapon: Weapon,
    status: LearningStatus,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(weapon.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = weapon.nameJa,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // 習得状況バッジ
                val (badgeText, badgeColor) = when (status) {
                    LearningStatus.MASTERED -> "◯" to MaterialTheme.colorScheme.primary
                    LearningStatus.REVIEWING -> "△" to MaterialTheme.colorScheme.tertiary
                    LearningStatus.LEARNING -> "✕" to MaterialTheme.colorScheme.error
                    LearningStatus.UNSEEN -> "-" to MaterialTheme.colorScheme.outline
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = weapon.nameJa,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = weapon.natoOrAlt,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = weapon.countryLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = weapon.categoryLabel.take(4),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun WeaponDetailDialog(
    weapon: Weapon,
    progressManager: ProgressManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var currentStatus by remember { mutableStateOf(progressManager.getStatus(weapon.id)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // クローズボタンとタイトルバー
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "兵器詳細",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "閉じる")
                    }
                }

                // スクロール可能な本文
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 画像
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(weapon.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = weapon.nameJa,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // バッジと名称
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(onClick = {}, label = { Text(weapon.countryLabel) })
                        SuggestionChip(onClick = {}, label = { Text(weapon.categoryLabel) })
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = weapon.nameJa,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${weapon.nameEn} / NATO: ${weapon.natoOrAlt}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = weapon.type,
                            style = MaterialTheme.typography.labelLarge,
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
                                style = MaterialTheme.typography.titleSmall,
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
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "主な特徴", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        weapon.features.forEach { feat ->
                            Text(text = "• $feat", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // スペック
                    if (weapon.specs.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "主要スペック", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            weapon.specs.forEach { (k, v) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = k, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = v, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // ライセンス情報
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "画像ライセンス", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(text = "作者: ${weapon.licenseAuthor}", style = MaterialTheme.typography.labelSmall)
                            Text(text = "ライセンス: ${weapon.licenseType}", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Wikipediaで開くボタン
                    OutlinedButton(
                        onClick = {
                            val url = "https://en.wikipedia.org/wiki/${weapon.wikiTitle}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Wikipediaで詳細記事を開く")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 暗記ステータスの手動変更
                Text(text = "学習状態の登録", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statuses = listOf(
                        Triple(LearningStatus.LEARNING, "✕ 未習得", MaterialTheme.colorScheme.error),
                        Triple(LearningStatus.REVIEWING, "△ 要復習", MaterialTheme.colorScheme.tertiary),
                        Triple(LearningStatus.MASTERED, "◯ 習得", MaterialTheme.colorScheme.primary)
                    )
                    statuses.forEach { (st, label, col) ->
                        val isSelected = currentStatus == st
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                progressManager.setStatus(weapon.id, st)
                                currentStatus = st
                            },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
