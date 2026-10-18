package com.example.data.model

data class ReverentRecitation(
    val id: Int,
    val title: String,
    val reciterNameArabic: String,
    val surahName: String,
    val audioUrl: String,
    val durationText: String,
    val description: String,
    val tag: String = "تلاوة خاشعة"
)

object ReverentRecitationsData {

    val list: List<ReverentRecitation> = listOf(
        ReverentRecitation(
            id = 1,
            title = "آية الكرسي وتلاوة خاشعة تأخذ القلوب",
            reciterNameArabic = "الشيخ إسلام صبحي",
            surahName = "سورة البقرة وآية الكرسي",
            audioUrl = "https://ia801503.us.archive.org/15/items/islam-sobhi-ayat-al-kursi/ayat_al_kursi_islam_sobhi.mp3",
            durationText = "03:45",
            description = "تلاوة هادئة خاشعة بصوت ندي تملأ الروح سكينة وطمأنينة وتطرد الشياطين من البيت.",
            tag = "سكينة وحفظ"
        ),
        ReverentRecitation(
            id = 2,
            title = "المقطع التاريخي النادر من سورة مريم",
            reciterNameArabic = "الشيخ عبد الباسط عبد الصمد",
            surahName = "سورة مريم",
            audioUrl = "https://server7.mp3quran.net/basit/019.mp3",
            durationText = "18:20",
            description = "تسجيل نادر فريد من الحرم الشريف عام ١٩٥٣؛ أداء أسطوري أذهل العالم الإسلامي.",
            tag = "نوادر تاريخية"
        ),
        ReverentRecitation(
            id = 3,
            title = "روائع سورة الحشر المجودة النادرة",
            reciterNameArabic = "الشيخ محمد صديق المنشاوي",
            surahName = "سورة الحشر (لو أنزلنا هذا القرآن)",
            audioUrl = "https://server10.mp3quran.net/minsh/059.mp3",
            durationText = "14:15",
            description = "الصوت الباكي الخاشع في تلاوة ترتجف لها القلوب لعظمة الخالق وجلال أسمائه الحسنى.",
            tag = "صوت باكي"
        ),
        ReverentRecitation(
            id = 4,
            title = "تلاوة سورة النمل الشهيرة بنبرة إعجازية",
            reciterNameArabic = "الشيخ مصطفى إسماعيل",
            surahName = "سورة النمل",
            audioUrl = "https://server8.mp3quran.net/mustafa/027.mp3",
            durationText = "22:40",
            description = "عبقري التلاوة ومقرئ الملوك والرؤساء في تسجيل نادر يبهر الآذان بدقة المقامات والنغم القرآني.",
            tag = "إعجاز النغم"
        ),
        ReverentRecitation(
            id = 5,
            title = "تلاوة مبكية لسورة ق وتدبر الآيات",
            reciterNameArabic = "الشيخ إسلام صبحي",
            surahName = "سورة ق",
            audioUrl = "https://server14.mp3quran.net/islam/050.mp3",
            durationText = "09:30",
            description = "وقفة إيمانية مهيبة مع آيات الموت والبعث والحساب بصوت خاشع ومؤثر.",
            tag = "تدبر وخشوع"
        ),
        ReverentRecitation(
            id = 6,
            title = "سورة القيامة والنازعات برواية حفص",
            reciterNameArabic = "الشيخ محمود علي البنا",
            surahName = "سورة القيامة والنازعات",
            audioUrl = "https://server8.mp3quran.net/banna/075.mp3",
            durationText = "11:05",
            description = "نقاء الصوت وجمال الأداء الرفيع لأحد أعمدة الإذاعة المصرية والتلاوة الأصيلة.",
            tag = "أصالة التلاوة"
        ),
        ReverentRecitation(
            id = 7,
            title = "تراويح تهتز لها القلوب من سورة يوسف",
            reciterNameArabic = "الشيخ ناصر القطامي",
            surahName = "سورة يوسف",
            audioUrl = "https://server6.mp3quran.net/qtm/012.mp3",
            durationText = "28:10",
            description = "تلاوة حجازية شجية من ليالي رمضان المباركة وقصة أحسن القصص.",
            tag = "تراويح باكية"
        ),
        ReverentRecitation(
            id = 8,
            title = "تلاوة مؤثرة تفيض هيبة وسكينة",
            reciterNameArabic = "الشيخ ياسر الدوسري",
            surahName = "سورة الرحمن",
            audioUrl = "https://server11.mp3quran.net/yasser/055.mp3",
            durationText = "12:50",
            description = "تلاوة مميزة من الحرم المكي الشريف تجمع بين قوة النبرة وحلاوة الترتيل.",
            tag = "من رحاب الحرم"
        ),
        ReverentRecitation(
            id = 9,
            title = "سكينة القلوب وترتيل عذب هادئ",
            reciterNameArabic = "الشيخ هزاع البلوشي",
            surahName = "سورة الكهف",
            audioUrl = "https://server11.mp3quran.net/hazza/018.mp3",
            durationText = "24:35",
            description = "نغمات هادئة تبعث الراحة في الصدور وتزيل التوتر والقلق لنور يوم الجمعة.",
            tag = "راحة القلب"
        )
    )
}
