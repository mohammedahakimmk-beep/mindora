package com.mindora.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindora.app.BuildConfig
import com.mindora.app.ui.theme.WarmSand

@Composable
fun VersionWatermark(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
            color = WarmSand.copy(alpha = 0.45f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
