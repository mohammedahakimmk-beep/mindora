package com.mindora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mindora.app.ui.navigation.MindoraNavGraph
import com.mindora.app.ui.components.VersionWatermark
import com.mindora.app.ui.screens.ForceUpdateGate
import com.mindora.app.ui.theme.MindoraTheme
import com.mindora.app.ui.theme.NightSky
import androidx.compose.foundation.layout.Box

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NightSky
                ) {
                    Box(Modifier.fillMaxSize()) {
                        ForceUpdateGate {
                            MindoraNavGraph()
                        }
                        VersionWatermark()
                    }
                }
            }
        }
    }
}
