package com.example.feature.nutrition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.ui.components.EmptyStateView
import com.example.data.database.AppDatabase
import com.example.data.database.entity.NutritionPlanEntity
import com.example.data.database.entity.NutritionRecordEntity
import com.example.data.repository.AviaryRepositoryImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NutritionScreen(
    modifier: Modifier = Modifier,
    onNavigateToBird: (String) -> Unit = {},
    viewModel: NutritionViewModel = run {
        val context = LocalContext.current
        val db = AppDatabase.getInstance(context)
        val repo = AviaryRepositoryImpl(db)
        viewModel(factory = NutritionViewModelFactory(repo))
    }
) {
    val currentLang = LocalAppLanguage.current
    val isFa = currentLang == AppLanguage.PERSIAN

    val nutritionRecords by viewModel.allNutritionRecords.collectAsState()
    val nutritionPlans by viewModel.allNutritionPlans.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    var showAddFeedingDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US) }
    val totalCost = remember(nutritionRecords) { nutritionRecords.sumOf { it.cost } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("nutrition_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // Top Overview Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isFa) "تغذیه، جیره‌بندی و مکمل‌ها" else "Nutrition & Feeding Management",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${nutritionRecords.size} ${if (isFa) "وعده ثبت شده" else "logs"}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isFa)
                            "ثبت جیره غذایی روزانه، نوع آب، مکمل‌ها، مصرف پرندگان و محاسبه هزینه‌های تغذیه."
                        else
                            "Daily feeding logs, water regimen, supplements, consumption tracking, and feed costs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Metrics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NutritionStatBox(
                    title = if (isFa) "مجموع وعده‌ها" else "Total Logs",
                    value = "${nutritionRecords.size}",
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Filled.Restaurant,
                    modifier = Modifier.weight(1f)
                )

                NutritionStatBox(
                    title = if (isFa) "کل هزینه خوراک" else "Feed Cost",
                    value = "$%.2f".format(Locale.US, totalCost),
                    color = Color(0xFF2E7D32),
                    icon = Icons.Filled.AttachMoney,
                    modifier = Modifier.weight(1f)
                )

                NutritionStatBox(
                    title = if (isFa) "برنامه‌های فصلی" else "Diet Regimes",
                    value = "4",
                    color = MaterialTheme.colorScheme.secondary,
                    icon = Icons.Filled.Grass,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Add Feeding Button
        item {
            Button(
                onClick = { showAddFeedingDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_log_feeding")
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isFa) "ثبت وعده تغذیه جدید" else "Log Feeding Record")
            }
        }

        // Tab Selector
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text(if (isFa) "سوابق روزانه (${nutritionRecords.size})" else "Daily Logs (${nutritionRecords.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text(if (isFa) "برنامه‌های غذایی استاندارد" else "Seasonal Diet Plans") }
                )
            }
        }

        if (selectedTab == 0) {
            // Daily Feeding Records
            if (nutritionRecords.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Filled.Restaurant,
                        title = if (isFa) "هیچ سابقه تغذیه‌ای ثبت نشده است" else "No Feeding Records Yet",
                        description = if (isFa)
                            "برای پایش سلامت گله و جوجه‌ها، وعده‌های غذایی و مکمل‌های مصرفی را ثبت کنید."
                        else
                            "Track seed mixes, soft food, supplements, and consumption to keep birds in peak condition.",
                        actionLabel = if (isFa) "ثبت اولین وعده" else "Log First Meal",
                        onActionClick = { showAddFeedingDialog = true },
                        testTag = "nutrition_empty_view"
                    )
                }
            } else {
                items(nutritionRecords, key = { it.id }) { record ->
                    NutritionRecordCard(
                        record = record,
                        isPersian = isFa,
                        dateFormat = dateFormat,
                        onBirdClick = { ring -> if (!ring.isNullOrBlank()) onNavigateToBird(ring) },
                        onDelete = { viewModel.deleteNutritionRecord(record) }
                    )
                }
            }
        } else {
            // Standard Seasonal Diets
            val diets = listOf(
                DietPlanItem(
                    title = if (isFa) "جیره پایه دان مخلوط سالن" else "Base Seed Blend",
                    phase = if (isFa) "تمام فصول" else "All Season Base",
                    food = if (isFa) "ارزن سفید و زرد (۶۰٪)، تخم کتان (۲۰٪)، ارزن قرمز (۱۰٪)، هفت تخم سبک (۱۰٪)" else "Canary seed 60%, Yellow millet 30%, Groats/Oats 10%",
                    water = if (isFa) "آب تازه روزانه تصفیه شده" else "Fresh Filtered Tap Water",
                    supplements = if (isFa) "بلوک کلسیم، کف دریا، سنگریزه بهداشتی" else "Cuttlebone, Mineral block, Sterilized grit",
                    schedule = if (isFa) "در دسترس دائم (Ad Libitum)" else "Continuous Ad Libitum",
                    costEst = "$0.20 / bird / day"
                ),
                DietPlanItem(
                    title = if (isFa) "غذای تخم‌مرغی و آماده‌سازی جفت‌ها" else "Breeding & Conditioning Egg Food",
                    phase = if (isFa) "فصل تکثیر و جوجه‌کشی" else "Breeding Season",
                    food = if (isFa) "تخم‌مرغ آب‌پز رنده شده با پوسته ضدعفونی، هویج، پودر سوخاری و مخمر آبجو" else "Hard boiled egg, grated carrot, dry egg-food rusk, brewer's yeast",
                    water = if (isFa) "آب با مولتی‌ویتامین E و سلنیوم (Fertivit)" else "Water with Vitamin E & Selenium (Fertivit)",
                    supplements = if (isFa) "کالسی‌وت مایع، پروبیوتیک، پودر آویشن" else "Calcivet liquid calcium, Probiotic powder",
                    schedule = if (isFa) "هر روز صبح به مقدار تمام‌شدنی در ۲ ساعت" else "Every morning fresh, clear after 2h",
                    costEst = "$0.65 / pair / day"
                ),
                DietPlanItem(
                    title = if (isFa) "جوانه‌ها و سبزیجات تقویتی" else "Sprouted Seeds & Fresh Greens",
                    phase = if (isFa) "رشد جوجه‌ها و تغذیه والدین" else "Chick Rearing & Weaning",
                    food = if (isFa) "جوانه تازه گندم، ماش و عدس، برگ بروکلی، اسفناج و جعفری شسته شده" else "Fresh sprouted wheat & mung beans, broccoli florets, fresh spinach",
                    water = if (isFa) "آب اسیدی با سرکه سیب (۵ قطره در ۱۰۰ سی‌سی)" else "Apple Cider Vinegar acidified water (5ml/L)",
                    supplements = if (isFa) "پودر اسپیرولینا، مولتی‌ویتامین نکتون" else "Spirulina powder, Nekton S vitamins",
                    schedule = if (isFa) "۳ روز در هفته بعدازظهرها" else "3x weekly afternoons",
                    costEst = "$0.40 / cage"
                ),
                DietPlanItem(
                    title = if (isFa) "جیره دوره استراحت و پرریزی (لک)" else "Molting & Resting Recovery Diet",
                    phase = if (isFa) "دوره پرریزی و استراحت" else "Molting & Rest",
                    food = if (isFa) "دان سبک با افزایش تخم کتان، تخم کلم و کاهو برای رویش پرها" else "Light canary blend + linseed, niger seed and lettuce seed for feathers",
                    water = if (isFa) "محلول الکترولیت و اسیدهای آمینه (Muta-Vit)" else "Amino acids & electrolytes (Muta-Vit)",
                    supplements = if (isFa) "عصاره خار مریم (سیلیمارین) برای تقویت کبد" else "Milk thistle extract (Silymarin) for liver support",
                    schedule = if (isFa) "روزانه نوبت صبح" else "Morning feeding",
                    costEst = "$0.30 / bird / day"
                )
            )

            items(diets) { diet ->
                DietPlanCard(diet = diet, isPersian = isFa)
            }
        }
    }

    if (showAddFeedingDialog) {
        LogNutritionDialog(
            isPersian = isFa,
            onDismiss = { showAddFeedingDialog = false },
            onSave = { record ->
                viewModel.saveNutritionRecord(record)
                showAddFeedingDialog = false
            }
        )
    }
}

@Composable
private fun NutritionRecordCard(
    record: NutritionRecordEntity,
    isPersian: Boolean,
    dateFormat: SimpleDateFormat,
    onBirdClick: (String?) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("nutrition_record_card_${record.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (record.birdRingNumber != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { onBirdClick(record.birdRingNumber) }
                        ) {
                            Text(
                                text = record.birdRingNumber,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    } else if (record.cageCode != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Cage: ${record.cageCode}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = record.foodType,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Amount, Consumption, Schedule
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${if (isPersian) "مقدار:" else "Amount:"} ${record.amount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "${if (isPersian) "میزان مصرف:" else "Consumption:"} ${record.consumptionRate}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = when (record.consumptionRate) {
                        "FINISHED_ALL", "HIGH" -> Color(0xFF2E7D32)
                        "LOW", "REFUSED" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Water & Supplements
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.LocalDrink, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = record.waterType,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (record.supplements.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Spa, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = record.supplements,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Footer: Schedule, Date and Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${dateFormat.format(Date(record.recordDate))} (${record.feedingSchedule})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                if (record.cost > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF2E7D32).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "$%.2f".format(Locale.US, record.cost),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class DietPlanItem(
    val title: String,
    val phase: String,
    val food: String,
    val water: String,
    val supplements: String,
    val schedule: String,
    val costEst: String
)

@Composable
private fun DietPlanCard(
    diet: DietPlanItem,
    isPersian: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = diet.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = diet.phase,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${if (isPersian) "ترکیب خوراک:" else "Feed Formula:"} ${diet.food}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${if (isPersian) "رژیم آب:" else "Water Regimen:"} ${diet.water}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "${if (isPersian) "مکمل‌ها:" else "Supplements:"} ${diet.supplements}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${if (isPersian) "زمان‌بندی:" else "Schedule:"} ${diet.schedule}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "${if (isPersian) "برآورد هزینه:" else "Est. Cost:"} ${diet.costEst}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
private fun NutritionStatBox(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}
