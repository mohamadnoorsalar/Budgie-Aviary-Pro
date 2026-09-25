package com.example.core.backup

import com.example.core.common.BirdGender
import com.example.core.common.BirdStatus
import com.example.core.common.BudgieVariety
import com.example.core.common.CageType
import com.example.core.common.ReminderPriority
import com.example.core.common.ReminderType
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ClutchEntity
import com.example.data.database.entity.ExpenseEntity
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.IncomeEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.ReminderEntity
import org.json.JSONArray
import org.json.JSONObject

object BackupSerializer {

    fun toJson(backup: AviaryFullBackup): String {
        val root = JSONObject()

        val meta = JSONObject().apply {
            put("formatVersion", backup.metadata.formatVersion)
            put("appVersion", backup.metadata.appVersion)
            put("backupTimestamp", backup.metadata.backupTimestamp)
            put("deviceId", backup.metadata.deviceId)
            put("deviceName", backup.metadata.deviceName)
            put("aviaryName", backup.metadata.aviaryName)
            put("latestRecordTimestamp", backup.metadata.latestRecordTimestamp)
            put("totalBirds", backup.metadata.totalBirds)
            put("totalPairs", backup.metadata.totalPairs)
            put("totalClutches", backup.metadata.totalClutches)
            put("totalHealthRecords", backup.metadata.totalHealthRecords)
            put("totalFinancialRecords", backup.metadata.totalFinancialRecords)
        }
        root.put("metadata", meta)

        // Birds
        val birdsArr = JSONArray()
        backup.birds.forEach { b ->
            val obj = JSONObject().apply {
                put("ringNumber", b.ringNumber)
                put("name", b.name ?: "")
                put("gender", b.gender.name)
                put("variety", b.variety.name)
                put("mutation", b.mutation)
                put("color", b.color)
                put("birthDate", b.birthDate ?: 0L)
                put("placeOfBirth", b.placeOfBirth ?: "")
                put("generation", b.generation ?: "")
                put("photoUri", b.photoUri ?: "")
                put("cageCode", b.cageCode ?: "")
                put("pairId", b.pairId ?: 0L)
                put("status", b.status.name)
                put("fatherRing", b.fatherRing ?: "")
                put("motherRing", b.motherRing ?: "")
                put("notes", b.notes ?: "")
                put("createdAt", b.createdAt)
                put("updatedAt", b.updatedAt)
            }
            birdsArr.put(obj)
        }
        root.put("birds", birdsArr)

        // Pairs
        val pairsArr = JSONArray()
        backup.pairs.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("maleRingNumber", p.maleRingNumber)
                put("femaleRingNumber", p.femaleRingNumber)
                put("cageCode", p.cageCode ?: "")
                put("nestId", p.nestId ?: "")
                put("pairingDate", p.pairingDate)
                put("matingDate", p.matingDate ?: 0L)
                put("endDate", p.endDate ?: 0L)
                put("status", p.status)
                put("isActive", p.isActive)
                put("clutchCount", p.clutchCount)
                put("totalChicks", p.totalChicks)
                put("notes", p.notes ?: "")
                put("updatedAt", p.updatedAt)
            }
            pairsArr.put(obj)
        }
        root.put("pairs", pairsArr)

        // Cages
        val cagesArr = JSONArray()
        backup.cages.forEach { c ->
            val obj = JSONObject().apply {
                put("code", c.code)
                put("type", c.type.name)
                put("capacity", c.capacity)
                put("currentOccupancy", c.currentOccupancy)
                put("location", c.location ?: "")
                put("isClean", c.isClean)
                put("lastCleanedDate", c.lastCleanedDate ?: 0L)
                put("photoUri", c.photoUri ?: "")
                put("notes", c.notes ?: "")
                put("updatedAt", c.updatedAt)
            }
            cagesArr.put(obj)
        }
        root.put("cages", cagesArr)

        // Clutches
        val clutchesArr = JSONArray()
        backup.clutches.forEach { cl ->
            val obj = JSONObject().apply {
                put("id", cl.id)
                put("pairId", cl.pairId)
                put("clutchNumber", cl.clutchNumber)
                put("matingDate", cl.matingDate ?: 0L)
                put("startDate", cl.startDate)
                put("endDate", cl.endDate ?: 0L)
                put("eggCount", cl.eggCount)
                put("fertileCount", cl.fertileCount)
                put("hatchedCount", cl.hatchedCount)
                put("isActive", cl.isActive)
                put("notes", cl.notes ?: "")
            }
            clutchesArr.put(obj)
        }
        root.put("clutches", clutchesArr)

        // Expenses
        val expensesArr = JSONArray()
        backup.expenses.forEach { exp ->
            val obj = JSONObject().apply {
                put("id", exp.id)
                put("title", exp.title)
                put("category", exp.category)
                put("amount", exp.amount)
                put("date", exp.date)
                put("inventoryItemId", exp.inventoryItemId ?: "")
                put("pairId", exp.pairId ?: "")
                put("birdRingNumber", exp.birdRingNumber ?: "")
                put("receiptNumber", exp.receiptNumber ?: "")
                put("notes", exp.notes ?: "")
                put("updatedAt", exp.updatedAt)
            }
            expensesArr.put(obj)
        }
        root.put("expenses", expensesArr)

        // Income
        val incomeArr = JSONArray()
        backup.incomes.forEach { inc ->
            val obj = JSONObject().apply {
                put("id", inc.id)
                put("title", inc.title)
                put("category", inc.category)
                put("amount", inc.amount)
                put("date", inc.date)
                put("soldBirdRingNumber", inc.soldBirdRingNumber ?: "")
                put("soldChickId", inc.soldChickId ?: "")
                put("buyerName", inc.buyerName ?: "")
                put("buyerContact", inc.buyerContact ?: "")
                put("notes", inc.notes ?: "")
                put("updatedAt", inc.updatedAt)
            }
            incomeArr.put(obj)
        }
        root.put("incomes", incomeArr)

        // Health Records
        val healthArr = JSONArray()
        backup.healthRecords.forEach { h ->
            val obj = JSONObject().apply {
                put("id", h.id)
                put("birdRingNumber", h.birdRingNumber ?: "")
                put("cageCode", h.cageCode ?: "")
                put("recordDate", h.recordDate)
                put("recordType", h.recordType)
                put("symptoms", h.symptoms)
                put("diagnosis", h.diagnosis)
                put("recordedProblem", h.recordedProblem)
                put("veterinarianName", h.veterinarianName ?: "")
                put("medicationName", h.medicationName ?: "")
                put("dosage", h.dosage ?: "")
                put("frequency", h.frequency ?: "")
                put("treatmentDurationDays", h.treatmentDurationDays)
                put("startDate", h.startDate)
                put("endDate", h.endDate ?: 0L)
                put("supplements", h.supplements ?: "")
                put("isResolved", h.isResolved)
                put("notes", h.notes ?: "")
                put("updatedAt", h.updatedAt)
            }
            healthArr.put(obj)
        }
        root.put("healthRecords", healthArr)

        // Reminders
        val remArr = JSONArray()
        backup.reminders.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("title", r.title)
                put("description", r.description ?: "")
                put("dueDate", r.dueDate)
                put("priority", r.priority.name)
                put("type", r.type.name)
                put("isCompleted", r.isCompleted)
                put("relatedRingNumber", r.relatedRingNumber ?: "")
                put("pairId", r.pairId ?: 0L)
                put("cageCode", r.cageCode ?: "")
                put("repeatIntervalDays", r.repeatIntervalDays)
                put("updatedAt", r.updatedAt)
            }
            remArr.put(obj)
        }
        root.put("reminders", remArr)

        return root.toString()
    }

    fun fromJson(jsonStr: String): AviaryFullBackup {
        val root = JSONObject(jsonStr)

        val metaObj = root.optJSONObject("metadata") ?: JSONObject()
        val metadata = AviaryBackupMetadata(
            formatVersion = metaObj.optInt("formatVersion", 1),
            appVersion = metaObj.optString("appVersion", "1.0.0"),
            backupTimestamp = metaObj.optLong("backupTimestamp", System.currentTimeMillis()),
            deviceId = metaObj.optString("deviceId", "UNKNOWN"),
            deviceName = metaObj.optString("deviceName", "Device"),
            aviaryName = metaObj.optString("aviaryName", "Budgie Aviary"),
            latestRecordTimestamp = metaObj.optLong("latestRecordTimestamp", 0L),
            totalBirds = metaObj.optInt("totalBirds", 0),
            totalPairs = metaObj.optInt("totalPairs", 0),
            totalClutches = metaObj.optInt("totalClutches", 0),
            totalHealthRecords = metaObj.optInt("totalHealthRecords", 0),
            totalFinancialRecords = metaObj.optInt("totalFinancialRecords", 0)
        )

        // Birds
        val birds = mutableListOf<BirdEntity>()
        val birdsArr = root.optJSONArray("birds") ?: JSONArray()
        for (i in 0 until birdsArr.length()) {
            val obj = birdsArr.getJSONObject(i)
            val gender = try {
                BirdGender.valueOf(obj.optString("gender", BirdGender.UNKNOWN.name))
            } catch (_: Exception) {
                BirdGender.UNKNOWN
            }
            val variety = try {
                BudgieVariety.valueOf(obj.optString("variety", BudgieVariety.ENGLISH_SHOW.name))
            } catch (_: Exception) {
                BudgieVariety.ENGLISH_SHOW
            }
            val status = try {
                BirdStatus.valueOf(obj.optString("status", BirdStatus.ACTIVE.name))
            } catch (_: Exception) {
                BirdStatus.ACTIVE
            }

            birds.add(
                BirdEntity(
                    ringNumber = obj.getString("ringNumber"),
                    name = obj.optString("name").takeIf { it.isNotEmpty() },
                    gender = gender,
                    variety = variety,
                    mutation = obj.optString("mutation", "Normal"),
                    color = obj.optString("color", "Green"),
                    birthDate = obj.optLong("birthDate").takeIf { it > 0L },
                    placeOfBirth = obj.optString("placeOfBirth").takeIf { it.isNotEmpty() },
                    generation = obj.optString("generation").takeIf { it.isNotEmpty() },
                    photoUri = obj.optString("photoUri").takeIf { it.isNotEmpty() },
                    cageCode = obj.optString("cageCode").takeIf { it.isNotEmpty() },
                    pairId = obj.optLong("pairId").takeIf { it > 0L },
                    status = status,
                    fatherRing = obj.optString("fatherRing").takeIf { it.isNotEmpty() },
                    motherRing = obj.optString("motherRing").takeIf { it.isNotEmpty() },
                    notes = obj.optString("notes").takeIf { it.isNotEmpty() },
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            )
        }

        // Pairs
        val pairs = mutableListOf<PairEntity>()
        val pairsArr = root.optJSONArray("pairs") ?: JSONArray()
        for (i in 0 until pairsArr.length()) {
            val obj = pairsArr.getJSONObject(i)
            pairs.add(
                PairEntity(
                    id = obj.optLong("id", 0L),
                    maleRingNumber = obj.optString("maleRingNumber", obj.optString("maleRing", "")),
                    femaleRingNumber = obj.optString("femaleRingNumber", obj.optString("femaleRing", "")),
                    cageCode = obj.optString("cageCode").takeIf { it.isNotEmpty() },
                    nestId = obj.optString("nestId").takeIf { it.isNotEmpty() },
                    pairingDate = obj.optLong("pairingDate", System.currentTimeMillis()),
                    matingDate = obj.optLong("matingDate").takeIf { it > 0L },
                    endDate = obj.optLong("endDate").takeIf { it > 0L },
                    status = obj.optString("status", "ACTIVE"),
                    isActive = obj.optBoolean("isActive", true),
                    clutchCount = obj.optInt("clutchCount", 0),
                    totalChicks = obj.optInt("totalChicks", 0),
                    notes = obj.optString("notes").takeIf { it.isNotEmpty() },
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            )
        }

        // Cages
        val cages = mutableListOf<CageEntity>()
        val cagesArr = root.optJSONArray("cages") ?: JSONArray()
        for (i in 0 until cagesArr.length()) {
            val obj = cagesArr.getJSONObject(i)
            val type = try {
                CageType.valueOf(obj.optString("type", CageType.BREEDING_BOX.name))
            } catch (_: Exception) {
                CageType.BREEDING_BOX
            }
            cages.add(
                CageEntity(
                    code = obj.getString("code"),
                    type = type,
                    capacity = obj.optInt("capacity", 2),
                    currentOccupancy = obj.optInt("currentOccupancy", 0),
                    location = obj.optString("location").takeIf { it.isNotEmpty() },
                    isClean = obj.optBoolean("isClean", true),
                    lastCleanedDate = obj.optLong("lastCleanedDate").takeIf { it > 0L },
                    photoUri = obj.optString("photoUri").takeIf { it.isNotEmpty() },
                    notes = obj.optString("notes").takeIf { it.isNotEmpty() },
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            )
        }

        // Clutches
        val clutches = mutableListOf<ClutchEntity>()
        val clutchesArr = root.optJSONArray("clutches") ?: JSONArray()
        for (i in 0 until clutchesArr.length()) {
            val obj = clutchesArr.getJSONObject(i)
            clutches.add(
                ClutchEntity(
                    id = obj.optLong("id", 0L),
                    pairId = obj.optLong("pairId", 0L),
                    clutchNumber = obj.optInt("clutchNumber", 1),
                    matingDate = obj.optLong("matingDate").takeIf { it > 0L },
                    startDate = obj.optLong("startDate", System.currentTimeMillis()),
                    endDate = obj.optLong("endDate").takeIf { it > 0L },
                    eggCount = obj.optInt("eggCount", 0),
                    fertileCount = obj.optInt("fertileCount", 0),
                    hatchedCount = obj.optInt("hatchedCount", 0),
                    isActive = obj.optBoolean("isActive", true),
                    notes = obj.optString("notes").takeIf { it.isNotEmpty() }
                )
            )
        }

        // Expenses
        val expenses = mutableListOf<ExpenseEntity>()
        val expensesArr = root.optJSONArray("expenses") ?: JSONArray()
        for (i in 0 until expensesArr.length()) {
            val obj = expensesArr.getJSONObject(i)
            expenses.add(
                ExpenseEntity(
                    id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    title = obj.optString("title", "Expense"),
                    category = obj.optString("category", "FOOD"),
                    amount = obj.optDouble("amount", 0.0),
                    date = obj.optLong("date", System.currentTimeMillis()),
                    inventoryItemId = obj.optString("inventoryItemId").takeIf { it.isNotEmpty() },
                    pairId = obj.optString("pairId").takeIf { it.isNotEmpty() },
                    birdRingNumber = obj.optString("birdRingNumber").takeIf { it.isNotEmpty() },
                    receiptNumber = obj.optString("receiptNumber").takeIf { it.isNotEmpty() },
                    notes = obj.optString("notes").takeIf { it.isNotEmpty() },
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            )
        }

        // Incomes
        val incomes = mutableListOf<IncomeEntity>()
        val incomesArr = root.optJSONArray("incomes") ?: JSONArray()
        for (i in 0 until incomesArr.length()) {
            val obj = incomesArr.getJSONObject(i)
            incomes.add(
                IncomeEntity(
                    id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    title = obj.optString("title", "Income"),
                    category = obj.optString("category", "BIRD_SALE"),
                    amount = obj.optDouble("amount", 0.0),
                    date = obj.optLong("date", System.currentTimeMillis()),
                    soldBirdRingNumber = obj.optString("soldBirdRingNumber").takeIf { it.isNotEmpty() },
                    soldChickId = obj.optString("soldChickId").takeIf { it.isNotEmpty() },
                    buyerName = obj.optString("buyerName").takeIf { it.isNotEmpty() },
                    buyerContact = obj.optString("buyerContact").takeIf { it.isNotEmpty() },
                    notes = obj.optString("notes").takeIf { it.isNotEmpty() },
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            )
        }

        // Health Records
        val healthRecords = mutableListOf<HealthRecordEntity>()
        val healthArr = root.optJSONArray("healthRecords") ?: JSONArray()
        for (i in 0 until healthArr.length()) {
            val obj = healthArr.getJSONObject(i)
            healthRecords.add(
                HealthRecordEntity(
                    id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    birdRingNumber = obj.optString("birdRingNumber").takeIf { it.isNotEmpty() },
                    cageCode = obj.optString("cageCode").takeIf { it.isNotEmpty() },
                    recordDate = obj.optLong("recordDate", System.currentTimeMillis()),
                    recordType = obj.optString("recordType", "EXAMINATION"),
                    symptoms = obj.optString("symptoms", "Checkup"),
                    diagnosis = obj.optString("diagnosis", "Normal"),
                    recordedProblem = obj.optString("recordedProblem", "Normal"),
                    veterinarianName = obj.optString("veterinarianName").takeIf { it.isNotEmpty() },
                    medicationName = obj.optString("medicationName").takeIf { it.isNotEmpty() },
                    dosage = obj.optString("dosage").takeIf { it.isNotEmpty() },
                    frequency = obj.optString("frequency").takeIf { it.isNotEmpty() },
                    treatmentDurationDays = obj.optInt("treatmentDurationDays", 0),
                    startDate = obj.optLong("startDate", System.currentTimeMillis()),
                    endDate = obj.optLong("endDate").takeIf { it > 0L },
                    supplements = obj.optString("supplements").takeIf { it.isNotEmpty() },
                    isResolved = obj.optBoolean("isResolved", false),
                    notes = obj.optString("notes").takeIf { it.isNotEmpty() },
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            )
        }

        // Reminders
        val reminders = mutableListOf<ReminderEntity>()
        val remArr = root.optJSONArray("reminders") ?: JSONArray()
        for (i in 0 until remArr.length()) {
            val obj = remArr.getJSONObject(i)
            val priority = try {
                ReminderPriority.valueOf(obj.optString("priority", ReminderPriority.NORMAL.name))
            } catch (_: Exception) {
                ReminderPriority.NORMAL
            }
            val type = try {
                ReminderType.valueOf(obj.optString("type", ReminderType.DAILY_TASK.name))
            } catch (_: Exception) {
                ReminderType.DAILY_TASK
            }

            reminders.add(
                ReminderEntity(
                    id = obj.optLong("id", 0L),
                    title = obj.optString("title", "Reminder"),
                    description = obj.optString("description").takeIf { it.isNotEmpty() },
                    dueDate = obj.optLong("dueDate", System.currentTimeMillis()),
                    priority = priority,
                    type = type,
                    isCompleted = obj.optBoolean("isCompleted", false),
                    relatedRingNumber = obj.optString("relatedRingNumber").takeIf { it.isNotEmpty() },
                    pairId = obj.optLong("pairId").takeIf { it > 0L },
                    cageCode = obj.optString("cageCode").takeIf { it.isNotEmpty() },
                    repeatIntervalDays = obj.optInt("repeatIntervalDays", 0),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            )
        }

        return AviaryFullBackup(
            metadata = metadata,
            birds = birds,
            pairs = pairs,
            cages = cages,
            clutches = clutches,
            expenses = expenses,
            incomes = incomes,
            healthRecords = healthRecords,
            reminders = reminders
        )
    }
}
