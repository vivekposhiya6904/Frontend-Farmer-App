package com.example.farmhelper.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import com.example.farmhelper.ui.auth.models.AuthState

private val Context.dataStore by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {

        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val TOKEN_TYPE = stringPreferencesKey("token_type")
        private val EXPIRES_IN = stringPreferencesKey("expires_in")

        private val USER_ID = stringPreferencesKey("user_id")
        private val FULL_NAME = stringPreferencesKey("full_name")
        private val EMAIL = stringPreferencesKey("email")
        private val MOBILE = stringPreferencesKey("mobile")
        private val IS_ACTIVE = stringPreferencesKey("is_active")

        private val IS_LOGIN_IN = stringPreferencesKey("is_login_in")
        private val IS_EXPIRED = stringPreferencesKey("is_expired")

        @Volatile
        private var cachedAccessToken: String? = null

        @Volatile
        private var cachedRefreshToken: String? = null

        @Volatile
        private var isLoggedInCached: Boolean? = null
    }

    suspend fun saveSession(
        access_token: String,
        refresh_token: String,
        token_type: String,
        expires_in: Int,
        user_id: String,
        full_name: String,
        email: String,
        mobile: String,
        is_active: Boolean,
    ) {
        cachedAccessToken = access_token
        cachedRefreshToken = refresh_token
        isLoggedInCached = true

        context.dataStore.edit { preferences ->

            preferences[ACCESS_TOKEN] = access_token
            preferences[REFRESH_TOKEN] = refresh_token
            preferences[TOKEN_TYPE] = token_type
            preferences[EXPIRES_IN] = expires_in.toString()
            preferences[USER_ID] = user_id
            preferences[FULL_NAME] = full_name
            preferences[EMAIL] = email
            preferences[MOBILE] = mobile
            preferences[IS_ACTIVE] = is_active.toString()
            preferences[IS_LOGIN_IN] = "true"

        }

    }

    suspend fun logout() {
        cachedAccessToken = null
        cachedRefreshToken = null
        isLoggedInCached = false

        context.dataStore.edit {

            it.clear()

        }

    }

    val isLoggedIn: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[IS_LOGIN_IN] == "true"
        }

    val accessToken: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN]
        }

    val refreshToken: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN]
        }

    val userFullName: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[FULL_NAME]
        }

    fun getAccessTokenSync(): String? {
        if (isLoggedInCached == false) return null
        val cached = cachedAccessToken
        if (cached != null) return cached

        return kotlinx.coroutines.runBlocking {
            try {
                val prefs = context.dataStore.data.firstOrNull()
                isLoggedInCached = prefs?.get(IS_LOGIN_IN) == "true"
                cachedAccessToken = prefs?.get(ACCESS_TOKEN)
                cachedRefreshToken = prefs?.get(REFRESH_TOKEN)
                cachedAccessToken
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getRefreshTokenSync(): String? {
        if (isLoggedInCached == false) return null
        val cached = cachedRefreshToken
        if (cached != null) return cached

        return kotlinx.coroutines.runBlocking {
            try {
                val prefs = context.dataStore.data.firstOrNull()
                isLoggedInCached = prefs?.get(IS_LOGIN_IN) == "true"
                cachedAccessToken = prefs?.get(ACCESS_TOKEN)
                cachedRefreshToken = prefs?.get(REFRESH_TOKEN)
                cachedRefreshToken
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun updateAccessToken(newAccessToken: String) {
        cachedAccessToken = newAccessToken
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = newAccessToken
        }
    }

    suspend fun markSessionExpired() {
        cachedAccessToken = null
        cachedRefreshToken = null
        isLoggedInCached = false

        context.dataStore.edit { preferences ->
            preferences[IS_LOGIN_IN] = "false"
            preferences[IS_EXPIRED] = "true"
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
        }
    }

    suspend fun clearExpiredState() {
        context.dataStore.edit { preferences ->
            preferences[IS_EXPIRED] = "false"
        }
    }

    val authState: Flow<AuthState> = context.dataStore.data.map { preferences ->
        val isLoggedIn = preferences[IS_LOGIN_IN] == "true"
        val isExpired = preferences[IS_EXPIRED] == "true"
        val accessToken = preferences[ACCESS_TOKEN]

        if (isExpired) {
            AuthState.SessionExpired
        } else if (isLoggedIn && !accessToken.isNullOrEmpty()) {
            AuthState.Authenticated
        } else {
            AuthState.Unauthenticated
        }
    }

}