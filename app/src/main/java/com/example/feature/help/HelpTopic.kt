package com.example.feature.help

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.graphics.vector.ImageVector

enum class HelpCategory(val titleEn: String, val titleFa: String) {
    GETTING_STARTED("Getting Started", "شروع و راه‌اندازی"),
    BREEDING("Breeding & Genetics", "تکثیر و ژنتیک"),
    CARE("Care & Nutrition", "مراقبت، تغذیه و سلامت"),
    BUSINESS("Business & Analytics", "مدیریت مالی و گزارش‌ها"),
    SYSTEM("System, AI & Security", "سیستم، هوش مصنوعی و امنیت")
}

data class HelpTopic(
    val id: String,
    val category: HelpCategory,
    val icon: ImageVector,
    val titleEn: String,
    val titleFa: String,
    val summaryEn: String,
    val summaryFa: String,
    val contentEn: String,
    val contentFa: String,
    val tags: List<String>
)

object HelpContentRepository {

    val topics: List<HelpTopic> = listOf(
        HelpTopic(
            id = "getting_started",
            category = HelpCategory.GETTING_STARTED,
            icon = Icons.Filled.HelpOutline,
            titleEn = "Getting Started & Aviary Setup",
            titleFa = "شروع به کار و راه‌اندازی سالن",
            summaryEn = "Learn how to configure your aviary, set up cages, and register initial breeding stock.",
            summaryFa = "آموزش اولیه پیکربندی سالن، تعریف باکس‌ها و قفس‌ها و ثبت پرندگان مولد اولیه.",
            contentEn = """
                Welcome to Budgie Aviary Manager! Follow these initial steps to set up your facility:
                1. Configure Facility Details: Set your official aviary name, registration code, and preferences in Settings.
                2. Add Cages: Register your breeding boxes, flight cages, stock cages, and quarantine areas with their capacities.
                3. Register Birds: Input your breeding stock with official ring numbers, colors/mutations, and cage assignments.
                4. Pair Birds: Establish bonded breeding pairs and assign them to dedicated breeding boxes with nest boxes.
                5. Monitor Cycles: Track egg lay dates, candling, hatch dates, chick ringing, and weaning effortlessly.
            """.trimIndent(),
            contentFa = """
                به سامانه تخصصی مدیریت سالن مرغ عشق خوش آمدید! جهت راه‌اندازی گام‌های زیر را دنبال کنید:
                ۱. تنظیم مشخصات سالن: نام سالن، کد رسمی پرورش‌دهنده و تنظیمات را در بخش تنظیمات وارد کنید.
                ۲. ثبت قفس‌ها و باکس‌ها: باکس‌های جوجه‌کشی، پران‌ها و قفس‌های قرنطینه را همراه با ظرفیت تعریف کنید.
                ۳. ثبت پرندگان: پرندگان مولد را با شماره حلقه رسمی، جهش‌های رنگی و قفس مربوطه ثبت نمایید.
                ۴. جفت‌اندازی: جفت‌های مولد را ایجاد کرده و به باکس‌های تکثیر اختصاص دهید.
                ۵. پیگیری دوره‌ها: تاریخ‌های تخم‌گذاری، نطفه‌سنجی، هچ، حلقه‌گذاری جوجه‌ها و استقلال را مدیریت کنید.
            """.trimIndent(),
            tags = listOf("start", "setup", "intro", "guide", "راهنما", "شروع", "سالن", "آموزش")
        ),
        HelpTopic(
            id = "birds",
            category = HelpCategory.GETTING_STARTED,
            icon = Icons.Filled.Pets,
            titleEn = "Bird Registry & Profiles",
            titleFa = "مدیریت و ثبت مشخصات پرندگان",
            summaryEn = "Registering ring numbers, varieties, lineage, mutations, and status.",
            summaryFa = "ثبت شناسنامه، شماره حلقه رسمی، جهش‌های رنگی، شجره و وضعیت پرنده.",
            contentEn = """
                The Bird Registry is the core of your facility:
                - Ring Number: Must be unique per bird (e.g., IR-2026-001). Ring numbers cannot be duplicated.
                - Varieties & Mutations: Choose from English Show, Australian Standard, Crested, Hagoromo, Opaline, Spangle, Lutino, etc.
                - Status Management: Mark birds as Active, Breeding, Resting, Quarantine, Sold, or Deceased.
                - Pedigree Linking: Specify sire (father) and dam (mother) to automatically compute genetic ancestry and inbreeding coefficients.
            """.trimIndent(),
            contentFa = """
                بخش پرندگان قلب تپنده سالن شماست:
                - شماره حلقه: شناسه یکتای هر پرنده است (مانند IR-2026-001) و تکرار آن مجاز نیست.
                - گونه و جهش: انگلیسی نمایشی، معمولی/استرالیایی، کاکلی، هاگورومو، اسپانگل، اوپالین، لوتینو و غیره.
                - وضعیت پرنده: فعال، در حال جفت‌گیری، استراحت، قرنطینه، فروخته شده یا تلف شده.
                - پیوند شجره‌نامه: با تعیین پدر و مادر، ضرایب هم‌خونی و نمودار نسبی به صورت خودکار محاسبه می‌گردد.
            """.trimIndent(),
            tags = listOf("birds", "ring", "mutations", "status", "پرنده", "حلقه", "مولد", "ثبت")
        ),
        HelpTopic(
            id = "cages",
            category = HelpCategory.GETTING_STARTED,
            icon = Icons.Filled.GridView,
            titleEn = "Cages & Aviary Management",
            titleFa = "مدیریت قفس‌ها، پران‌ها و باکس‌ها",
            summaryEn = "Manage breeding boxes, flight cages, stock cages, and capacity tracking.",
            summaryFa = "تعریف باکس‌های تکثیر، قفس‌های پران، قرنطینه و پایش تراکم و ظرفیت.",
            contentEn = """
                Organize your facility layout:
                - Cage Types: Breeding Box (capacity 2), Flight Cage (for juveniles and exercise), Stock Cage, and Quarantine Cage.
                - Occupancy Alerts: The system warns you when a cage exceeds its recommended capacity.
                - Sanitation Tracking: Log cage cleanings and disinfection dates to prevent bacterial outbreaks.
            """.trimIndent(),
            contentFa = """
                سازماندهی محیط فیزیکی سالن:
                - انواع قفس: باکس تکثیر (ظرفیت ۲ پرنده)، پران (جهت پرواز جوجه‌ها و تقویت عضلات)، نگهداری و قرنطینه.
                - هشدار تراکم: سامانه در صورت پر شدن ظرفیت قفس به شما هشدار می‌دهد.
                - نظافت و ضدعفونی: تاریخ‌های شستشو و ضدعفونی را ثبت کنید تا از بیماری‌ها پیشگیری شود.
            """.trimIndent(),
            tags = listOf("cages", "boxes", "flight", "aviary", "قفس", "باکس", "پران", "قرنطینه")
        ),
        HelpTopic(
            id = "pairs",
            category = HelpCategory.BREEDING,
            icon = Icons.Filled.Favorite,
            titleEn = "Breeding Pairs & Compatibility",
            titleFa = "تشکیل جفت‌ها و سازگاری مولدین",
            summaryEn = "Forming pairs, inbreeding coefficient checks, and box assignment.",
            summaryFa = "جفت‌اندازی اصولی، بررسی ضریب هم‌خونی و اختصاص باکس اختصاصی.",
            contentEn = """
                Guidelines for successful pairing:
                - Mating Compatibility: Select sexually mature cocks (10+ months) and hens (12+ months).
                - Inbreeding Check: The app calculates the Wright's Inbreeding Coefficient (F) before pairing to prevent lethal genes.
                - Cage Assignment: Pair records link directly to breeding boxes and nesting boxes.
            """.trimIndent(),
            contentFa = """
                اصول جفت‌اندازی موفق:
                - سن و آمادگی: نرها بالای ۱۰ ماه و ماده‌ها بالای ۱۲ ماه باشند.
                - ضریب هم‌خونی: سامانه ضریب هم‌خونی را قبل از جفت‌اندازی محاسبه می‌کند تا از تلفات ژنتیکی جلوگیری شود.
                - اختصاص باکس: هر جفت به یک باکس تکثیر همراه با لانه چوبی اختصاص می‌یابد.
            """.trimIndent(),
            tags = listOf("pairs", "breeding", "mating", "inbreeding", "جفت", "جفت‌اندازی", "سازگاری")
        ),
        HelpTopic(
            id = "reproduction",
            category = HelpCategory.BREEDING,
            icon = Icons.Filled.Egg,
            titleEn = "Reproduction & Clutches",
            titleFa = "دوره‌های تکثیر و لانه‌داری",
            summaryEn = "Managing clutches, mating dates, rest intervals, and clutch limits.",
            summaryFa = "مدیریت کلاچ‌ها، تاریخ‌های جفت‌گیری، دوره‌های استراحت و سقف کلاچ سالانه.",
            contentEn = """
                Clutch management protocols:
                - Maximum Clutches: Do not allow hens to lay more than 2-3 clutches per year to prevent calcium depletion.
                - Incubation Period: 18 days from the start of steady incubation (typically beginning with the second egg).
                - Rest Periods: Provide at least 3 months of rest in flight cages between breeding seasons.
            """.trimIndent(),
            contentFa = """
                پروتکل‌های مدیریت دوره تکثیر:
                - سقف کلاچ: بیش از ۲ الی ۳ دوره در سال به ماده اجازه تخم‌گذاری ندهید تا دچار کمبود کلسیم نشود.
                - دوره خوابیدن روی تخم: ۱۸ روز از زمان شروع خوابیدن مداوم (معمولاً از تخم دوم).
                - استراحت: بین دوره‌ها حداقل ۳ ماه استراحت در پران همراه با تغذیه مقوی فراهم کنید.
            """.trimIndent(),
            tags = listOf("reproduction", "clutch", "incubation", "تکثیر", "لانه", "دوره", "تخم‌گذاری")
        ),
        HelpTopic(
            id = "eggs",
            category = HelpCategory.BREEDING,
            icon = Icons.Filled.Egg,
            titleEn = "Egg Registry & Candling",
            titleFa = "دفترچه تخم‌ها و نطفه‌سنجی (Candling)",
            summaryEn = "Candling procedures, fertility logs, and expected hatch calculations.",
            summaryFa = "روش صحیح نطفه‌سنجی، ثبت باروری تخم‌ها و محاسبه تاریخ دقیق هچ.",
            contentEn = """
                Egg tracking & candling guidelines:
                - Egg Laying Interval: Budgies lay an egg every 48 hours (every other day).
                - Candling Schedule: Candle eggs on Day 5-7 post-lay using a dedicated cool-light candler.
                - Red spider-web veins indicate fertility; clear yellow indicates infertile (clear) eggs.
                - Status options: Laid, Fertile, Infertile, Hatched, Broken, or Abandoned.
            """.trimIndent(),
            contentFa = """
                راهنمای ثبت و نطفه‌سنجی تخم‌ها:
                - فاصله تخم‌گذاری: مرغ عشق‌ها معمولاً یک روز در میان تخم می‌گذارند.
                - زمان کندلینگ: روز ۵ تا ۷ پس از تخم‌گذاری با چراغ نطفه‌سنج سرد بررسی کنید.
                - رگه‌های عنکبوتی قرمز نشان‌دهنده نطفه‌داری است؛ شفاف بودن تخم نشانگر بی‌نطفه بودن است.
                - وضعیت‌ها: گذاشته شده، نطفه‌دار، بدون نطفه، هچ شده، شکسته یا رها شده.
            """.trimIndent(),
            tags = listOf("eggs", "candling", "fertility", "hatch", "تخم", "نطفه‌سنجی", "کندلینگ", "هچ")
        ),
        HelpTopic(
            id = "chicks",
            category = HelpCategory.BREEDING,
            icon = Icons.Filled.ChildCare,
            titleEn = "Chick Care & Ringing",
            titleFa = "مراقبت از جوجه‌ها و حلقه‌گذاری",
            summaryEn = "Official ringing age, growth curves, crop checks, and weaning.",
            summaryFa = "سن استاندارد انداختن حلقه، منحنی رشد وزنی، کنترل چینه‌دان و استقلال جوجه.",
            contentEn = """
                Chick growth & closed-band ringing:
                - Ringing Window: Closed rings must be placed between Day 6 and Day 8 post-hatch.
                - Crop Monitoring: Verify parents are feeding adequately with crop milk and softened seed.
                - Weaning: Chicks exit the nest around Day 30-35 and achieve full independence by Day 42.
                - 1-Click Promotion: Promote thriving chicks directly to adult Bird records when independent.
            """.trimIndent(),
            contentFa = """
                مراقبت از جوجه‌ها و انداختن حلقه بسته:
                - زمان حلقه‌گذاری: حلقه رسمی بسته باید بین روز ۶ تا ۸ پس از هچ انداخته شود.
                - کنترل چینه‌دان: بررسی کنید والدین شیره چینه‌دان و دانه نرم را به خوبی تغذیه می‌کنند.
                - خروج از لانه: جوجه‌ها در روز ۳۰ تا ۳۵ از لانه خارج شده و تا روز ۴۲ کاملاً دانه خور و مستقل می‌شوند.
                - تبدیل سریع به پرنده: با یک کلیک جوجه مستقل شده را به عنوان پرنده بالغ با شماره حلقه در سیستم ثبت کنید.
            """.trimIndent(),
            tags = listOf("chicks", "ringing", "weaning", "growth", "جوجه", "حلقه", "حلقه‌گذاری", "دانه‌خور")
        ),
        HelpTopic(
            id = "genetics",
            category = HelpCategory.BREEDING,
            icon = Icons.Filled.Biotech,
            titleEn = "Genetics & Mutation Calculator",
            titleFa = "ژنتیک و ماشین‌حساب جهش‌های رنگی",
            summaryEn = "Dominant, recessive, sex-linked genes, and offspring outcome probabilities.",
            summaryFa = "محاسبه احتمالات ژنتیکی غالب، مغلوب، وابسته به جنس و پیش‌بینی رنگ جوجه‌ها.",
            contentEn = """
                Understand Budgie Color Genetics:
                - Dominant: Normal Green, Dominant Pied, Spangle (requires only one copy to express).
                - Autosomal Recessive: Blue, Recessive Pied, Dilute, Fallow (requires 2 copies or splits).
                - Sex-Linked: Opaline, Cinnamon, Ino (Lutino/Albino), Texas Clearbody (males can split, females cannot).
                - Outcome Predictor: Select Sire and Dam mutations to see visual and split probabilities for cocks and hens.
            """.trimIndent(),
            contentFa = """
                آشنایی با ژنتیک رنگ مرغ عشق:
                - جهش‌های غالب: سبز معمولی، ابلق غالب، اسپانگل (با یک ژن ظاهر می‌شود).
                - جهش‌های مغلوب: آبی، ابلق مغلوب، دایلوت، فالو (نیاز به دو ژن دارد).
                - وابسته به جنس: اوپالین، دارچینی (سینامون)، اینو (لوتینو/آلبینو)، کلیربادی (نرها می‌توانند ناقل باشند، ماده‌ها خیر).
                - پیش‌بینی رنگ: جهش‌های نر و ماده را انتخاب کنید تا درصد احتمالات جوجه‌های نر و ماده نمایش داده شود.
            """.trimIndent(),
            tags = listOf("genetics", "mutations", "opaline", "spangle", "ino", "ژنتیک", "رنگ", "اوپالین", "اسپانگل")
        ),
        HelpTopic(
            id = "pedigree",
            category = HelpCategory.BREEDING,
            icon = Icons.Filled.AccountTree,
            titleEn = "Pedigree & Family Trees",
            titleFa = "شجره‌نامه و تبارشناسی",
            summaryEn = "Multi-generation lineage trees, ancestor tracking, and Wright's coefficient.",
            summaryFa = "نمودار چند نسلی تبار، بررسی اجداد و محاسبه ضرایب هم‌خونی نسلی.",
            contentEn = """
                Explore multi-generational pedigrees:
                - Ancestry Mapping: Displays 3 to 5 generations of ancestors (Parents, Grandparents, Great-Grandparents).
                - Inbreeding Alert: Highlights duplicate ancestors to prevent inbreeding depression and genetic faults.
                - Official PDF Export: Generate standardized pedigree certificates with breeder signatures and logos.
            """.trimIndent(),
            contentFa = """
                بررسی شجره‌نامه چند نسلی:
                - تبارشناسی: نمایش ۳ تا ۵ نسل از اجداد پرنده (والدین، پدربزرگ/مادربزرگ و نسل‌های قبل).
                - هشدار هم‌خونی: اجداد مشترک را مشخص می‌کند تا از تضعیف نژاد جلوگیری شود.
                - خروجی رسمی PDF: گواهی شجره‌نامه با مشخصات حلقه، افتخارات و امضای پرورش‌دهنده ایجاد کنید.
            """.trimIndent(),
            tags = listOf("pedigree", "lineage", "ancestors", "tree", "شجره‌نامه", "اجداد", "تبار", "نسب")
        ),
        HelpTopic(
            id = "health",
            category = HelpCategory.CARE,
            icon = Icons.Filled.MedicalServices,
            titleEn = "Health, Medical & Quarantine",
            titleFa = "بهداشت، درمان و قرنطینه",
            summaryEn = "Treatment regimens, weight tracking, quarantine protocols, and dosage logs.",
            summaryFa = "ثبت پروتکل‌های درمانی، توزین، قرنطینه پرندگان جدید و دوز داروهای مصرفی.",
            contentEn = """
                Facility healthcare protocols:
                - Medical Records: Log symptoms, diagnosis, veterinarian notes, and prescribed medications.
                - Quarantine Tracking: Keep track of new arrivals or ill birds isolated in quarantine cages.
                - Weight Logs: Regular weighing (in grams) is the earliest detector of illness; a 10% drop requires immediate attention.
            """.trimIndent(),
            contentFa = """
                پروتکل‌های سلامت سالن:
                - پرونده درمانی: علائم، تشخیص، تجویز دامپزشک و داروهای مصرفی را ثبت کنید.
                - مدیریت قرنطینه: پرندگان تازه‌وارد یا بیمار را تا بهبودی کامل در بخش قرنطینه ثبت نمایید.
                - توزین منظم: کاهش وزن ۱۰ درصدی اولین نشانه بیماری است؛ وزن پرندگان را مرتب ثبت کنید.
            """.trimIndent(),
            tags = listOf("health", "medicine", "quarantine", "weight", "سلامت", "دارو", "درمان", "قرنطینه", "وزن")
        ),
        HelpTopic(
            id = "nutrition",
            category = HelpCategory.CARE,
            icon = Icons.Filled.Restaurant,
            titleEn = "Nutrition & Diet Schedules",
            titleFa = "تغذیه، جیره‌ها و مکمل‌ها",
            summaryEn = "Breeding diets, egg food recipes, sprouted seeds, vitamins, and minerals.",
            summaryFa = "جیره دوره تکثیر، غذای تخم‌مرغی، جوانه دانه‌ها، ویتامین‌ها و مواد معدنی.",
            contentEn = """
                Optimal nutrition programs:
                - Seed Mix: High-quality canary seed, white/yellow millet, red millet, and oats.
                - Egg Food & Softfood: Provide daily during breeding and chick-rearing for essential protein.
                - Sprouted Seeds: Boost fertility, vitamin E, and enzyme activity.
                - Minerals: Continuous access to cuttlebone, mineral blocks, and soluble calcium during laying.
            """.trimIndent(),
            contentFa = """
                برنامه‌ریزی اصولی تغذیه:
                - مخلوط دانه: ارزن، کتان، تخم کاهو، نیجر و جو دوسر متناسب با فصل.
                - غذای نرم و تخم‌مرغی: در دوره جوجه‌داری روزانه جهت تأمین پروتئین در اختیار جفت‌ها قرار گیرد.
                - جوانه دانه‌ها: افزایش دهنده قوی باروری، ویتامین E و آنزیم‌های فعال هضم.
                - مکمل و مواد معدنی: کف دریا، بلوک معدنی و کلسیم مایع در آب برای پیشگیری از گیر کردن تخم.
            """.trimIndent(),
            tags = listOf("nutrition", "diet", "seed", "calcium", "تغذیه", "جیره", "ویتامین", "کلسیم", "دانه")
        ),
        HelpTopic(
            id = "inventory",
            category = HelpCategory.CARE,
            icon = Icons.Filled.Inventory2,
            titleEn = "Inventory & Logistics",
            titleFa = "انبار، ملزومات و تجهیزات",
            summaryEn = "Track feed, supplements, rings, nest boxes, and minimum stock alerts.",
            summaryFa = "مدیریت موجودی دان، داروها، حلقه‌ها، پوشال لانه و هشدارهای اتمام کالا.",
            contentEn = """
                Stock management features:
                - Inventory Items: Track seeds (kg), medications (ml), closed rings (pcs), and nest materials.
                - Low Stock Alerts: Set minimum thresholds to receive dashboard notifications before stock runs out.
                - Expiration Tracking: Track expiration dates for vitamins and medications to maintain efficacy.
            """.trimIndent(),
            contentFa = """
                امکانات مدیریت انبار:
                - دسته‌بندی اقلام: دان و مکمل (کیلوگرم)، داروها (میلی‌لیتر)، حلقه‌ها (عدد) و ملزومات لانه.
                - هشدار کسری موجودی: با تعیین حداقل موجودی، قبل از اتمام کالا روی داشبورد هشدار دریافت کنید.
                - تاریخ انقضا: تاریخ انقضای مکمل‌ها و داروها را ثبت کنید تا از داروی منقضی استفاده نشود.
            """.trimIndent(),
            tags = listOf("inventory", "stock", "supplies", "rings", "انبار", "موجودی", "ملزومات", "تجهیزات")
        ),
        HelpTopic(
            id = "finance",
            category = HelpCategory.BUSINESS,
            icon = Icons.Filled.AccountBalance,
            titleEn = "Finance & Aviary Accounting",
            titleFa = "امور مالی، درآمد و هزینه‌ها",
            summaryEn = "Track bird sales, show entries, feed costs, equipment expenses, and profit margins.",
            summaryFa = "ثبت فروش پرندگان، هزینه‌های دان و دارو، تجهیزات سالن و محاسبه سود خالص.",
            contentEn = """
                Keep your aviary financially profitable:
                - Incomes: Log bird sales, stud fees, show prizes, and equipment disposals.
                - Expenses: Track purchases of feed, vitamins, cages, rings, vet bills, and utility expenses.
                - Financial Summary: View net profits, monthly breakdown charts, and category summaries.
            """.trimIndent(),
            contentFa = """
                مدیریت سودآوری و حسابداری سالن:
                - درآمدها: ثبت فروش پرندگان، جوایز مسابقات و ارائه خدمات سالن.
                - هزینه‌ها: ثبت خرید دان، مکمل‌ها، قفس‌ها، حلقه‌ها، ویزیت دامپزشک و قبوض سالن.
                - تراز و گزارش: مشاهده سود خالص، نمودارهای تفکیکی ماهانه و سود سالانه.
            """.trimIndent(),
            tags = listOf("finance", "income", "expense", "accounting", "مالی", "درآمد", "هزینه", "فروش", "سود")
        ),
        HelpTopic(
            id = "competitions",
            category = HelpCategory.BUSINESS,
            icon = Icons.Filled.EmojiEvents,
            titleEn = "Competitions & Show Standards",
            titleFa = "مسابقات، نمایشگاه‌ها و استانداردها",
            summaryEn = "Official show judging points, awards, exhibition cages, and show condition logs.",
            summaryFa = "امتیازدهی استانداردهای نمایشی (WBO)، ثبت افتخارات، قفس‌های استاندارد مسابقه.",
            contentEn = """
                Prepare champion show birds:
                - Official Standards: World Budgerigar Organisation (WBO) standards for head size, spots, posture, and feathering.
                - Competition Records: Record event names, judges, classes, placement (1st, Best of Show), and awards.
                - Condition Score: Score show condition from 0-100 to time peak condition for exhibition day.
            """.trimIndent(),
            contentFa = """
                آماده‌سازی پرندگان قهرمان مسابقات:
                - استانداردهای بین‌المللی: استانداردهای WBO برای اندازه سر، ماسک، خال‌ها، استقرار روی چوب و پوشش پر.
                - ثبت افتخارات: ثبت نام مسابقه، داوران، رتبه کسب‌شده (مقام اول، قهرمان سالن) و جوایز.
                - ارزیابی آمادگی: امتیازدهی آمادگی پرنده از ۰ تا ۱۰۰ جهت زمان‌بندی دقیق اوج پرنده در روز نمایشگاه.
            """.trimIndent(),
            tags = listOf("competitions", "shows", "awards", "wbo", "مسابقه", "نمایشگاه", "استاندارد", "جوایز")
        ),
        HelpTopic(
            id = "ai_assistant",
            category = HelpCategory.SYSTEM,
            icon = Icons.Filled.AutoAwesome,
            titleEn = "AI Aviary Assistant (Gemini)",
            titleFa = "دستیار هوشمند سالن (Gemini)",
            summaryEn = "Ask specialized breeding questions, health diagnoses, diet plans, and pair suggestions.",
            summaryFa = "مشاوره تخصصی تکثیر، پیشنهاد جفت‌اندازی ژنتیکی، راهنمای تغذیه و درمان به کمک هوش مصنوعی.",
            contentEn = """
                Leverage server-side Google Gemini AI:
                - Expert Advice: Ask questions regarding budgie illness symptoms, rare color genetics, and incubation anomalies.
                - Pairing Recommendations: Request pairing suggestions based on your active flock inventory.
                - Privacy & Speed: Integrated via secure server-side proxy without leaking personal credentials.
            """.trimIndent(),
            contentFa = """
                بهره‌گیری از هوش مصنوعی سرور-ساید گوگل جمینای:
                - مشاوره تخصصی: پرسش و پاسخ پیرامون بیماری‌ها، جهش‌های نادر رنگی و مشکلات جوجه‌کشی.
                - پیشنهاد جفت‌اندازی: دریافت بهترین ترکیبات جفت‌اندازی بر اساس موجودی پرندگان سالن شما.
                - امنیت و سرعت: اتصال امن بدون افشای کلیدهای محرمانه شما.
            """.trimIndent(),
            tags = listOf("ai", "gemini", "assistant", "smart", "هوش مصنوعی", "جمینای", "دستیار", "مشاور")
        ),
        HelpTopic(
            id = "ai_photo_registration",
            category = HelpCategory.SYSTEM,
            icon = Icons.Filled.QrCodeScanner,
            titleEn = "AI Photo Registration & Inspection",
            titleFa = "ثبت هوشمند پرنده از روی تصویر",
            summaryEn = "Automatic mutation detection, cere gender detection, and feather assessment via AI.",
            summaryFa = "تشخیص خودکار جهش رنگی، جنسیت از روی پوزه (سر) و کیفیت پرها از روی عکس.",
            contentEn = """
                Speed up bird entry with image recognition:
                - Visual Analysis: Snap a photo of your budgie to auto-detect variety, base color (green/blue series), and markings.
                - Cere Inspection: Evaluates cere color (blue/pink for cocks, brown/white for hens) to predict gender.
                - Fast Autofill: Populates the bird registration form for quick verification and saving.
            """.trimIndent(),
            contentFa = """
                ثبت سریع پرندگان با اسکن و پردازش تصویر:
                - تحلیل بصری: عکس پرنده را بارگذاری کنید تا جهش رنگی، سری رنگ و طرح پرها به طور خودکار استخراج شود.
                - بررسی پوزه (سر): تحلیل رنگ پوزه جهت پیش‌بینی دقیق جنسیت نر یا ماده.
                - پر کردن خودکار فرم: فیلدهای ثبت پرنده را به صورت خودکار مقداردهی کرده و با یک تأیید ذخیره کنید.
            """.trimIndent(),
            tags = listOf("photo", "camera", "ai", "vision", "عکس", "دوربین", "اسکن", "تشخیص")
        ),
        HelpTopic(
            id = "reports",
            category = HelpCategory.BUSINESS,
            icon = Icons.Filled.BarChart,
            titleEn = "Reports & Analytics",
            titleFa = "گزارش‌ها و آمارهای مدیریتی",
            summaryEn = "Fertility rates, hatch success percentages, monthly financial metrics, and flock summaries.",
            summaryFa = "درصد باروری تخم‌ها، نرخ موفقیت هچ، سودآوری ماهانه و نمودارهای تحلیلی گله.",
            contentEn = """
                Analyze aviary productivity:
                - Breeding Analytics: Fertility rates, hatch rates, chick survival metrics, and seasonal comparisons.
                - Population Breakdown: Variety distribution, active pairs, and cage occupancy percentages.
                - Filter by Date: Select customized date ranges (last month, breeding season, full year).
            """.trimIndent(),
            contentFa = """
                تحلیل جامع بازدهی سالن:
                - آمار تکثیر: درصد نطفه‌داری تخم‌ها، نرخ موفقیت هچ، درصد بقای جوجه‌ها و مقایسه فصول.
                - ترکیب جمعیتی: نمودار تفکیکی گونه‌ها، تنوع رنگی و درصد اشغال باکس‌ها.
                - فیلتر زمانی: بررسی عملکرد سالن در بازه‌های ماهانه، دوره تکثیر و سالانه.
            """.trimIndent(),
            tags = listOf("reports", "analytics", "charts", "statistics", "گزارش", "نمودار", "آمار", "بهره‌وری")
        ),
        HelpTopic(
            id = "pdf_excel",
            category = HelpCategory.BUSINESS,
            icon = Icons.Filled.PictureAsPdf,
            titleEn = "PDF & Excel Exporting",
            titleFa = "تولید گواهی PDF و خروجی اکسل",
            summaryEn = "Generate official pedigree certificates, egg logs, flock lists, and print-ready sheets.",
            summaryFa = "صدور شناسنامه رسمی شجره‌نامه PDF، دفترچه تخم‌ها و خروجی جدول اطلاعات.",
            contentEn = """
                Export official documents:
                - Pedigree Certificate: Generates a high-resolution, branded PDF pedigree ready for printing and sharing.
                - Flock Registry: Export all registered birds with full characteristics and lineage in CSV/PDF formats.
                - Print-Ready Breeding Cards: Print cage cards to hang directly on physical breeding boxes.
            """.trimIndent(),
            contentFa = """
                خروجی اسناد رسمی و استاندارد:
                - گواهی شجره‌نامه PDF: تولید فایل باکیفیت و رسمی همراه با نشان سالن، آماده چاپ و ارسال به خریدار.
                - خروجی لیست پرندگان: دریافت فایل کامل مشخصات و شجره پرندگان.
                - کارت باکس تکثیر: چاپ کارت مشخصات جفت جهت نصب روی باکس‌های فیزیکی سالن.
            """.trimIndent(),
            tags = listOf("pdf", "excel", "export", "print", "پی‌دی‌اف", "اکسل", "چاپ", "خروجی", "گواهی")
        ),
        HelpTopic(
            id = "reminders",
            category = HelpCategory.SYSTEM,
            icon = Icons.Filled.Notifications,
            titleEn = "Reminders & Notifications",
            titleFa = "یادآورها و اعلان‌های هوشمند",
            summaryEn = "Automated alerts for egg candling, banding day, cage disinfection, and medication schedules.",
            summaryFa = "یادآور خودکار نطفه‌سنجی، روز انداختن حلقه جوجه، نظافت باکس‌ها و نوبت داروها.",
            contentEn = """
                Never miss an essential breeding event:
                - Automated Breeding Reminders: The system automatically schedules Day 6 candling and Day 7 ringing when eggs are logged.
                - Custom Tasks: Set recurring tasks for water changes, vitamin supplements, and deep cleaning.
                - System Notifications: Delivered promptly on your device even when the app is closed.
            """.trimIndent(),
            contentFa = """
                عدم فراموشی رویدادهای حساس تکثیر:
                - یادآور خودکار تکثیر: با ثبت تخم، یادآور روز ۶ (نطفه‌سنجی) و روز ۷ (حلقه‌گذاری) خودکار ایجاد می‌شود.
                - یادآورهای سفارشی: تعریف وظایف دوره‌ای تعویض آب، ارائه ویتامین و ضدعفونی کلی سالن.
                - اعلان‌های سیستمی: ارسال به موقع پیام حتی در زمان بسته بودن برنامه.
            """.trimIndent(),
            tags = listOf("reminders", "notifications", "alerts", "tasks", "یادآور", "اعلان", "وظایف", "هشدار")
        ),
        HelpTopic(
            id = "security",
            category = HelpCategory.SYSTEM,
            icon = Icons.Filled.Lock,
            titleEn = "Security, PIN & Role Permissions",
            titleFa = "امنیت، پین‌کد و سطوح دسترسی",
            summaryEn = "Protect your aviary data with 4-6 digit PINs, auto-lock timeouts, and multi-user roles.",
            summaryFa = "حفاظت از اطلاعات با پین‌کد، قفل خودکار و تعیین سطوح دسترسی مدیر، تکثیرکننده و دستیار.",
            contentEn = """
                Keep your commercial breeding data secure:
                - PIN Protection: Set up a 4 to 6 digit Master PIN to lock access to sensitive financial and breeder data.
                - Auto-Lock: Configurable inactivity timeouts (1 min, 5 min, 15 min) or immediate background lock.
                - Role-Based Access: Assign Owner, Breeder, or Caretaker roles to limit permissions on shared devices.
                - Audit Logging: Tracks all security events, failed unlock attempts, and critical data modifications.
            """.trimIndent(),
            contentFa = """
                حفاظت کامل از اطلاعات تجاری سالن:
                - قفل پین‌کد: تعیین رمز ۴ الی ۶ رقمی جهت محافظت از اطلاعات مالی و شجره‌های اختصاصی.
                - قفل خودکار: تنظیم زمان قفل در صورت عدم فعالیت (۱ دقیقه، ۵ دقیقه، ۱۵ دقیقه) یا خروج از برنامه.
                - سطوح دسترسی: تعریف نقش‌های مدیر سالن، مسئول تکثیر و کارگر سالن در دستگاه‌های اشتراکی.
                - لاگ رویدادها: ثبت دقیق تلاش‌های ورود، تغییرات امنیتی و اصلاحات کلیدی.
            """.trimIndent(),
            tags = listOf("security", "pin", "lock", "roles", "audit", "امنیت", "پین", "رمز", "دسترسی", "لاگ")
        ),
        HelpTopic(
            id = "backup",
            category = HelpCategory.SYSTEM,
            icon = Icons.Filled.Sync,
            titleEn = "Encrypted Backup, Restore & Sync",
            titleFa = "پشتیبان‌گیری رمزنگاری، بازیابی و همگام‌سازی",
            summaryEn = "AES-256-GCM encrypted snapshots, device transfer, and offline sync with conflict resolution.",
            summaryFa = "پشتیبان‌گیری رمزگذاری‌شده با کلید AES-256، انتقال به دستگاه جدید و همگام‌سازی بدون تداخل.",
            contentEn = """
                Total safety for your breeding data:
                - AES-256 Encrypted Backups: Export your full facility records into password-protected `.aviary` archive files.
                - Automatic Local Snapshots: Daily backups saved locally with rolling 7-day retention.
                - Safe Overwrite Guard: Prevents older backups from accidentally replacing newer live records.
                - Device Migration: Transfer your aviary directly to a new phone with a 6-digit one-time transfer code.
            """.trimIndent(),
            contentFa = """
                حفاظت دائمی از زحمات سالن‌داری شما:
                - پشتیبان رمزگذاری‌شده AES-256: خروجی فایل رمزدار اختصاصی `.aviary` با کلمه عبور دلخواه.
                - پشتیبان‌گیری خودکار: ذخیره روزانه نسخه‌های پشتیبان در حافظه دستگاه با نگهداری ۷ روزه.
                - محافظت در برابر بازنویسی: اخطار هوشمند در صورت تلاش برای بازیابی فایل قدیمی‌تر از اطلاعات جاری.
                - انتقال مستقیم دستگاه: جابجایی کامل اطلاعات به گوشی یا تبلت جدید با کد انتقال ۶ رقمی.
            """.trimIndent(),
            tags = listOf("backup", "restore", "sync", "crypto", "پشتیبان", "بازیابی", "همگام‌سازی", "انتقال")
        ),
        HelpTopic(
            id = "api_keys",
            category = HelpCategory.SYSTEM,
            icon = Icons.Filled.Key,
            titleEn = "API Keys & AI Studio Configuration",
            titleFa = "کلیدهای API و تنظیمات هوش مصنوعی",
            summaryEn = "Secure configuration of Gemini API keys via the Secrets panel.",
            summaryFa = "تنظیم و اتصال امن کلیدهای هوش مصنوعی از طریق پنل امنیتی بدون افشا در کد.",
            contentEn = """
                Configuring AI capabilities safely:
                - Secrets Panel: API keys are securely managed via the platform environment and `BuildConfig`.
                - No Hardcoding: Never paste secret keys directly into text files or share raw keys.
                - Server-Side Proxy: Model queries execute server-side to guarantee zero exposure on client devices.
            """.trimIndent(),
            contentFa = """
                تنظیم امن قابلیت‌های هوش مصنوعی:
                - پنل Secrets: کلیدهای هوش مصنوعی از طریق محیط امن و `BuildConfig` مدیریت می‌شوند.
                - عدم درج مستقیم: هرگز کلیدهای محرمانه را در فایل‌ها یا محیط ناامن وارد نکنید.
                - پروکسی امن سرور: ارتباطات هوش مصنوعی از سرور پردازش شده و در کلاینت افشا نمی‌شود.
            """.trimIndent(),
            tags = listOf("api", "keys", "secrets", "gemini", "کلید", "تنظیمات", "هوش مصنوعی")
        ),
        HelpTopic(
            id = "settings",
            category = HelpCategory.SYSTEM,
            icon = Icons.Filled.Settings,
            titleEn = "Settings & Appearance Preferences",
            titleFa = "تنظیمات عمومی و ظاهر برنامه",
            summaryEn = "Language switching (Persian RTL / English LTR), theme toggles, and facility profiles.",
            summaryFa = "تغییر زبان (فارسی راست‌چین / انگلیسی چپ‌چین)، حالت شب و روز و تنظیمات پروفایل.",
            contentEn = """
                Personalize your experience:
                - Language: Switch freely between Persian (فارسی RTL) and English (LTR). Data is never altered when switching languages.
                - Theme: Choose between Light Mode and Dark Mode with true emerald avian accents.
                - Breeder Info: Set default ring prefixes, federation codes, and breeder contact details.
            """.trimIndent(),
            contentFa = """
                شخصی‌سازی محیط کاربری:
                - تغییر زبان: جابجایی لحظه‌ای بین فارسی (راست‌چین) و انگلیسی (چپ‌چین) بدون هیچ‌گونه حذف یا تغییر در داده‌ها.
                - پوسته: انتخاب حالت روشن یا تاریک متناسب با نور سالن پرورش.
                - مشخصات پرورش‌دهنده: تنظیم پیش‌فرض پیشوند حلقه، کد عضویت در انجمن و مشخصات ارتباطی.
            """.trimIndent(),
            tags = listOf("settings", "theme", "language", "profile", "تنظیمات", "زبان", "پوسته", "پروفایل")
        )
    )

    fun searchTopics(query: String, isPersian: Boolean): List<HelpTopic> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return topics

        return topics.filter { topic ->
            val title = if (isPersian) topic.titleFa else topic.titleEn
            val summary = if (isPersian) topic.summaryFa else topic.summaryEn
            val content = if (isPersian) topic.contentFa else topic.contentEn

            title.lowercase().contains(trimmed) ||
                    summary.lowercase().contains(trimmed) ||
                    content.lowercase().contains(trimmed) ||
                    topic.tags.any { it.lowercase().contains(trimmed) }
        }
    }

    fun getTopicById(id: String): HelpTopic? {
        return topics.find { it.id.equals(id, ignoreCase = true) }
    }
}
