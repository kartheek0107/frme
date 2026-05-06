package com.fibcam.ui.screen

import android.graphics.SurfaceTexture
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.fibcam.model.*
import com.fibcam.ui.overlay.OverlayCanvas
import com.fibcam.viewmodel.CameraViewModel
import kotlinx.coroutines.launch

@Composable
fun CameraScreen(viewModel: CameraViewModel = hiltViewModel()) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var showOverlayPicker by remember { mutableStateOf(false) }
    var showManualControls by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Camera Preview ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomRatio = (zoomRatio * zoom).coerceIn(
                            uiState.minZoom,
                            uiState.maxZoom
                        )
                        viewModel.setZoom(zoomRatio)
                    }
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).also { previewView ->
                        viewModel.bindCamera(lifecycleOwner, previewView)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // ── Overlay Canvas ──────────────────────────────────────────────
            OverlayCanvas(
                overlays = uiState.activeOverlays,
                color = Color(uiState.overlayColor.hexArgb),
                alpha = uiState.overlayAlpha,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ── Top Control Bar ─────────────────────────────────────────────────
        TopControlBar(
            isRawSupported = uiState.isRawSupported,
            isRawEnabled = uiState.isRawEnabled,
            flashMode = uiState.flashMode,
            onFlashChange = { viewModel.setFlashMode(it) },
            onSettingsClick = { showSettings = true },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )

        // ── Bottom Control Panel ────────────────────────────────────────────
        BottomControlPanel(
            isCapturing = uiState.isCapturing,
            zoomRatio = zoomRatio,
            minZoom = uiState.minZoom,
            maxZoom = uiState.maxZoom,
            onZoomChange = {
                zoomRatio = it
                viewModel.setZoom(it)
            },
            onCaptureClick = { viewModel.capturePhoto() },
            onOverlayClick = { showOverlayPicker = true },
            onManualControlsClick = { showManualControls = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        )

        // ── Center Zoom Indicator ───────────────────────────────────────────
        if (zoomRatio != 1f) {
            ZoomIndicator(
                zoomRatio = zoomRatio,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    // ── Overlay Picker Sheet ────────────────────────────────────────────────
    if (showOverlayPicker) {
        ModalBottomSheet(
            onDismissRequest = { showOverlayPicker = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxHeight(0.9f),
            containerColor = Color(0xFF111111),
            scrimColor = Color.Black.copy(alpha = 0.32f)
        ) {
            OverlayPickerSheet(
                activeOverlays = uiState.activeOverlays,
                overlayColor = uiState.overlayColor,
                overlayAlpha = uiState.overlayAlpha,
                onOverlayToggle = { viewModel.toggleOverlay(it) },
                onColorChange = { viewModel.setOverlayColor(it) },
                onAlphaChange = { viewModel.setOverlayAlpha(it) },
                onClearAll = { viewModel.clearOverlays() }
            )
        }
    }

    // ── Manual Controls Sheet ───────────────────────────────────────────────
    if (showManualControls && uiState.isRawSupported) {
        ModalBottomSheet(
            onDismissRequest = { showManualControls = false },
            containerColor = Color(0xFF111111),
            scrimColor = Color.Black.copy(alpha = 0.32f)
        ) {
            ManualControlsSheet(
                isManualMode = uiState.isManualMode,
                iso = uiState.iso,
                isoRange = uiState.isoRange,
                shutterSpeed = uiState.shutterSpeed,
                whiteBalance = uiState.whiteBalance,
                onManualModeToggle = { viewModel.setManualMode(it) },
                onIsoChange = { viewModel.setIso(it) },
                onShutterChange = { viewModel.setShutterSpeed(it) },
                onWhiteBalanceChange = { viewModel.setWhiteBalance(it) }
            )
        }
    }

    // ── Settings Sheet ──────────────────────────────────────────────────────
    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            containerColor = Color(0xFF111111),
            scrimColor = Color.Black.copy(alpha = 0.32f)
        ) {
            SettingsSheet(
                availableLenses = uiState.availableLenses,
                currentZoom = zoomRatio,
                showLevelIndicator = uiState.showLevelIndicator,
                onLevelIndicatorToggle = { viewModel.setLevelIndicator(it) }
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: Top Control Bar
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopControlBar(
    isRawSupported: Boolean,
    isRawEnabled: Boolean,
    flashMode: FlashMode,
    onFlashChange: (FlashMode) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flash button
        IconButton(
            onClick = {
                val next = when (flashMode) {
                    FlashMode.OFF -> FlashMode.AUTO
                    FlashMode.AUTO -> FlashMode.ON
                    FlashMode.ON -> FlashMode.TORCH
                    FlashMode.TORCH -> FlashMode.OFF
                }
                onFlashChange(next)
            },
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xCC000000))
        ) {
            Icon(
                imageVector = when (flashMode) {
                    FlashMode.OFF -> Icons.Default.FlashOff
                    FlashMode.AUTO -> Icons.Default.Flash
                    FlashMode.ON -> Icons.Default.FlashOn
                    FlashMode.TORCH -> Icons.Default.FlashOn
                },
                contentDescription = flashMode.label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Settings button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xCC000000))
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: Bottom Control Panel
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun BottomControlPanel(
    isCapturing: Boolean,
    zoomRatio: Float,
    minZoom: Float,
    maxZoom: Float,
    onZoomChange: (Float) -> Unit,
    onCaptureClick: () -> Unit,
    onOverlayClick: () -> Unit,
    onManualControlsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Zoom slider
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xCC000000))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Zoom: ${String.format("%.1f", zoomRatio)}x",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp
            )
            Slider(
                value = zoomRatio,
                onValueChange = onZoomChange,
                valueRange = minZoom..maxZoom,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFFD700),
                    activeTrackColor = Color(0xFFFFD700),
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
        }

        // Control buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Overlay button
            ComposeButton(
                icon = Icons.Default.GridView,
                label = "Overlay",
                onClick = onOverlayClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            // Manual controls button
            ComposeButton(
                icon = Icons.Default.Tune,
                label = "Manual",
                onClick = onManualControlsClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            // Capture button
            Button(
                onClick = { if (!isCapturing) onCaptureClick() },
                modifier = Modifier
                    .weight(1.5f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    disabledContainerColor = Color(0xFFFFD700).copy(alpha = 0.5f)
                ),
                enabled = !isCapturing
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Capture",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: Compose Button (Reusable)
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun ComposeButton(
    icon: androidx.compose.material.icons.Icons.Filled,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xCC000000)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Text(
                label,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: Zoom Indicator
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun ZoomIndicator(
    zoomRatio: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xCC000000)
        )
    ) {
        Text(
            "${String.format("%.1f", zoomRatio)}x",
            modifier = Modifier.padding(16.dp, 12.dp),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: Overlay Picker Sheet
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun OverlayPickerSheet(
    activeOverlays: Set<OverlayType>,
    overlayColor: OverlayColor,
    overlayAlpha: Float,
    onOverlayToggle: (OverlayType) -> Unit,
    onColorChange: (OverlayColor) -> Unit,
    onAlphaChange: (Float) -> Unit,
    onClearAll: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                "Composition Guides",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(OverlayType.ALL.chunked(2)) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { overlay ->
                    OverlayCard(
                        overlay = overlay,
                        isSelected = overlay in activeOverlays,
                        onClick = { onOverlayToggle(overlay) },
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp)
                    )
                }
            }
        }

        item {
            Divider(
                color = Color.White.copy(alpha = 0.1f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            Text(
                "Overlay Color & Opacity",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(OverlayColor.values()) { color ->
                    ColorSwatch(
                        color = color,
                        isSelected = color == overlayColor,
                        onClick = { onColorChange(color) },
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Opacity: ${(overlayAlpha * 100).toInt()}%",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
                Slider(
                    value = overlayAlpha,
                    onValueChange = onAlphaChange,
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFFD700),
                        activeTrackColor = Color(0xFFFFD700)
                    )
                )
            }
        }

        item {
            Button(
                onClick = onClearAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF333333)
                )
            ) {
                Text("Clear All", color = Color.White)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun OverlayCard(
    overlay: OverlayType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFD700) else Color(0xFF222222)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                overlay.label,
                color = if (isSelected) Color.Black else Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    color: OverlayColor,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(color.hexArgb))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: Manual Controls Sheet
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun ManualControlsSheet(
    isManualMode: Boolean,
    iso: Int,
    isoRange: IntRange,
    shutterSpeed: Long,
    whiteBalance: WhiteBalanceMode,
    onManualModeToggle: (Boolean) -> Unit,
    onIsoChange: (Int) -> Unit,
    onShutterChange: (Long) -> Unit,
    onWhiteBalanceChange: (WhiteBalanceMode) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF222222))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Manual Mode", color = Color.White)
                Switch(
                    checked = isManualMode,
                    onCheckedChange = onManualModeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFFD700),
                        checkedTrackColor = Color(0xFFFFD700).copy(alpha = 0.3f)
                    )
                )
            }
        }

        if (isManualMode) {
            item {
                Text(
                    "ISO: $iso",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = iso.toFloat(),
                    onValueChange = { onIsoChange(it.toInt()) },
                    valueRange = isoRange.first.toFloat()..isoRange.last.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFFD700),
                        activeTrackColor = Color(0xFFFFD700)
                    )
                )
            }

            item {
                Text(
                    "White Balance",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(WhiteBalanceMode.values()) { mode ->
                        AssistChip(
                            selected = mode == whiteBalance,
                            onClick = { onWhiteBalanceChange(mode) },
                            label = { Text(mode.label, fontSize = 10.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (mode == whiteBalance) Color(0xFFFFD700) else Color(0xFF333333),
                                labelColor = if (mode == whiteBalance) Color.Black else Color.White
                            )
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// COMPOSABLE: Settings Sheet
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSheet(
    availableLenses: List<LensInfo>,
    currentZoom: Float,
    showLevelIndicator: Boolean,
    onLevelIndicatorToggle: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Camera Settings",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
        }

        if (availableLenses.isNotEmpty()) {
            item {
                Text(
                    "Available Lenses",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableLenses) { lens ->
                        AssistChip(
                            selected = lens.zoomRatio == currentZoom,
                            onClick = { },
                            label = { Text("${lens.label} ${lens.zoomRatio}x") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF333333),
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF222222))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Level Indicator", color = Color.White)
                Switch(
                    checked = showLevelIndicator,
                    onCheckedChange = onLevelIndicatorToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFFD700),
                        checkedTrackColor = Color(0xFFFFD700).copy(alpha = 0.3f)
                    )
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}