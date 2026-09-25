package com.example.core.ai

import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import com.example.data.database.entity.ChickEntity
import com.example.data.database.entity.ClutchEntity
import com.example.data.database.entity.EggEntity
import com.example.data.database.entity.ExpenseEntity
import com.example.data.database.entity.HealthRecordEntity
import com.example.data.database.entity.IncomeEntity
import com.example.data.database.entity.InventoryItemEntity
import com.example.data.database.entity.PairEntity
import com.example.data.database.entity.ReminderEntity
import com.example.data.database.entity.WeightRecordEntity
import com.example.data.repository.AviaryRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Deterministic and semantic database retriever that extracts accurate facility data
 * to build the context for the AI Assistant without inventing records.
 */
class AviaryDataRetriever(private val repository: AviaryRepository) {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    suspend fun retrieveContextForQuery(query: String): FacilityContextData {
        // Retrieve current live snapshot from repository
        val birds = repository.allBirds.first()
        val pairs = repository.allPairs.first()
        val clutches = repository.allClutches.first()
        val eggs = repository.allEggs.first()
        val chicks = repository.allChicks.first()
        val cages = repository.allCages.first()
        val reminders = repository.pendingReminders.first()
        val health = repository.allHealthRecords.first()
        val weights = repository.allWeightRecords.first()
        val inventory = repository.allInventoryItems.first()
        val expenses = repository.allExpenses.first()
        val incomes = repository.allIncome.first()

        return FacilityContextData(
            query = query,
            birds = birds,
            pairs = pairs,
            clutches = clutches,
            eggs = eggs,
            chicks = chicks,
            cages = cages,
            reminders = reminders,
            health = health,
            weights = weights,
            inventory = inventory,
            expenses = expenses,
            incomes = incomes
        )
    }

    /**
     * Build rich, factual prompt representation of facility data relevant to the query.
     */
    fun buildContextPrompt(context: FacilityContextData): String {
        val q = context.query.lowercase(Locale.ROOT)
        val sb = StringBuilder()

        sb.append("=== FACILITY LIVE DATA (REAL DATABASE SNAPSHOT) ===\n")
        sb.append("Current Date: ${dateFormat.format(Date())}\n\n")

        // 1. Reminders & Tasks Today
        if (q.contains("امروز") || q.contains("کارهای امروز") || q.contains("وظایف") || q.contains("today") || q.contains("task") || q.contains("reminder")) {
            sb.append("--- REMINDERS & TASKS ---\n")
            val pending = context.reminders.filter { !it.isCompleted }
            if (pending.isEmpty()) {
                sb.append("No pending reminders or tasks found for today.\n")
            } else {
                pending.forEach {
                    val dStr = dateFormat.format(Date(it.dueDate))
                    sb.append("• ${it.title}: ${it.description ?: ""} (Due: $dStr, Priority: ${it.priority.name})\n")
                }
            }
            sb.append("\n")
        }

        // 2. Eggs & Upcoming Hatches
        if (q.contains("تخم") || q.contains("هچ") || q.contains("egg") || q.contains("hatch")) {
            sb.append("--- EGGS & INCUBATION STATUS ---\n")
            if (context.eggs.isEmpty()) {
                sb.append("No eggs registered in database.\n")
            } else {
                val now = System.currentTimeMillis()
                context.eggs.forEach { egg ->
                    val layStr = dateFormat.format(Date(egg.layDate))
                    val hatchExpStr = egg.expectedHatchDate?.let { dateFormat.format(Date(it)) } ?: "Not set"
                    val daysToHatch = egg.expectedHatchDate?.let { (it - now) / (1000 * 60 * 60 * 24) }
                    sb.append("• Egg #${egg.eggNumber} (ID: ${egg.id}, PairId: ${egg.pairId}, ClutchId: ${egg.clutchId ?: "N/A"}): LayDate=$layStr, Status=${egg.fertilityStatus}, ExpectedHatch=$hatchExpStr, DaysUntilHatch=${daysToHatch ?: "N/A"}\n")
                }
            }
            sb.append("\n")
        }

        // 3. Pairs & Breeding Performance
        val extractedPairNumber = extractNumberAfterKeyword(q, listOf("جفت شماره", "جفت #", "جفت", "pair #", "pair"))
        if (q.contains("جفت") || q.contains("pair") || q.contains("عملکرد") || q.contains("performance")) {
            sb.append("--- BREEDING PAIRS ---\n")
            val matchedPairs = if (extractedPairNumber != null) {
                context.pairs.filter {
                    it.id == extractedPairNumber ||
                    (it.cageCode != null && it.cageCode.contains(extractedPairNumber.toString()))
                }
            } else {
                context.pairs.take(15)
            }

            if (matchedPairs.isEmpty()) {
                sb.append("Pair with specified number ($extractedPairNumber) was NOT found in the database.\n")
            } else {
                matchedPairs.forEach { pair ->
                    sb.append("• Pair #${pair.id} (Male: ${pair.maleRingNumber}, Female: ${pair.femaleRingNumber}, Cage: ${pair.cageCode ?: "N/A"}, Status: ${pair.status}, Active: ${pair.isActive}):\n")
                    val pairClutches = context.clutches.filter { it.pairId == pair.id }.sortedByDescending { it.startDate }
                    sb.append("  Total Clutches/Rounds: ${pairClutches.size}\n")
                    pairClutches.take(5).forEachIndexed { idx, clutch ->
                        val startStr = dateFormat.format(Date(clutch.startDate))
                        sb.append("   - Round ${idx + 1} (Clutch #${clutch.clutchNumber}, Date: $startStr): Total Eggs=${clutch.eggCount}, Fertile=${clutch.fertileCount}, Hatched=${clutch.hatchedCount}, Active=${clutch.isActive}\n")
                    }
                    val pairChicks = context.chicks.filter { it.pairId == pair.id }
                    sb.append("  Total Chicks for this Pair: ${pairChicks.size} (${pairChicks.map { "Chick #${it.hatchOrder} (Ring: ${it.bandedRingNumber ?: "Unringed"}) Status: ${it.status}" }.joinToString(", ")})\n")
                }
            }
            sb.append("\n")
        }

        // 4. Cage Query
        val extractedCageNumber = extractNumberAfterKeyword(q, listOf("قفس شماره", "قفس #", "قفس", "cage #", "cage"))
        if (q.contains("قفس") || q.contains("cage")) {
            sb.append("--- CAGES & BIRDS IN CAGES ---\n")
            val matchedCages = if (extractedCageNumber != null) {
                context.cages.filter { it.code.contains(extractedCageNumber.toString(), ignoreCase = true) }
            } else {
                context.cages.take(15)
            }

            if (matchedCages.isEmpty() && extractedCageNumber != null) {
                sb.append("Cage $extractedCageNumber was NOT found in the database.\n")
            } else {
                matchedCages.forEach { cage ->
                    val birdsInCage = context.birds.filter { it.cageCode != null && it.cageCode.equals(cage.code, ignoreCase = true) }
                    sb.append("• Cage '${cage.code}' (Type: ${cage.type}, Capacity: ${cage.capacity}, Clean: ${cage.isClean}):\n")
                    if (birdsInCage.isEmpty()) {
                        sb.append("  No birds currently assigned to this cage.\n")
                    } else {
                        sb.append("  Occupants (${birdsInCage.size} birds): ${birdsInCage.map { "${it.ringNumber} (${it.gender.name}, ${it.variety.name}, Color: ${it.color})" }.joinToString("; ")}\n")
                    }
                }
            }
            sb.append("\n")
        }

        // 5. Bird Query & Weights
        val hasBirdQuery = q.contains("پرنده") || q.contains("وزن") || q.contains("ring") || q.contains("bird") || q.contains("weight")
        if (hasBirdQuery) {
            sb.append("--- BIRDS & LATEST WEIGHTS ---\n")
            val candidateBirds = context.birds.filter { b ->
                q.contains(b.ringNumber.lowercase(Locale.ROOT)) ||
                (b.name != null && q.contains(b.name.lowercase(Locale.ROOT)))
            }.ifEmpty { context.birds.take(15) }

            candidateBirds.forEach { bird ->
                val birdWeights = context.weights.filter { it.birdRingNumber == bird.ringNumber }.sortedByDescending { it.recordedDate }
                val latestWeight = birdWeights.firstOrNull()
                val latestWeightStr = if (latestWeight != null) {
                    "${latestWeight.weightGrams}g on ${dateFormat.format(Date(latestWeight.recordedDate))} (Note: ${latestWeight.notes ?: "-"})"
                } else {
                    "No weight records recorded"
                }
                sb.append("• Bird Ring: ${bird.ringNumber} (Name: ${bird.name ?: "None"}, Gender: ${bird.gender.name}, Variety: ${bird.variety.name}, Cage: ${bird.cageCode ?: "None"}, Status: ${bird.status.name})\n")
                sb.append("  Latest Weight: $latestWeightStr\n")
            }
            sb.append("\n")
        }

        // 6. Chicks Query
        if (q.contains("جوجه") || q.contains("chick")) {
            sb.append("--- CHICKS SUMMARY ---\n")
            if (context.chicks.isEmpty()) {
                sb.append("No chicks recorded in database.\n")
            } else {
                context.chicks.take(20).forEach { ch ->
                    val hatchStr = dateFormat.format(Date(ch.hatchDate))
                    sb.append("• Chick #${ch.hatchOrder} (ID: ${ch.id}, PairId: ${ch.pairId}, HatchDate: $hatchStr, Status: ${ch.status}, Ring: ${ch.bandedRingNumber ?: "Unringed"}, Cage: ${ch.cageCode ?: "Nest"})\n")
                }
            }
            sb.append("\n")
        }

        // 7. Inventory & Finances
        if (q.contains("موجودی") || q.contains("انبار") || q.contains("دانه") || q.contains("دارو") || q.contains("هزینه") || q.contains("مالی") || q.contains("stock") || q.contains("inventory") || q.contains("finance")) {
            sb.append("--- INVENTORY & FINANCE ---\n")
            context.inventory.forEach { inv ->
                sb.append("• Item: ${inv.name} (Category: ${inv.category}, Current: ${inv.currentStock} ${inv.unit}, MinStock: ${inv.minStockThreshold}, Cost: ${inv.costPerUnit})\n")
            }
            sb.append("Total Expenses: ${context.expenses.sumOf { it.amount }} | Total Income: ${context.incomes.sumOf { it.amount }}\n\n")
        }

        sb.append("=== INSTRUCTIONS FOR AI ===\n")
        sb.append("1. Answer accurately using ONLY the database records provided above.\n")
        sb.append("2. Support both Persian (فارسی) and English according to the language of the user's question.\n")
        sb.append("3. DO NOT invent or hallucinate any records. If information is missing or not found in the database, explicitly state: 'اطلاعات مورد نظر در پایگاه داده ثبت نشده است' (or in English: 'The requested data is not available in the database').\n")
        sb.append("4. Present answers concisely with clear bullet points and numbers.\n")

        return sb.toString()
    }

    /**
     * Local deterministic fallback generator if API key is missing or offline.
     * Ensures user gets 100% accurate, non-hallucinated answers from their local DB directly.
     */
    fun answerLocallyWithoutApi(context: FacilityContextData): String {
        val q = context.query.lowercase(Locale.ROOT)
        val isFa = q.any { it in '\u0600'..'\u06FF' }

        // Specific Scenario 1: Birds in Cage X (check cage first so queries like "پرنده‌های قفس ۲۵" route to cage)
        if (q.contains("قفس") || q.contains("cage")) {
            val cageNum = extractNumberAfterKeyword(q, listOf("قفس شماره", "قفس #", "قفس", "cage #", "cage"))
            val codeToSearch = cageNum?.toString() ?: context.cages.firstOrNull { q.contains(it.code.lowercase()) }?.code
            val cage = if (codeToSearch != null) {
                context.cages.firstOrNull { it.code.contains(codeToSearch, ignoreCase = true) }
            } else {
                context.cages.firstOrNull()
            }

            return if (cage == null) {
                if (isFa) "اطلاعات مورد نظر در پایگاه داده ثبت نشده است: قفس مورد نظر یافت نشد."
                else "The requested data is not available in the database: Cage was not found."
            } else {
                val birdsInCage = context.birds.filter { it.cageCode != null && it.cageCode.equals(cage.code, ignoreCase = true) }
                if (birdsInCage.isEmpty()) {
                    if (isFa) "قفس ${cage.code} (${cage.type}) در حال حاضر خالی است و هیچ پرنده‌ای در آن ثبت نشده است."
                    else "Cage ${cage.code} (${cage.type}) is currently empty with no birds recorded."
                } else {
                    val birdsDesc = birdsInCage.mapIndexed { idx, b ->
                        "${idx + 1}. پلاک **${b.ringNumber}** | جنسیت: ${b.gender.name} | گونه/رنگ: ${b.variety.name} (${b.color}) | وضعیت: ${b.status.name}"
                    }.joinToString("\n")
                    if (isFa) "🕊️ پرندگان مستقر در قفس ${cage.code} (${birdsInCage.size} پرنده):\n$birdsDesc"
                    else "🕊️ Birds currently in Cage ${cage.code} (${birdsInCage.size} birds):\n$birdsDesc"
                }
            }
        }

        // Specific Scenario 2: Pair Performance
        if (q.contains("جفت") || q.contains("pair") || q.contains("عملکرد") || q.contains("performance")) {
            val pairNum = extractNumberAfterKeyword(q, listOf("جفت شماره", "جفت #", "جفت", "pair #", "pair"))
            val matchedPair = if (pairNum != null) {
                context.pairs.firstOrNull { it.id == pairNum || (it.cageCode != null && it.cageCode.contains(pairNum.toString())) }
            } else {
                context.pairs.firstOrNull { q.contains(it.id.toString()) }
            }

            return if (matchedPair == null) {
                if (isFa) "اطلاعات مورد نظر در پایگاه داده ثبت نشده است: جفت مورد نظر یافت نشد."
                else "The requested data is not available in the database: The specified pair was not found."
            } else {
                val pairClutches = context.clutches.filter { it.pairId == matchedPair.id }.sortedByDescending { it.startDate }
                val recentClutches = pairClutches.take(3)
                if (recentClutches.isEmpty()) {
                    if (isFa) "جفت شماره ${matchedPair.id} (نر: ${matchedPair.maleRingNumber}، ماده: ${matchedPair.femaleRingNumber}) در سیستم ثبت است، اما هنوز هیچ دوره‌ی تخم‌گذاری یا کلاچی برای آن ثبت نشده است."
                    else "Pair #${matchedPair.id} (M: ${matchedPair.maleRingNumber}, F: ${matchedPair.femaleRingNumber}) has no breeding rounds recorded yet."
                } else {
                    val roundsDesc = recentClutches.mapIndexed { idx, c ->
                        val startStr = dateFormat.format(Date(c.startDate))
                        "• **دوره ${c.clutchNumber}** ($startStr): کل تخم‌ها: ${c.eggCount} | نطفه‌دار: ${c.fertileCount} | هچ‌شده: ${c.hatchedCount} (فعال: ${c.isActive})"
                    }.joinToString("\n")
                    val totalHatched = recentClutches.sumOf { it.hatchedCount }
                    val totalEggs = recentClutches.sumOf { it.eggCount }
                    if (isFa) {
                        "📊 عملکرد جفت شماره **${matchedPair.id}** (نر: ${matchedPair.maleRingNumber}، ماده: ${matchedPair.femaleRingNumber}) در ${recentClutches.size} دوره اخیر:\n\n$roundsDesc\n\n📌 مجموع: $totalEggs تخم گذاشته شده و $totalHatched جوجه هچ شده است."
                    } else {
                        "📊 Performance of Pair **#${matchedPair.id}** in last ${recentClutches.size} rounds:\n\n$roundsDesc\n\n📌 Total: $totalEggs eggs laid and $totalHatched chicks hatched."
                    }
                }
            }
        }

        // Specific Scenario 3: Tasks / Reminders today
        if (q.contains("امروز") || q.contains("کار") || q.contains("وظایف") || q.contains("today") || q.contains("task")) {
            val pending = context.reminders.filter { !it.isCompleted }
            return if (pending.isEmpty()) {
                if (isFa) "✅ امروز هیچ یادآوری یا کار برنامه‌ریزی‌شده معوقی در پایگاه داده ثبت نشده است."
                else "✅ No pending tasks or reminders scheduled for today in the database."
            } else {
                val listStr = pending.mapIndexed { idx, r ->
                    val dStr = dateFormat.format(Date(r.dueDate))
                    "${idx + 1}. **${r.title}** (موعد: $dStr - اولویت: ${r.priority.name})"
                }.joinToString("\n")
                if (isFa) "📋 کارهای برنامه‌ریزی‌شده امروز شما (${pending.size} مورد):\n$listStr"
                else "📋 Your scheduled tasks for today (${pending.size} items):\n$listStr"
            }
        }

        // Specific Scenario 4: Chicks of a pair
        if (q.contains("جوجه") || q.contains("chick")) {
            val pairNum = extractNumberAfterKeyword(q, listOf("جفت شماره", "جفت #", "جفت", "pair #", "pair"))
            val pair = if (pairNum != null) context.pairs.firstOrNull { it.id == pairNum } else context.pairs.firstOrNull()
            return if (pair == null) {
                if (isFa) "اطلاعات مورد نظر در پایگاه داده ثبت نشده است: جفتی مشخص نشده یا در سیستم موجود نیست."
                else "The requested data is not available in the database: No pair specified or found."
            } else {
                val pairChicks = context.chicks.filter { it.pairId == pair.id }
                if (pairChicks.isEmpty()) {
                    if (isFa) "هیچ جوجه‌ای برای جفت #${pair.id} در سیستم ثبت نشده است."
                    else "No chicks recorded for Pair #${pair.id}."
                } else {
                    val chicksStr = pairChicks.mapIndexed { idx, ch ->
                        val hatchStr = dateFormat.format(Date(ch.hatchDate))
                        "${idx + 1}. جوجه #${ch.hatchOrder} (پلاک: ${ch.bandedRingNumber ?: "هنوز حلقه ندارد"}) | تاریخ هچ: $hatchStr | وضعیت: ${ch.status}"
                    }.joinToString("\n")
                    if (isFa) "🐣 جوجه‌های ثبت‌شده برای جفت #${pair.id} (${pairChicks.size} جوجه):\n$chicksStr"
                    else "🐣 Chicks recorded for Pair #${pair.id} (${pairChicks.size} chicks):\n$chicksStr"
                }
            }
        }

        // Specific Scenario 5: Latest weight of bird
        if (q.contains("وزن") || q.contains("weight")) {
            val ringMatch = context.birds.firstOrNull { b ->
                q.contains(b.ringNumber.lowercase(Locale.ROOT)) ||
                (b.name != null && q.contains(b.name.lowercase(Locale.ROOT)))
            }
            val targetBird = ringMatch ?: context.birds.firstOrNull()
            return if (targetBird == null) {
                if (isFa) "اطلاعات مورد نظر در پایگاه داده ثبت نشده است: هیچ پرنده‌ای در سیستم یافت نشد."
                else "The requested data is not available in the database: No bird found."
            } else {
                val weights = context.weights.filter { it.birdRingNumber == targetBird.ringNumber }.sortedByDescending { it.recordedDate }
                val latest = weights.firstOrNull()
                if (latest == null) {
                    if (isFa) "پرنده **${targetBird.ringNumber}** در سیستم موجود است، اما هیچ رکورد وزنی برای آن در پایگاه داده ثبت نشده است."
                    else "Bird **${targetBird.ringNumber}** is registered, but no weight history has been recorded."
                } else {
                    val dateStr = dateFormat.format(Date(latest.recordedDate))
                    if (isFa) "⚖️ آخرین وزن ثبت‌شده برای پرنده **${targetBird.ringNumber}**:\nمقدار: **${latest.weightGrams} گرم**\nتاریخ ثبت: $dateStr\nوضعیت/یادداشت: ${latest.notes ?: "عادی"}"
                    else "⚖️ Latest recorded weight for bird **${targetBird.ringNumber}**:\nWeight: **${latest.weightGrams} g**\nDate: $dateStr\nNotes: ${latest.notes ?: "Normal"}"
                }
            }
        }

        // Specific Scenario 6: Eggs near hatching
        if (q.contains("تخم") || q.contains("هچ") || q.contains("نزدیک") || q.contains("egg") || q.contains("hatch")) {
            val now = System.currentTimeMillis()
            val threeDaysMs = 3L * 24 * 60 * 60 * 1000
            val nearHatchEggs = context.eggs.filter { egg ->
                egg.fertilityStatus.equals("FERTILE", ignoreCase = true) &&
                egg.expectedHatchDate != null &&
                (egg.expectedHatchDate - now) <= threeDaysMs &&
                (egg.expectedHatchDate - now) >= -threeDaysMs
            }

            return if (nearHatchEggs.isEmpty()) {
                if (isFa) "🐣 در حال حاضر هیچ تخم نطفه‌داری که در آستانه هچ (۱ تا ۳ روز آینده) باشد در پایگاه داده ثبت نشده است."
                else "🐣 No fertile eggs currently due for hatching within the next 3 days."
            } else {
                val eggsStr = nearHatchEggs.mapIndexed { idx, e ->
                    val hatchStr = dateFormat.format(Date(e.expectedHatchDate!!))
                    val days = (e.expectedHatchDate - now) / (1000 * 60 * 60 * 24)
                    val daysText = if (days <= 0) "امروز / هچ مورد انتظار" else "$days روز مانده"
                    "${idx + 1}. تخم شماره #${e.eggNumber} (جفت: ${e.pairId}) | تاریخ هچ: $hatchStr ($daysText)"
                }.joinToString("\n")
                if (isFa) "🥚 تخم‌های نزدیک به هچ در سالن (${nearHatchEggs.size} تخم):\n$eggsStr"
                else "🥚 Eggs near hatching in aviary (${nearHatchEggs.size} eggs):\n$eggsStr"
            }
        }

        // Default factual summary
        return if (isFa) {
            "اطلاعات کلی سالن پرورش شما:\n" +
            "• تعداد پرندگان: ${context.birds.size}\n" +
            "• تعداد جفت‌های فعال: ${context.pairs.count { it.isActive }}\n" +
            "• کارهای امروز: ${context.reminders.count { !it.isCompleted }} مورد معوق\n" +
            "• قفس‌ها: ${context.cages.size} قفس ثبت‌شده\n\n" +
            "می‌توانید سوالات دقیق‌تری مانند:\n" +
            "- «امروز چه کارهایی دارم؟»\n" +
            "- «پرنده‌های قفس ۲۵ را نشان بده»\n" +
            "- «جفت ۱۲ در سه دوره اخیر چه عملکردی داشته؟»\n" +
            "- «کدام تخم‌ها نزدیک هچ هستند؟»\nبپرسید."
        } else {
            "Aviary Overview:\n" +
            "• Birds: ${context.birds.size}\n" +
            "• Active Pairs: ${context.pairs.count { it.isActive }}\n" +
            "• Today's Tasks: ${context.reminders.count { !it.isCompleted }} pending\n" +
            "• Cages: ${context.cages.size} registered\n\n" +
            "You can ask specific questions like: 'What tasks do I have today?', 'Show birds in cage 25', 'How did pair 12 perform?', or 'Which eggs are near hatching?'."
        }
    }

    private fun extractNumberAfterKeyword(query: String, keywords: List<String>): Long? {
        // Normalize Persian and Arabic numerals to English ASCII digits
        val normalized = query.map { ch ->
            when (ch) {
                '۰', '٠' -> '0'
                '۱', '١' -> '1'
                '۲', '٢' -> '2'
                '۳', '٣' -> '3'
                '۴', '٤' -> '4'
                '۵', '٥' -> '5'
                '۶', '٦' -> '6'
                '۷', '٧' -> '7'
                '۸', '٨' -> '8'
                '۹', '٩' -> '9'
                else -> ch
            }
        }.joinToString("")

        for (kw in keywords) {
            val idx = normalized.indexOf(kw)
            if (idx != -1) {
                val after = normalized.substring(idx + kw.length).trim()
                val match = Regex("""^#?\s*([0-9]+)""").find(after)
                if (match != null) {
                    return match.groupValues[1].toLongOrNull()
                }
            }
        }
        // General number extraction if single number in query
        val numbers = Regex("""\b([0-9]+)\b""").findAll(normalized).mapNotNull { it.groupValues[1].toLongOrNull() }.toList()
        return if (numbers.size == 1) numbers.first() else null
    }
}

data class FacilityContextData(
    val query: String,
    val birds: List<BirdEntity>,
    val pairs: List<PairEntity>,
    val clutches: List<ClutchEntity>,
    val eggs: List<EggEntity>,
    val chicks: List<ChickEntity>,
    val cages: List<CageEntity>,
    val reminders: List<ReminderEntity>,
    val health: List<HealthRecordEntity>,
    val weights: List<WeightRecordEntity>,
    val inventory: List<InventoryItemEntity>,
    val expenses: List<ExpenseEntity>,
    val incomes: List<IncomeEntity>
)
