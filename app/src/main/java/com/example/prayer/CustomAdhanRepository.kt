package com.example.prayer

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class CustomAdhanItem(
    val id: String,
    val title: String,
    val filePath: String,
    val dateAddedMs: Long = System.currentTimeMillis(),
    val assignedPrayers: Set<PrayerType> = emptySet() // If empty, applies to all prayers
)

class CustomAdhanRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("custom_adhans_prefs", Context.MODE_PRIVATE)
    private val adhanDir: File = File(context.filesDir, "custom_adhans").apply {
        if (!exists()) mkdirs()
    }

    private val _adhansFlow = MutableStateFlow<List<CustomAdhanItem>>(loadAdhans())
    val adhansFlow: StateFlow<List<CustomAdhanItem>> = _adhansFlow.asStateFlow()

    private fun loadAdhans(): List<CustomAdhanItem> {
        val jsonStr = prefs.getString("custom_adhans_list", null) ?: return emptyList()
        val list = mutableListOf<CustomAdhanItem>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val title = obj.getString("title")
                val filePath = obj.getString("filePath")
                val dateAddedMs = obj.optLong("dateAddedMs", System.currentTimeMillis())
                val prayersArr = obj.optJSONArray("assignedPrayers")
                val assignedPrayers = mutableSetOf<PrayerType>()
                if (prayersArr != null) {
                    for (j in 0 until prayersArr.length()) {
                        try {
                            assignedPrayers.add(PrayerType.valueOf(prayersArr.getString(j)))
                        } catch (e: Exception) {}
                    }
                }
                if (File(filePath).exists()) {
                    list.add(CustomAdhanItem(id, title, filePath, dateAddedMs, assignedPrayers))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun saveAdhans(list: List<CustomAdhanItem>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("filePath", item.filePath)
                put("dateAddedMs", item.dateAddedMs)
                val prayersArr = JSONArray()
                item.assignedPrayers.forEach { prayersArr.put(it.name) }
                put("assignedPrayers", prayersArr)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("custom_adhans_list", jsonArray.toString()).apply()
        _adhansFlow.value = list
    }

    fun addCustomAdhanFromUri(uri: Uri, title: String): CustomAdhanItem? {
        val id = UUID.randomUUID().toString()
        val file = File(adhanDir, "adhan_$id.mp3")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            val displayTitle = title.ifBlank { "أذان مخصص ${System.currentTimeMillis() % 1000}" }
            val item = CustomAdhanItem(
                id = id,
                title = displayTitle,
                filePath = file.absolutePath,
                dateAddedMs = System.currentTimeMillis(),
                assignedPrayers = emptySet()
            )
            val updated = _adhansFlow.value + item
            saveAdhans(updated)
            item
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun togglePrayerAssignment(id: String, prayer: PrayerType) {
        val updated = _adhansFlow.value.map { item ->
            if (item.id == id) {
                val newPrayers = if (item.assignedPrayers.contains(prayer)) {
                    item.assignedPrayers - prayer
                } else {
                    item.assignedPrayers + prayer
                }
                item.copy(assignedPrayers = newPrayers)
            } else {
                item
            }
        }
        saveAdhans(updated)
    }

    fun deleteAdhan(id: String) {
        val item = _adhansFlow.value.firstOrNull { it.id == id }
        if (item != null) {
            try {
                File(item.filePath).delete()
            } catch (e: Exception) {}
            val updated = _adhansFlow.value.filter { it.id != id }
            saveAdhans(updated)
        }
    }

    fun getAdhanForPrayer(prayer: PrayerType): CustomAdhanItem? {
        // Return item specifically assigned to this prayer, or first generic (empty assignments)
        val all = _adhansFlow.value
        return all.firstOrNull { it.assignedPrayers.contains(prayer) }
            ?: all.firstOrNull { it.assignedPrayers.isEmpty() }
    }
}
