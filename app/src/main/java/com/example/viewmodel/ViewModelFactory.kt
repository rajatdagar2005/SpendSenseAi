package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.auth.AuthManager
import com.example.data.StorageManager
import com.example.repository.ExpenseRepository
import com.google.firebase.firestore.FirebaseFirestore

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            val firestore = FirebaseFirestore.getInstance()
            val repository = ExpenseRepository(firestore)
            val authManager = AuthManager(context)
            val storageManager = StorageManager()
            val chatDao = com.example.data.db.AppDatabase.getDatabase(context).chatDao()
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, authManager, storageManager, chatDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
