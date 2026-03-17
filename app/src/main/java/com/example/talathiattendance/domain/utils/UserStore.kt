package com.example.talathiattendance.domain.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.talathiattendance.domain.utils.Constants.PREF_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserStore(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(PREF_NAME)
    }

    fun getStringData(key:String): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(key)] ?: ""
    }

    suspend fun saveStringData(data: String,key:String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = data
        }
    }


}