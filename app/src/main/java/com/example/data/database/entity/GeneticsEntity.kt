package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "bird_genetics",
    indices = [
        Index(value = ["birdRingNumber"], unique = true)
    ]
)
data class BirdGeneticsEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val birdRingNumber: String,
    val visualMutations: String,
    val splitMutations: String = "",
    val baseSeries: String = "BLUE", // GREEN, BLUE
    val darkFactors: Int = 0, // 0 = Light, 1 = Single Dark (Cobalt/Dark Green), 2 = Double Dark (Mauve/Olive)
    val violetFactor: Boolean = false,
    val greyFactor: Boolean = false,
    val yellowFaceType: String? = null, // YellowFace I, YellowFace II, Goldenface
    val dilution: String = "NONE", // NONE, GREYWING, CLEARWING, DILUTE
    val cinnamonFactor: Boolean = false,
    val inoFactor: Boolean = false,
    val opalineFactor: Boolean = false,
    val spangleFactor: String = "NONE", // NONE, SINGLE_FACTOR, DOUBLE_FACTOR
    val piebaldType: String = "NONE", // NONE, DOMINANT_PIED, RECESSIVE_PIED, CLEARFLIGHT
    val crestedType: String = "NONE", // NONE, TUFTED, HALF_CIRCULAR, FULL_CIRCULAR, HAGOROMO
    val notes: String? = null,
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
