package com.example.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.common.BirdStatus
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.BirdGeneticsEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.CompetitionEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.ExpenseEntity
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.IncomeEntity
import com.example.data.database.entity.InventoryItemEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.PedigreeRecordEntity
import com.example.data.database.entity.WeightRecordEntity
import com.example.data.database.relation.PairWithBreedingDetails
import com.example.data.repository.AviaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ReportCategory(val titleEn: String, val titleFa: String) {
    BIRDS("Birds", "پرندگان"),
    PAIRS("Breeding Pairs", "جفت‌های مولد"),
    REPRODUCTION("Reproduction", "تکثیر و تخم‌گذاری"),
    EGGS("Eggs", "تخم‌ها"),
    CHICKS("Chicks", "جوجه‌ها"),
    GENETICS("Genetics", "ژنتیک و موتاسیون‌ها"),
    PEDIGREE("Pedigree Records", "شجره‌نامه‌ها"),
    HEALTH("Health & Medical", "سلامت و بیماری‌ها"),
    WEIGHTS("Weight Tracking", "رشد و وزن‌کشی"),
    MORTALITY("Mortality", "تلفات و مرگ‌ومیر"),
    INVENTORY("Inventory & Stock", "انبار و تجهیزات"),
    FINANCE("Financial Accounts", "امور مالی و حسابداری"),
    COMPETITIONS("Competitions & Shows", "مسابقات و افتخارات"),
    FACILITY_PERFORMANCE("Facility Performance", "بهره‌وری و عملکرد سالن")
}

data class ReportFilterState(
    val category: ReportCategory = ReportCategory.BIRDS,
    val searchQuery: String = "",
    val birdRing: String = "",
    val pairId: String = "",
    val cageCode: String = "",
    val status: String = "ALL",
    val dateRangeDays: Int = 0 // 0 = all time, 7 = last week, 30 = last month, 90 = 3 months, 365 = 1 year
)

data class FacilityPerformanceMetrics(
    val totalBirds: Int = 0,
    val activePairs: Int = 0,
    val totalEggs: Int = 0,
    val fertileEggs: Int = 0,
    val hatchedEggs: Int = 0,
    val fertilityRate: Double = 0.0,
    val hatchRate: Double = 0.0,
    val totalChicks: Int = 0,
    val weanedChicks: Int = 0,
    val chickMortalityCount: Int = 0,
    val chickSurvivalRate: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalIncome: Double = 0.0,
    val netProfit: Double = 0.0,
    val cagesOccupancyRate: Double = 0.0
)

class ReportsViewModel(
    private val repository: AviaryRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(ReportFilterState())
    val filterState: StateFlow<ReportFilterState> = _filterState.asStateFlow()

    // Raw Repository Streams
    val birds: StateFlow<List<BirdEntity>> = repository.allBirds.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pairs: StateFlow<List<PairEntity>> = repository.allPairs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pairsWithDetails: StateFlow<List<PairWithBreedingDetails>> = repository.allPairsWithBreedingDetails.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cages: StateFlow<List<CageEntity>> = repository.allCages.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val eggs: StateFlow<List<EggEntity>> = repository.allEggs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val chicks: StateFlow<List<ChickEntity>> = repository.allChicks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val genetics: StateFlow<List<BirdGeneticsEntity>> = repository.allGenetics.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pedigrees: StateFlow<List<PedigreeRecordEntity>> = repository.allPedigrees.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val healthRecords: StateFlow<List<HealthRecordEntity>> = repository.allHealthRecords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val weightRecords: StateFlow<List<WeightRecordEntity>> = repository.allWeightRecords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val inventoryItems: StateFlow<List<InventoryItemEntity>> = repository.allInventoryItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val incomes: StateFlow<List<IncomeEntity>> = repository.allIncome.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val competitions: StateFlow<List<CompetitionEntity>> = repository.allCompetitions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val facilityPerformance: StateFlow<FacilityPerformanceMetrics> = combine(
        combine(birds, pairs, eggs, chicks) { b, p, e, c ->
            val totalE = e.size
            val fertileE = e.count { it.fertilityStatus.uppercase() in listOf("FERTILE", "HATCHED") || it.isHatched }
            val hatchedE = e.count { it.isHatched || it.actualHatchDate != null }
            val fertRate = if (totalE > 0) (fertileE.toDouble() / totalE) * 100 else 0.0
            val hRate = if (fertileE > 0) (hatchedE.toDouble() / fertileE) * 100 else (if (totalE > 0) (hatchedE.toDouble() / totalE) * 100 else 0.0)

            val totalC = c.size
            val weanedC = c.count { it.growthStage.uppercase() in listOf("WEANED", "BANDED") || it.status.uppercase() == "TRANSFERRED" }
            val deadC = c.count { it.mortalityDate != null || it.mortalityReason != null }
            val survivalRate = if (totalC > 0) ((totalC - deadC).toDouble() / totalC) * 100 else 100.0

            Quad(b, p, Pair(totalE, fertileE), Triple(hatchedE, fertRate, hRate), Triple(totalC, weanedC, deadC), survivalRate)
        },
        combine(expenses, incomes, cages) { exp, inc, cg ->
            val totalExp = exp.sumOf { it.amount }
            val totalInc = inc.sumOf { it.amount }
            val net = totalInc - totalExp
            val totalCageCapacity = cg.sumOf { it.capacity }
            Triple(totalExp, totalInc, Pair(net, totalCageCapacity))
        }
    ) { bData, finData ->
        val birdList = bData.b
        val pairList = bData.p
        val totalE = bData.eggs.first
        val fertileE = bData.eggs.second
        val hatchedE = bData.hatchTriple.first
        val fertRate = bData.hatchTriple.second
        val hRate = bData.hatchTriple.third
        val totalC = bData.chickTriple.first
        val weanedC = bData.chickTriple.second
        val deadC = bData.chickTriple.third
        val survivalRate = bData.survivalRate

        val totalExp = finData.first
        val totalInc = finData.second
        val net = finData.third.first
        val totalCageCapacity = finData.third.second
        val occupiedBirds = birdList.count { it.status == BirdStatus.ACTIVE && !it.cageCode.isNullOrBlank() }
        val occRate = if (totalCageCapacity > 0) (occupiedBirds.toDouble() / totalCageCapacity) * 100 else 0.0

        FacilityPerformanceMetrics(
            totalBirds = birdList.size,
            activePairs = pairList.count { it.isActive },
            totalEggs = totalE,
            fertileEggs = fertileE,
            hatchedEggs = hatchedE,
            fertilityRate = fertRate,
            hatchRate = hRate,
            totalChicks = totalC,
            weanedChicks = weanedC,
            chickMortalityCount = deadC,
            chickSurvivalRate = survivalRate,
            totalExpenses = totalExp,
            totalIncome = totalInc,
            netProfit = net,
            cagesOccupancyRate = occRate
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FacilityPerformanceMetrics())

    fun setCategory(category: ReportCategory) {
        _filterState.value = _filterState.value.copy(category = category)
    }

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun updateFilters(
        birdRing: String = _filterState.value.birdRing,
        pairId: String = _filterState.value.pairId,
        cageCode: String = _filterState.value.cageCode,
        status: String = _filterState.value.status,
        dateRangeDays: Int = _filterState.value.dateRangeDays
    ) {
        _filterState.value = _filterState.value.copy(
            birdRing = birdRing,
            pairId = pairId,
            cageCode = cageCode,
            status = status,
            dateRangeDays = dateRangeDays
        )
    }

    fun clearFilters() {
        _filterState.value = _filterState.value.copy(
            searchQuery = "",
            birdRing = "",
            pairId = "",
            cageCode = "",
            status = "ALL",
            dateRangeDays = 0
        )
    }

    fun computeTableData(): Pair<List<String>, List<List<String>>> {
        val filter = _filterState.value
        val now = System.currentTimeMillis()
        val minTime = if (filter.dateRangeDays > 0) now - (filter.dateRangeDays.toLong() * 86400000L) else 0L

        return when (filter.category) {
            ReportCategory.BIRDS -> {
                val headers = listOf("Ring #", "Name", "Gender", "Variety", "Color", "Cage", "Status", "Birth Date", "Father", "Mother")
                val list = birds.value.filter { b ->
                    (filter.searchQuery.isBlank() || b.ringNumber.contains(filter.searchQuery, true) || (b.name?.contains(filter.searchQuery, true) == true) || b.color.contains(filter.searchQuery, true)) &&
                    (filter.cageCode.isBlank() || b.cageCode.equals(filter.cageCode, true)) &&
                    (filter.status == "ALL" || b.status.name.equals(filter.status, true)) &&
                    (minTime == 0L || (b.birthDate ?: 0L) >= minTime)
                }
                val rows = list.map { b ->
                    listOf(
                        b.ringNumber,
                        b.name ?: "—",
                        b.gender.name,
                        b.variety.name.replace("_", " "),
                        b.color,
                        b.cageCode ?: "—",
                        b.status.name,
                        b.birthDate?.let { formatDate(it) } ?: "—",
                        b.fatherRing ?: "—",
                        b.motherRing ?: "—"
                    )
                }
                Pair(headers, rows)
            }
            ReportCategory.PAIRS -> {
                val headers = listOf("Pair ID", "Male Ring", "Female Ring", "Cage", "Status", "Pair Date", "Total Clutches", "Total Eggs", "Total Chicks")
                val list = pairsWithDetails.value.filter { p ->
                    val pair = p.pair
                    (filter.searchQuery.isBlank() || pair.id.toString().contains(filter.searchQuery) || pair.maleRingNumber.contains(filter.searchQuery, true) || pair.femaleRingNumber.contains(filter.searchQuery, true)) &&
                    (filter.cageCode.isBlank() || pair.cageCode.equals(filter.cageCode, true)) &&
                    (filter.birdRing.isBlank() || pair.maleRingNumber.equals(filter.birdRing, true) || pair.femaleRingNumber.equals(filter.birdRing, true)) &&
                    (filter.status == "ALL" || (if (pair.isActive) "ACTIVE" else "INACTIVE") == filter.status) &&
                    (minTime == 0L || pair.pairingDate >= minTime)
                }
                val rows = list.map { p ->
                    listOf(
                        p.pair.id.toString(),
                        p.pair.maleRingNumber,
                        p.pair.femaleRingNumber,
                        p.pair.cageCode ?: "—",
                        if (p.pair.isActive) "ACTIVE" else "INACTIVE",
                        formatDate(p.pair.pairingDate),
                        p.clutches.size.toString(),
                        p.eggs.size.toString(),
                        p.chicks.size.toString()
                    )
                }
                Pair(headers, rows)
            }
            ReportCategory.REPRODUCTION -> {
                val headers = listOf("Pair ID", "Cage", "Clutches", "Eggs Laid", "Fertile Eggs", "Chicks Hatched", "Fertility %", "Hatch %")
                val list = pairsWithDetails.value.filter { p ->
                    (filter.pairId.isBlank() || p.pair.id.toString() == filter.pairId) &&
                    (filter.cageCode.isBlank() || p.pair.cageCode.equals(filter.cageCode, true))
                }
                val rows = list.map { p ->
                    val totalEggs = p.eggs.size
                    val fertile = p.eggs.count { it.fertilityStatus.uppercase() in listOf("FERTILE", "HATCHED") || it.isHatched }
                    val hatched = p.chicks.size
                    val fPct = if (totalEggs > 0) String.format(Locale.US, "%.1f%%", (fertile.toDouble() / totalEggs) * 100) else "0%"
                    val hPct = if (fertile > 0) String.format(Locale.US, "%.1f%%", (hatched.toDouble() / fertile) * 100) else "0%"
                    listOf(
                        p.pair.id.toString(),
                        p.pair.cageCode ?: "—",
                        p.clutches.size.toString(),
                        totalEggs.toString(),
                        fertile.toString(),
                        hatched.toString(),
                        fPct,
                        hPct
                    )
                }
                Pair(headers, rows)
            }
            ReportCategory.EGGS -> {
                val headers = listOf("Egg ID", "Pair ID", "Egg #", "Lay Date", "Status", "Expected Hatch", "Fertility", "Notes")
                val list = eggs.value.filter { e ->
                    (filter.searchQuery.isBlank() || e.id.contains(filter.searchQuery, true)) &&
                    (filter.pairId.isBlank() || e.pairId.toString() == filter.pairId) &&
                    (filter.status == "ALL" || e.fertilityStatus.equals(filter.status, true)) &&
                    (minTime == 0L || e.layDate >= minTime)
                }
                val rows = list.map { e ->
                    listOf(
                        e.id,
                        e.pairId.toString(),
                        e.eggNumber.toString(),
                        formatDate(e.layDate),
                        if (e.isHatched) "HATCHED" else e.fertilityStatus,
                        e.expectedHatchDate?.let { formatDate(it) } ?: "—",
                        e.fertilityStatus,
                        e.notes ?: "—"
                    )
                }
                Pair(headers, rows)
            }
            ReportCategory.CHICKS -> {
                val headers = listOf("Chick ID", "Pair ID", "Hatch Order", "Hatch Date", "Banded Ring", "Stage", "Weight (g)", "Cage")
                val list = chicks.value.filter { c ->
                    (filter.searchQuery.isBlank() || c.id.contains(filter.searchQuery, true) || (c.bandedRingNumber?.contains(filter.searchQuery, true) == true)) &&
                    (filter.pairId.isBlank() || c.pairId.toString() == filter.pairId) &&
                    (filter.cageCode.isBlank() || c.cageCode.equals(filter.cageCode, true)) &&
                    (filter.status == "ALL" || c.status.equals(filter.status, true)) &&
                    (minTime == 0L || (c.hatchDate ?: 0L) >= minTime)
                }
                val rows = list.map { c ->
                    listOf(
                        c.id,
                        c.pairId.toString(),
                        c.hatchOrder.toString(),
                        c.hatchDate?.let { formatDate(it) } ?: "—",
                        c.bandedRingNumber ?: "—",
                        c.growthStage,
                        c.weightGrams?.toString() ?: "—",
                        c.cageCode ?: "—"
                    )
                }
                Pair(headers, rows)
            }
            ReportCategory.GENETICS -> {
                val headers = listOf("Bird Ring", "Base Series", "Visual Mutations", "Split Mutations", "Violet", "Grey", "Cinnamon", "Ino")
                val list = genetics.value.filter { g ->
                    (filter.searchQuery.isBlank() || g.birdRingNumber.contains(filter.searchQuery, true) || g.visualMutations.contains(filter.searchQuery, true)) &&
                    (filter.birdRing.isBlank() || g.birdRingNumber.equals(filter.birdRing, true))
                }
                val rows = list.map { g ->
                    listOf(
                        g.birdRingNumber,
                        g.baseSeries,
                        g.visualMutations,
                        g.splitMutations ?: "—",
                        if (g.violetFactor) "YES" else "NO",
                        if (g.greyFactor) "YES" else "NO",
                        if (g.cinnamonFactor) "YES" else "NO",
                        if (g.inoFactor) "YES" else "NO"
                    )
                }
                Pair(headers, rows)
            }
            ReportCategory.PEDIGREE -> {
                val headers = listOf("Bird Ring", "Sire Ring", "Dam Ring", "Breeder", "COI %", "Pat. Grandsire", "Mat. Grandsire")
                val list = pedigrees.value.filter { p ->
                    (filter.searchQuery.isBlank() || p.birdRingNumber.contains(filter.searchQuery, true) || (p.sireRing?.contains(filter.searchQuery, true) == true)) &&
                    (filter.birdRing.isBlank() || p.birdRingNumber.equals(filter.birdRing, true))
                }
                val rows = list.map { p ->
                    listOf(
                        p.birdRingNumber,
                        p.sireRing ?: "—",
                        p.damRing ?: "—",
                        p.breederName ?: "—",
                        String.format(Locale.US, "%.2f%%", (p.inbreedingCoefficient ?: 0.0) * 100),
                        p.paternalGrandsire ?: "—",
                        p.maternalGrandsire ?: "—"
                    )
                }
                Pair(headers, rows)
            }
            ReportCategory.HEALTH -> {
                val headers = listOf("Date", "Bird Ring", "Problem / Diagnosis", "Type", "Symptoms", "Medication", "Vet", "Status")
                val list = healthRecords.value.filter { h ->
                    (filter.searchQuery.isBlank() || (h.birdRingNumber?.contains(filter.searchQuery, true) == true) || h.diagnosis.contains(filter.searchQuery, true) || h.recordedProblem.contains(filter.searchQuery, true)) &&
                    (filter.birdRing.isBlank() || h.birdRingNumber.equals(filter.birdRing, true)) &&
                    (minTime == 0L || h.recordDate >= minTime)
                }
                val rows = list.map { h ->
                    listOf(
                        formatDate(h.recordDate),
                        h.birdRingNumber ?: "—",
                        h.recordedProblem,
                        h.recordType,
                        h.symptoms,
                        h.medicationName ?: "—",
                        h.veterinarianName ?: "—",
                        if (h.isResolved) "RESOLVED" else "ACTIVE"
                    )
                }
                Pair(headers, rows)
            }
            ReportCategory.WEIGHTS -> {
                val headers = listOf("Date", "Bird Ring", "Weight (g)", "Condition Score", "Recorded By", "Notes")
                val list = weightRecords.value.filter { w ->
                    (filter.searchQuery.isBlank() || w.birdRingNumber.contains(filter.searchQuery, true)) &&
                    (filter.birdRing.isBlank() || w.birdRingNumber.equals(filter.birdRing, true)) &&
                    (minTime == 0L || w.recordedDate >= minTime)
                }
                val rows = list.map { w ->
                    listOf(
                        formatDate(w.recordedDate),
                        w.birdRingNumber,
                        "${w.weightGrams}g",
                        w.conditionScore,
                        "Aviary Log",
                        w.notes ?: "—"
                    )
                }
                Pair(headers, rows)
            }
            ReportCategory.MORTALITY -> {
                val headers = listOf("Entity Type", "Identifier", "Date", "Cause / Reason", "Stage / Category", "Notes")
                val deadChicks = chicks.value.filter { it.mortalityDate != null || it.mortalityReason != null }.map { c ->
                    listOf(
                        "CHICK",
                        c.bandedRingNumber ?: c.id,
                        c.mortalityDate?.let { formatDate(it) } ?: "—",
                        c.mortalityReason ?: "Unknown",
                        c.growthStage,
                        c.notes ?: "—"
                    )
                }
                val deadBirds = birds.value.filter { it.status == BirdStatus.DECEASED }.map { b ->
                    listOf(
                        "BIRD",
                        b.ringNumber,
                        b.birthDate?.let { formatDate(it) } ?: "—",
                        "Natural / Aviary Log",
                        b.variety.name,
                        b.notes ?: "—"
                    )
                }
                val rows = (deadChicks + deadBirds).filter { row ->
                    filter.searchQuery.isBlank() || row[1].contains(filter.searchQuery, true) || row[3].contains(filter.searchQuery, true)
                }
                Pair(headers, rows)
            }
            ReportCategory.INVENTORY -> {
                val headers = listOf("Item Name", "Category", "Current Stock", "Unit", "Min Threshold", "Cost/Unit ($)", "Status")
                val list = inventoryItems.value.filter { i ->
                    filter.searchQuery.isBlank() || i.name.contains(filter.searchQuery, true) || i.category.contains(filter.searchQuery, true)
                }
                val rows = list.map { i ->
                    val statusStr = if (i.currentStock <= i.minStockThreshold) "LOW STOCK" else "OPTIMAL"
                    listOf(
                        i.name,
                        i.category,
                        i.currentStock.toString(),
                        i.unit,
                        i.minStockThreshold.toString(),
                        "$${i.costPerUnit}",
                        statusStr
                    )
                }
                Pair(headers, rows)
            }
            ReportCategory.FINANCE -> {
                val headers = listOf("Type", "Category", "Amount ($)", "Date", "Reference", "Notes")
                val expRows = expenses.value.filter { minTime == 0L || it.date >= minTime }.map { e ->
                    listOf(
                        "EXPENSE",
                        e.category,
                        "-$${e.amount}",
                        formatDate(e.date),
                        e.pairId ?: e.birdRingNumber ?: "—",
                        e.notes ?: "—"
                    )
                }
                val incRows = incomes.value.filter { minTime == 0L || it.date >= minTime }.map { i ->
                    listOf(
                        "INCOME",
                        i.category,
                        "+$${i.amount}",
                        formatDate(i.date),
                        i.soldBirdRingNumber ?: i.buyerName ?: "—",
                        i.notes ?: "—"
                    )
                }
                val allRows = (expRows + incRows).filter { r ->
                    filter.searchQuery.isBlank() || r[1].contains(filter.searchQuery, true) || r[4].contains(filter.searchQuery, true)
                }
                Pair(headers, allRows)
            }
            ReportCategory.COMPETITIONS -> {
                val headers = listOf("Competition Name", "Date", "Location", "Standard", "Status", "Club")
                val list = competitions.value.filter { c ->
                    (filter.searchQuery.isBlank() || c.title.contains(filter.searchQuery, true) || c.location.contains(filter.searchQuery, true)) &&
                    (minTime == 0L || c.eventDate >= minTime)
                }
                val rows = list.map { c ->
                    listOf(
                        c.title,
                        formatDate(c.eventDate),
                        c.location,
                        c.showStandard,
                        c.status,
                        c.organizingClub
                    )
                }
                Pair(headers, rows)
            }
            ReportCategory.FACILITY_PERFORMANCE -> {
                val headers = listOf("Operational KPI Metric", "Calculated Value", "Benchmark Target", "Status")
                val perf = facilityPerformance.value
                val rows = listOf(
                    listOf("Total Registered Flock", "${perf.totalBirds} birds", "> 20 birds", "OPTIMAL"),
                    listOf("Active Breeding Pairs", "${perf.activePairs} pairs", "> 5 pairs", "OPTIMAL"),
                    listOf("Egg Fertility Rate", String.format(Locale.US, "%.1f%%", perf.fertilityRate), "> 80.0%", if (perf.fertilityRate >= 80) "OPTIMAL" else "ATTENTION"),
                    listOf("Hatch Success Rate", String.format(Locale.US, "%.1f%%", perf.hatchRate), "> 75.0%", if (perf.hatchRate >= 75) "OPTIMAL" else "ATTENTION"),
                    listOf("Chick Survival Rate", String.format(Locale.US, "%.1f%%", perf.chickSurvivalRate), "> 90.0%", if (perf.chickSurvivalRate >= 90) "OPTIMAL" else "ATTENTION"),
                    listOf("Cage Occupancy Ratio", String.format(Locale.US, "%.1f%%", perf.cagesOccupancyRate), "< 85.0%", if (perf.cagesOccupancyRate <= 85) "OPTIMAL" else "HIGH DENSITY"),
                    listOf("Total Incurred Expenses", "$${perf.totalExpenses}", "Budgeted", "LOGGED"),
                    listOf("Total Generated Revenue", "$${perf.totalIncome}", "Targeted", "LOGGED"),
                    listOf("Net Aviary Profit / Balance", "$${perf.netProfit}", "> $0", if (perf.netProfit >= 0) "POSITIVE" else "DEFICIT")
                )
                Pair(headers, rows)
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(timestamp))
    }

    private data class Quad(
        val b: List<BirdEntity>,
        val p: List<PairEntity>,
        val eggs: Pair<Int, Int>,
        val hatchTriple: Triple<Int, Double, Double>,
        val chickTriple: Triple<Int, Int, Int>,
        val survivalRate: Double
    )

    class Factory(private val repository: AviaryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportsViewModel(repository) as T
        }
    }
}
