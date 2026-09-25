package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "competitions",
    indices = [
        Index(value = ["eventDate"])
    ]
)
data class CompetitionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val eventDate: Long = System.currentTimeMillis(),
    val location: String,
    val organizingClub: String,
    val showStandard: String = "WBO", // WBO, NATIONAL, CUSTOM, etc.
    val categoriesJson: String = "[]", // List<String> of classes/categories e.g. ["English Show Cock", "Young Hen", "Normal Green"]
    val judgingCodesJson: String = "[]", // List<String> of judging codes e.g. ["J-01", "J-02"]
    val status: String = "SCHEDULED", // SCHEDULED, IN_PROGRESS, COMPLETED, ARCHIVED
    val notes: String? = null,
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "judges",
    indices = [
        Index(value = ["name"])
    ]
)
data class JudgeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val certification: String = "NATIONAL", // NATIONAL, WBO_INTERNATIONAL, CLUB, GUEST
    val affiliation: String? = null,
    val country: String? = null,
    val contactInfo: String? = null,
    val assignedCodes: String? = null, // e.g. "J-01, J-02"
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "competition_criteria",
    indices = [
        Index(value = ["competitionId"])
    ]
)
data class CompetitionCriterionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val competitionId: String, // Can also be "DEFAULT" for global reusable criteria
    val name: String,
    val description: String? = null,
    val maxScore: Double = 20.0,
    val weight: Double = 1.0,
    val displayOrder: Int = 0,
    val isDeleted: Boolean = false
)

@Entity(
    tableName = "competition_scores",
    indices = [
        Index(value = ["competitionId"]),
        Index(value = ["birdRingNumber"]),
        Index(value = ["judgeId"]),
        Index(value = ["showClass"])
    ]
)
data class CompetitionScoreEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val competitionId: String,
    val birdRingNumber: String,
    val participantName: String? = null, // Exhibitor/Breeder name
    val judgeId: String? = null,
    val judgeName: String? = null,
    val judgingCode: String? = null, // Anonymized show judging code e.g. "CAGE-42" / "JC-101"
    val showClass: String, // Category e.g. "Adult Cock - Normal Blue"
    val cageNumberInShow: String? = null,
    // Detailed criteria breakdown in JSON: Map<criterionId, scoreValue>
    val criteriaScoresJson: String = "{}",
    // Standard quick scores (kept for backwards compatibility & quick access)
    val headScore: Double = 0.0,
    val maskSpotsScore: Double = 0.0,
    val stanceScore: Double = 0.0,
    val featherConditionScore: Double = 0.0,
    val overallImpressionScore: Double = 0.0,
    val totalScore: Double = 0.0,
    val awardTitle: String? = null, // BEST_IN_SHOW, FIRST_IN_CLASS, SECOND, THIRD, DIPLOMA, PARTICIPATION
    val notes: String? = null, // Judge comments
    val photoUri: String? = null, // Photo of bird in show cage
    val isDigitallyConfirmed: Boolean = false, // Digital confirmation by judge
    val confirmedAt: Long? = null,
    val confirmedByJudgeSignature: String? = null, // Digital signature string/name
    val visualAiSuggestion: String? = null, // AI visual advice/preliminary hints (optional)
    val userId: String? = "default_user",
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
