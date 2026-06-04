package com.example.repository

import com.example.data.model.ExpenseEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ExpenseRepository(private val firestore: FirebaseFirestore) {

    fun getExpenses(userId: String): Flow<List<ExpenseEntity>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            return@callbackFlow
        }
        val subscription = firestore.collection("expenses")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("ExpenseRepository", "Error fetching expenses", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val expenses = snapshot.documents
                        .mapNotNull { it.toObject(ExpenseEntity::class.java)?.copy(id = it.id) }
                        .sortedByDescending { it.dateMillis }
                    trySend(expenses)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addExpense(
        userId: String,
        amount: Double,
        category: String,
        merchant: String,
        description: String,
        dateMillis: Long,
        type: String = "EXPENSE",
        receiptUrl: String? = null
    ) {
        if (userId.isBlank()) return
        val expense = ExpenseEntity(
            userId = userId,
            amount = amount,
            category = category,
            merchant = merchant,
            description = description,
            dateMillis = dateMillis,
            type = type,
            receiptUrl = receiptUrl
        )
        val docRef = firestore.collection("expenses").document()
        firestore.collection("expenses").document(docRef.id).set(expense.copy(id = docRef.id)).await()
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        if (expense.id.isNotBlank()) {
            firestore.collection("expenses").document(expense.id).delete().await()
        }
    }
}
