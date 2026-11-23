package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.beityDataStore: DataStore<Preferences> by preferencesDataStore(name = "beity_prefs")

class PreferencesRepository(private val context: Context) {
    private val darkModeKey = booleanPreferencesKey("dark_mode_enabled")

    val darkModeFlow: Flow<Boolean> = context.beityDataStore.data.map { prefs ->
        prefs[darkModeKey] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.beityDataStore.edit { prefs ->
            prefs[darkModeKey] = enabled
        }
    }
}
