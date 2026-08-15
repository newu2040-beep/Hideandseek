package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.HiddenAppEntity
import com.example.data.model.IntruderLogEntity
import com.example.data.model.VaultMediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    // Vault Media (Active)
    @Query("SELECT * FROM vault_media WHERE isTrash = 0 ORDER BY createdAt DESC")
    fun getAllMedia(): Flow<List<VaultMediaEntity>>

    @Query("SELECT * FROM vault_media WHERE mediaType = :type AND isTrash = 0 ORDER BY createdAt DESC")
    fun getMediaByType(type: String): Flow<List<VaultMediaEntity>>

    @Query("SELECT * FROM vault_media WHERE id = :id")
    suspend fun getMediaById(id: Long): VaultMediaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: VaultMediaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(mediaList: List<VaultMediaEntity>): List<Long>

    @Update
    suspend fun updateMedia(media: VaultMediaEntity)

    @Delete
    suspend fun deleteMedia(media: VaultMediaEntity)

    @Query("DELETE FROM vault_media WHERE id IN (:ids)")
    suspend fun deleteMediaByIds(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM vault_media WHERE isTrash = 0")
    fun getMediaCount(): Flow<Int>

    // Trash / Recycle Bin
    @Query("SELECT * FROM vault_media WHERE isTrash = 1 ORDER BY deletedAt DESC")
    fun getTrashMedia(): Flow<List<VaultMediaEntity>>

    @Query("SELECT * FROM vault_media WHERE isTrash = 1")
    suspend fun getTrashMediaList(): List<VaultMediaEntity>

    @Query("SELECT COUNT(*) FROM vault_media WHERE isTrash = 1")
    fun getTrashCount(): Flow<Int>

    @Query("UPDATE vault_media SET isTrash = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun moveToTrash(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE vault_media SET isTrash = 1, deletedAt = :deletedAt WHERE id IN (:ids)")
    suspend fun moveToTrashList(ids: List<Long>, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE vault_media SET isTrash = 0, deletedAt = 0 WHERE id = :id")
    suspend fun restoreFromTrash(id: Long)

    @Query("UPDATE vault_media SET isTrash = 0, deletedAt = 0 WHERE id IN (:ids)")
    suspend fun restoreFromTrashList(ids: List<Long>)

    @Query("DELETE FROM vault_media WHERE isTrash = 1")
    suspend fun emptyTrash()

    // Hidden Apps
    @Query("SELECT * FROM hidden_apps")
    fun getAllHiddenApps(): Flow<List<HiddenAppEntity>>

    @Query("SELECT * FROM hidden_apps")
    suspend fun getAllHiddenAppsList(): List<HiddenAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAppHidden(app: HiddenAppEntity)

    @Delete
    suspend fun removeHiddenApp(app: HiddenAppEntity)

    @Query("DELETE FROM hidden_apps WHERE packageName = :packageName")
    suspend fun unhideApp(packageName: String)

    // Intruder Logs
    @Query("SELECT * FROM intruder_logs ORDER BY attemptTimestamp DESC")
    fun getAllIntruderLogs(): Flow<List<IntruderLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntruderLog(log: IntruderLogEntity): Long

    @Query("DELETE FROM intruder_logs WHERE id = :id")
    suspend fun deleteIntruderLog(id: Long)

    @Query("DELETE FROM intruder_logs")
    suspend fun clearIntruderLogs()
}
