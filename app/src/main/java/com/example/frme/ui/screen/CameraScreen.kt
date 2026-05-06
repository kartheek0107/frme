package com.fibcam.ui.screen

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.fibcam.viewmodel.CameraViewModel

@Composable
fun CameraScreen(viewModel: CameraViewModel = hiltViewModel()) {
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    viewModel.bindCamera(lifecycleOwner, previewView)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}