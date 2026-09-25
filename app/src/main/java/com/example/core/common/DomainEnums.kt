package com.example.core.common

enum class BirdGender {
    MALE,
    FEMALE,
    UNKNOWN
}

enum class BirdStatus {
    ACTIVE,
    BREEDING,
    RESTING,
    QUARANTINE,
    SOLD,
    DECEASED
}

enum class BudgieVariety {
    ENGLISH_SHOW,      // مرغ عشق انگلیسی (نمایشی)
    AUSTRALIAN_WILD,   // مرغ عشق معمولی / استرالیایی
    CRESTED,           // کاکلی
    HAGOROMO           // ژاپنی / هاگورومو
}

val BudgieVariety.displayName: String
    get() = when (this) {
        BudgieVariety.ENGLISH_SHOW -> "English Show"
        BudgieVariety.AUSTRALIAN_WILD -> "Australian Standard"
        BudgieVariety.CRESTED -> "Crested"
        BudgieVariety.HAGOROMO -> "Hagoromo"
    }

enum class CageType {
    BREEDING_BOX,      // قفس جفت‌اندازی / باکس
    FLIGHT_CAGE,       // پران
    STOCK_CAGE,        // قفس نگهداری انفرادی
    QUARANTINE_CAGE    // قفس قرنطینه
}

enum class EggStatus {
    LAID,
    FERTILE,
    INFERTILE,
    HATCHED,
    BROKEN
}

enum class EggFertilityStatus {
    PENDING,
    FERTILE,
    INFERTILE,
    DEAD_IN_SHELL
}

enum class ReminderPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

enum class ReminderType {
    EGG_LAYING,
    EXPECTED_HATCH,
    MATING,
    MEDICATION,
    SUPPLEMENTS,
    WEIGHT_MEASUREMENT,
    FEEDING,
    CLEANING,
    INVENTORY,
    DAILY_TASK,
    IMPORTANT_EVENT,
    GENETIC_CALENDAR
}

val ReminderType.displayNameEn: String
    get() = when (this) {
        ReminderType.EGG_LAYING -> "Egg Laying"
        ReminderType.EXPECTED_HATCH -> "Expected Hatch"
        ReminderType.MATING -> "Mating & Pairing"
        ReminderType.MEDICATION -> "Medication"
        ReminderType.SUPPLEMENTS -> "Supplements & Vitamins"
        ReminderType.WEIGHT_MEASUREMENT -> "Weight Measurement"
        ReminderType.FEEDING -> "Feeding Schedule"
        ReminderType.CLEANING -> "Cleaning & Disinfection"
        ReminderType.INVENTORY -> "Inventory & Reorder"
        ReminderType.DAILY_TASK -> "Daily Task"
        ReminderType.IMPORTANT_EVENT -> "Important Event"
        ReminderType.GENETIC_CALENDAR -> "Genetic Calendar Event"
    }

val ReminderType.displayNameFa: String
    get() = when (this) {
        ReminderType.EGG_LAYING -> "تخم‌گذاری و کندلینگ"
        ReminderType.EXPECTED_HATCH -> "زمان پیش‌بینی تولد جوجه"
        ReminderType.MATING -> "جفت‌اندازی و معرفی"
        ReminderType.MEDICATION -> "دارو و درمان"
        ReminderType.SUPPLEMENTS -> "مکمل‌ها و ویتامین‌ها"
        ReminderType.WEIGHT_MEASUREMENT -> "توزین و وزن‌کشی"
        ReminderType.FEEDING -> "برنامه غذایی و دانه‌بندی"
        ReminderType.CLEANING -> "نظافت و ضدعفونی سالن"
        ReminderType.INVENTORY -> "موجودی انبار و دان"
        ReminderType.DAILY_TASK -> "وظیفه روزانه"
        ReminderType.IMPORTANT_EVENT -> "رویداد و یادداشت مهم"
        ReminderType.GENETIC_CALENDAR -> "تقویم و برنامه ژنتیکی"
    }

enum class TransactionType {
    INCOME,
    EXPENSE
}
