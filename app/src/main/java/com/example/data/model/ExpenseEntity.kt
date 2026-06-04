package com.example.data.model

import com.google.firebase.firestore.Exclude
import java.util.Date

data class ExpenseEntity(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val merchant: String = "",
    val description: String = "",
    val dateMillis: Long = 0L,
    val type: String = "EXPENSE", // "EXPENSE" or "INCOME"
    val receiptUrl: String? = null
) {
    @get:Exclude
    val date: Date
        get() = Date(dateMillis)
}
