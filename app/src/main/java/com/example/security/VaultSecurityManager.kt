package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
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
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

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
            // Fallback for environments with keystore reset
            context.getSharedPreferences("vault_secure_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    val vaultMediaDir: File by lazy {
        File(context.filesDir, "vault_media").apply {
            if (!exists()) mkdirs()
        }
    }

    val vaultThumbsDir: File by lazy {
        File(context.filesDir, "vault_thumbs").apply {
            if (!exists()) mkdirs()
        }
    }

    val decryptedCacheDir: File by lazy {
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
        private const val KEY_THEME_MODE = "key_theme_mode" // "DARK", "LIGHT", "SYSTEM", etc.
        private const val KEY_FAILED_ATTEMPTS = "key_failed_attempts"
        private const val KEY_DEVICE_ADMIN_ENABLED = "key_device_admin_enabled"
        private const val KEY_VAULT_AES_KEY = "key_vault_aes_key"

        const val DEFAULT_PIN = "1234"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }

    init {
        // Initialize default PIN if none set
        if (!hasCustomPin()) {
            setPin(DEFAULT_PIN)
        }
        ensureAesKeyGenerated()
    }

    private fun ensureAesKeyGenerated() {
        if (!securePrefs.contains(KEY_VAULT_AES_KEY)) {
            val keyBytes = ByteArray(32)
            SecureRandom().nextBytes(keyBytes)
            val hexKey = keyBytes.joinToString("") { "%02x".format(it) }
            securePrefs.edit().putString(KEY_VAULT_AES_KEY, hexKey).apply()
        }
    }

    private fun getSecretKey(): SecretKeySpec {
        var hexKey = securePrefs.getString(KEY_VAULT_AES_KEY, null)
        if (hexKey == null || hexKey.length != 64) {
            val keyBytes = ByteArray(32)
            SecureRandom().nextBytes(keyBytes)
            hexKey = keyBytes.joinToString("") { "%02x".format(it) }
            securePrefs.edit().putString(KEY_VAULT_AES_KEY, hexKey).apply()
        }
        val keyBytes = ByteArray(32)
        for (i in 0 until 32) {
            keyBytes[i] = hexKey.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return SecretKeySpec(keyBytes, "AES")
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

    // Encrypt & Save Media to Vault Storage with Thumbnail Generation
    suspend fun saveEncryptedMedia(
        fileName: String,
        inputStream: InputStream,
        mediaType: String,
        originalPath: String = ""
    ): VaultMediaEntity = withContext(Dispatchers.IO) {
        val uniqueId = UUID.randomUUID().toString()
        val encryptedFileName = "enc_${uniqueId}_$fileName"
        val encryptedFileTarget = File(vaultMediaDir, encryptedFileName)
        val thumbTarget = File(vaultThumbsDir, "thumb_${uniqueId}.jpg")

        val rawBytes = inputStream.readBytes()
        val sizeBytes = rawBytes.size.toLong()
        var videoDurationMs = 0L

        // 1. Generate Thumbnail for Instant Preview
        try {
            if (mediaType == "VIDEO") {
                val retriever = MediaMetadataRetriever()
                try {
                    if (originalPath.isNotEmpty() && originalPath.startsWith("content://")) {
                        retriever.setDataSource(context, Uri.parse(originalPath))
                    } else if (originalPath.isNotEmpty() && File(originalPath).exists()) {
                        retriever.setDataSource(originalPath)
                    } else {
                        // Write raw bytes to temporary cache to extract frame
                        val tempRaw = File(decryptedCacheDir, "raw_${uniqueId}.mp4")
                        FileOutputStream(tempRaw).use { it.write(rawBytes) }
                        retriever.setDataSource(tempRaw.absolutePath)
                        tempRaw.delete()
                    }
                    val frameBitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        ?: retriever.frameAtTime
                    val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    videoDurationMs = durationStr?.toLongOrNull() ?: 0L

                    if (frameBitmap != null) {
                        FileOutputStream(thumbTarget).use { out ->
                            frameBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try { retriever.release() } catch (ignored: Exception) {}
                }
            } else {
                // Photo thumbnail
                val options = BitmapFactory.Options().apply { inSampleSize = 2 }
                val bmp = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, options)
                if (bmp != null) {
                    FileOutputStream(thumbTarget).use { out ->
                        bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Encrypt Raw Media with AES-GCM
        try {
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().nextBytes(iv)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), spec)

            val encryptedBytes = cipher.doFinal(rawBytes)
            FileOutputStream(encryptedFileTarget).use { output ->
                output.write(iv) // Prepend 12-byte IV
                output.write(encryptedBytes)
                output.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Direct write fallback
            FileOutputStream(encryptedFileTarget).use { output ->
                output.write(rawBytes)
            }
        }

        VaultMediaEntity(
            fileName = fileName,
            originalPath = originalPath,
            encryptedPath = encryptedFileTarget.absolutePath,
            mediaType = mediaType,
            sizeBytes = sizeBytes,
            durationMs = videoDurationMs,
            createdAt = System.currentTimeMillis()
        )
    }

    // Get Thumbnail File for Grid Display
    fun getThumbnailFile(encryptedPath: String): File? {
        val encFile = File(encryptedPath)
        val name = encFile.name.removePrefix("enc_")
        val uniqueId = name.substringBefore("_")
        val thumbFile = File(vaultThumbsDir, "thumb_${uniqueId}.jpg")
        return if (thumbFile.exists() && thumbFile.length() > 0) thumbFile else null
    }

    // Decrypt media to temporary cache file for viewing in Coil / ExoPlayer
    suspend fun getDecryptedFile(encryptedPath: String): File = withContext(Dispatchers.IO) {
        val encFile = File(encryptedPath)
        val ext = if (encryptedPath.endsWith(".mp4", ignoreCase = true)) ".mp4" else ".jpg"
        val tempFile = File(decryptedCacheDir, "dec_${encFile.name.removePrefix("enc_").substringBeforeLast(".")}$ext")

        if (tempFile.exists() && tempFile.length() > 0) {
            return@withContext tempFile
        }

        try {
            val fileBytes = FileInputStream(encFile).use { it.readBytes() }
            if (fileBytes.size > GCM_IV_LENGTH) {
                val iv = fileBytes.copyOfRange(0, GCM_IV_LENGTH)
                val cipherBytes = fileBytes.copyOfRange(GCM_IV_LENGTH, fileBytes.size)

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

                val decryptedBytes = cipher.doFinal(cipherBytes)
                FileOutputStream(tempFile).use { output ->
                    output.write(decryptedBytes)
                    output.flush()
                }
            } else {
                FileOutputStream(tempFile).use { it.write(fileBytes) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Direct copy fallback if unencrypted
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
        val name = file.name.removePrefix("enc_")
        val uniqueId = name.substringBefore("_")
        val thumbFile = File(vaultThumbsDir, "thumb_${uniqueId}.jpg")
        if (thumbFile.exists()) {
            thumbFile.delete()
        }
        decryptedCacheDir.listFiles()?.forEach { f ->
            if (f.name.contains(uniqueId)) f.delete()
        }
    }

    // Clear all temporary decrypted cache
    fun clearDecryptedCache() {
        decryptedCacheDir.listFiles()?.forEach { it.delete() }
    }
}
