package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.db.VaultDatabase
import com.example.data.model.HiddenAppEntity
import com.example.data.model.IntruderLogEntity
import com.example.data.model.VaultMediaEntity
import com.example.security.AppCategory
import com.example.security.DisguiseModeManager
import com.example.security.DisguiseOption
import com.example.security.InstalledAppItem
import com.example.security.InstalledAppsManager
import com.example.security.VaultSecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class VaultUiState(
    val isUnlocked: Boolean = false,
    val isBiometricPromptShowing: Boolean = false,
    val isPinMode: Boolean = false,
    val enteredPin: String = "",
    val pinError: Boolean = false,
    val failedAttempts: Int = 0,
    val showRadialUnlock: Boolean = false,

    val selectedTab: Int = 0, // 0 = Photos, 1 = Videos, 2 = Apps
    val allMedia: List<VaultMediaEntity> = emptyList(),
    val isSelectionMode: Boolean = false,
    val selectedMediaIds: Set<Long> = emptySet(),
    val importProgress: Float? = null,
    val activeMediaDetail: VaultMediaEntity? = null,
    val activeMediaDecryptedPath: String? = null,

    // Trash & Recycle Bin State
    val trashMedia: List<VaultMediaEntity> = emptyList(),
    val trashCount: Int = 0,
    val isTrashSelectionMode: Boolean = false,
    val selectedTrashIds: Set<Long> = emptySet(),
    val showEmptyTrashDialog: Boolean = false,
    val toastMessage: String? = null,

    val installedApps: List<InstalledAppItem> = emptyList(),
    val appSearchQuery: String = "",
    val selectedAppCategory: AppCategory = AppCategory.ALL,
    val isLoadingApps: Boolean = false,

    val isBiometricEnabled: Boolean = true,
    val isIntruderSelfieEnabled: Boolean = true,
    val themeMode: String = "DARK",
    val activeDisguise: DisguiseOption? = null,
    val isDeviceAdminEnabled: Boolean = false,
    val intruderLogs: List<IntruderLogEntity> = emptyList(),

    val showPinChangeDialog: Boolean = false,
    val showDisguisePicker: Boolean = false,
    val showIntruderLogs: Boolean = false,
    val showAdminInfoDialog: Boolean = false,
    val showAboutDialog: Boolean = false,
    val showPermissionsDialog: Boolean = false,
    val isDisguiseCalculatorActive: Boolean = false,
    val calculatorExpression: String = "0"
)

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    val securityManager = VaultSecurityManager(application)
    val disguiseManager = DisguiseModeManager(application)
    private val appsManager = InstalledAppsManager(application)
    private val database = VaultDatabase.getInstance(application)
    private val vaultDao = database.vaultDao()

    private val _uiState = MutableStateFlow(
        VaultUiState(
            isBiometricEnabled = securityManager.isBiometricEnabled,
            isIntruderSelfieEnabled = securityManager.isIntruderSelfieEnabled,
            themeMode = securityManager.themeMode,
            activeDisguise = disguiseManager.getCurrentDisguise(securityManager),
            isDeviceAdminEnabled = securityManager.isDeviceAdminFrictionEnabled,
            isDisguiseCalculatorActive = securityManager.activeDisguiseAlias == "CALCULATOR"
        )
    )
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        // Observe media from Room (Active media only)
        viewModelScope.launch {
            vaultDao.getAllMedia().collect { mediaList ->
                _uiState.update { it.copy(allMedia = mediaList) }
                // Seed sample photos if database is fresh
                if (mediaList.isEmpty()) {
                    seedSampleVaultMedia()
                }
            }
        }

        // Observe trash media from Room
        viewModelScope.launch {
            vaultDao.getTrashMedia().collect { trashList ->
                _uiState.update { it.copy(trashMedia = trashList, trashCount = trashList.size) }
            }
        }

        // Observe intruder logs
        viewModelScope.launch {
            vaultDao.getAllIntruderLogs().collect { logs ->
                _uiState.update { it.copy(intruderLogs = logs) }
            }
        }

        // Observe hidden apps and refresh installed apps list
        viewModelScope.launch {
            vaultDao.getAllHiddenApps().collect { hiddenList ->
                val hiddenSet = hiddenList.filter { it.isHidden }.map { it.packageName }.toSet()
                loadInstalledApps(hiddenSet)
            }
        }
    }

    private fun seedSampleVaultMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            val sampleDrawables = listOf(
                Pair(R.drawable.sample_mountain, "DSC_Alpine_Peak_GoldenHour.jpg"),
                Pair(R.drawable.sample_lake, "IMG_Turquoise_Lake_Reflection.jpg"),
                Pair(R.drawable.sample_traveler, "PHOTO_Coast_Traveler_Sunset.jpg")
            )

            sampleDrawables.forEach { (resId, name) ->
                try {
                    val inputStream = getApplication<Application>().resources.openRawResource(resId)
                    val media = securityManager.saveEncryptedMedia(
                        fileName = name,
                        inputStream = inputStream,
                        mediaType = "IMAGE",
                        originalPath = "/storage/emulated/0/DCIM/Camera/$name"
                    )
                    vaultDao.insertMedia(media)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun loadInstalledApps(hiddenSet: Set<String> = emptySet()) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingApps = true) }
            val apps = appsManager.getInstalledApps(hiddenSet)
            _uiState.update {
                it.copy(
                    installedApps = apps,
                    isLoadingApps = false
                )
            }
        }
    }

    // --- Unlock & Authentication ---

    fun onPinDigitEntered(digit: String) {
        val currentPin = _uiState.value.enteredPin
        if (currentPin.length >= 6) return

        val newPin = currentPin + digit
        _uiState.update { it.copy(enteredPin = newPin, pinError = false) }

        if (newPin.length >= 4) {
            if (securityManager.verifyPin(newPin)) {
                unlockSuccess()
            } else if (newPin.length == 6 || (newPin.length == 4 && !securityManager.verifyPin(newPin))) {
                // Check if user set 4-digit PIN
                if (newPin.length == 4) {
                    // Give chance for 6 digit, or fail if length reaches max
                }
            }
        }
    }

    fun onPinSubmit() {
        val pin = _uiState.value.enteredPin
        if (securityManager.verifyPin(pin)) {
            unlockSuccess()
        } else {
            unlockFailed()
        }
    }

    fun onPinBackspace() {
        val current = _uiState.value.enteredPin
        if (current.isNotEmpty()) {
            _uiState.update { it.copy(enteredPin = current.dropLast(1), pinError = false) }
        }
    }

    fun onPinClear() {
        _uiState.update { it.copy(enteredPin = "", pinError = false) }
    }

    fun unlockSuccess() {
        securityManager.failedAttemptsCount = 0
        _uiState.update {
            it.copy(
                isUnlocked = true,
                showRadialUnlock = true,
                enteredPin = "",
                pinError = false,
                failedAttempts = 0
            )
        }
    }

    fun unlockFailed() {
        val newFailCount = _uiState.value.failedAttempts + 1
        securityManager.failedAttemptsCount = newFailCount
        _uiState.update {
            it.copy(
                enteredPin = "",
                pinError = true,
                failedAttempts = newFailCount
            )
        }
    }

    fun lockVault() {
        securityManager.clearDecryptedCache()
        _uiState.update {
            it.copy(
                isUnlocked = false,
                enteredPin = "",
                pinError = false,
                isSelectionMode = false,
                selectedMediaIds = emptySet(),
                activeMediaDetail = null,
                activeMediaDecryptedPath = null
            )
        }
    }

    fun togglePinOrBiometricMode(usePin: Boolean) {
        _uiState.update { it.copy(isPinMode = usePin, enteredPin = "", pinError = false) }
    }

    // --- Disguise Calculator Secret Code ---
    fun onCalculatorKey(key: String) {
        val current = _uiState.value.calculatorExpression
        when (key) {
            "C" -> _uiState.update { it.copy(calculatorExpression = "0") }
            "=" -> {
                // Check if the typed expression contains or equals the PIN
                if (securityManager.verifyPin(current) || current.endsWith(securityManager.getPinHash() ?: "")) {
                    unlockSuccess()
                } else {
                    // Try evaluating arithmetic
                    val evalResult = evaluateSimpleMath(current)
                    _uiState.update { it.copy(calculatorExpression = evalResult) }
                }
            }
            else -> {
                val newExpr = if (current == "0" && key != ".") key else current + key
                // Real-time secret PIN check
                if (securityManager.verifyPin(newExpr)) {
                    unlockSuccess()
                } else {
                    _uiState.update { it.copy(calculatorExpression = newExpr) }
                }
            }
        }
    }

    private fun evaluateSimpleMath(expr: String): String {
        return try {
            val sanitized = expr.replace("×", "*").replace("÷", "/")
            if (sanitized.contains("+")) {
                val parts = sanitized.split("+")
                (parts[0].toDouble() + parts[1].toDouble()).toString().removeSuffix(".0")
            } else if (sanitized.contains("-")) {
                val parts = sanitized.split("-")
                (parts[0].toDouble() - parts[1].toDouble()).toString().removeSuffix(".0")
            } else if (sanitized.contains("*")) {
                val parts = sanitized.split("*")
                (parts[0].toDouble() * parts[1].toDouble()).toString().removeSuffix(".0")
            } else if (sanitized.contains("/")) {
                val parts = sanitized.split("/")
                (parts[0].toDouble() / parts[1].toDouble()).toString().removeSuffix(".0")
            } else {
                expr
            }
        } catch (e: Exception) {
            "Error"
        }
    }

    // --- Media Gallery Operations ---

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun openMediaDetail(media: VaultMediaEntity) {
        viewModelScope.launch {
            val decryptedFile = securityManager.getDecryptedFile(media.encryptedPath)
            _uiState.update {
                it.copy(
                    activeMediaDetail = media,
                    activeMediaDecryptedPath = decryptedFile.absolutePath
                )
            }
        }
    }

    fun closeMediaDetail() {
        _uiState.update {
            it.copy(
                activeMediaDetail = null,
                activeMediaDecryptedPath = null
            )
        }
    }

    fun toggleMediaSelection(id: Long) {
        val current = _uiState.value.selectedMediaIds.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _uiState.update {
            it.copy(
                selectedMediaIds = current,
                isSelectionMode = current.isNotEmpty()
            )
        }
    }

    fun selectAllMedia() {
        val allIds = _uiState.value.allMedia.map { it.id }.toSet()
        _uiState.update {
            it.copy(
                selectedMediaIds = allIds,
                isSelectionMode = true
            )
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedMediaIds = emptySet(),
                isSelectionMode = false
            )
        }
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = vaultDao.getMediaById(id) ?: return@launch
            val updated = item.copy(isFavorite = !item.isFavorite)
            vaultDao.updateMedia(updated)
            if (_uiState.value.activeMediaDetail?.id == id) {
                _uiState.update { it.copy(activeMediaDetail = updated) }
            }
        }
    }

    // Move single media to Trash (Soft Delete)
    fun moveMediaToTrash(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            vaultDao.moveToTrash(id)
            withContext(Dispatchers.Main) {
                if (_uiState.value.activeMediaDetail?.id == id) {
                    closeMediaDetail()
                }
                setToastMessage("Moved to Trash")
            }
        }
    }

    // Move selected media to Trash (Soft Delete)
    fun moveSelectedToTrash() {
        val ids = _uiState.value.selectedMediaIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            vaultDao.moveToTrashList(ids)
            withContext(Dispatchers.Main) {
                clearSelection()
                if (_uiState.value.activeMediaDetail != null && ids.contains(_uiState.value.activeMediaDetail?.id)) {
                    closeMediaDetail()
                }
                setToastMessage("Moved ${ids.size} items to Trash")
            }
        }
    }

    // Export single media to Device Public Gallery / Storage
    fun exportMedia(media: VaultMediaEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val uri = securityManager.exportMediaToDeviceGallery(media)
            withContext(Dispatchers.Main) {
                if (uri != null) {
                    setToastMessage("Exported to device gallery")
                } else {
                    setToastMessage("Failed to export media")
                }
            }
        }
    }

    // Export selected media to Device Public Gallery / Storage and remove from vault or retain
    fun exportSelectedMedia() {
        val ids = _uiState.value.selectedMediaIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val mediaToExport = _uiState.value.allMedia.filter { ids.contains(it.id) }
            var exportedCount = 0
            mediaToExport.forEach { media ->
                val uri = securityManager.exportMediaToDeviceGallery(media)
                if (uri != null) exportedCount++
            }
            withContext(Dispatchers.Main) {
                clearSelection()
                setToastMessage("Exported $exportedCount items to Device Gallery")
            }
        }
    }

    // --- Trash / Recycle Bin Operations ---

    fun restoreMediaFromTrash(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            vaultDao.restoreFromTrash(id)
            withContext(Dispatchers.Main) {
                setToastMessage("Restored to Vault")
            }
        }
    }

    fun restoreSelectedTrash() {
        val ids = _uiState.value.selectedTrashIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            vaultDao.restoreFromTrashList(ids)
            withContext(Dispatchers.Main) {
                clearTrashSelection()
                setToastMessage("Restored ${ids.size} items to Vault")
            }
        }
    }

    fun permanentlyDeleteTrashMedia(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val media = vaultDao.getMediaById(id)
            if (media != null) {
                securityManager.deleteEncryptedFile(media.encryptedPath)
                vaultDao.deleteMedia(media)
            }
            withContext(Dispatchers.Main) {
                setToastMessage("Permanently deleted")
            }
        }
    }

    fun permanentlyDeleteSelectedTrash() {
        val ids = _uiState.value.selectedTrashIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val items = _uiState.value.trashMedia.filter { ids.contains(it.id) }
            items.forEach { media ->
                securityManager.deleteEncryptedFile(media.encryptedPath)
            }
            vaultDao.deleteMediaByIds(ids)
            withContext(Dispatchers.Main) {
                clearTrashSelection()
                setToastMessage("Permanently deleted ${ids.size} items")
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            val allTrashed = vaultDao.getTrashMediaList()
            allTrashed.forEach { media ->
                securityManager.deleteEncryptedFile(media.encryptedPath)
            }
            vaultDao.emptyTrash()
            withContext(Dispatchers.Main) {
                clearTrashSelection()
                setShowEmptyTrashDialog(false)
                setToastMessage("Trash emptied")
            }
        }
    }

    fun toggleTrashSelection(id: Long) {
        val current = _uiState.value.selectedTrashIds.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _uiState.update {
            it.copy(
                selectedTrashIds = current,
                isTrashSelectionMode = current.isNotEmpty()
            )
        }
    }

    fun selectAllTrash() {
        val allIds = _uiState.value.trashMedia.map { it.id }.toSet()
        _uiState.update {
            it.copy(
                selectedTrashIds = allIds,
                isTrashSelectionMode = true
            )
        }
    }

    fun clearTrashSelection() {
        _uiState.update {
            it.copy(
                selectedTrashIds = emptySet(),
                isTrashSelectionMode = false
            )
        }
    }

    fun setShowEmptyTrashDialog(show: Boolean) {
        _uiState.update { it.copy(showEmptyTrashDialog = show) }
    }

    fun setToastMessage(msg: String?) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun importMediaUris(context: Context, uris: List<Uri>, mediaType: String = "IMAGE") {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(importProgress = 0.01f) }
            val total = uris.size
            uris.forEachIndexed { index, uri ->
                try {
                    val contentResolver = context.contentResolver
                    val fileName = "imported_${System.currentTimeMillis()}_${index + 1}.${if (mediaType == "VIDEO") "mp4" else "jpg"}"
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val media = securityManager.saveEncryptedMedia(
                            fileName = fileName,
                            inputStream = inputStream,
                            mediaType = mediaType,
                            originalPath = uri.toString()
                        )
                        vaultDao.insertMedia(media)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val progress = (index + 1).toFloat() / total.toFloat()
                _uiState.update { it.copy(importProgress = progress) }
            }
            _uiState.update {
                it.copy(
                    importProgress = null,
                    toastMessage = "Imported $total ${if (mediaType == "VIDEO") "videos" else "photos"} into private vault"
                )
            }
        }
    }

    // --- App Hider Toggles ---

    fun toggleAppHidden(item: InstalledAppItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val newHidden = !item.isHidden
            if (newHidden) {
                vaultDao.setAppHidden(
                    HiddenAppEntity(
                        packageName = item.packageName,
                        appName = item.appName,
                        isHidden = true
                    )
                )
            } else {
                vaultDao.unhideApp(item.packageName)
            }
        }
    }

    fun onAppSearchQueryChanged(query: String) {
        _uiState.update { it.copy(appSearchQuery = query) }
    }

    fun onAppCategorySelected(category: AppCategory) {
        _uiState.update { it.copy(selectedAppCategory = category) }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val hiddenList = vaultDao.getAllHiddenAppsList()
            val hiddenSet = hiddenList.filter { it.isHidden }.map { it.packageName }.toSet()
            loadInstalledApps(hiddenSet)
        }
    }

    // --- Settings & Customization ---

    fun setBiometricEnabled(enabled: Boolean) {
        securityManager.isBiometricEnabled = enabled
        _uiState.update { it.copy(isBiometricEnabled = enabled) }
    }

    fun setIntruderSelfieEnabled(enabled: Boolean) {
        securityManager.isIntruderSelfieEnabled = enabled
        _uiState.update { it.copy(isIntruderSelfieEnabled = enabled) }
    }

    fun setThemeMode(mode: String) {
        securityManager.themeMode = mode
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setDisguiseOption(disguiseId: String) {
        val success = disguiseManager.applyDisguise(disguiseId, securityManager)
        if (success) {
            val current = disguiseManager.getCurrentDisguise(securityManager)
            _uiState.update {
                it.copy(
                    activeDisguise = current,
                    isDisguiseCalculatorActive = disguiseId == "CALCULATOR"
                )
            }
        }
    }

    fun updatePin(newPin: String) {
        securityManager.setPin(newPin)
        _uiState.update { it.copy(showPinChangeDialog = false) }
    }

    fun deleteIntruderLog(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            vaultDao.deleteIntruderLog(id)
        }
    }

    fun clearAllIntruderLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            vaultDao.clearIntruderLogs()
        }
    }

    fun setShowPinChangeDialog(show: Boolean) {
        _uiState.update { it.copy(showPinChangeDialog = show) }
    }

    fun setShowDisguisePicker(show: Boolean) {
        _uiState.update { it.copy(showDisguisePicker = show) }
    }

    fun setShowIntruderLogs(show: Boolean) {
        _uiState.update { it.copy(showIntruderLogs = show) }
    }

    fun setShowAdminInfoDialog(show: Boolean) {
        _uiState.update { it.copy(showAdminInfoDialog = show) }
    }

    fun setShowAboutDialog(show: Boolean) {
        _uiState.update { it.copy(showAboutDialog = show) }
    }

    fun setShowPermissionsDialog(show: Boolean) {
        _uiState.update { it.copy(showPermissionsDialog = show) }
    }
}
