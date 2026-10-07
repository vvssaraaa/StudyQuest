package com.example.studyquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studyquest.ui.theme.StudyQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyQuestTheme {
                StudyQuestApp()
            }
        }
    }
}

@Composable
fun StudyQuestApp(){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ){
        Text(
            text = "StudyQuest",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { 0.0f },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "0 / 100 xp"
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "🔥 0 day streak",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Today's quests",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))

        QuestCard(
            title = "Study algorithms",
            subject = "Algdat",
            xp = 30
        )
        Spacer(modifier = Modifier.height(8.dp))

        QuestCard(
            title = "Work on webapp project",
            subject = "Web Applications",
            xp = 50
        )
        Spacer(modifier = Modifier.height(8.dp))

        QuestCard(
            title = "Security quiz",
            subject = "Data Security",
            xp = 20
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                //
            },
            modifier = Modifier.fillMaxWidth()){
            Text("+ Add new quest!")
        }
    }
}
@Composable
fun QuestCard(title: String, subject: String, xp: Int){
    Card(
        modifier = Modifier.fillMaxWidth()
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Column{
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subject,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "+$xp XP",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}