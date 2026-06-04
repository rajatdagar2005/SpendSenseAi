package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.viewmodel.MainViewModel

@Composable
fun ProfileScreen(viewModel: MainViewModel, onLogout: () -> Unit) {
    val currentUser by viewModel.currentUser.collectAsState()
    
    val email = currentUser?.email ?: "guest@example.com"
    val initial = email.take(1).uppercase()
    val displayName = currentUser?.displayName ?: "SpendSense User"
    val photoUrl = currentUser?.photoUrl?.toString()

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(displayName) }
    var editPhotoUrl by remember { mutableStateOf(photoUrl ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .clip(CircleShape)
                .clickable { showEditDialog = true },
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(initial, style = MaterialTheme.typography.displayMedium, color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { showEditDialog = true }) {
            Text(displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().clickable {
                viewModel.logout()
                onLogout()
            },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            ListItem(
                headlineContent = { Text("Logout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.background(Color.Transparent)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }

    if (showEditDialog) {
        var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
        val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
        ) { uri: android.net.Uri? ->
            selectedImageUri = uri
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (selectedImageUri != null) "New Image Selected" else "Upload Image from Gallery")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateProfile(editName, null, selectedImageUri)
                    showEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
