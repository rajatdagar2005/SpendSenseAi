package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntity
import com.example.viewmodel.MainViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val leakInsight by viewModel.dashboardLeak.collectAsState()
    val goalInsight by viewModel.dashboardGoal.collectAsState()
    
    // Refresh insights when expenses are loaded for the first time
    androidx.compose.runtime.LaunchedEffect(expenses.size) {
        if (expenses.isNotEmpty() && leakInsight == "Analyzing...") {
            viewModel.refreshDashboardInsights()
        }
    }

    val balance = totalIncome - totalSpent
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 16.dp)) {
        
        HeaderSection(viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        // Balance Card styled like Elegant Dark html
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total Balance", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(currencyFormat.format(balance), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Light, color = Color.White)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    MetricItem(
                        title = "Income", 
                        amount = currencyFormat.format(totalIncome), 
                        accentColor = MaterialTheme.colorScheme.secondary
                    )
                    MetricItem(
                        title = "Spent", 
                        amount = currencyFormat.format(totalSpent), 
                        accentColor = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth().height(128.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // AI Leak Detector Card
            Card(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Text("⚡", fontSize = 16.sp)
                    }
                    Column {
                        Text("AI LEAK DETECTOR", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            leakInsight, 
                            style = MaterialTheme.typography.bodySmall, 
                            color = Color.White,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Savings Goal Card
            Card(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Box(modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Text("💰", fontSize = 16.sp)
                    }
                    Column {
                        Text("SAVINGS GOAL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            goalInsight, 
                            style = MaterialTheme.typography.bodySmall, 
                            color = Color.White,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recent Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = Color.White)
            Text("View All", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (expenses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions yet.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(expenses) { expense ->
                    TransactionItem(expense, onDelete = { viewModel.deleteExpense(expense) })
                }
            }
        }
    }
}

@Composable
fun HeaderSection(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val displayName = currentUser?.displayName ?: "User"
    val initial = displayName.take(1).uppercase()
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("SPENDSENSE AI", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp)
            Text("Hello, $displayName", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = Color.White)
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (currentUser?.photoUrl != null) {
                coil.compose.AsyncImage(
                    model = currentUser?.photoUrl.toString(),
                    contentDescription = "Profile Image",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(initial, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun MetricItem(title: String, amount: String, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(amount, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = accentColor)
    }
}

@Composable
fun TransactionItem(expense: ExpenseEntity, onDelete: () -> Unit) {
    val isExpense = expense.type == "EXPENSE"
    val color = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    val prefix = if (isExpense) "-" else "+"
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), 
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (isExpense) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp)), 
                contentAlignment = Alignment.Center
            ) {
                Text(expense.category.take(1).uppercase(), fontWeight = FontWeight.Bold, color = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.merchant, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Text(expense.category + " • " + dateFormat.format(expense.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(prefix + currencyFormat.format(expense.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
