package com.omni.ai.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.omniDataStore: DataStore<Preferences> by preferencesDataStore(name = "omni_settings")

data class AppSettings(
    val apiKey: String = "",
    val model: String = "",
    val temperature: Float = 0.8f,
    val companionName: String = "OmNi",
)

class SettingsStore(private val context: Context) {

    private object Keys {
        val API_KEY = stringPreferencesKey("api_key")
        val MODEL = stringPreferencesKey("model")
        val TEMPERATURE = floatPreferencesKey("temperature")
        val COMPANION_NAME = stringPreferencesKey("companion_name")
    }

    val settings: Flow<AppSettings> = context.omniDataStore.data.map { prefs ->
        AppSettings(
            apiKey = prefs[Keys.API_KEY].orEmpty(),
            model = prefs[Keys.MODEL].orEmpty(),
            temperature = prefs[Keys.TEMPERATURE] ?: 0.8f,
            companionName = prefs[Keys.COMPANION_NAME] ?: "OmNi",
        )
    }

    suspend fun save(settings: AppSettings) {
        context.omniDataStore.edit { prefs ->
            prefs[Keys.API_KEY] = settings.apiKey
            prefs[Keys.MODEL] = settings.model
            prefs[Keys.TEMPERATURE] = settings.temperature
            prefs[Keys.COMPANION_NAME] = settings.companionName
        }
    }
}
