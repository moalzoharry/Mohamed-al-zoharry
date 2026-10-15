package com.example.adhkar

enum class AdhkarCategory(
    val id: String,
    val titleArabic: String,
    val emoji: String,
    val timeDescription: String
) {
    MORNING("morning", "أذكار الصباح", "☀️", "تقرأ من بعد صلاة الفجر حتى شروق الشمس"),
    EVENING("evening", "أذكار المساء", "🌙", "تقرأ من بعد صلاة العصر حتى غروب الشمس"),
    AFTER_PRAYER("after_prayer", "أذكار بعد الصلاة", "🕌", "الأذكار والاستغفار الوارد عقب الصلوات المكتوبة"),
    SLEEP("sleep", "أذكار النوم", "⭐", "أذكار التحصين والراحة عند النوم والاستيقاظ")
}

data class DhikrItem(
    val id: Int,
    val textArabic: String,
    val virtueArabic: String = "",
    val referenceArabic: String = "",
    val targetCount: Int = 1,
    val currentCount: Int = 0
) {
    val isCompleted: Boolean
        get() = currentCount >= targetCount
}
