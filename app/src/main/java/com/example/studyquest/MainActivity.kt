package com.example.studyquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studyquest.ui.theme.LowPolyForestBackground
import com.example.studyquest.ui.theme.StudyQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is always dark now, so tell Android to draw LIGHT status/navigation bar icons
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            StudyQuestTheme {
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
    val profileViewModel: ProfileViewModel = viewModel()

    // The forest is drawn first, the app sits on top of it
    Box(modifier = Modifier.fillMaxSize()) {
        LowPolyForestBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,   // let the forest show through
            // With a transparent container Compose can't guess the text colour,
            // so we set it ourselves (otherwise text can turn black)
            contentColor = MaterialTheme.colorScheme.onBackground,
            bottomBar = {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            )
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
                    Tab.PROFILE -> ProfileScreen(
                        viewModel = profileViewModel,
                        questXp = tasksViewModel.quests.filter { it.isCompleted }.sumOf { it.xp }
                    )
                }
            }
        }
    }
}