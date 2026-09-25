package com.example.feature.birds

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.IncomeEntity
import com.example.data.database.entity.MediaDocumentEntity
import com.example.data.database.entity.MedicationEntity
import com.example.data.database.entity.NutritionRecordEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.PedigreeRecordEntity
import com.example.data.database.entity.WeightRecordEntity
import com.example.data.database.relation.BirdWithChildren
import com.example.data.database.relation.BirdWithDetails
import com.example.data.database.relation.BirdWithParents
import com.example.data.repository.AviaryRepository
import com.example.feature.birds.export.BirdPdfExporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class BirdDetailViewModel(
    private val repository: AviaryRepository,
    initialRingNumber: String = ""
) : ViewModel() {

    private val _currentRing = MutableStateFlow(initialRingNumber)
    val currentRing: StateFlow<String> = _currentRing.asStateFlow()

    fun selectBird(ringNumber: String) {
        _currentRing.value = ringNumber
    }

    val birdWithDetails: StateFlow<BirdWithDetails?> = _currentRing.flatMapLatest { ring ->
        if (ring.isBlank()) flowOf(null) else repository.getBirdWithDetails(ring)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val parents: StateFlow<BirdWithParents?> = _currentRing.flatMapLatest { ring ->
        if (ring.isBlank()) flowOf(null) else repository.getBirdWithParents(ring)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val children: StateFlow<BirdWithChildren?> = _currentRing.flatMapLatest { ring ->
        if (ring.isBlank()) flowOf(null) else repository.getBirdWithChildren(ring)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pairs: StateFlow<List<PairEntity>> = _currentRing.flatMapLatest { ring ->
        if (ring.isBlank()) flowOf(emptyList()) else repository.getPairsForBird(ring)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeRecords: StateFlow<List<IncomeEntity>> = _currentRing.flatMapLatest { ring ->
        if (ring.isBlank()) flowOf(emptyList()) else repository.getIncomeForBird(ring)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val genetics: StateFlow<BirdGeneticsEntity?> = _currentRing.flatMapLatest { ring ->
        if (ring.isBlank()) flowOf(null) else repository.getGeneticsForBird(ring)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pedigree: StateFlow<PedigreeRecordEntity?> = _currentRing.flatMapLatest { ring ->
        if (ring.isBlank()) flowOf(null) else repository.getPedigreeForBird(ring)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val photos: StateFlow<List<MediaDocumentEntity>> = _currentRing.flatMapLatest { ring ->
        if (ring.isBlank()) flowOf(emptyList()) else repository.getMediaForEntity("BIRD", ring)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medications: StateFlow<List<MedicationEntity>> = _currentRing.flatMapLatest { ring ->
        if (ring.isBlank()) flowOf(emptyList()) else repository.getMedicationsForBird(ring)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nutritionRecords: StateFlow<List<NutritionRecordEntity>> = _currentRing.flatMapLatest { ring ->
        if (ring.isBlank()) flowOf(emptyList()) else repository.getNutritionRecordsForBird(ring)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveBird(updatedBird: BirdEntity) {
        viewModelScope.launch {
            repository.saveBird(updatedBird)
            repository.recordAuditLog(
                actionType = "UPDATE_BIRD",
                entityType = "BIRD",
                entityId = updatedBird.ringNumber,
                summary = "Updated bird profile: ${updatedBird.ringNumber}"
            )
        }
    }

    fun addWeight(
        weightGrams: Double,
        conditionScore: Int = 3,
        condition: String = "Good",
        notes: String? = null
    ) {
        val ring = _currentRing.value
        if (ring.isBlank()) return
        viewModelScope.launch {
            repository.saveWeight(
                WeightRecordEntity(
                    birdRingNumber = ring,
                    weightGrams = weightGrams,
                    recordedDate = System.currentTimeMillis(),
                    conditionScore = condition,
                    notes = notes
                )
            )
            repository.recordAuditLog(
                actionType = "ADD_WEIGHT",
                entityType = "BIRD",
                entityId = ring,
                summary = "Recorded weight of ${weightGrams}g for $ring"
            )
        }
    }

    fun addHealthRecord(
        issue: String,
        symptoms: String = "",
        diagnosis: String = "",
        vet: String = "",
        cost: Double = 0.0
    ) {
        val ring = _currentRing.value
        if (ring.isBlank()) return
        viewModelScope.launch {
            repository.saveHealthRecord(
                HealthRecordEntity(
                    birdRingNumber = ring,
                    recordDate = System.currentTimeMillis(),
                    recordType = "EXAMINATION",
                    symptoms = symptoms,
                    diagnosis = if (cost > 0) "$diagnosis (Treatment Cost: $$cost)" else diagnosis,
                    veterinarianName = vet.takeIf { it.isNotBlank() },
                    notes = issue
                )
            )
            repository.recordAuditLog(
                actionType = "ADD_HEALTH_RECORD",
                entityType = "BIRD",
                entityId = ring,
                summary = "Added health issue '$issue' for $ring"
            )
        }
    }

    fun addFullHealthRecord(record: HealthRecordEntity, scheduleReminders: Boolean) {
        val ring = _currentRing.value
        if (ring.isBlank()) return
        viewModelScope.launch {
            repository.saveHealthRecord(record.copy(birdRingNumber = ring))
            if (scheduleReminders && !record.medicationName.isNullOrBlank()) {
                val med = MedicationEntity(
                    healthRecordId = record.id,
                    birdRingNumber = ring,
                    medicationName = record.medicationName,
                    dosage = record.dosage ?: "As prescribed",
                    frequency = record.frequency ?: "ONCE_DAILY",
                    startDate = record.startDate,
                    endDate = record.endDate ?: (record.startDate + (record.treatmentDurationDays.coerceAtLeast(1) * 86400000L)),
                    notes = record.recordedProblem
                )
                repository.scheduleMedicationWithReminders(med)
            }
            repository.recordAuditLog(
                actionType = "LOG_TREATMENT",
                entityType = "BIRD",
                entityId = ring,
                summary = "Logged treatment ${record.recordedProblem} with ${record.medicationName ?: "care"} for $ring"
            )
        }
    }

    fun addNutritionRecord(record: NutritionRecordEntity) {
        val ring = _currentRing.value
        if (ring.isBlank()) return
        viewModelScope.launch {
            repository.saveNutritionRecord(record.copy(birdRingNumber = ring))
            repository.recordAuditLog(
                actionType = "ADD_NUTRITION",
                entityType = "BIRD",
                entityId = ring,
                summary = "Logged ${record.foodType} feeding for $ring"
            )
        }
    }

    fun deleteHealthRecord(record: HealthRecordEntity) {
        viewModelScope.launch {
            repository.deleteHealthRecord(record)
        }
    }

    fun deleteNutritionRecord(record: NutritionRecordEntity) {
        viewModelScope.launch {
            repository.deleteNutritionRecord(record)
        }
    }

    fun deleteWeightRecord(record: WeightRecordEntity) {
        viewModelScope.launch {
            repository.deleteWeight(record)
        }
    }

    fun addPhoto(
        title: String,
        fileUri: String,
        isPrimary: Boolean = false
    ) {
        val ring = _currentRing.value
        if (ring.isBlank()) return
        viewModelScope.launch {
            repository.saveMedia(
                MediaDocumentEntity(
                    relatedEntityType = "BIRD",
                    relatedEntityId = ring,
                    mediaType = "PHOTO",
                    filePathOrUri = fileUri,
                    fileName = title.ifBlank { "bird_photo.jpg" },
                    caption = title,
                    isPrimaryPhoto = isPrimary
                )
            )
            if (isPrimary) {
                birdWithDetails.value?.bird?.let { currentBird ->
                    repository.saveBird(currentBird.copy(photoUri = fileUri))
                }
            }
        }
    }

    fun setPrimaryPhoto(photo: MediaDocumentEntity) {
        val ring = _currentRing.value
        if (ring.isBlank()) return
        viewModelScope.launch {
            repository.saveMedia(photo.copy(isPrimaryPhoto = true))
            birdWithDetails.value?.bird?.let { currentBird ->
                repository.saveBird(currentBird.copy(photoUri = photo.filePathOrUri))
            }
        }
    }

    fun saveGenetics(entity: BirdGeneticsEntity) {
        viewModelScope.launch {
            repository.saveGenetics(entity)
        }
    }

    fun savePedigree(entity: PedigreeRecordEntity) {
        viewModelScope.launch {
            repository.savePedigree(entity)
        }
    }

    fun deleteBird(onDeleted: () -> Unit) {
        val ring = _currentRing.value
        if (ring.isBlank()) return
        viewModelScope.launch {
            repository.deleteBird(ring)
            onDeleted()
        }
    }

    fun exportAndSharePdf(context: Context): File? {
        val currentDetails = birdWithDetails.value ?: return null
        val generatedFile = BirdPdfExporter.generateAndSharePdf(
            context = context,
            birdWithDetails = currentDetails,
            pedigree = pedigree.value,
            genetics = genetics.value,
            children = children.value
        )
        if (generatedFile != null) {
            BirdPdfExporter.sharePdf(context, generatedFile)
        }
        return generatedFile
    }

    fun exportFullDossierPdf(context: Context, isPersian: Boolean = false): File? {
        val currentDetails = birdWithDetails.value ?: return null
        val generatedFile = com.example.core.export.BirdDossierPdfExporter.generateFullDossierPdf(
            context = context,
            birdWithDetails = currentDetails,
            pedigree = pedigree.value,
            genetics = genetics.value,
            children = children.value,
            breedingPairs = pairs.value,
            medications = medications.value,
            incomeRecords = incomeRecords.value,
            isPersian = isPersian
        )
        if (generatedFile != null) {
            com.example.core.export.BirdDossierPdfExporter.sharePdf(
                context = context,
                pdfFile = generatedFile,
                title = "Full Bird Dossier & Pedigree"
            )
        }
        return generatedFile
    }

    class Factory(
        private val repository: AviaryRepository,
        private val initialRingNumber: String = ""
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BirdDetailViewModel(repository, initialRingNumber) as T
        }
    }
}
