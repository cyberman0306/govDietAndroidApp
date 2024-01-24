package com.hyunjun.govdietandroid

import android.content.Context
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hyunjun.govdietandroid.SettingDataStore.Companion.dataStore

class SettingDataStore {
    companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    }
}

val bootingGbdValue_key = stringPreferencesKey("bootingGbdValue_key")
@Composable
fun SaveDateShowPage() {
    val dataStore = (LocalContext.current).dataStore
    var testString = remember { mutableStateOf("false") }

    LaunchedEffect(Unit) {
        dataStore.data.collect { preferences ->
            testString.value = preferences[bootingGbdValue_key] ?: "CD003"
        }
    }

    LaunchedEffect(testString.value) {
        dataStore.edit { preferences ->
            preferences[bootingGbdValue_key] = testString.value
        }
    }

    Button(
        onClick = {
            testString.value = "saved!"
        }
    ) {
        Text(testString.value)
    }
}

@Composable
fun SaveAndLoadFunc(startValue: String) {
    val dataStore = (LocalContext.current).dataStore
    var startValue = remember { mutableStateOf(startValue) }

    LaunchedEffect(Unit) {
        dataStore.data.collect { preferences ->
            startValue.value = preferences[bootingGbdValue_key] ?: "CD003"
        }
    }

    LaunchedEffect(startValue.value) {
        dataStore.edit { preferences ->
            preferences[bootingGbdValue_key] = startValue.value
        }
    }
}