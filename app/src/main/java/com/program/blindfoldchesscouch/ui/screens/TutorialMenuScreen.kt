// ui/screens/TutorialMenuScreen.kt
package com.program.blindfoldchesscouch.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.program.blindfoldchesscouch.model.TutorialRepository
import com.program.blindfoldchesscouch.model.TutorialTopic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialMenuScreen(
    onTopicSelected: (String) -> Unit // Funkcija koja se poziva sa ID-jem teme
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Uputstvo - Sadržaj") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(TutorialRepository.topics) { topic ->
                TutorialTopicCard(topic = topic, onClick = {
                    onTopicSelected(topic.id)
                })
            }
        }
    }
}

@Composable
fun TutorialTopicCard(topic: TutorialTopic, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = topic.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = topic.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}