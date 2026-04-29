package com.fibcam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fibcam.ui.navigation.FibCamNavHost
import com.fibcam.ui.theme.FibCamTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Full bleed — camera viewfinder goes edge to edge
        enableEdgeToEdge()

        setContent {
            FibCamTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FibCamNavHost()
                }
            }
        }
    }
}