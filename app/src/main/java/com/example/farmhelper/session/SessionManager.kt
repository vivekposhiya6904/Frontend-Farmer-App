package com.example.farmhelper.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

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

    }

    suspend fun saveSession(
        access_token: String,
        refresh_token: String,
        token_type: String,
        expires_in: String,
        user_id: String,
        full_name: String,
        email: String,
        mobile: String,
        is_active: String,
        is_login_in: String
    ) {

        context.dataStore.edit { preferences ->

            preferences[ACCESS_TOKEN] = access_token
            preferences[REFRESH_TOKEN] = refresh_token
            preferences[TOKEN_TYPE] = token_type
            preferences[EXPIRES_IN] = expires_in
            preferences[USER_ID] = user_id
            preferences[FULL_NAME] = full_name
            preferences[EMAIL] = email
            preferences[MOBILE] = mobile
            preferences[IS_ACTIVE] = is_active
            preferences[IS_LOGIN_IN] = is_login_in

        }

    }

    suspend fun logout() {

        context.dataStore.edit {

            it.clear()

        }

    }

}