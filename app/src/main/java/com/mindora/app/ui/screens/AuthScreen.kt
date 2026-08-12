package com.mindora.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.mindora.app.BuildConfig
import com.mindora.app.R
import com.mindora.app.ui.components.ForgeButton
import com.mindora.app.ui.components.ForgeOutlinedButton
import com.mindora.app.ui.components.ForgeTextField
import com.mindora.app.ui.components.LoadingScreen
import com.mindora.app.ui.theme.Ember
import com.mindora.app.ui.theme.StarGold
import com.mindora.app.ui.theme.TealLight
import com.mindora.app.ui.theme.WarmSand
import com.mindora.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onAuthenticated: () -> Unit, viewModel: AuthViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (state.isLoading) {
        LoadingScreen()
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✦", style = MaterialTheme.typography.displayLarge, color = StarGold)
        Spacer(Modifier.height(8.dp))
        Text("Mindora", style = MaterialTheme.typography.headlineLarge, color = TealLight)
        Spacer(Modifier.height(4.dp))
        Text(
            if (state.isSignUp) "Create your forge account" else "Welcome back, apprentice",
            color = WarmSand.copy(0.8f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        if (state.isSignUp) {
            ForgeTextField(state.displayName, viewModel::updateDisplayName, "Display Name")
            Spacer(Modifier.height(12.dp))
        }
        ForgeTextField(state.email, viewModel::updateEmail, stringResource(R.string.email))
        Spacer(Modifier.height(12.dp))
        ForgeTextField(state.password, viewModel::updatePassword, stringResource(R.string.password))
        Spacer(Modifier.height(8.dp))

        state.error?.let {
            Text(it, color = Ember, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(16.dp))
        ForgeButton(
            if (state.isSignUp) stringResource(R.string.sign_up) else stringResource(R.string.sign_in)
        ) { viewModel.signInWithEmail(onAuthenticated) }
        Spacer(Modifier.height(12.dp))
        ForgeOutlinedButton(stringResource(R.string.continue_with_google)) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                        .build()
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()
                    val result = CredentialManager.create(context).getCredential(context, request)
                    val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
                    viewModel.signInWithGoogle(credential.idToken, onAuthenticated)
                } catch (e: Exception) {
                    viewModel.clearError()
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        ForgeOutlinedButton(
            if (state.isSignUp) "Already have an account? Sign In" else "New here? Create Account",
            onClick = viewModel::toggleSignUp
        )
    }
}
