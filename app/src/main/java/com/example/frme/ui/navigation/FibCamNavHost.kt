package com.fibcam.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fibcam.ui.screen.CameraScreen
import com.fibcam.ui.screen.PermissionScreen

@Composable
fun FibCamNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "camera") {
        composable("camera") {
            PermissionScreen {
                CameraScreen()
            }
        }
    }
}