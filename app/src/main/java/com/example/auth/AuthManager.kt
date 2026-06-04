package com.example.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        return auth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun tryGoogleSignIn(activityContext: Context): AuthResult? {
        val credentialManager = CredentialManager.create(activityContext)
        
        val webClientId = activityContext.getString(com.example.R.string.default_web_client_id)
        if (webClientId == "YOUR_WEB_CLIENT_ID_HERE" || webClientId.isBlank()) {
            throw Exception("Web Client ID not configured. Generate SHA-1, add it to Firebase, and update strings.xml")
        }
        
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setNonce(java.util.UUID.randomUUID().toString())
            .setAutoSelectEnabled(false)
            .build()
            
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
            
        return try {
            val activity = activityContext as? android.app.Activity 
                ?: generateSequence(activityContext) { (it as? android.content.ContextWrapper)?.baseContext }.filterIsInstance<android.app.Activity>().firstOrNull()
                ?: activityContext
            val result = credentialManager.getCredential(activity, request)
            handleSignIn(result)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): AuthResult? {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                return auth.signInWithCredential(firebaseCredential).await()
            } catch (e: GoogleIdTokenParsingException) {
                e.printStackTrace()
                return null
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
        return null
    }

    suspend fun updateProfile(name: String, photoUrl: String) {
        val user = auth.currentUser ?: return
        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .setPhotoUri(if (photoUrl.isNotBlank()) android.net.Uri.parse(photoUrl) else null)
            .build()
        user.updateProfile(profileUpdates).await()
        _currentUser.value = auth.currentUser
    }

    fun signOut() {
        auth.signOut()
    }
}
