package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var receiptUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> receiptUri = uri }
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Add Transaction", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            FilterChip(
                selected = isExpense,
                onClick = { isExpense = true },
                label = { Text("Expense") },
                modifier = Modifier.padding(end = 8.dp)
            )
            FilterChip(
                selected = !isExpense,
                onClick = { isExpense = false },
                label = { Text("Income") }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("$") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = merchant,
            onValueChange = { merchant = it },
            label = { Text(if (isExpense) "Merchant" else "Source") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category (e.g. Food, Transport)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { imagePicker.launch("image/*") }) {
                Text(if (receiptUri == null) "Attach Receipt" else "Receipt Attached")
            }
            if (receiptUri != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { receiptUri = null }) {
                    Text("Remove")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val amountVal = amount.toDoubleOrNull() ?: 0.0
                if (amountVal > 0 && merchant.isNotBlank() && category.isNotBlank() && !isSaving) {
                    isSaving = true
                    viewModel.addExpense(
                        amount = amountVal,
                        category = category,
                        merchant = merchant,
                        description = description,
                        type = if (isExpense) "EXPENSE" else "INCOME",
                        receiptUri = receiptUri
                    )
                    // Normally we should wait for success, but for UI responsiveness we can pop
                    // However, we added isSaving to prevent double taps
                    onNavigateBack()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Save Transaction")
            }
        }
    }
}
