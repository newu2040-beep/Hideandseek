@file:OptIn(
    androidx.media3.common.util.UnstableApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.VaultMediaEntity
import com.example.ui.VaultViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.DarkSurface
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    viewModel: VaultViewModel,
    media: VaultMediaEntity,
    decryptedFilePath: String?,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val allList = uiState.allMedia
    val currentIndex = allList.indexOfFirst { it.id == media.id }.coerceAtLeast(0) + 1
    val totalCount = allList.size

    val isVideo = media.mediaType == "VIDEO"
    var showInfoSheet by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }

    // Gesture state for zoom and pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val fallbackRes = when {
        media.fileName.contains("Peak") || media.fileName.contains("Alpine") -> R.drawable.sample_mountain
        media.fileName.contains("Lake") -> R.drawable.sample_lake
        media.fileName.contains("Coast") || media.fileName.contains("Traveler") -> R.drawable.sample_traveler
        else -> R.drawable.sample_mountain
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main Media Content (Photo or Video Player)
        if (isVideo) {
            VideoPlayerView(
                filePath = decryptedFilePath,
                fallbackResId = fallbackRes,
                controlsVisible = controlsVisible,
                onToggleControls = { controlsVisible = !controlsVisible }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { controlsVisible = !controlsVisible },
                            onDoubleTap = {
                                if (scale > 1f) {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    scale = 2.5f
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(decryptedFilePath?.let { File(it) } ?: fallbackRes)
                        .error(fallbackRes)
                        .crossfade(true)
                        .build(),
                    contentDescription = media.fileName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                )
            }
        }

        // Top Navigation Bar
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = if (totalCount > 0) "$currentIndex / $totalCount" else media.fileName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Info Button
                    IconButton(
                        onClick = { showInfoSheet = true },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Media Info",
                            tint = Color.White
                        )
                    }

                    // Favorite Button
                    IconButton(
                        onClick = { viewModel.toggleFavorite(media.id) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = if (media.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (media.isFavorite) Color(0xFFFF375F) else Color.White
                        )
                    }
                }
            }
        }

        // Bottom Action Bar: Export to Device, Move to Trash, Share
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF141724).copy(alpha = 0.94f),
                shadowElevation = 10.dp,
                border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFF262B42))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Export to Device Gallery
                    MediaDetailActionButton(
                        icon = Icons.Default.Download,
                        label = "Export",
                        tint = AccentCyan,
                        onClick = {
                            viewModel.exportMedia(media)
                        }
                    )

                    // 2. Share Action
                    MediaDetailActionButton(
                        icon = Icons.Default.Share,
                        label = "Share",
                        tint = Color.White,
                        onClick = {
                            val file = decryptedFilePath?.let { File(it) }
                            if (file != null && file.exists()) {
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = if (isVideo) "video/*" else "image/*"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Media"))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    )

                    // 3. Move to Trash
                    MediaDetailActionButton(
                        icon = Icons.Default.Delete,
                        label = "Move to Trash",
                        tint = Color(0xFFFF453A),
                        onClick = {
                            showDeleteConfirmDialog = true
                        }
                    )
                }
            }
        }

        // Delete / Move to Trash Confirmation Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Move to Trash?", fontWeight = FontWeight.Bold) },
                text = { Text("This ${if (isVideo) "video" else "photo"} will be moved to Trash where you can restore it anytime or delete it permanently.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            viewModel.moveMediaToTrash(media.id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A))
                    ) {
                        Text("Move to Trash", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = DarkSurface,
                titleContentColor = Color.White,
                textContentColor = Color(0xFFBDC2D8)
            )
        }

        // Media Details Bottom Sheet
        if (showInfoSheet) {
            ModalBottomSheet(
                onDismissRequest = { showInfoSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = Color(0xFF141624)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = AccentPurpleLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Media Details & Security",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    val dateFormat = SimpleDateFormat("MMMM d, yyyy • h:mm:ss a", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(media.createdAt))

                    DetailInfoRow("File Name", media.fileName.removePrefix("imported_").removePrefix("enc_"))
                    DetailInfoRow("Media Type", if (isVideo) "Encrypted Video" else "Encrypted Photo")
                    DetailInfoRow("File Size", formatBytes(media.sizeBytes))
                    DetailInfoRow("Import Date", dateStr)
                    DetailInfoRow("Encryption", "AES-256 GCM (Encrypted Storage)")
                    DetailInfoRow("Original Source", media.originalPath.ifEmpty { "Device Storage / Camera" })

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = Color(0xFF8E92A8))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
fun MediaDetailActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = tint,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(
    filePath: String?,
    fallbackResId: Int,
    controlsVisible: Boolean,
    onToggleControls: () -> Unit
) {
    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(1000L) }

    DisposableEffect(filePath) {
        val player = ExoPlayer.Builder(context).build().apply {
            if (filePath != null && File(filePath).exists()) {
                val mediaItem = MediaItem.fromUri(Uri.fromFile(File(filePath)))
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_ALL
            }
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        durationMs = duration.coerceAtLeast(1000L)
                    }
                }
            })
        }
        exoPlayer = player

        onDispose {
            player.release()
            exoPlayer = null
        }
    }

    // Periodic time updater
    LaunchedEffect(exoPlayer, isPlaying) {
        while (true) {
            exoPlayer?.let { p ->
                currentPositionMs = p.currentPosition.coerceAtLeast(0L)
                if (p.duration > 0) {
                    durationMs = p.duration
                }
            }
            delay(250)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onToggleControls),
        contentAlignment = Alignment.Center
    ) {
        if (filePath != null && File(filePath).exists()) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = exoPlayer
                        useController = false
                        setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                    }
                },
                update = { playerView ->
                    if (playerView.player != exoPlayer) {
                        playerView.player = exoPlayer
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = fallbackResId,
                    contentDescription = "Video Poster",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                androidx.compose.material3.CircularProgressIndicator(
                    color = AccentPurple,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Center Play / Pause / Seek Controls
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                // Replay 10s
                IconButton(
                    onClick = {
                        exoPlayer?.let { p ->
                            p.seekTo((p.currentPosition - 10000).coerceAtLeast(0))
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10 seconds",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Play / Pause Main
                Surface(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable {
                            exoPlayer?.let { p ->
                                if (p.isPlaying) {
                                    p.pause()
                                    isPlaying = false
                                } else {
                                    p.play()
                                    isPlaying = true
                                }
                            }
                        },
                    shape = CircleShape,
                    color = AccentPurple.copy(alpha = 0.85f),
                    shadowElevation = 8.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Forward 10s
                IconButton(
                    onClick = {
                        exoPlayer?.let { p ->
                            p.seekTo((p.currentPosition + 10000).coerceAtMost(durationMs))
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Forward 10 seconds",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Bottom Progress Bar & Time Tracker
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp, start = 20.dp, end = 20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Slider(
                        value = if (durationMs > 0) currentPositionMs.toFloat() / durationMs.toFloat() else 0f,
                        onValueChange = { frac ->
                            val targetMs = (frac * durationMs).toLong()
                            currentPositionMs = targetMs
                            exoPlayer?.seekTo(targetMs)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = AccentCyan,
                            activeTrackColor = AccentCyan,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPositionMs),
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )

                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                                exoPlayer?.volume = if (isMuted) 0f else 1f
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                contentDescription = "Mute Toggle",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = formatTime(durationMs),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        "%.1f MB".format(mb)
    } else {
        "%.0f KB".format(kb)
    }
}
