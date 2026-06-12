package com.mjland.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class TokenManager(private val context: Context) {
    companion object {
        private val ANILIST_TOKEN_KEY = stringPreferencesKey("anilist_token")
        
        @Volatile
        private var INSTANCE: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                val instance = TokenManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    val anilistTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ANILIST_TOKEN_KEY]
    }

    suspend fun saveAnilistToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[ANILIST_TOKEN_KEY] = token
        }
    }

    suspend fun clearAnilistToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(ANILIST_TOKEN_KEY)
        }
    }
}
