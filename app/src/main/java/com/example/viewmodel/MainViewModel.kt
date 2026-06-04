package com.example.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.ai.GeminiService
import com.example.data.StorageManager
import com.example.data.model.ExpenseEntity
import com.example.repository.ExpenseRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.db.dao.ChatDao
import com.example.data.db.ChatEntity

class MainViewModel(
    private val repository: ExpenseRepository,
    val authManager: AuthManager,
    val storageManager: StorageManager,
    private val chatDao: ChatDao
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authManager.currentUser

    fun isUserLoggedIn(): Boolean = authManager.isUserLoggedIn()

    fun logout() {
        authManager.signOut()
    }

    fun updateProfile(name: String, photoUrl: String?, imageUri: Uri? = null) {
        viewModelScope.launch {
            try {
                var finalPhotoUrl = photoUrl ?: currentUser.value?.photoUrl?.toString() ?: ""
                
                if (imageUri != null) {
                    val user = currentUser.value
                    if (user != null) {
                        finalPhotoUrl = storageManager.uploadProfileImage(user.uid, imageUri)
                    }
                }
                
                authManager.updateProfile(name, finalPhotoUrl)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses: StateFlow<List<ExpenseEntity>> = currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getExpenses(user.uid)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalSpent: StateFlow<Double> = expenses.map { list ->
        list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalIncome: StateFlow<Double> = expenses.map { list ->
        list.filter { it.type == "INCOME" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    private val _aiInsight = MutableStateFlow("Tap 'Analyze' to get AI insights on your spending.")
    val aiInsight: StateFlow<String> = _aiInsight.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    fun addExpense(amount: Double, category: String, merchant: String, description: String, type: String = "EXPENSE", receiptUri: Uri? = null) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            try {
                var receiptUrl: String? = null
                val tempId = System.currentTimeMillis().toString()
                if (receiptUri != null) {
                    receiptUrl = storageManager.uploadReceipt(user.uid, tempId, receiptUri)
                }
                repository.addExpense(user.uid, amount, category, merchant, description, System.currentTimeMillis(), type, receiptUrl)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    private val _dashboardLeak = MutableStateFlow("Analyzing...")
    val dashboardLeak: StateFlow<String> = _dashboardLeak.asStateFlow()

    private val _dashboardGoal = MutableStateFlow("Calculating goal...")
    val dashboardGoal: StateFlow<String> = _dashboardGoal.asStateFlow()

    fun refreshDashboardInsights() {
        viewModelScope.launch {
            val currentExpenses = expenses.value.take(50).joinToString("\n") { 
                "${it.date} - ${it.merchant} (${it.category}): $${it.amount} [${it.type}]"
            }
            if (currentExpenses.isBlank()) {
                _dashboardLeak.value = "No expenses to scan for leaks."
                _dashboardGoal.value = "Save $100 this month."
                return@launch
            }
            _dashboardLeak.value = "Scanning..."
            _dashboardGoal.value = "Calculating..."
            
            val prompt = "Analyze the following expenses. Provide a short 1-sentence money leak alert (starting with 'LEAK: ') and a short 1-sentence savings goal (starting with 'GOAL: '). Use currency symbols.\n$currentExpenses"
            try {
                // Inline call using the raw gemini service to avoid adding too much logic inside GeminiService
                val result = GeminiService.askFinancialCoach("Provide the short summary as requested", currentExpenses + "\n\n" + prompt)
                var leak = result.lines().find { it.contains("LEAK:") } ?: "No major leaks found."
                var goal = result.lines().find { it.contains("GOAL:") } ?: "Try to save 20% of your income."
                leak = leak.replace("LEAK:", "").trim()
                goal = goal.replace("GOAL:", "").trim()
                _dashboardLeak.value = leak
                _dashboardGoal.value = goal
            } catch (e: Exception) {
                _dashboardLeak.value = "Leak analysis failed."
                _dashboardGoal.value = "Goal calculation failed."
            }
        }
    }

    val chatHistory: StateFlow<List<ChatEntity>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    suspend fun askCoach(question: String): String {
        val currentExpenses = expenses.value.take(50).joinToString("\n") { 
            "${it.date} - ${it.merchant} (${it.category}): $${it.amount} [${it.type}]"
        }
        val response = try {
            GeminiService.askFinancialCoach(question, currentExpenses)
        } catch (e: Exception) {
            "Failed to contact coach: ${e.localizedMessage}"
        }
        
        chatDao.insertMessage(ChatEntity(text = question, isUser = true))
        chatDao.insertMessage(ChatEntity(text = response, isUser = false))
        
        return response
    }
    
    fun sendInitialCoachMessage(message: String) {
        viewModelScope.launch {
            if (chatHistory.value.isEmpty()) {
                chatDao.insertMessage(ChatEntity(text = message, isUser = false))
            }
        }
    }

    fun analyzeSpending() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val currentExpenses = expenses.value.take(50).joinToString("\n") { 
                    "${it.date} - ${it.merchant} (${it.category}): $${it.amount} [${it.type}] - ${it.description}"
                }
                
                if (currentExpenses.isBlank()) {
                    _aiInsight.value = "Not enough data to analyze. Add some transactions first."
                } else {
                    val insight = GeminiService.analyzeSpending(currentExpenses)
                    _aiInsight.value = insight
                }
            } finally {
                _isAnalyzing.value = false
            }
        }
    }
}
