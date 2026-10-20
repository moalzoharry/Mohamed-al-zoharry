package com.example.museum

import androidx.annotation.DrawableRes
import com.example.R

/**
 * Data models for Islamic Heritage, Museum Relics, Sacred Monuments,
 * Companions (Sahaba Heroes), and Spiritual Wallpapers.
 */

data class IslamicRelic(
    val id: String,
    val nameArabic: String,
    val englishName: String,
    val location: String, // e.g. "قصر طوب قابي (Topkapi Palace) - إسطنبول، تركيا"
    val cityCountry: String,
    val historicalContext: String,
    val description: String,
    val authenticNarrations: String,
    val category: String, // سيوف نبوية، مقتنيات شريفة، رايات وأختام
    val iconEmoji: String,
    @DrawableRes val imageRes: Int? = null
)

data class IslamicMonument(
    val id: String,
    val nameArabic: String,
    val englishName: String,
    val location: String,
    val era: String,
    val significance: String,
    val architecturalHighlights: String,
    val holyVersesOrHadith: String,
    val category: String, // الحرمين الشريفين، مساجد كبرى، عمارة أندلسية
    val iconEmoji: String,
    @DrawableRes val imageRes: Int? = null
)

data class SahabaHero(
    val id: String,
    val nameArabic: String,
    val titleArabic: String, // e.g. "فارس الإسلام وبطل القادسية", "سيف الله المسلول"
    val famousQuoteOrHadith: String,
    val epicFeats: String, // مآثره وبطولاته الخارقة (كقتال الأسود، المشي على الماء، خوض حديقة الموت)
    val battleHighlights: List<String>,
    val moralAndHeroism: String,
    val iconEmoji: String
)

data class IslamicWallpaperItem(
    val id: String,
    val title: String,
    val category: String, // الكعبة والمدينة، عمارة إسلامية، خط وزخارف
    @DrawableRes val resId: Int,
    val description: String
)

object IslamicHeritageRepository {

    val relics: List<IslamicRelic> = listOf(
        IslamicRelic(
            id = "prophet_sword_mathur",
            nameArabic = "سيف رسول الله ﷺ (المأثور)",
            englishName = "The Sword Al-Ma'thur",
            location = "جناح الأمانات المقدسة - قصر طوب قابي",
            cityCountry = "إسطنبول، تركيا",
            historicalContext = "أول سيف ملكه النبي ﷺ، ورثه عن أبيه عبد الله بن عبد المطلب قبل البعثة النبوية، وهاجر به من مكة إلى المدينة المنورة.",
            description = "نصل مستقيم من الفولاذ الدمشقي بطول 99 سم، مقبضه على هيئة ثعبانين من الذهب الخالص المرصع بالزمرد والفيروز، ونُقش على مقبضه بالخط الكوفي: «عبد الله بن عبد المطلب».",
            authenticNarrations = "روي في السير أنه سيف الميراث النبوي الذي صحبه ﷺ في بدايات الدعوة وحفظته الأجيال رمزاً للشرف النبوي.",
            category = "السيوف النبوية",
            iconEmoji = "⚔️",
            imageRes = R.drawable.islamic_museum_banner
        ),
        IslamicRelic(
            id = "prophet_sandal_sharaf",
            nameArabic = "النعل الشريف (السبتية النبوية)",
            englishName = "The Noble Sandal (Al-Na'l Al-Sharif)",
            location = "متحف قصر طوب قابي & مسجد الإمام الحسين",
            cityCountry = "إسطنبول والقاهرة",
            historicalContext = "النعل النبوي الشريف المصنوع من الجلد المدبوغ (السِّبْت) الخالي من الشعر، والذي وطئت به قدما خير البرية أرض الإسراء والمعراج وأرجاء المدينة المنورة.",
            description = "نعل جلدي ذو قبالين (سيران يمران بين الأصابع)، ناعم الملمس متماسك، خُلد رسمه وشكله في كتب أئمة الحديث والسير كـ «فتح المتعال في وصف النعال» للعلامة المقري.",
            authenticNarrations = "قال أنس بن مالك رضي الله عنه: «كان نعل النبي ﷺ له قبالان» (رواه البخاري). ونظم العلماء في بركة أثره قصائد الشوق للمصطفى ﷺ.",
            category = "المقتنيات الشريفة",
            iconEmoji = "👡",
            imageRes = R.drawable.app_logo_emblem
        ),
        IslamicRelic(
            id = "prophet_mantle_burdah",
            nameArabic = "البردة النبوية الشريفة (خرقة السعادة)",
            englishName = "The Holy Mantle (Hırka-i Şerif)",
            location = "مسجد الخرقة الشريفة وقصر طوب قابي",
            cityCountry = "إسطنبول، تركيا",
            historicalContext = "الكساء الأسود الصوفي الذي أهداه النبي ﷺ لكعب بن زهير رضي الله عنه بعد إنشاده قصيدة «بانت سعاد»، واشتراها معاوية رضي الله عنه لاحقاً وتوارثها الخلفاء.",
            description = "كساء من الصوف الخالص ذو بطانة حريرية رقيقة بلون بيج مائل للسواد، يُحفظ في صندوق مطلي بالذهب ومطرز بأرقى آيات القرآن الكريم.",
            authenticNarrations = "«أن النبي ﷺ كسا كعب بن زهير بردته حين أسلم وأنشد قصيدته الشهيرة، فكانت رمزاً للعفو والرحمة المهداة للعالمين».",
            category = "المقتنيات الشريفة",
            iconEmoji = "🧥",
            imageRes = R.drawable.islamic_museum_banner
        ),
        IslamicRelic(
            id = "prophet_hair_strands",
            nameArabic = "الشعرات النبوية الشريفة",
            englishName = "Noble Hair Strands of the Prophet ﷺ",
            location = "مسجد الإمام الحسين & طوب قابي ومساجد المغرب والشام",
            cityCountry = "القاهرة، إسطنبول، دمشق، وفاس",
            historicalContext = "شعرات اقتسمها الصحابة الكرام يوم الحديبية وفي حجة الوداع حين حلق النبي ﷺ رأسه ووزع شعره على أصحابه تبركاً ووفاءً.",
            description = "شعرات شريفة محفورة في كبسولات زجاجية مطلية بالذهب ومطرزة بالعنبر والمسك، تتلألأ بحفظها المتوارث عبر أسانيد عائلية ورسمية عبر القرون.",
            authenticNarrations = "ثبت في الصحيحين: «أن رسول الله ﷺ لما حلق رأسه ناول أبا طلحة شقه الأيمن فقسمه بين الناس، ثم قال: احلق، فناوله أبا طلحة فقال: اقسمه بين الناس» (رواه مسلم).",
            category = "المقتنيات الشريفة",
            iconEmoji = "✨",
            imageRes = R.drawable.app_logo_emblem
        ),
        IslamicRelic(
            id = "prophet_seal_ring",
            nameArabic = "خاتم النبي ﷺ (محمد رسول الله)",
            englishName = "The Noble Seal Ring",
            location = "قسم الأمانات المقدسة - طوب قابي",
            cityCountry = "إسطنبول، تركيا",
            historicalContext = "اتخذه النبي ﷺ عندما أراد أن يكتب إلى ملوك الروم وفارس، فقيل له: إنهم لا يقرأون كتاباً إلا إذا كان مختوماً، فاتخذ خاتماً من فضة نقش فيه: «محمد رسول الله».",
            description = "فص عقيق مستدير منقوش عليه من الأسفل للأعلى تعظيماً: (الله) في السطر الأول، (رسول) في الثاني، (محمد) في الثالث، ليكون لفظ الجلالة في الأعالي.",
            authenticNarrations = "عن أنس رضي الله عنه: «أن النبي ﷺ اتخذ خاتماً من فضة فصه منه، ونقش فيه: محمد رسول الله» (رواه البخاري ومسلم).",
            category = "رايات وأختام",
            iconEmoji = "💍",
            imageRes = R.drawable.app_logo_emblem
        ),
        IslamicRelic(
            id = "prophet_bow_al_kattum",
            nameArabic = "قوس النبي ﷺ (الكَتوم)",
            englishName = "The Noble Bow (Al-Kattum)",
            location = "خزانة السلاح الشريف - قصر طوب قابي",
            cityCountry = "إسطنبول، تركيا",
            historicalContext = "سُمي (الكتوم) لخفوت صوته عند رمي السهام، وكان من أعز الأسلحة التي استعملت في الدفاع عن الحق في غزوتي بدر وأحد.",
            description = "قوس مصنوع من خشب النبع المتين المغلف بطبقات من الجلد المذهب المقوى، بطول 118 سم ومغطى بنقوش إسلامية تحكي ذكريات الرمي في سبيل الله.",
            authenticNarrations = "روي أن النبي ﷺ رمى به في أحد حتى انكسرت سِيَتُه، فأخذه قتادة بن النعمان رضي الله عنه.",
            category = "السيوف النبوية",
            iconEmoji = "🏹",
            imageRes = R.drawable.islamic_museum_banner
        )
    )

    val monuments: List<IslamicMonument> = listOf(
        IslamicMonument(
            id = "kaaba_shareef",
            nameArabic = "الكعبة المشرفة والمسجد الحرام",
            englishName = "The Holy Kaaba & Al-Masjid Al-Haram",
            location = "مكة المكرمة، المملكة العربية السعودية",
            era = "بناها إبراهيم وإسماعيل عليهما السلام بأمر الله",
            significance = "قبلة المسلمين في صلاتهم، ومهوى أفئدتهم في حجهم وعمرتهم، وأول بيت وضع للناس في الأرض.",
            architecturalHighlights = "مغطاة بكسوة الحرير الأسود المطرزة بأسلاك الفضة والذهب الخالص، تضم الحجر الأسود، الملتزم، الحطيم (حجر إسماعيل)، ومقام إبراهيم.",
            holyVersesOrHadith = "﴿إِنَّ أَوَّلَ بَيْتٍ وُضِعَ لِلنَّاسِ لَلَّذِي بِبَكَّةَ مُبَارَكًا وَهُدًى لِّلْعَالَمِينَ﴾ [آل عمران: 96]",
            category = "الحرمين الشريفين",
            iconEmoji = "🕋",
            imageRes = R.drawable.islamic_wallpaper_kaaba
        ),
        IslamicMonument(
            id = "prophet_mosque",
            nameArabic = "المسجد النبوي الشريف والروضة",
            englishName = "Al-Masjid An-Nabawi & The Rawdah",
            location = "المدينة المنورة، المملكة العربية السعودية",
            era = "أسسه النبي ﷺ في السنة الأولى من الهجرة",
            significance = "ثاني الحرمين الشريفين، يضم الحجرة الشريفة حيث دُفن النبي ﷺ وصاحباه أبو بكر وعمر، وفيه الروضة الشريفة روضة من رياض الجنة.",
            architecturalHighlights = "القبة الخضراء الشاهقة، المظلات الذكية المتحركة، المنارة التاريخية، والمنبر النبوي الشريف.",
            holyVersesOrHadith = "قال رسول الله ﷺ: «صلاة في مسجدي هذا خير من ألف صلاة فيما سواه إلا المسجد الحرام» (متفق عليه).",
            category = "الحرمين الشريفين",
            iconEmoji = "🕌",
            imageRes = R.drawable.islamic_wallpaper_madina
        ),
        IslamicMonument(
            id = "al_aqsa_mosque",
            nameArabic = "المسجد الأقصى المبارك وقبة الصخرة",
            englishName = "Al-Aqsa Mosque & Dome of the Rock",
            location = "القدس الشريف، فلسطين",
            era = "بُني بعد المسجد الحرام بأربعين عاماً",
            significance = "أولى القبلتين، وثالث الحرمين الشريفين، ومسرى النبي محمد ﷺ في رحلة الإسراء والمعراج إلى السماوات العلى.",
            architecturalHighlights = "قبة الصخرة الذهبية الثمانية، المصلى القبلي ذو القبة الرصاصية، المصلى المرواني، وباب المغاربة والمآذن الأربعة.",
            holyVersesOrHadith = "﴿سُبْحَانَ الَّذِي أَسْرَىٰ بِعَبْدِهِ لَيْلًا مِّنَ الْمَسْجِدِ الْحَرَامِ إِلَى الْمَسْجِدِ الْأَقْصَى الَّذِي بَارَكْنَا حَوْلَهُ﴾ [الإسراء: 1]",
            category = "مساجد كبرى",
            iconEmoji = "🕌",
            imageRes = R.drawable.islamic_museum_banner
        ),
        IslamicMonument(
            id = "al_azhar_mosque",
            nameArabic = "الجامع الأزهر الشريف",
            englishName = "Al-Azhar Mosque & University",
            location = "القاهرة المعزية، مصر",
            era = "تأسس عام 361 هـ / 972 م في عهد الفاطميين",
            significance = "منارة العلم الوسطي وحصن اللغة العربية وعلوم القرآن والشريعة لأكثر من ألف عام، وأقدم جامعة عاملة في العالم.",
            architecturalHighlights = "الصحن المكشوف المحاط بأروقة رخامية، مئذنة قايتباي والغانوري ذات الرؤوس المزدوجة، وقبة المحراب التاريخية.",
            holyVersesOrHadith = "خرج منه أئمة الفقه والحديث والتفسير الذين نشروا سماحة الإسلام في مشارق الأرض ومغاربها.",
            category = "مساجد كبرى",
            iconEmoji = "🏛️",
            imageRes = R.drawable.islamic_museum_banner
        ),
        IslamicMonument(
            id = "alhambra_granada",
            nameArabic = "قصر الحمراء وجنة العريف",
            englishName = "Alhambra Palace & Generalife",
            location = "غرناطة، الأندلس (إسبانيا)",
            era = "بناه ملوك بني نصر في القرن السابع الهجري (1238م)",
            significance = "درة العمارة الإسلامية في الأندلس والشاهد الخالد على رقي الحضارة الإسلامية في الفنون والهندسة والماء والجمال.",
            architecturalHighlights = "فناء السباع الرخامي الشهير، نقوش المقرنصات الخلابة، وشعار النصريين الخالد المنقوش على كل جدار: «ولا غالب إلا الله».",
            holyVersesOrHadith = "شاهد تاريخي فريد يجمع بين الهندسة الصوتية، والحدائق المعلقة، وتدفق مياه الجبال الذاتي.",
            category = "عمارة أندلسية",
            iconEmoji = "🏰",
            imageRes = R.drawable.islamic_museum_banner
        )
    )

    val heroes: List<SahabaHero> = listOf(
        SahabaHero(
            id = "al_qaqaa",
            nameArabic = "القعقاع بن عمرو التميمي",
            titleArabic = "البطل الأسطوري الذي صوته في الجيش بألف رجل",
            famousQuoteOrHadith = "قال عنه الصديق أبو بكر رضي الله عنه: «لا يُهزم جيشٌ فيه مثل هذا.. وصوتُ القعقاع في الجيش خيرٌ من ألف رجل».",
            epicFeats = "صاحب الحيل الحربية العبكرة في معركة اليرموك، وقائد قلب معركة القادسية الذي ألبس الإبل أقمشة مرعبة لإفزاع خيل الفرس، ثم قاد فدائيين لقتل الفيل الأبيض وعور الفيل الأجرب لكسر شوكة جيش رستم، وفتح المدائن.",
            battleHighlights = listOf("معركة اليرموك", "معركة القادسية وفتح العراق", "فتح المدائن وكسر طغيان كسرى"),
            moralAndHeroism = "رمز الشجاعة الفائقة والذكاء الاستراتيجي الذي قلب موازين أكبر إمبراطوريتين في العالم وقتها بأقل عدد من الرجال.",
            iconEmoji = "⚔️"
        ),
        SahabaHero(
            id = "khalid_bin_walid",
            nameArabic = "خالد بن الوليد",
            titleArabic = "سيف الله المسلول وقاهر الإمبراطوريات",
            famousQuoteOrHadith = "قال عنه رسول الله ﷺ: «نِعْمَ عبدُ اللهِ وأخو العشيرةِ خالدُ بنُ الوليدِ، سيفٌ من سيوفِ اللهِ سلَّهُ اللهُ على الكفارِ والمنافقين».",
            epicFeats = "خاض أكثر من 100 معركة حاسمة ولم يُهزم في معركة واحدة قط طوال حياته! عبر صحراء السماوة القاتلة بجيشه في معجزة عسكرية، وكسر 9 سيوف في يده في غزوة مؤتة وهو ينقذ جيش المسلمين، وأسقط جيوش الروم في اليرموك.",
            battleHighlights = listOf("غزوة مؤتة (تكسرت في يده 9 أسياف)", "معركة اليرموك الحاسمة", "معركة ذات السلاسل والولجة"),
            moralAndHeroism = "قال كلمته الخالدة على فراش الموت: «لقد شهدتُ مائة زحف وما في جسدي موضع شبر إلا وفيه ضربة بسيف أو طعنة برمح، وها أنا أموت على فراشي كما يموت البعير.. فلا نامت أعين الجبناء!».",
            iconEmoji = "🗡️"
        ),
        SahabaHero(
            id = "al_baraa_bin_malik",
            nameArabic = "البراء بن مالك الأنصاري",
            titleArabic = "الفدائي الخارق وقاتل الأسود ومقتحم حديقة الموت",
            famousQuoteOrHadith = "كتب أمير المؤمنين عمر بن الخطاب لجيوشه: «لا تُولّوا البراءَ بنَ مالكٍ جيشاً من جيوش المسلمين؛ فإنه مهلكةٌ من المهالك يقدم بهم حتى يهلكهم لشجاعته!». وكان مُجاب الدعوة.",
            epicFeats = "بارز أكثر من 100 مقاتل وجهاً لوجه وقتلهم جميعاً مبارزة! وفي معركة اليمامة ضد مسيلمة الكذاب تحصن العدو في حديقة محصنة بأسوار عملاقة، فقال للصحابة: «ضعوني على ترس وارفعوني بالرماح والقوني عليهم داخل السور!»، فألقوه وحده فقاتل حتى فتح الباب للجيش وبه أكثر من 80 جرحاً ونجا!",
            battleHighlights = listOf("اقتحام حديقة الموت باليمامة", "فتح تستر (تلقف سلاسل الجمر المحمى بيديه)", "مقاتلة الأسود في المعارك"),
            moralAndHeroism = "المقاتل الذي كان يبحث عن الشهادة كما يبحث الناس عن الحياة، وكان إذا أقسم على الله لأبرّه.",
            iconEmoji = "🛡️"
        ),
        SahabaHero(
            id = "dhirar_bin_al_azwar",
            nameArabic = "ضرار بن الأزور الأسدي",
            titleArabic = "الفارس المغوار (الفارس عاري الصدر)",
            famousQuoteOrHadith = "كانت فرسان الروم ترتعد منه ويسمونه «الشيطان عاري الصدر» لشدة بسالته وسرعته واقتحامه صفوفهم وحيداً دون درع.",
            epicFeats = "في معركة أجنادين واليرموك كان يخلع درعه ويلقي بقميصه أرضاً ويقاتل بسيفه عاري الصدر ليزداد خفة ورشاقة، فكان يخترق كتائب الروم ويسقط قادتهم واحداً تلو الآخر حتى اعتقد الروم أنه ليس بشراً بل قوة خارقة!",
            battleHighlights = listOf("معركة أجنادين", "معركة مرج الصفر", "حصار دمشق ومعركة اليرموك"),
            moralAndHeroism = "بطل شجاع وفارس وشاعر، كان نموذجاً للاستبسال الذي بث الرعب في جيوش أكبر إمبراطورية في ذلك الزمان.",
            iconEmoji = "🦁"
        ),
        SahabaHero(
            id = "saad_bin_abi_waqqas",
            nameArabic = "سعد بن أبي وقاص",
            titleArabic = "خال النبي ﷺ وفارس دجلة الذي مشى جيشه على الماء",
            famousQuoteOrHadith = "قال له النبي ﷺ يوم أحد: «ارمِ سعد.. فداك أبي وأمي!»، ولم يجمع النبي ﷺ أبويه لأحد غير سعد رضي الله عنه.",
            epicFeats = "أول من رمى بسهم في سبيل الله، ومستجاب الدعوة ببركة دعاء النبي. قائد معركة القادسية التي أسقطت عرش كسرى، وفي فتح المدائن خاض بجيشه خيولاً ورجالاً مياه نهر دجلة الجارف وهو يجري كالجبال، فمشى المسلمون على وجه الماء حتى عبروا ولم يفقدوا إلا قدحاً رُبط بحبل!",
            battleHighlights = listOf("معركة القادسية الكبرى", "عبور نهر دجلة خوضاً على الماء", "فتح المدائن وإيوان كسرى"),
            moralAndHeroism = "من العشرة المبشرين بالجنة، جمع بين حكمة القيادة، واستجابة الدعوة، والشجاعة الفائقة.",
            iconEmoji = "🌊"
        ),
        SahabaHero(
            id = "jafar_al_tayyar",
            nameArabic = "جعفر بن أبي طالب (ذو الجناحين)",
            titleArabic = "شهيد مؤتة وأشبه الناس برسول الله خَلقاً وخُلقاً",
            famousQuoteOrHadith = "قال النبي ﷺ: «رأيتُ جعفراً يطير في الجنة مع الملائكة بجناحين من ياقوت».",
            epicFeats = "حمل راية الإسلام في معركة مؤتة بعد استشهاد زيد بن حارثة، فقُطعت يده اليمنى فأمسك الراية بيساره، فقُطعت يساره فاحتضن الراية بعضديه وصدره حتى استشهد ووجدوا في مقدم جسده أكثر من تسعين ضربة وطعنة ليس فيها شيء في ظهره.",
            battleHighlights = listOf("موقف الهجرة للحبشة أمام النجاشي", "معركة مؤتة العظمى"),
            moralAndHeroism = "الصمود الأسطوري حتى آخر قطرة دم دون التخلي عن راية التوحيد والحق.",
            iconEmoji = "🕊️"
        )
    )

    val wallpapers: List<IslamicWallpaperItem> = listOf(
        IslamicWallpaperItem(
            id = "wp_kaaba_gold",
            title = "الكعبة المشرفة بكسوتها الذهبية",
            category = "الكعبة والحرمين",
            resId = R.drawable.islamic_wallpaper_kaaba,
            description = "خلفية روحانية بدقة فائقة للكعبة المشرفة بكسوة الحرير الموشحة بالذهب وهالة النور النبوي."
        ),
        IslamicWallpaperItem(
            id = "wp_madina_glow",
            title = "المسجد النبوي والقبة الخضراء",
            category = "الكعبة والحرمين",
            resId = R.drawable.islamic_wallpaper_madina,
            description = "إشراقة سكينة للمسجد النبوي الشريف ومآذنه البهية ومظلات الصحن الشريف في ليلة مقمرة."
        ),
        IslamicWallpaperItem(
            id = "wp_museum_relics",
            title = "محراب الآثار الإسلامية الملكية",
            category = "عمارة إسلامية",
            resId = R.drawable.islamic_museum_banner,
            description = "أروقة المتحف الإسلامي التاريخي ومخطوطات القرآن الكريم وسيوف الشرف في أبهى حلة."
        ),
        IslamicWallpaperItem(
            id = "wp_quran_emblem",
            title = "شعار الفرقان بلس الذهبي",
            category = "خط وزخارف",
            resId = R.drawable.app_logo_emblem,
            description = "شعار المصحف الشريف الذهبي على الرِحال الأندلسي الفاخر بخلفية زمردية مريحة للعين."
        )
    )
}
