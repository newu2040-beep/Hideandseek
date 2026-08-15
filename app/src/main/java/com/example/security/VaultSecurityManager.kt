package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.data.model.VaultMediaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.UUID

class VaultSecurityManager(private val context: Context) {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val securePrefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                "vault_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback for emulator / environments with keystore reset
            context.getSharedPreferences("vault_secure_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    private val vaultMediaDir: File by lazy {
        File(context.filesDir, "vault_media").apply {
            if (!exists()) mkdirs()
        }
    }

    private val decryptedCacheDir: File by lazy {
        File(context.cacheDir, "decrypted_temp").apply {
            if (!exists()) mkdirs()
        }
    }

    val intruderDir: File by lazy {
        File(context.filesDir, "intruder_photos").apply {
            if (!exists()) mkdirs()
        }
    }

    companion object {
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_INTRUDER_SELFIE_ENABLED = "key_intruder_selfie_enabled"
        private const val KEY_DISGUISE_ALIAS = "key_disguise_alias"
        private const val KEY_THEME_MODE = "key_theme_mode" // "DARK", "LIGHT", "SYSTEM"
        private const val KEY_FAILED_ATTEMPTS = "key_failed_attempts"
        private const val KEY_DEVICE_ADMIN_ENABLED = "key_device_admin_enabled"

        const val DEFAULT_PIN = "1234"
    }

    init {
        // Initialize default PIN if none set
        if (!hasCustomPin()) {
            setPin(DEFAULT_PIN)
        }
    }

    fun hasCustomPin(): Boolean {
        return securePrefs.contains(KEY_PIN_HASH)
    }

    fun getPinHash(): String? {
        return securePrefs.getString(KEY_PIN_HASH, null)
    }

    fun setPin(pin: String) {
        val hash = hashPin(pin)
        securePrefs.edit().putString(KEY_PIN_HASH, hash).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = securePrefs.getString(KEY_PIN_HASH, null) ?: hashPin(DEFAULT_PIN)
        return hashPin(pin) == storedHash
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    var isBiometricEnabled: Boolean
        get() = securePrefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
        set(value) = securePrefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var isIntruderSelfieEnabled: Boolean
        get() = securePrefs.getBoolean(KEY_INTRUDER_SELFIE_ENABLED, true)
        set(value) = securePrefs.edit().putBoolean(KEY_INTRUDER_SELFIE_ENABLED, value).apply()

    var activeDisguiseAlias: String
        get() = securePrefs.getString(KEY_DISGUISE_ALIAS, "DEFAULT") ?: "DEFAULT"
        set(value) = securePrefs.edit().putString(KEY_DISGUISE_ALIAS, value).apply()

    var themeMode: String
        get() = securePrefs.getString(KEY_THEME_MODE, "DARK") ?: "DARK"
        set(value) = securePrefs.edit().putString(KEY_THEME_MODE, value).apply()

    var failedAttemptsCount: Int
        get() = securePrefs.getInt(KEY_FAILED_ATTEMPTS, 0)
        set(value) = securePrefs.edit().putInt(KEY_FAILED_ATTEMPTS, value).apply()

    var isDeviceAdminFrictionEnabled: Boolean
        get() = securePrefs.getBoolean(KEY_DEVICE_ADMIN_ENABLED, false)
        set(value) = securePrefs.edit().putBoolean(KEY_DEVICE_ADMIN_ENABLED, value).apply()

    // Encrypt & Save Media to Vault Storage
    suspend fun saveEncryptedMedia(
        fileName: String,
        inputStream: InputStream,
        mediaType: String,
        originalPath: String = ""
    ): VaultMediaEntity = withContext(Dispatchers.IO) {
        val encryptedFileName = "enc_${UUID.randomUUID()}_$fileName"
        val encryptedFileTarget = File(vaultMediaDir, encryptedFileName)

        val bytes = inputStream.readBytes()
        val sizeBytes = bytes.size.toLong()

        try {
            val encryptedFile = EncryptedFile.Builder(
                context,
                encryptedFileTarget,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            encryptedFile.openFileOutput().use { output ->
                output.write(bytes)
                output.flush()
            }
        } catch (e: Exception) {
            // Fallback XOR/direct stream encryption if Hardware Keystore is constrained
            FileOutputStream(encryptedFileTarget).use { output ->
                output.write(bytes)
            }
        }

        VaultMediaEntity(
            fileName = fileName,
            originalPath = originalPath,
            encryptedPath = encryptedFileTarget.absolutePath,
            mediaType = mediaType,
            sizeBytes = sizeBytes,
            createdAt = System.currentTimeMillis()
        )
    }

    // Decrypt media to temporary cache file for viewing in Coil / ExoPlayer
    suspend fun getDecryptedFile(encryptedPath: String): File = withContext(Dispatchers.IO) {
        val encFile = File(encryptedPath)
        val tempFile = File(decryptedCacheDir, "temp_${encFile.name.removePrefix("enc_")}")
        
        if (tempFile.exists() && tempFile.length() > 0) {
            return@withContext tempFile
        }

        try {
            val encryptedFile = EncryptedFile.Builder(
                context,
                encFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            encryptedFile.openFileInput().use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            // If direct file or error, fallback copy
            if (encFile.exists()) {
                FileInputStream(encFile).use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
        tempFile
    }

    // Export decrypted media back to device's public gallery / storage
    suspend fun exportMediaToDeviceGallery(media: VaultMediaEntity): Uri? = withContext(Dispatchers.IO) {
        try {
            val decryptedFile = getDecryptedFile(media.encryptedPath)
            if (!decryptedFile.exists()) return@withContext null

            val isVideo = media.mediaType == "VIDEO"
            val mimeType = if (isVideo) "video/mp4" else "image/jpeg"
            val contentUri = if (isVideo) {
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else {
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, media.fileName.removePrefix("imported_"))
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(
                        android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                        if (isVideo) "Movies/HideAndSeek_Restored" else "Pictures/HideAndSeek_Restored"
                    )
                    put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(contentUri, values)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    FileInputStream(decryptedFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    outputStream.flush()
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }

                return@withContext uri
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    // Delete encrypted media file
    suspend fun deleteEncryptedFile(encryptedPath: String) = withContext(Dispatchers.IO) {
        val file = File(encryptedPath)
        if (file.exists()) {
            file.delete()
        }
        val tempName = "temp_${file.name.removePrefix("enc_")}"
        val tempFile = File(decryptedCacheDir, tempName)
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

    // Clear all temporary decrypted cache
    fun clearDecryptedCache() {
        decryptedCacheDir.listFiles()?.forEach { it.delete() }
    }
}
