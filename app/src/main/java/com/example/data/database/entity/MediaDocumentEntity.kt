package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "media_documents",
    indices = [
        Index(value = ["relatedEntityType", "relatedEntityId"]),
        Index(value = ["mediaType"]),
        Index(value = ["uploadedAt"])
    ]
)
data class MediaDocumentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val relatedEntityType: String, // BIRD, PAIR, EGG, HEALTH, COMPETITION, PEDIGREE, EXPENSE
    val relatedEntityId: String,
    val mediaType: String = "PHOTO", // PHOTO, DOCUMENT_PDF, CERTIFICATE, DNA_REPORT
    val filePathOrUri: String,
    val fileName: String,
    val fileSizeBytes: Long = 0L,
    val caption: String? = null,
    val isPrimaryPhoto: Boolean = false,
    val uploadedAt: Long = System.currentTimeMillis(),
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
