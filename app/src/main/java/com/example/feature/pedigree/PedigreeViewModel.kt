package com.example.feature.pedigree

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.pedigree.InteractivePedigreeTreeData
import com.example.core.pedigree.PedigreeTreeBuilder
import com.example.core.pedigree.PedigreeTreeNode
import com.example.data.database.AppDatabase
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.PedigreeRecordEntity
import com.example.data.repository.AviaryRepository
import com.example.data.repository.AviaryRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PedigreeViewModel(
    application: Application,
    private val repository: AviaryRepository = AviaryRepositoryImpl(AppDatabase.getInstance(application))
) : AndroidViewModel(application) {

    val allBirds: StateFlow<List<BirdEntity>> = repository.allBirds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPedigrees: StateFlow<List<PedigreeRecordEntity>> = repository.allPedigrees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGenetics: StateFlow<List<BirdGeneticsEntity>> = repository.allGenetics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedBirdRing = MutableStateFlow("")
    val selectedBirdRing = _selectedBirdRing.asStateFlow()

    private val _treeData = MutableStateFlow<InteractivePedigreeTreeData?>(null)
    val treeData = _treeData.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Ancestors, 1: Descendants, 2: Lineage Stats
    val selectedTab = _selectedTab.asStateFlow()

    private val _selectedNodeDetail = MutableStateFlow<PedigreeTreeNode?>(null)
    val selectedNodeDetail = _selectedNodeDetail.asStateFlow()

    init {
        viewModelScope.launch {
            combine(allBirds, allPedigrees, allGenetics, _selectedBirdRing) { birds, pedigrees, genetics, selectedRing ->
                val ringToUse = if (selectedRing.isNotBlank()) {
                    selectedRing
                } else {
                    birds.firstOrNull()?.ringNumber ?: ""
                }

                if (ringToUse.isNotBlank()) {
                    _selectedBirdRing.value = ringToUse
                    _treeData.value = PedigreeTreeBuilder.buildPedigreeTree(ringToUse, birds, pedigrees, genetics)
                } else {
                    _treeData.value = null
                }
            }.collect {}
        }
    }

    fun selectBird(ringNumber: String) {
        if (ringNumber.isBlank()) return
        _selectedBirdRing.value = ringNumber
        _selectedNodeDetail.value = null
        _treeData.value = PedigreeTreeBuilder.buildPedigreeTree(
            ringNumber,
            allBirds.value,
            allPedigrees.value,
            allGenetics.value
        )
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun selectNodeDetail(node: PedigreeTreeNode?) {
        _selectedNodeDetail.value = node
    }

    fun savePedigree(pedigree: PedigreeRecordEntity) {
        viewModelScope.launch {
            repository.savePedigree(pedigree)
            repository.recordAuditLog(
                actionType = "UPDATE_PEDIGREE",
                entityType = "PEDIGREE",
                entityId = pedigree.birdRingNumber,
                summary = "Updated lineage certificate for ${pedigree.birdRingNumber}"
            )
            // Rebuild tree
            _treeData.value = PedigreeTreeBuilder.buildPedigreeTree(
                _selectedBirdRing.value,
                allBirds.value,
                allPedigrees.value,
                allGenetics.value
            )
        }
    }
}
