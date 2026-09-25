package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.database.entity.CompetitionCriterionEntity
import com.example.data.database.entity.CompetitionEntity
import com.example.data.database.entity.CompetitionScoreEntity
import com.example.data.database.entity.JudgeEntity
import com.example.data.database.relation.CompetitionWithScores
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetitionDao {
    // --- Competitions ---
    @Query("SELECT * FROM competitions WHERE isDeleted = 0 ORDER BY eventDate DESC")
    fun getAllCompetitions(): Flow<List<CompetitionEntity>>

    @Query("SELECT * FROM competitions WHERE id = :competitionId AND isDeleted = 0 LIMIT 1")
    fun getCompetitionById(competitionId: String): Flow<CompetitionEntity?>

    @Query("SELECT * FROM competitions WHERE id = :competitionId AND isDeleted = 0 LIMIT 1")
    suspend fun getCompetitionByIdSync(competitionId: String): CompetitionEntity?

    @Transaction
    @Query("SELECT * FROM competitions WHERE id = :competitionId AND isDeleted = 0 LIMIT 1")
    fun getCompetitionWithScores(competitionId: String): Flow<CompetitionWithScores?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompetition(competition: CompetitionEntity)

    @Update
    suspend fun updateCompetition(competition: CompetitionEntity)

    @Delete
    suspend fun deleteCompetition(competition: CompetitionEntity)

    @Query("UPDATE competitions SET isDeleted = 1 WHERE id = :competitionId")
    suspend fun softDeleteCompetition(competitionId: String)

    // --- Judges ---
    @Query("SELECT * FROM judges WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllJudges(): Flow<List<JudgeEntity>>

    @Query("SELECT * FROM judges WHERE id = :judgeId AND isDeleted = 0 LIMIT 1")
    suspend fun getJudgeById(judgeId: String): JudgeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJudge(judge: JudgeEntity)

    @Update
    suspend fun updateJudge(judge: JudgeEntity)

    @Delete
    suspend fun deleteJudge(judge: JudgeEntity)

    // --- Competition Criteria ---
    @Query("SELECT * FROM competition_criteria WHERE competitionId = :competitionId AND isDeleted = 0 ORDER BY displayOrder ASC")
    fun getCriteriaForCompetition(competitionId: String): Flow<List<CompetitionCriterionEntity>>

    @Query("SELECT * FROM competition_criteria WHERE competitionId = :competitionId AND isDeleted = 0 ORDER BY displayOrder ASC")
    suspend fun getCriteriaForCompetitionSync(competitionId: String): List<CompetitionCriterionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCriterion(criterion: CompetitionCriterionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCriteria(criteria: List<CompetitionCriterionEntity>)

    @Update
    suspend fun updateCriterion(criterion: CompetitionCriterionEntity)

    @Delete
    suspend fun deleteCriterion(criterion: CompetitionCriterionEntity)

    @Query("DELETE FROM competition_criteria WHERE competitionId = :competitionId")
    suspend fun deleteCriteriaForCompetition(competitionId: String)

    // --- Competition Scores (Bird -> Competition History & Competition -> Scores) ---
    @Query("SELECT * FROM competition_scores WHERE birdRingNumber = :ringNumber AND isDeleted = 0 ORDER BY totalScore DESC")
    fun getScoresForBird(ringNumber: String): Flow<List<CompetitionScoreEntity>>

    @Query("SELECT * FROM competition_scores WHERE competitionId = :competitionId AND isDeleted = 0 ORDER BY totalScore DESC")
    fun getScoresForCompetition(competitionId: String): Flow<List<CompetitionScoreEntity>>

    @Query("SELECT * FROM competition_scores WHERE competitionId = :competitionId AND showClass = :showClass AND isDeleted = 0 ORDER BY totalScore DESC")
    fun getScoresForClass(competitionId: String, showClass: String): Flow<List<CompetitionScoreEntity>>

    @Query("SELECT * FROM competition_scores WHERE id = :scoreId LIMIT 1")
    suspend fun getScoreById(scoreId: String): CompetitionScoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: CompetitionScoreEntity)

    @Update
    suspend fun updateScore(score: CompetitionScoreEntity)

    @Delete
    suspend fun deleteScore(score: CompetitionScoreEntity)

    @Query("DELETE FROM competition_scores WHERE id = :scoreId")
    suspend fun deleteScoreById(scoreId: String)
}
