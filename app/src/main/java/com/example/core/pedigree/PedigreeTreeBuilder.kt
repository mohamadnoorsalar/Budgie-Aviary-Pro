package com.example.core.pedigree

import com.example.core.common.BirdGender
import com.example.core.genetics.GeneticsCalculator
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.PedigreeRecordEntity

object PedigreeTreeBuilder {

    fun buildPedigreeTree(
        subjectRing: String,
        allBirds: List<BirdEntity>,
        allPedigrees: List<PedigreeRecordEntity>,
        allGenetics: List<BirdGeneticsEntity>
    ): InteractivePedigreeTreeData? {
        val birdMap = allBirds.associateBy { it.ringNumber }
        val pedigreeMap = allPedigrees.associateBy { it.birdRingNumber }
        val geneticsMap = allGenetics.associateBy { it.birdRingNumber }

        val subject = birdMap[subjectRing] ?: return null
        val subjectPed = pedigreeMap[subjectRing]
        val subjectGenetics = geneticsMap[subjectRing]

        // Helper to construct a node
        fun makeNode(ring: String?, role: PedigreeNodeRole, fallbackGender: BirdGender = BirdGender.UNKNOWN): PedigreeTreeNode? {
            if (ring.isNullOrBlank() || ring == "-") return null
            val existingBird = birdMap[ring]
            val ped = pedigreeMap[ring]

            return PedigreeTreeNode(
                ringNumber = ring,
                name = existingBird?.name,
                role = role,
                gender = existingBird?.gender ?: fallbackGender,
                variety = existingBird?.variety,
                mutation = existingBird?.mutation ?: "Not Recorded",
                color = existingBird?.color ?: "Unknown",
                generation = existingBird?.generation,
                photoUri = existingBird?.photoUri,
                isPresentInAviary = existingBird != null,
                sireRing = ped?.sireRing ?: existingBird?.fatherRing,
                damRing = ped?.damRing ?: existingBird?.motherRing
            )
        }

        // 1. Parents
        val sireRing = subjectPed?.sireRing ?: subject.fatherRing
        val damRing = subjectPed?.damRing ?: subject.motherRing

        val sireNode = makeNode(sireRing, PedigreeNodeRole.SIRE, BirdGender.MALE)
        val damNode = makeNode(damRing, PedigreeNodeRole.DAM, BirdGender.FEMALE)

        // 2. Grandparents
        val patGrandsireRing = subjectPed?.paternalGrandsire ?: sireNode?.sireRing
        val patGranddamRing = subjectPed?.paternalGranddam ?: sireNode?.damRing
        val matGrandsireRing = subjectPed?.maternalGrandsire ?: damNode?.sireRing
        val matGranddamRing = subjectPed?.maternalGranddam ?: damNode?.damRing

        val patGrandsire = makeNode(patGrandsireRing, PedigreeNodeRole.PATERNAL_GRANDSIRE, BirdGender.MALE)
        val patGranddam = makeNode(patGranddamRing, PedigreeNodeRole.PATERNAL_GRANDDAM, BirdGender.FEMALE)
        val matGrandsire = makeNode(matGrandsireRing, PedigreeNodeRole.MATERNAL_GRANDSIRE, BirdGender.MALE)
        val matGranddam = makeNode(matGranddamRing, PedigreeNodeRole.MATERNAL_GRANDDAM, BirdGender.FEMALE)

        // 3. Great-Grandparents (8 ancestors)
        val ggList = mutableListOf<PedigreeTreeNode>()
        listOf(
            patGrandsire?.sireRing to BirdGender.MALE,
            patGrandsire?.damRing to BirdGender.FEMALE,
            patGranddam?.sireRing to BirdGender.MALE,
            patGranddam?.damRing to BirdGender.FEMALE,
            matGrandsire?.sireRing to BirdGender.MALE,
            matGrandsire?.damRing to BirdGender.FEMALE,
            matGranddam?.sireRing to BirdGender.MALE,
            matGranddam?.damRing to BirdGender.FEMALE
        ).forEach { (ggRing, gender) ->
            makeNode(ggRing, PedigreeNodeRole.GREAT_GRANDPARENT, gender)?.let { ggList.add(it) }
        }

        // 4. Descendants: Direct Children
        val children = allBirds.filter { it.fatherRing == subjectRing || it.motherRing == subjectRing }
            .map { child ->
                PedigreeTreeNode(
                    ringNumber = child.ringNumber,
                    name = child.name,
                    role = PedigreeNodeRole.CHILD,
                    gender = child.gender,
                    variety = child.variety,
                    mutation = child.mutation,
                    color = child.color,
                    generation = child.generation,
                    photoUri = child.photoUri,
                    isPresentInAviary = true,
                    sireRing = child.fatherRing,
                    damRing = child.motherRing
                )
            }

        // 5. Descendants: Grandchildren
        val childRings = children.map { it.ringNumber }.toSet()
        val grandchildren = allBirds.filter {
            (it.fatherRing != null && it.fatherRing in childRings) ||
                    (it.motherRing != null && it.motherRing in childRings)
        }.map { gChild ->
            PedigreeTreeNode(
                ringNumber = gChild.ringNumber,
                name = gChild.name,
                role = PedigreeNodeRole.GRANDCHILD,
                gender = gChild.gender,
                variety = gChild.variety,
                mutation = gChild.mutation,
                color = gChild.color,
                generation = gChild.generation,
                photoUri = gChild.photoUri,
                isPresentInAviary = true,
                sireRing = gChild.fatherRing,
                damRing = gChild.motherRing
            )
        }

        // 6. Calculate Ancestry Completeness %
        // Out of 14 ancestors in 3 generations (2 parents + 4 grandparents + 8 great-grandparents)
        var knownCount = 0
        if (sireNode != null) knownCount++
        if (damNode != null) knownCount++
        if (patGrandsire != null) knownCount++
        if (patGranddam != null) knownCount++
        if (matGrandsire != null) knownCount++
        if (matGranddam != null) knownCount++
        knownCount += ggList.size

        val completenessPercent = ((knownCount.toDouble() / 14.0) * 100.0).toInt().coerceIn(0, 100)

        // 7. Calculate Inbreeding Coefficient for this subject from its Sire & Dam
        val inbreedingF = if (!sireRing.isNullOrBlank() && !damRing.isNullOrBlank()) {
            val rep = GeneticsCalculator.calculateRelatedness(sireRing, damRing, allBirds, allPedigrees)
            rep.inbreedingCoefficientF
        } else {
            subjectPed?.inbreedingCoefficient ?: 0.0
        }

        return InteractivePedigreeTreeData(
            subjectBird = subject,
            subjectGenetics = subjectGenetics,
            sireNode = sireNode,
            damNode = damNode,
            paternalGrandsireNode = patGrandsire,
            paternalGranddamNode = patGranddam,
            maternalGrandsireNode = matGrandsire,
            maternalGranddamNode = matGranddam,
            greatGrandparents = ggList,
            directChildren = children,
            grandchildren = grandchildren,
            inbreedingCoefficientF = inbreedingF,
            ancestryCompletenessPercent = completenessPercent,
            pedigreeRecord = subjectPed
        )
    }
}
