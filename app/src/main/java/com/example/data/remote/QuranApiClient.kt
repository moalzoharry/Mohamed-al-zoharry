package com.example.data.remote

import com.example.data.model.Ayah
import com.example.data.repository.QuranStaticData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class QuranApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches complete verses and both Tafsirs (ar.jalalayn and ar.muyassar) for any Surah (1-114).
     */
    suspend fun fetchSurahAyahs(surahNumber: Int): List<Ayah>? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,ar.jalalayn,ar.muyassar"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) return@withContext null
            val bodyString = response.body?.string() ?: return@withContext null

            val rootJson = JSONObject(bodyString)
            if (rootJson.optInt("code") != 200) return@withContext null

            val dataArray = rootJson.getJSONArray("data")
            if (dataArray.length() < 3) return@withContext null

            val quranEdition = dataArray.getJSONObject(0)
            val jalalaynEdition = dataArray.getJSONObject(1)
            val muyassarEdition = dataArray.getJSONObject(2)

            val quranAyahs = quranEdition.getJSONArray("ayahs")
            val jalalaynAyahs = jalalaynEdition.getJSONArray("ayahs")
            val muyassarAyahs = muyassarEdition.getJSONArray("ayahs")

            val resultList = mutableListOf<Ayah>()
            for (i in 0 until quranAyahs.length()) {
                val qObj = quranAyahs.getJSONObject(i)
                val jObj = if (i < jalalaynAyahs.length()) jalalaynAyahs.getJSONObject(i) else null
                val mObj = if (i < muyassarAyahs.length()) muyassarAyahs.getJSONObject(i) else null

                val ayahNumberInSurah = qObj.getInt("numberInSurah")
                val qText = qObj.getString("text")
                val jText = jObj?.optString("text") ?: ""
                val mText = mObj?.optString("text") ?: ""
                val juz = qObj.optInt("juz", 1)
                val page = qObj.optInt("page", 1)

                val cleanText = QuranStaticData.normalizeArabic(qText)

                resultList.add(
                    Ayah(
                        surahNumber = surahNumber,
                        ayahNumber = ayahNumberInSurah,
                        textWithTashkeel = qText,
                        textClean = cleanText,
                        tafsirJalalayn = jText.ifEmpty { "تفسير الجلالين للآية الكريمة." },
                        tafsirMuyassar = mText.ifEmpty { "التفسير الميسر للآية الكريمة." },
                        juzNumber = juz,
                        pageNumber = page
                    )
                )
            }
            return@withContext resultList
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    suspend fun fetchAyahTranslation(
        surahNumber: Int,
        ayahNumber: Int,
        languageCode: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val edition = when (languageCode) {
                "en" -> "en.sahih"
                "fr" -> "fr.hamidullah"
                "tr" -> "tr.diyanet"
                "de" -> "de.bubenheim"
                "ur" -> "ur.jalandhry"
                else -> "en.sahih"
            }
            val url = "https://api.alquran.cloud/v1/ayah/$surahNumber:$ayahNumber/$edition"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            val json = JSONObject(body)
            if (json.optInt("code") == 200) {
                val data = json.getJSONObject("data")
                return@withContext data.optString("text", null)
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
