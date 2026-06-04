package com.example.data

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageManager {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun uploadProfileImage(userId: String, imageUri: Uri): String {
        val userRef = storageRef.child("users/$userId/profile.jpg")
        userRef.putFile(imageUri).await()
        return userRef.downloadUrl.await().toString()
    }

    suspend fun uploadReceipt(userId: String, expenseId: String, imageUri: Uri): String {
        val receiptRef = storageRef.child("users/$userId/receipts/$expenseId.jpg")
        receiptRef.putFile(imageUri).await()
        return receiptRef.downloadUrl.await().toString()
    }
}
