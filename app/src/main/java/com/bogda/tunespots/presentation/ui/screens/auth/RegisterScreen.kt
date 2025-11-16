package com.bogda.tunespots.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bogda.tunespots.presentation.auth.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    // State for all form fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    // var imageUri by remember { mutableStateOf<Uri?>(null) } // For when you implement image picking

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Listen for state changes from the ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> {
                Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                onRegisterSuccess()
                viewModel.resetState()
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Profile Image Picker
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable {
                        // TODO: Launch image picker (from gallery or camera)
                    },
                contentAlignment = Alignment.Center
            ) {
                // We'll show the selected image here later. For now, just an icon.
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Add profile picture",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(Modifier.height(24.dp))

            // Form Fields
            val fields = listOf(
                "Email" to email,
                "Password" to password,
                "Username" to username,
                "First Name" to firstName,
                "Last Name" to lastName,
                "Phone Number" to phoneNumber
            )

            fields.forEachIndexed { index, (label, value) ->
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        when(label) {
                            "Email" -> email = it
                            "Password" -> password = it
                            "Username" -> username = it
                            "First Name" -> firstName = it
                            "Last Name" -> lastName = it
                            "Phone Number" -> phoneNumber = it
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(label) },
                    singleLine = true,
                    visualTransformation = if (label == "Password") PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = when(label) {
                        "Email" -> KeyboardOptions(keyboardType = KeyboardType.Email)
                        "Password" -> KeyboardOptions(keyboardType = KeyboardType.Password)
                        "Phone Number" -> KeyboardOptions(keyboardType = KeyboardType.Phone)
                        else -> KeyboardOptions.Default
                    }
                )
                if(index < fields.lastIndex) {
                    Spacer(Modifier.height(16.dp))
                }
            }

            Spacer(Modifier.height(32.dp))

            // Submit Button
            FilledTonalButton(
                onClick = {
                    viewModel.registerUser(
                        email, password, username, firstName, lastName, phoneNumber, imageUri = null // Pass the actual URI later
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AuthUiState.Loading
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
