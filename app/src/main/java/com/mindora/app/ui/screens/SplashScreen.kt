package com.mindora.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mindora.app.R
import com.mindora.app.ui.theme.Ember
import com.mindora.app.ui.theme.StarGold
import com.mindora.app.ui.theme.TealLight
import com.mindora.app.ui.theme.WarmSand
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigate: (Boolean) -> Unit, isLoggedIn: Boolean) {
    LaunchedEffect(Unit) {
        delay(1500)
        onNavigate(isLoggedIn)
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✦", style = MaterialTheme.typography.displayLarge, color = StarGold)
        Spacer(Modifier.height(16.dp))
        Text("Mindora", style = MaterialTheme.typography.displayLarge, color = TealLight)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.splash_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = WarmSand,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Text("Forging knowledge among the stars…", style = MaterialTheme.typography.bodyMedium, color = Ember.copy(0.7f))
    }
}
