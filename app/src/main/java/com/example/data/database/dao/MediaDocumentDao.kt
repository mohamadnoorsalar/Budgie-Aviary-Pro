package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entity.MediaDocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDocumentDao {
    @Query("SELECT * FROM media_documents WHERE relatedEntityType = :type AND relatedEntityId = :id AND isDeleted = 0 ORDER BY isPrimaryPhoto DESC, uploadedAt DESC")
    fun getMediaForEntity(type: String, id: String): Flow<List<MediaDocumentEntity>>

    @Query("SELECT * FROM media_documents WHERE relatedEntityType = :type AND relatedEntityId = :id AND isPrimaryPhoto = 1 AND isDeleted = 0 LIMIT 1")
    fun getPrimaryPhotoForEntity(type: String, id: String): Flow<MediaDocumentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaDocumentEntity)

    @Update
    suspend fun updateMedia(media: MediaDocumentEntity)

    @Delete
    suspend fun deleteMedia(media: MediaDocumentEntity)
}
