package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkTheme by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )
        
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Preferences", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            
            ListItem(
                headlineContent = { Text("Enable Notifications", color = Color.White) },
                supportingContent = { Text("Budget alerts and weekly reports", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingContent = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
            
            ListItem(
                headlineContent = { Text("Dark Theme", color = Color.White) },
                supportingContent = { Text("Requires Elegant Dark mode", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingContent = {
                    Switch(
                        checked = darkTheme,
                        onCheckedChange = { darkTheme = it }
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Data & Privacy", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            
            ListItem(
                headlineContent = { Text("Clear Local Cache", color = Color.White) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}
