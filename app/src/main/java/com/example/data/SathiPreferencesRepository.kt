package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ScriptTone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sathiDataStore: DataStore<Preferences> by preferencesDataStore(name = "sathi_prefs")

data class SathiPreferences(
    val autoSpeakEnabled: Boolean = true,
    val autoExecuteActions: Boolean = true,
    val preferredTone: ScriptTone = ScriptTone.FRIENDLY
)

class SathiPreferencesRepository(private val context: Context) {

    private object Keys {
        val AUTO_SPEAK = booleanPreferencesKey("auto_speak_enabled")
        val AUTO_EXECUTE = booleanPreferencesKey("auto_execute_actions")
        val PREFERRED_TONE = stringPreferencesKey("preferred_tone")
    }

    val preferences: Flow<SathiPreferences> = context.sathiDataStore.data.map { prefs ->
        val toneName = prefs[Keys.PREFERRED_TONE]
        val tone = ScriptTone.entries.firstOrNull { it.name == toneName } ?: ScriptTone.FRIENDLY
        SathiPreferences(
            autoSpeakEnabled = prefs[Keys.AUTO_SPEAK] ?: true,
            autoExecuteActions = prefs[Keys.AUTO_EXECUTE] ?: true,
            preferredTone = tone
        )
    }

    suspend fun setAutoSpeak(enabled: Boolean) {
        context.sathiDataStore.edit { it[Keys.AUTO_SPEAK] = enabled }
    }

    suspend fun setAutoExecute(enabled: Boolean) {
        context.sathiDataStore.edit { it[Keys.AUTO_EXECUTE] = enabled }
    }

    suspend fun setPreferredTone(tone: ScriptTone) {
        context.sathiDataStore.edit { it[Keys.PREFERRED_TONE] = tone.name }
    }
}
