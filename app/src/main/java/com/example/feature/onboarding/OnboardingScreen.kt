package com.example.feature.onboarding

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.common.BirdGender
import com.example.core.common.BudgieVariety
import com.example.core.common.CageType
import com.example.core.localization.AppLanguage
import com.example.data.database.AppDatabase
import com.example.data.database.entity.BirdEntity
import com.example.data.database.entity.CageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object OnboardingManager {
    private const val PREFS_NAME = "aviary_onboarding_prefs"
    private const val KEY_ONBOARDING_COMPLETED = "has_completed_onboarding"

    fun isOnboardingCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean = true) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }
}

enum class OnboardingStep(
    val stepIndex: Int,
    val titleEn: String,
    val titleFa: String,
    val icon: ImageVector
) {
    FACILITY(0, "Facility Information", "اطلاعات سالن پرورش", Icons.Filled.Home),
    CAGES(1, "Cage Setup", "تعریف قفس‌ها و باکس‌ها", Icons.Filled.GridView),
    BIRDS(2, "Breeding Stock", "ثبت پرندگان اولیه", Icons.Filled.Pets),
    PAIRS(3, "Pairs & Compatibility", "جفت‌اندازی مولدین", Icons.Filled.Favorite),
    NESTS(4, "Nests & Reproduction", "لانه‌ها و دوره تکثیر", Icons.Filled.Egg),
    REMINDERS(5, "Smart Reminders", "یادآورهای هوشمند", Icons.Filled.Notifications),
    BACKUP(6, "Encrypted Backup", "پشتیبان‌گیری رمزدار", Icons.Filled.Sync),
    AI(7, "AI Assistant Ready", "دستیار هوش مصنوعی", Icons.Filled.AutoAwesome)
}

@Composable
fun OnboardingScreen(
    currentLanguage: AppLanguage,
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isFa = currentLanguage == AppLanguage.PERSIAN
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val steps = OnboardingStep.entries.toTypedArray()
    val currentStep = steps[currentStepIndex]

    // Form states
    var facilityName by remember { mutableStateOf("سالن تکثیر رویال") }
    var breederCode by remember { mutableStateOf("IR-2026") }
    var initialCageCount by remember { mutableStateOf("4") }
    var sampleMaleRing by remember { mutableStateOf("IR-2026-001") }
    var sampleFemaleRing by remember { mutableStateOf("IR-2026-002") }
    var autoBackupEnabled by remember { mutableStateOf(true) }
    var autoRemindersEnabled by remember { mutableStateOf(true) }

    fun completeOnboardingAndSave() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                // Seed initial cages if needed
                val count = initialCageCount.toIntOrNull() ?: 2
                for (i in 1..count) {
                    val cageCode = "BOX-%02d".format(i)
                    if (db.cageDao().getCageByCode(cageCode) == null) {
                        db.cageDao().insertCage(
                            CageEntity(
                                code = cageCode,
                                type = CageType.BREEDING_BOX,
                                capacity = 2,
                                notes = "Initial breeding box setup via onboarding wizard"
                            )
                        )
                    }
                }
                // Seed flight cage
                if (db.cageDao().getCageByCode("FLIGHT-01") == null) {
                    db.cageDao().insertCage(
                        CageEntity(
                            code = "FLIGHT-01",
                            type = CageType.FLIGHT_CAGE,
                            capacity = 12,
                            notes = "Main juvenile flight cage"
                        )
                    )
                }

                OnboardingManager.setOnboardingCompleted(context, true)
            }
            onFinishOnboarding()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Top Bar: Skip button and Step Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFa) "گام ${currentStepIndex + 1} از ${steps.size}" else "Step ${currentStepIndex + 1} of ${steps.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                TextButton(
                    onClick = {
                        OnboardingManager.setOnboardingCompleted(context, true)
                        onFinishOnboarding()
                    },
                    modifier = Modifier.testTag("onboarding_skip_btn")
                ) {
                    Text(
                        text = if (isFa) "رد کردن و شروع کار" else "Skip to App",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (currentStepIndex + 1).toFloat() / steps.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = currentStep.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = if (isFa) currentStep.titleFa else currentStep.titleEn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isFa) "پیکربندی هوشمند سالن مرغ عشق" else "Budgerigar Breeding Setup Wizard",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Step Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "onboarding_step_content"
                ) { targetStep ->
                    when (targetStep) {
                        OnboardingStep.FACILITY -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = if (isFa) "اطلاعات شناسنامه‌ای و رسمی سالن پرورش خود را وارد کنید:" else "Enter your official aviary profile & breeder details:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                OutlinedTextField(
                                    value = facilityName,
                                    onValueChange = { facilityName = it },
                                    label = { Text(if (isFa) "نام سالن پرورش" else "Aviary Name") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_aviary_name")
                                )
                                OutlinedTextField(
                                    value = breederCode,
                                    onValueChange = { breederCode = it },
                                    label = { Text(if (isFa) "کد و پیشوند رسمی حلقه" else "Breeder Ring Prefix") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_breeder_code")
                                )
                            }
                        }
                        OnboardingStep.CAGES -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = if (isFa) "تعداد باکس‌های استاندارد جفت‌اندازی اولیه سالن را مشخص کنید:" else "Specify initial standard breeding boxes to create:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                OutlinedTextField(
                                    value = initialCageCount,
                                    onValueChange = { initialCageCount = it },
                                    label = { Text(if (isFa) "تعداد باکس‌های تکثیر" else "Number of Breeding Boxes") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("onboarding_cage_count")
                                )
                                Text(
                                    text = if (isFa) "یک قفس پران اختصاصی (FLIGHT-01) نیز جهت پرواز جوجه‌ها به صورت خودکار ساخته خواهد شد." else "A dedicated juvenile flight cage (FLIGHT-01) will also be configured automatically.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        OnboardingStep.BIRDS -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = if (isFa) "ثبت پرندگان مولد پایه با شماره حلقه یکتا:" else "Register initial foundation breeding stock with ring numbers:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                OutlinedTextField(
                                    value = sampleMaleRing,
                                    onValueChange = { sampleMaleRing = it },
                                    label = { Text(if (isFa) "شماره حلقه نر پایه (Cock)" else "Founder Cock Ring #") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = sampleFemaleRing,
                                    onValueChange = { sampleFemaleRing = it },
                                    label = { Text(if (isFa) "شماره حلقه ماده پایه (Hen)" else "Founder Hen Ring #") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        OnboardingStep.PAIRS -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = if (isFa) "جفت‌اندازی اصولی و پیوند با باکس تکثیر:" else "Pairing & Breeding Box Assignment:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = if (isFa) "سازگاری ژنتیکی خودکار" else "Automated Inbreeding Safeguard",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isFa) "هنگام ایجاد جفت، ضریب هم‌خونی Wright به صورت آنی محاسبه می‌شود تا از نقایص ژنتیکی جلوگیری گردد." else "When forming pairs, Wright's inbreeding coefficient is calculated instantly to protect offspring health.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                        OnboardingStep.NESTS -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = if (isFa) "ثبت دوره تکثیر، تخم‌ها و فرآیند هچ:" else "Nests, Clutches & Incubation Timelines:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (isFa)
                                        "• فاصله تخم‌گذاری: یک روز در میان\n• نطفه‌سنجی: روز ۵ تا ۷\n• هچ جوجه: روز ۱۸ پس از شروع خوابیدن روی تخم\n• حلقه‌گذاری: روز ۶ تا ۸ پس از تولد جوجه"
                                    else
                                        "• Lay interval: Every 48 hours\n• Candling: Day 5 to 7\n• Expected Hatch: Day 18\n• Closed Band Ringing: Day 6 to 8 post-hatch",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        OnboardingStep.REMINDERS -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = if (isFa) "سیستم یادآورهای هوشمند و هشدارهای تکثیر:" else "Automated Smart Breeding Reminders:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = if (isFa) "زمان‌بندی خودکار وظایف" else "Automatic Task Scheduling",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isFa) "با ثبت هر تخم، یادآور نطفه‌سنجی و حلقه‌گذاری جوجه به صورت خودکار به تقویم اضافه می‌گردد." else "Logging an egg automatically schedules candling and ringing reminders on your device.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                        OnboardingStep.BACKUP -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = if (isFa) "پشتیبان‌گیری رمزگذاری‌شده با کلید AES-256:" else "AES-256 Encrypted Offline & Cloud Backups:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = if (isFa) "پشتیبان‌گیری خودکار روزانه" else "Daily Automatic Snapshots",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isFa) "نسخه‌های پشتیبان به صورت خودکار با نگهداری ۷ روزه در حافظه دستگاه ذخیره می‌شوند." else "Automatic local snapshots are captured daily with 7-day rolling retention and one-click restore.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                        OnboardingStep.AI -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = if (isFa) "دستیار هوش مصنوعی آماده پاسخگویی و ثبت با عکس است:" else "Google Gemini AI Assistant is configured & ready:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = if (isFa) "پاسخ به سوالات تکثیر و ثبت از عکس" else "Breeding Advice & Photo Auto-Entry",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isFa) "از بخش دستیار هوشمند می‌توانید سوالات درمانی بپرسید یا عکس پرنده را برای پر کردن خودکار فرم بارگذاری کنید." else "Ask medical/dietary questions or scan a bird photograph to auto-populate variety and gender.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Actions: Back / Next / Finish
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStepIndex > 0) {
                    OutlinedButton(
                        onClick = { currentStepIndex-- },
                        modifier = Modifier.testTag("onboarding_back_btn")
                    ) {
                        Text(if (isFa) "قبلی" else "Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (currentStepIndex < steps.size - 1) {
                    Button(
                        onClick = { currentStepIndex++ },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("onboarding_next_btn")
                    ) {
                        Text(if (isFa) "مرحله بعد" else "Next Step")
                    }
                } else {
                    Button(
                        onClick = { completeOnboardingAndSave() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("onboarding_finish_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isFa) "تکمیل و ورود به برنامه" else "Complete & Enter Aviary")
                    }
                }
            }
        }
    }
}
