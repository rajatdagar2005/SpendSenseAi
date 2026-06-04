package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

import com.example.data.db.ChatEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCoachScreen(viewModel: MainViewModel) {
    val insight by viewModel.aiInsight.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    var inputText by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (chatHistory.isEmpty()) {
            viewModel.sendInitialCoachMessage("Hello! I am your AI Financial Coach. Ask me how to save more, where you're overspending, or any other finance questions.")
        }
    }

    // When the top-level insight updates (from the analyze button), add it to chat
    LaunchedEffect(insight) {
        if (insight.isNotBlank() && insight != "Tap 'Analyze' to get AI insights on your spending.") {
            viewModel.sendInitialCoachMessage(insight)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("SpendSense AI Coach", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Medium, color = Color.White)
        }

        // Chat View
        var showTypingIndicator by remember { mutableStateOf(false) }
        
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = true
        ) {
            if (isAnalyzing || showTypingIndicator) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.CenterStart) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
            items(chatHistory.reversed()) { message ->
                ChatBubble(message)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask about your spending...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { 
                    if (inputText.isNotBlank()) {
                        val q = inputText
                        inputText = ""
                        showTypingIndicator = true
                        coroutineScope.launch {
                            viewModel.askCoach(q)
                            showTypingIndicator = false
                        }
                    }
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatEntity) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(
                        topStart = 16.dp, 
                        topEnd = 16.dp, 
                        bottomStart = if (message.isUser) 16.dp else 4.dp, 
                        bottomEnd = if (message.isUser) 4.dp else 16.dp
                    )
                )
                .padding(16.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isUser) MaterialTheme.colorScheme.onPrimary else Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
