package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.MainViewModel

@Composable
fun AnalyticsScreen(viewModel: MainViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    
    val categoryTotals = expenses
        .filter { it.type == "EXPENSE" }
        .groupBy { it.category }
        .mapValues { (_, items) -> items.sumOf { it.amount }.toFloat() }
        .toList()
        .sortedByDescending { it.second }
        
    val maxSpent = categoryTotals.sumOf { it.second.toDouble() }.toFloat()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Analytics & Insights", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Monthly Spending Trend", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Simple Compose Canvas Bar Chart Mock
                    val colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.error,
                        MaterialTheme.colorScheme.inversePrimary
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 16.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            
                            val topCat = categoryTotals.take(5)
                            if (topCat.isNotEmpty() && maxSpent > 0) {
                                val barWidth = width / (topCat.size * 2f)
                                val spacing = barWidth
                                var currentX = (width - (topCat.size * barWidth + (topCat.size - 1) * spacing)) / 2f
                                
                                topCat.forEachIndexed { index, (_, amount) ->
                                    val barHeight = height * (amount / maxSpent).coerceIn(0f, 1f)
                                    val color = if (index < colors.size) colors[index] else colors.last()
                                    
                                    drawRoundRect(
                                        color = color,
                                        topLeft = androidx.compose.ui.geometry.Offset(currentX, height - barHeight),
                                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                                    )
                                    currentX += barWidth + spacing
                                }
                            } else {
                                // Draw empty layout
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    start = androidx.compose.ui.geometry.Offset(0f, height),
                                    end = androidx.compose.ui.geometry.Offset(width, height),
                                    strokeWidth = 2f
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Top SPENDING CATEGORIES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (categoryTotals.isEmpty()) {
                        Text("No spending data available.", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    } else {
                        categoryTotals.take(5).forEachIndexed { index, (category, amount) ->
                            val progress = if (maxSpent > 0) amount / maxSpent else 0f
                            val color = when(index) {
                                0 -> MaterialTheme.colorScheme.primary
                                1 -> MaterialTheme.colorScheme.secondary
                                2 -> MaterialTheme.colorScheme.tertiary
                                3 -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.inversePrimary
                            }
                            CategoryProgress(category, progress, color)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryProgress(name: String, progress: Float, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    }
}
