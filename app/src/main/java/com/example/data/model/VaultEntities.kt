package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_media")
data class VaultMediaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val originalPath: String,
    val encryptedPath: String,
    val mediaType: String, // "IMAGE", "VIDEO"
    val sizeBytes: Long,
    val durationMs: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isTrash: Boolean = false,
    val deletedAt: Long = 0L
)

@Entity(tableName = "hidden_apps")
data class HiddenAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isHidden: Boolean = true,
    val hiddenAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "intruder_logs")
data class IntruderLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val photoPath: String,
    val attemptTimestamp: Long = System.currentTimeMillis(),
    val attemptedPin: String = "****"
)
