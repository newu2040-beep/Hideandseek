package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.VaultMediaEntity
import com.example.ui.VaultViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightSurface
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrashScreen(
    viewModel: VaultViewModel,
    isDark: Boolean = true,
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var previewMediaItem by remember { mutableStateOf<VaultMediaEntity?>(null) }

    val bgColor = if (isDark) DarkBackground else LightBackground
    val textColor = if (isDark) Color.White else Color(0xFF131522)
    val cardBg = if (isDark) DarkSurface else LightSurface
    val borderColor = if (isDark) DarkBorder else LightBorder

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = textColor
                            )
                        }
                    }

                    Text(
                        text = "Trash",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.testTag("trash_title")
                    )

                    if (uiState.trashCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF453A).copy(alpha = 0.2f),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = "${uiState.trashCount}",
                                color = Color(0xFFFF453A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.isTrashSelectionMode) {
                        IconButton(onClick = { viewModel.clearTrashSelection() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Selection",
                                tint = textColor
                            )
                        }
                    } else if (uiState.trashCount > 0) {
                        IconButton(onClick = { viewModel.selectAllTrash() }) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "Select Items",
                                tint = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)
                            )
                        }

                        // Empty Trash Button
                        TextButton(
                            onClick = { viewModel.setShowEmptyTrashDialog(true) },
                            modifier = Modifier.testTag("empty_trash_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Empty Trash",
                                tint = Color(0xFFFF453A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Empty",
                                color = Color(0xFFFF453A),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // 2. Info Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xFF1B1E30) else Color(0xFFE8ECF8),
                border = androidx.compose.foundation.BorderStroke(0.6.dp, borderColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Trash Policy",
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Items in Trash remain encrypted. Restore them anytime or empty trash to free up space.",
                        fontSize = 12.sp,
                        color = if (isDark) Color(0xFF9EA3C0) else Color(0xFF5D627D),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Main Content: Grid or Empty State
            if (uiState.trashMedia.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF1F2236) else Color(0xFFE5E9F5),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Trash is Empty",
                                    tint = if (isDark) Color(0xFF53587A) else Color(0xFFA0A6C0),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Trash is Empty",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Photos and videos you delete will appear here",
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFF7B809E) else Color(0xFF888EA6)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 96.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.trashMedia, key = { it.id }) { media ->
                        val isSelected = uiState.selectedTrashIds.contains(media.id)
                        TrashMediaGridItem(
                            media = media,
                            isSelected = isSelected,
                            isSelectionMode = uiState.isTrashSelectionMode,
                            isDark = isDark,
                            onClick = {
                                if (uiState.isTrashSelectionMode) {
                                    viewModel.toggleTrashSelection(media.id)
                                } else {
                                    previewMediaItem = media
                                }
                            },
                            onLongClick = {
                                viewModel.toggleTrashSelection(media.id)
                            }
                        )
                    }
                }
            }
        }

        // Selection Action Bar overlay
        AnimatedVisibility(
            visible = uiState.isTrashSelectionMode,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = cardBg,
                shadowElevation = 12.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.selectedTrashIds.size} selected",
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Restore Button
                        Button(
                            onClick = { viewModel.restoreSelectedTrash() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Restore",
                                tint = Color(0xFF0F111E),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore", color = Color(0xFF0F111E), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Permanently Delete Button
                        Button(
                            onClick = { viewModel.permanentlyDeleteSelectedTrash() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Delete Forever",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Empty Trash Confirmation Dialog
        if (uiState.showEmptyTrashDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setShowEmptyTrashDialog(false) },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFFF453A)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Empty Trash?", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text("This will permanently delete all ${uiState.trashCount} item(s) from your device storage. This action cannot be reversed.")
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.emptyTrash() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A))
                    ) {
                        Text("Empty Trash Forever", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setShowEmptyTrashDialog(false) }) {
                        Text("Cancel")
                    }
                },
                containerColor = cardBg,
                textContentColor = textColor,
                titleContentColor = textColor
            )
        }

        // Single Trashed Item Detail / Action Sheet Dialog
        previewMediaItem?.let { media ->
            AlertDialog(
                onDismissRequest = { previewMediaItem = null },
                title = {
                    Text(
                        text = media.fileName.removePrefix("imported_").removePrefix("enc_"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val fallbackRes = when {
                            media.fileName.contains("Peak") || media.fileName.contains("Alpine") -> R.drawable.sample_mountain
                            media.fileName.contains("Lake") -> R.drawable.sample_lake
                            media.fileName.contains("Coast") || media.fileName.contains("Traveler") -> R.drawable.sample_traveler
                            else -> R.drawable.sample_mountain
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(File(media.encryptedPath).takeIf { it.exists() } ?: fallbackRes)
                                    .error(fallbackRes)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = media.fileName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            if (media.mediaType == "VIDEO") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.35f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                        val deletedDateStr = if (media.deletedAt > 0) dateFormat.format(Date(media.deletedAt)) else "Recently"
                        
                        Text(
                            text = "Type: ${media.mediaType} • Size: ${formatBytes(media.sizeBytes)}",
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)
                        )
                        Text(
                            text = "Deleted: $deletedDateStr",
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)
                        )
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.permanentlyDeleteTrashMedia(media.id)
                                previewMediaItem = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF453A))
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete Forever")
                        }

                        Button(
                            onClick = {
                                viewModel.restoreMediaFromTrash(media.id)
                                previewMediaItem = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = Color(0xFF0F111E), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore", color = Color(0xFF0F111E), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                containerColor = cardBg,
                textContentColor = textColor,
                titleContentColor = textColor
            )
        }
    }
}

@Composable
fun TrashMediaGridItem(
    media: VaultMediaEntity,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val fallbackRes = when {
        media.fileName.contains("Peak") || media.fileName.contains("Alpine") -> R.drawable.sample_mountain
        media.fileName.contains("Lake") -> R.drawable.sample_lake
        media.fileName.contains("Coast") || media.fileName.contains("Traveler") -> R.drawable.sample_traveler
        else -> R.drawable.sample_mountain
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) Color(0xFF1A1D2D) else Color(0xFFE4E7F2))
            .clickable(onClick = onClick)
    ) {
        // Thumbnail image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(File(media.encryptedPath).takeIf { it.exists() } ?: fallbackRes)
                .error(fallbackRes)
                .crossfade(true)
                .build(),
            contentDescription = media.fileName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Semi-transparent trash overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
        )

        // Video Badge overlay
        if (media.mediaType == "VIDEO") {
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Video",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "VIDEO",
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Selection overlay checkmark
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isSelected) AccentPurple.copy(alpha = 0.45f) else Color.Transparent)
                    .padding(8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = if (isSelected) AccentCyan else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
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
