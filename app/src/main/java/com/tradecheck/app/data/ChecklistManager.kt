package com.tradecheck.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class ChecklistItem(
    val id: String,
    val textEn: String,
    val textFa: String,
    var isChecked: Boolean = false,
    val isCustom: Boolean = false
)

class ChecklistManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("trade_check_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ITEMS = "checklist_items_v1"
        private const val KEY_IS_PERSIAN = "is_persian"

        val DEFAULT_ITEMS = listOf(
            ChecklistItem(
                id = "1",
                textEn = "Higher Timeframe trend & key levels confirmed",
                textFa = "تأیید جهت روند و سطوح کلیدی تایم‌فریم بالاتر"
            ),
            ChecklistItem(
                id = "2",
                textEn = "Valid Setup & Entry Trigger formed",
                textFa = "تشکیل ستاپ و تریگر ورود معتبر"
            ),
            ChecklistItem(
                id = "3",
                textEn = "Strict Stop-Loss defined & placed",
                textFa = "تعیین و بررسی دقیق حد ضرر (Stop-Loss)"
            ),
            ChecklistItem(
                id = "4",
                textEn = "Risk per trade within 1% - 2% rule",
                textFa = "رعایت قانون ریسک حداکثر ۱ تا ۲ درصد سرمایه"
            ),
            ChecklistItem(
                id = "5",
                textEn = "Economic calendar checked (No high impact news)",
                textFa = "بررسی تقویم اقتصادی و عدم وجود اخبار سنگین"
            )
        )
    }

    fun loadItems(): List<ChecklistItem> {
        val rawJson = prefs.getString(KEY_ITEMS, null) ?: return DEFAULT_ITEMS
        return try {
            val jsonArray = JSONArray(rawJson)
            val list = mutableListOf<ChecklistItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ChecklistItem(
                        id = obj.getString("id"),
                        textEn = obj.getString("textEn"),
                        textFa = obj.getString("textFa"),
                        isChecked = obj.getBoolean("isChecked"),
                        isCustom = obj.optBoolean("isCustom", false)
                    )
                )
            }
            if (list.isEmpty()) DEFAULT_ITEMS else list
        } catch (e: Exception) {
            DEFAULT_ITEMS
        }
    }

    fun saveItems(items: List<ChecklistItem>) {
        val jsonArray = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("textEn", item.textEn)
                put("textFa", item.textFa)
                put("isChecked", item.isChecked)
                put("isCustom", item.isCustom)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_ITEMS, jsonArray.toString()).apply()
    }

    fun isPersian(): Boolean {
        return prefs.getBoolean(KEY_IS_PERSIAN, true) // Default Persian
    }

    fun setPersian(isFa: Boolean) {
        prefs.edit().putBoolean(KEY_IS_PERSIAN, isFa).apply()
    }
}
