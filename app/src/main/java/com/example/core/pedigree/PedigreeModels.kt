package com.example.core.pedigree

import com.example.core.common.BirdGender
import com.example.core.common.BudgieVariety
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.PedigreeRecordEntity

enum class PedigreeNodeRole(val labelEn: String, val labelFa: String) {
    SELF("Subject", "سوژه اصلی"),
    SIRE("Sire (Father)", "پدر (Sire)"),
    DAM("Dam (Mother)", "مادر (Dam)"),
    PATERNAL_GRANDSIRE("Paternal Grandsire", "پدربزرگ پدری"),
    PATERNAL_GRANDDAM("Paternal Granddam", "مادربزرگ پدری"),
    MATERNAL_GRANDSIRE("Maternal Grandsire", "پدربزرگ مادری"),
    MATERNAL_GRANDDAM("Maternal Granddam", "مادربزرگ مادری"),
    GREAT_GRANDPARENT("Great-Grandparent", "جد (نسل سوم)"),
    CHILD("Direct Offspring (Child)", "فرزند مستقیم"),
    GRANDCHILD("Grandchild (2nd Gen)", "نوه (نسل دوم)")
}

data class PedigreeTreeNode(
    val ringNumber: String,
    val name: String?,
    val role: PedigreeNodeRole,
    val gender: BirdGender,
    val variety: BudgieVariety?,
    val mutation: String,
    val color: String,
    val generation: String?,
    val photoUri: String?,
    val isPresentInAviary: Boolean,
    val inbreedingCoeff: Double? = null,
    val sireRing: String? = null,
    val damRing: String? = null
)

data class InteractivePedigreeTreeData(
    val subjectBird: BirdEntity,
    val subjectGenetics: BirdGeneticsEntity?,
    val sireNode: PedigreeTreeNode?,
    val damNode: PedigreeTreeNode?,
    // Grandparents: Paternal Grandsire, Paternal Granddam, Maternal Grandsire, Maternal Granddam
    val paternalGrandsireNode: PedigreeTreeNode?,
    val paternalGranddamNode: PedigreeTreeNode?,
    val maternalGrandsireNode: PedigreeTreeNode?,
    val maternalGranddamNode: PedigreeTreeNode?,
    // Great-Grandparents (8 nodes)
    val greatGrandparents: List<PedigreeTreeNode>,
    // Descendants: Children and Grandchildren
    val directChildren: List<PedigreeTreeNode>,
    val grandchildren: List<PedigreeTreeNode>,
    // Metrics
    val inbreedingCoefficientF: Double,
    val ancestryCompletenessPercent: Int, // Percentage of known ancestors in 3 generations (out of 14 ancestors)
    val pedigreeRecord: PedigreeRecordEntity?
)
