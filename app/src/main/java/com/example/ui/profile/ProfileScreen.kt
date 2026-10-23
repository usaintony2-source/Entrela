package com.example.ui.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.EntrelaViewModel

/**
 * Top-level Profile screen displaying the user's profile view with Firebase Auth account management.
 */
@Composable
fun ProfileScreen(
    viewModel: EntrelaViewModel,
    modifier: Modifier = Modifier
) {
    ProfileView(
        viewModel = viewModel,
        modifier = modifier.fillMaxSize()
    )
}
