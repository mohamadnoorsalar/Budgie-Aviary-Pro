package com.example.data.database.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.ClutchEntity
import com.example.data.database.entity.CompetitionEntity
import com.example.data.database.entity.CompetitionScoreEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.MediaDocumentEntity
import com.example.data.database.entity.MedicationEntity
import com.example.data.database.entity.NestEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.PedigreeRecordEntity
import com.example.data.database.entity.WeightRecordEntity

/**
 * Bird with its complete relational profile:
 * Bird -> Cage
 * Bird -> Genetics
 * Bird -> Pedigree
 * Bird -> Health
 * Bird -> Weight History
 * Bird -> Competition History
 */
data class BirdWithDetails(
    @Embedded val bird: BirdEntity,

    @Relation(parentColumn = "cageCode", entityColumn = "code")
    val cage: CageEntity?,

    @Relation(parentColumn = "ringNumber", entityColumn = "birdRingNumber")
    val genetics: BirdGeneticsEntity?,

    @Relation(parentColumn = "ringNumber", entityColumn = "birdRingNumber")
    val pedigree: PedigreeRecordEntity?,

    @Relation(parentColumn = "ringNumber", entityColumn = "birdRingNumber")
    val healthRecords: List<HealthRecordEntity>,

    @Relation(parentColumn = "ringNumber", entityColumn = "birdRingNumber")
    val weightHistory: List<WeightRecordEntity>,

    @Relation(parentColumn = "ringNumber", entityColumn = "birdRingNumber")
    val competitionScores: List<CompetitionScoreEntity>
)

/**
 * Cage with resident Birds, Breeding Pairs, and Nests
 */
data class CageWithDetails(
    @Embedded val cage: CageEntity,

    @Relation(parentColumn = "code", entityColumn = "cageCode")
    val birds: List<BirdEntity>,

    @Relation(parentColumn = "code", entityColumn = "cageCode")
    val pairs: List<PairEntity>,

    @Relation(parentColumn = "code", entityColumn = "cageCode")
    val nests: List<NestEntity>
) {
    val activePair: PairEntity? get() = pairs.firstOrNull { it.isActive }
    val primaryNest: NestEntity? get() = nests.firstOrNull { !it.isDeleted }
}

/**
 * Bird -> Parents relationship
 */
data class BirdWithParents(
    @Embedded val bird: BirdEntity,

    @Relation(parentColumn = "fatherRing", entityColumn = "ringNumber")
    val father: BirdEntity?,

    @Relation(parentColumn = "motherRing", entityColumn = "ringNumber")
    val mother: BirdEntity?
)

/**
 * Bird -> Children relationship
 */
data class BirdWithChildren(
    @Embedded val bird: BirdEntity,

    @Relation(parentColumn = "ringNumber", entityColumn = "fatherRing")
    val fatheredChildren: List<BirdEntity>,

    @Relation(parentColumn = "ringNumber", entityColumn = "motherRing")
    val motheredChildren: List<BirdEntity>
) {
    val allChildren: List<BirdEntity>
        get() = (fatheredChildren + motheredChildren).distinctBy { it.ringNumber }
}

/**
 * Pair -> Eggs, Chicks, Clutches, Nest, Cage, and parent Birds
 */
data class PairWithBreedingDetails(
    @Embedded val pair: PairEntity,

    @Relation(parentColumn = "maleRingNumber", entityColumn = "ringNumber")
    val maleBird: BirdEntity?,

    @Relation(parentColumn = "femaleRingNumber", entityColumn = "ringNumber")
    val femaleBird: BirdEntity?,

    @Relation(parentColumn = "cageCode", entityColumn = "code")
    val cage: CageEntity?,

    @Relation(parentColumn = "nestId", entityColumn = "id")
    val nest: NestEntity?,

    @Relation(parentColumn = "id", entityColumn = "pairId")
    val eggs: List<EggEntity>,

    @Relation(parentColumn = "id", entityColumn = "pairId")
    val chicks: List<ChickEntity>,

    @Relation(parentColumn = "id", entityColumn = "pairId")
    val clutches: List<ClutchEntity>
)

/**
 * Egg -> Chick relationship
 */
data class EggWithChick(
    @Embedded val egg: EggEntity,

    @Relation(parentColumn = "id", entityColumn = "eggId")
    val chick: ChickEntity?
)

/**
 * Chick -> Egg and Pair relationship
 */
data class ChickWithBreedingDetails(
    @Embedded val chick: ChickEntity,

    @Relation(parentColumn = "eggId", entityColumn = "id")
    val egg: EggEntity?,

    @Relation(parentColumn = "pairId", entityColumn = "id")
    val pair: PairEntity?
)

/**
 * Competition -> Scores
 */
data class CompetitionWithScores(
    @Embedded val competition: CompetitionEntity,

    @Relation(parentColumn = "id", entityColumn = "competitionId")
    val scores: List<CompetitionScoreEntity>
)

/**
 * Health Record -> Medications
 */
data class HealthRecordWithMedications(
    @Embedded val healthRecord: HealthRecordEntity,

    @Relation(parentColumn = "id", entityColumn = "healthRecordId")
    val medications: List<MedicationEntity>
)
