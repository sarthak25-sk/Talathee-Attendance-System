package com.example.talathiattendance.domain.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.talathiattendance.data.models.UserModel
import com.example.talathiattendance.domain.utils.Constants.USER_EMAIL_KEY
import com.example.talathiattendance.domain.utils.Constants.USER_ID_KEY
import com.example.talathiattendance.domain.utils.Constants.USER_IS_ADMIN_KEY
import com.example.talathiattendance.domain.utils.Constants.USER_NAME_KEY
import com.example.talathiattendance.domain.utils.Constants.USER_PREFERENCES_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionManager(private val context:Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME)

    companion object {
        val LAST_USER_ID_KEY = stringPreferencesKey(name = USER_ID_KEY)
        val LAST_USER_NAME_KEY = stringPreferencesKey(name = USER_NAME_KEY)
        val LAST_USER_EMAIL_KEY = stringPreferencesKey(name = USER_EMAIL_KEY)
        val LAST_USER_IS_ADMIN_KEY = booleanPreferencesKey(name = USER_IS_ADMIN_KEY)
    }
    suspend fun setUserPref(user: UserModel) {
        context.dataStore.edit { preference ->
            preference[LAST_USER_ID_KEY] = user.uid.toString()
            preference[LAST_USER_NAME_KEY] = user.name.toString()
            preference[LAST_USER_EMAIL_KEY] = user.email.toString()
            preference[LAST_USER_IS_ADMIN_KEY] = user.isAdmin.toString().toBoolean()
        }
    }

    suspend fun clearUserPref() {
        context.dataStore.edit { preference ->
            preference.clear()
        }
    }
    val getUserPref: Flow<UserModel> = context.dataStore.data
        .map { preferences ->
            UserModel(
                uid = preferences[LAST_USER_ID_KEY] ?:"",
                name = preferences[LAST_USER_NAME_KEY] ?:"",
                email = preferences[LAST_USER_EMAIL_KEY] ?:"",
                isAdmin = preferences[LAST_USER_IS_ADMIN_KEY] ?: false
            )

        }


}