package com.example.studyquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.studyquest.ui.theme.StudyQuestTheme
import androidx.lifecycle.viewmodel.compose.viewModel



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyQuestTheme{
                StudyQuestApp()
            }
        }
    }
}

// The three tabs: label and icon for each
enum class Tab(val label: String, val icon: ImageVector) {
    QUESTS("Quests", Icons.Filled.CheckCircle),
    TIMER("Timer", Icons.Filled.PlayArrow),
    PROFILE("Profile", Icons.Filled.Person)
}

@Composable
fun StudyQuestApp() {
    // Which tab is open. rememberSaveable also keeps it across rotation.
    var selectedTab by rememberSaveable { mutableStateOf(Tab.QUESTS) }

    // Created here, at the top, so they live as long as the activity and
    // are NOT thrown away when you switch tabs.
    val tasksViewModel: TasksViewModel = viewModel()
    val timerViewModel: TimerViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        // innerPadding keeps the content clear of the status bar and the bottom bar
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                Tab.QUESTS -> TasksScreen(tasksViewModel)
                Tab.TIMER -> TimerScreen(timerViewModel)
                Tab.PROFILE -> ProfileScreen()
            }
        }
    }
}