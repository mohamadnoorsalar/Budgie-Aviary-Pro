package com.example.core.security

enum class AppRole(
    val id: String,
    val titleEn: String,
    val titleFa: String,
    val descriptionEn: String,
    val descriptionFa: String
) {
    MANAGER(
        id = "manager",
        titleEn = "Manager",
        titleFa = "مدیر سالن",
        descriptionEn = "Full control over all aviary operations, records, finance, and settings.",
        descriptionFa = "دسترسی کامل به تمام امکانات، ثبت و حذف رکوردها، مالی و تنظیمات."
    ),
    FACILITY_WORKER(
        id = "facility_worker",
        titleEn = "Facility Worker",
        titleFa = "مسئول سالن",
        descriptionEn = "Care operations: birds, cages, pairs, nutrition, inventory, health logging.",
        descriptionFa = "عملیات نگهداری: پرندگان، قفس‌ها، جفت‌ها، تغذیه، انبار و ثبت علائم."
    ),
    VETERINARIAN(
        id = "veterinarian",
        titleEn = "Veterinarian",
        titleFa = "دامپزشک",
        descriptionEn = "Medical treatments, health records, quarantine, dosages, and nutrition plans.",
        descriptionFa = "درمان‌ها، پرونده سلامت، قرنطینه، دوز داروها و برنامه‌های تغذیه."
    ),
    ACCOUNTANT(
        id = "accountant",
        titleEn = "Accountant",
        titleFa = "حسابدار",
        descriptionEn = "Financial ledgers, sales, purchases, cost reports, and inventory valuation.",
        descriptionFa = "دفتر مالی، خرید و فروش، گزارش سود و زیان و ارزش‌گذاری انبار."
    ),
    JUDGE(
        id = "judge",
        titleEn = "Judge / Evaluator",
        titleFa = "داور / ارزیاب",
        descriptionEn = "Competition scoring, bird standard evaluations, ring checks, and show results.",
        descriptionFa = "امتیازدهی مسابقات، داوری استانداردهای پرنده، بررسی پلاک و نتایج نمایشگاه."
    ),
    CUSTOM(
        id = "custom",
        titleEn = "Custom Role",
        titleFa = "نقش سفارشی",
        descriptionEn = "User-customizable granular permissions per aviary module.",
        descriptionFa = "دسترسی‌های سفارشی و قابل تنظیم به ازای هر بخش پرورشگاه."
    );

    companion object {
        fun fromId(id: String?): AppRole {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: MANAGER
        }
    }
}

enum class AviaryModule(
    val key: String,
    val titleEn: String,
    val titleFa: String
) {
    BIRDS("birds", "Birds Directory", "پرندگان"),
    PAIRS("pairs", "Breeding Pairs", "جفت‌ها و تکثیر"),
    CAGES("cages", "Cages & Aviaries", "قفس‌ها و سالن‌ها"),
    REPRODUCTION("reproduction", "Eggs & Clutches", "تخم‌ها و جوجه‌ها"),
    HEALTH("health", "Health & Veterinary", "سلامت و درمان"),
    NUTRITION("nutrition", "Nutrition & Feeding", "تغذیه و جیره‌ها"),
    INVENTORY("inventory", "Inventory & Supplies", "انبار و لوازم"),
    FINANCE("finance", "Financial Records", "مالی و تراکنش‌ها"),
    COMPETITIONS("competitions", "Competitions & Shows", "مسابقات و داوری"),
    GENETICS_PEDIGREE("genetics", "Genetics & Pedigree", "ژنتیک و شجره‌نامه"),
    REPORTS("reports", "Reports & Export", "گزارش‌ها و خروجی"),
    SETTINGS("settings", "App Settings & Security", "تنظیمات و امنیت"),
    AUDIT_LOG("audit_log", "Security Audit Log", "گزارش فعالیت‌های امنیتی")
}

enum class PermissionLevel {
    NONE,
    READ_ONLY,
    READ_WRITE,
    FULL // includes delete sensitive data
}

data class RolePermissions(
    val role: AppRole,
    val modulePermissions: Map<AviaryModule, PermissionLevel>
) {
    fun canAccess(module: AviaryModule): Boolean {
        val level = modulePermissions[module] ?: PermissionLevel.NONE
        return level != PermissionLevel.NONE
    }

    fun canWrite(module: AviaryModule): Boolean {
        val level = modulePermissions[module] ?: PermissionLevel.NONE
        return level == PermissionLevel.READ_WRITE || level == PermissionLevel.FULL
    }

    fun canDelete(module: AviaryModule): Boolean {
        val level = modulePermissions[module] ?: PermissionLevel.NONE
        return level == PermissionLevel.FULL
    }

    companion object {
        fun defaultPermissions(role: AppRole): RolePermissions {
            val map = when (role) {
                AppRole.MANAGER -> AviaryModule.entries.associateWith { PermissionLevel.FULL }
                
                AppRole.FACILITY_WORKER -> mapOf(
                    AviaryModule.BIRDS to PermissionLevel.READ_WRITE,
                    AviaryModule.PAIRS to PermissionLevel.READ_WRITE,
                    AviaryModule.CAGES to PermissionLevel.READ_WRITE,
                    AviaryModule.REPRODUCTION to PermissionLevel.READ_WRITE,
                    AviaryModule.HEALTH to PermissionLevel.READ_WRITE,
                    AviaryModule.NUTRITION to PermissionLevel.READ_WRITE,
                    AviaryModule.INVENTORY to PermissionLevel.READ_WRITE,
                    AviaryModule.FINANCE to PermissionLevel.NONE,
                    AviaryModule.COMPETITIONS to PermissionLevel.READ_ONLY,
                    AviaryModule.GENETICS_PEDIGREE to PermissionLevel.READ_ONLY,
                    AviaryModule.REPORTS to PermissionLevel.READ_ONLY,
                    AviaryModule.SETTINGS to PermissionLevel.NONE,
                    AviaryModule.AUDIT_LOG to PermissionLevel.NONE
                )
                
                AppRole.VETERINARIAN -> mapOf(
                    AviaryModule.BIRDS to PermissionLevel.READ_WRITE,
                    AviaryModule.PAIRS to PermissionLevel.READ_ONLY,
                    AviaryModule.CAGES to PermissionLevel.READ_ONLY,
                    AviaryModule.REPRODUCTION to PermissionLevel.READ_WRITE,
                    AviaryModule.HEALTH to PermissionLevel.FULL,
                    AviaryModule.NUTRITION to PermissionLevel.READ_WRITE,
                    AviaryModule.INVENTORY to PermissionLevel.READ_ONLY,
                    AviaryModule.FINANCE to PermissionLevel.NONE,
                    AviaryModule.COMPETITIONS to PermissionLevel.READ_ONLY,
                    AviaryModule.GENETICS_PEDIGREE to PermissionLevel.READ_ONLY,
                    AviaryModule.REPORTS to PermissionLevel.READ_ONLY,
                    AviaryModule.SETTINGS to PermissionLevel.NONE,
                    AviaryModule.AUDIT_LOG to PermissionLevel.READ_ONLY
                )
                
                AppRole.ACCOUNTANT -> mapOf(
                    AviaryModule.BIRDS to PermissionLevel.READ_ONLY,
                    AviaryModule.PAIRS to PermissionLevel.READ_ONLY,
                    AviaryModule.CAGES to PermissionLevel.READ_ONLY,
                    AviaryModule.REPRODUCTION to PermissionLevel.READ_ONLY,
                    AviaryModule.HEALTH to PermissionLevel.READ_ONLY,
                    AviaryModule.NUTRITION to PermissionLevel.READ_ONLY,
                    AviaryModule.INVENTORY to PermissionLevel.READ_WRITE,
                    AviaryModule.FINANCE to PermissionLevel.FULL,
                    AviaryModule.COMPETITIONS to PermissionLevel.READ_ONLY,
                    AviaryModule.GENETICS_PEDIGREE to PermissionLevel.NONE,
                    AviaryModule.REPORTS to PermissionLevel.FULL,
                    AviaryModule.SETTINGS to PermissionLevel.NONE,
                    AviaryModule.AUDIT_LOG to PermissionLevel.READ_ONLY
                )
                
                AppRole.JUDGE -> mapOf(
                    AviaryModule.BIRDS to PermissionLevel.READ_ONLY,
                    AviaryModule.PAIRS to PermissionLevel.NONE,
                    AviaryModule.CAGES to PermissionLevel.NONE,
                    AviaryModule.REPRODUCTION to PermissionLevel.NONE,
                    AviaryModule.HEALTH to PermissionLevel.NONE,
                    AviaryModule.NUTRITION to PermissionLevel.NONE,
                    AviaryModule.INVENTORY to PermissionLevel.NONE,
                    AviaryModule.FINANCE to PermissionLevel.NONE,
                    AviaryModule.COMPETITIONS to PermissionLevel.READ_WRITE,
                    AviaryModule.GENETICS_PEDIGREE to PermissionLevel.READ_ONLY,
                    AviaryModule.REPORTS to PermissionLevel.READ_ONLY,
                    AviaryModule.SETTINGS to PermissionLevel.NONE,
                    AviaryModule.AUDIT_LOG to PermissionLevel.NONE
                )
                
                AppRole.CUSTOM -> AviaryModule.entries.associateWith { PermissionLevel.READ_ONLY }
            }
            return RolePermissions(role, map)
        }
    }
}

enum class LockType {
    NONE,
    PIN,
    PASSWORD
}

enum class AutoLockTimeout(val seconds: Long, val titleEn: String, val titleFa: String) {
    IMMEDIATE(0L, "Immediately on exit", "بلافاصله پس از خروج"),
    THIRTY_SEC(30L, "30 seconds", "۳۰ ثانیه"),
    ONE_MIN(60L, "1 minute", "۱ دقیقه"),
    FIVE_MIN(300L, "5 minutes", "۵ دقیقه"),
    FIFTEEN_MIN(900L, "15 minutes", "۱۵ دقیقه"),
    NEVER(-1L, "Never (Manual lock only)", "هرگز (فقط قفل دستی)");

    companion object {
        fun fromSeconds(sec: Long): AutoLockTimeout {
            return entries.find { it.seconds == sec } ?: ONE_MIN
        }
    }
}

data class SecurityState(
    val isLockConfigured: Boolean = false,
    val lockType: LockType = LockType.NONE,
    val isAppLocked: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val autoLockTimeout: AutoLockTimeout = AutoLockTimeout.ONE_MIN,
    val currentRole: AppRole = AppRole.MANAGER,
    val currentUserName: String = "Manager",
    val failedAttempts: Int = 0,
    val lockoutRemainingSeconds: Int = 0,
    val hasEncryptedApiKey: Boolean = false,
    val customPermissions: Map<AviaryModule, PermissionLevel> = emptyMap()
)
