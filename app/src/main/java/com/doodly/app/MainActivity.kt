package com.doodly.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.doodly.app.ui.navigation.DoodlyNavGraph
import com.doodly.app.ui.theme.DoodlyTheme

class MainActivity : ComponentActivity() {
    private var deepLinkIntent by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkIntent = intent
        enableEdgeToEdge()
        setContent {
            DoodlyTheme {
                val navController = rememberNavController()
                LaunchedEffect(deepLinkIntent) {
                    deepLinkIntent?.let(navController::handleDeepLink)
                }
                DoodlyNavGraph(
                    navController = navController,
                    container = (application as DoodlyApp).container
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkIntent = intent
    }
}
