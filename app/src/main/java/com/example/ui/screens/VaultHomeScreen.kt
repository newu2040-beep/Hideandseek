package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.VaultMediaEntity
import com.example.ui.VaultViewModel
import com.example.ui.components.PillTabRow
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightSurface
import java.io.File

import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Menu
import com.example.ui.theme.VaultThemePreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultHomeScreen(
    viewModel: VaultViewModel,
    activePreset: VaultThemePreset = VaultThemePreset.SUPER_AMOLED_BLACK,
    isDark: Boolean = activePreset.isDark,
    onOpenHamburgerMenu: () -> Unit = {},
    onNavigateToApps: () -> Unit,
    onNavigateToTrash: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showImportSheet by remember { mutableStateOf(false) }

    // Media picker launcher for photos
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importMediaUris(context, uris, mediaType = "IMAGE")
        }
    }

    // Media picker launcher for videos
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importMediaUris(context, uris, mediaType = "VIDEO")
        }
    }

    val displayedMedia = when (uiState.selectedTab) {
        0 -> uiState.allMedia.filter { it.mediaType == "IMAGE" }
        1 -> uiState.allMedia.filter { it.mediaType == "VIDEO" }
        else -> uiState.allMedia
    }

    val bgColor = activePreset.background
    val textColor = activePreset.textPrimary

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
            // 1. Top Bar with Hamburger Menu & Theme Studio Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onOpenHamburgerMenu,
                        modifier = Modifier.testTag("hamburger_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Themes Menu",
                            tint = textColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Text(
                        text = "Vault",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.testTag("vault_title")
                    )

                    // Theme badge chip
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = activePreset.primary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, activePreset.primary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenHamburgerMenu() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = "Theme",
                                tint = activePreset.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (activePreset.isAmoled) "AMOLED" else activePreset.accentBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = activePreset.primary
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Selection",
                                tint = if (isDark) Color.White else Color.Black
                            )
                        }
                    }

                    // Quick Trash Button with Badge
                    IconButton(
                        onClick = onNavigateToTrash,
                        modifier = Modifier.testTag("quick_trash_button")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Trash",
                                tint = if (uiState.trashCount > 0) Color(0xFFFF453A) else if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)
                            )
                            if (uiState.trashCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFF453A),
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            }
                        }
                    }

                    // System Permissions / Full Access Hub button
                    IconButton(
                        onClick = { viewModel.setShowPermissionsDialog(true) },
                        modifier = Modifier.testTag("permissions_hub_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "System Permissions Access",
                            tint = activePreset.secondary
                        )
                    }

                    // Lock button
                    IconButton(
                        onClick = { viewModel.lockVault() },
                        modifier = Modifier.testTag("lock_vault_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Vault",
                            tint = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1D2032)
                        )
                    }
                }
            }

            // 2. Pill Tabs (Photos, Videos, Apps)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                PillTabRow(
                    tabs = listOf("Photos", "Videos", "Apps"),
                    selectedIndex = uiState.selectedTab,
                    onTabSelected = { index ->
                        if (index == 2) {
                            onNavigateToApps()
                        } else {
                            viewModel.selectTab(index)
                        }
                    },
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Import Progress Indicator
            uiState.importProgress?.let { progress ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(18.dp),
                        color = AccentCyan,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Encrypting and hiding media ${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = AccentCyan
                    )
                }
            }

            // 3. Media Grid
            if (displayedMedia.isEmpty()) {
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
                        Icon(
                            imageVector = if (uiState.selectedTab == 1) Icons.Default.Videocam else Icons.Default.Image,
                            contentDescription = "Empty Media",
                            tint = if (isDark) Color(0xFF383C56) else Color(0xFFBDC2D8),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (uiState.selectedTab == 1) "No hidden videos yet" else "No hidden photos yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFF757A98) else Color(0xFF888EA6)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap + below to import into your private vault",
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFF555974) else Color(0xFFA0A5BB)
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
                    items(displayedMedia, key = { it.id }) { media ->
                        val isSelected = uiState.selectedMediaIds.contains(media.id)
                        VaultMediaGridItem(
                            media = media,
                            isSelected = isSelected,
                            isSelectionMode = uiState.isSelectionMode,
                            isDark = isDark,
                            onClick = {
                                if (uiState.isSelectionMode) {
                                    viewModel.toggleMediaSelection(media.id)
                                } else {
                                    viewModel.openMediaDetail(media)
                                }
                            },
                            onLongClick = {
                                viewModel.toggleMediaSelection(media.id)
                            }
                        )
                    }
                }
            }

            // 4. Bottom Counter & Floating Add Button Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item counter badge
                Text(
                    text = "${displayedMedia.size} items",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84),
                    modifier = Modifier.padding(start = 6.dp)
                )

                // Large Glowing Floating Action Button
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(AccentPurple, Color(0xFF5B2EEB))
                            )
                        )
                        .clickable { showImportSheet = true }
                        .testTag("add_media_fab"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Media to Vault",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Selection toggle button
                IconButton(
                    onClick = {
                        if (uiState.isSelectionMode) {
                            viewModel.clearSelection()
                        } else {
                            viewModel.selectAllMedia()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = "Selection Mode",
                        tint = if (uiState.isSelectionMode) AccentPurpleLight else if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)
                    )
                }
            }
        }

        // Selection Action Bar overlay
        AnimatedVisibility(
            visible = uiState.isSelectionMode,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) DarkSurface else LightSurface,
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.selectedMediaIds.size} selected",
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Export to Device Gallery
                        IconButton(
                            onClick = { viewModel.exportSelectedMedia() },
                            modifier = Modifier.testTag("export_selected_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export to Gallery",
                                tint = AccentCyan
                            )
                        }

                        // Move to Trash
                        IconButton(
                            onClick = { viewModel.moveSelectedToTrash() },
                            modifier = Modifier.testTag("move_trash_selected_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Move to Trash",
                                tint = Color(0xFFFF453A)
                            )
                        }
                    }
                }
            }
        }

        // Import Bottom Sheet
        if (showImportSheet) {
            ModalBottomSheet(
                onDismissRequest = { showImportSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = if (isDark) DarkSurface else LightSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Import to Private Vault",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Text(
                        text = "Imported media will be encrypted and hidden from the normal gallery.",
                        fontSize = 13.sp,
                        color = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                showImportSheet = false
                                photoPickerLauncher.launch("image/*")
                            },
                        color = if (isDark) Color(0xFF1F2336) else Color(0xFFECEFF8)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Photos",
                                tint = AccentPurpleLight,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Hide Photos",
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                                Text(
                                    text = "Choose pictures from your device gallery",
                                    fontSize = 12.sp,
                                    color = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                showImportSheet = false
                                videoPickerLauncher.launch("video/*")
                            },
                        color = if (isDark) Color(0xFF1F2336) else Color(0xFFECEFF8)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Videos",
                                tint = AccentCyan,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Hide Videos",
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                                Text(
                                    text = "Choose videos from your device",
                                    fontSize = 12.sp,
                                    color = if (isDark) Color(0xFF8E92A8) else Color(0xFF6B6E84)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun VaultMediaGridItem(
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

        // Video Badge overlay
        if (media.mediaType == "VIDEO") {
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
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
                        text = "0:15",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
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
