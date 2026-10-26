package com.example.militaryid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.militaryid.data.ProgressManager
import com.example.militaryid.ui.screens.*
import com.example.militaryid.ui.theme.MilitaryIDTheme

enum class AppTab(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "ホーム", Icons.Default.Home),
    FLASHCARD("flashcard", "暗記", Icons.Default.Style),
    QUIZ("quiz", "テスト", Icons.Default.Quiz),
    GALLERY("gallery", "図鑑", Icons.AutoMirrored.Filled.MenuBook),
    STATS("stats", "統計", Icons.Default.BarChart)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val progressManager = ProgressManager(this)

        setContent {
            MilitaryIDTheme {
                var currentTab by rememberSaveable { mutableStateOf(AppTab.HOME) }

                // ホーム以外にいるときは戻るボタンでホームに戻る
                BackHandler(enabled = currentTab != AppTab.HOME) {
                    currentTab = AppTab.HOME
                }

                Scaffold(
                    // ノッチとの干渉を防ぐためTopAppBarは設置せず、画面側でインセット制御を行う
                    bottomBar = {
                        NavigationBar {
                            AppTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = { currentTab = tab },
                                    icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        when (currentTab) {
                            AppTab.HOME -> HomeScreen(
                                progressManager = progressManager,
                                onNavigate = { targetRoute ->
                                    AppTab.entries.find { it.route == targetRoute }?.let {
                                        currentTab = it
                                    }
                                }
                            )
                            AppTab.FLASHCARD -> FlashcardScreen(
                                progressManager = progressManager
                            )
                            AppTab.QUIZ -> QuizScreen(
                                progressManager = progressManager
                            )
                            AppTab.GALLERY -> GalleryScreen(
                                progressManager = progressManager
                            )
                            AppTab.STATS -> StatsScreen(
                                progressManager = progressManager
                            )
                        }
                    }
                }
            }
        }
    }
}
