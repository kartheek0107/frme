package com.fibcam.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(onGranted: @Composable () -> Unit) {
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)

    if (cameraPermission.status.isGranted) {
        onGranted()
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Camera permission is required")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                Text("Grant Permission")
            }
        }
    }
}