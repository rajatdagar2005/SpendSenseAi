package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: MainViewModel, onNavigateToNext: (String) -> Unit) {
    LaunchedEffect(key1 = true) {
        delay(2000)
        if (viewModel.isUserLoggedIn()) {
            onNavigateToNext("dashboard")
        } else {
            onNavigateToNext("onboarding")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "SpendSense AI",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Smart Financial Intelligence",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
        }
    }
}
