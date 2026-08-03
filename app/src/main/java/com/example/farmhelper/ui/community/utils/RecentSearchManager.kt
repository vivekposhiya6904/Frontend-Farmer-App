package com.example.farmhelper.ui.community.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RecentSearchManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getRecentSearches(): List<String> {
        val json = prefs.getString(KEY_RECENT_SEARCHES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        val current = getRecentSearches().toMutableList()
        current.remove(trimmed) // Move to top if already present
        current.add(0, trimmed)

        // Limit to top 10 recent searches
        val trimmedList = current.take(10)
        saveList(trimmedList)
    }

    fun deleteRecentSearch(query: String) {
        val current = getRecentSearches().toMutableList()
        current.remove(query)
        saveList(current)
    }

    fun clearAllRecentSearches() {
        prefs.edit().remove(KEY_RECENT_SEARCHES).apply()
    }

    private fun saveList(list: List<String>) {
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_RECENT_SEARCHES, json).apply()
    }

    companion object {
        private const val PREF_NAME = "farm_helper_community_recent_searches"
        private const val KEY_RECENT_SEARCHES = "key_recent_searches"
    }
}
