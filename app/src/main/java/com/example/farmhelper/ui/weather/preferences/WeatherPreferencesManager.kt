package com.example.farmhelper.ui.weather.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private val Context.weatherDataStore by preferencesDataStore(name = "weather_preferences")

class WeatherPreferencesManager(private val context: Context) {

    companion object {
        private val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
        private val FAVORITE_LOCATIONS = stringPreferencesKey("favorite_locations")
        private val LAST_SELECTED_LOCATION = stringPreferencesKey("last_selected_location")
        private val CACHED_WEATHER_DATA = stringPreferencesKey("cached_weather_data")
    }

    // Recent Searches
    val recentSearches: Flow<List<String>> = context.weatherDataStore.data.map { prefs ->
        val jsonStr = prefs[RECENT_SEARCHES] ?: "[]"
        parseJsonArrayToList(jsonStr)
    }

    suspend fun addRecentSearch(location: String) {
        val cleaned = location.trim().titleCase()
        if (cleaned.isEmpty()) return

        context.weatherDataStore.edit { prefs ->
            val jsonStr = prefs[RECENT_SEARCHES] ?: "[]"
            val currentList = parseJsonArrayToList(jsonStr).toMutableList()
            currentList.remove(cleaned)
            currentList.add(0, cleaned)
            if (currentList.size > 8) {
                currentList.removeAt(currentList.size - 1)
            }
            prefs[RECENT_SEARCHES] = listToJsonArray(currentList)
        }
    }

    suspend fun removeRecentSearch(location: String) {
        val cleaned = location.trim().titleCase()
        context.weatherDataStore.edit { prefs ->
            val jsonStr = prefs[RECENT_SEARCHES] ?: "[]"
            val currentList = parseJsonArrayToList(jsonStr).toMutableList()
            currentList.remove(cleaned)
            prefs[RECENT_SEARCHES] = listToJsonArray(currentList)
        }
    }

    suspend fun clearRecentSearches() {
        context.weatherDataStore.edit { prefs ->
            prefs.remove(RECENT_SEARCHES)
        }
    }

    // Favorite Locations
    val favoriteLocations: Flow<List<String>> = context.weatherDataStore.data.map { prefs ->
        val jsonStr = prefs[FAVORITE_LOCATIONS] ?: "[]"
        parseJsonArrayToList(jsonStr)
    }

    suspend fun toggleFavorite(location: String) {
        val cleaned = location.trim().titleCase()
        if (cleaned.isEmpty()) return

        context.weatherDataStore.edit { prefs ->
            val jsonStr = prefs[FAVORITE_LOCATIONS] ?: "[]"
            val currentList = parseJsonArrayToList(jsonStr).toMutableList()
            if (currentList.contains(cleaned)) {
                currentList.remove(cleaned)
            } else {
                currentList.add(cleaned)
            }
            prefs[FAVORITE_LOCATIONS] = listToJsonArray(currentList)
        }
    }

    // Last Selected Location
    val lastSelectedLocation: Flow<String> = context.weatherDataStore.data.map { prefs ->
        prefs[LAST_SELECTED_LOCATION] ?: "Rajkot"
    }

    suspend fun saveLastSelectedLocation(location: String) {
        val cleaned = location.trim().titleCase()
        if (cleaned.isEmpty()) return
        context.weatherDataStore.edit { prefs ->
            prefs[LAST_SELECTED_LOCATION] = cleaned
        }
    }

    // Cached Weather Data (for Offline Support)
    val cachedWeatherData: Flow<String?> = context.weatherDataStore.data.map { prefs ->
        prefs[CACHED_WEATHER_DATA]
    }

    suspend fun saveCachedWeatherData(json: String) {
        context.weatherDataStore.edit { prefs ->
            prefs[CACHED_WEATHER_DATA] = json
        }
    }

    private fun parseJsonArrayToList(jsonStr: String): List<String> {
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun listToJsonArray(list: List<String>): String {
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    private fun String.titleCase(): String {
        return split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}
