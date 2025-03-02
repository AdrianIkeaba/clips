package com.ghostdev.video.clips

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ghostdev.video.clips.di.initKoin
import com.ghostdev.video.clips.navigation.NavGraph
import com.ghostdev.video.clips.ui.theme.ClipsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initKoin(
            this
        )
        setContent {
            ClipsApp()
        }
    }
}

@Composable
private fun ClipsApp() {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val isDarkTheme by remember { mutableStateOf(isSystemInDarkTheme) }

    ClipsTheme(darkTheme = isDarkTheme) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
        ) { innerPadding ->
            NavGraph(
                modifier = Modifier
                    .padding(innerPadding),
                isDarkTheme = isDarkTheme
            )
        }
    }
}