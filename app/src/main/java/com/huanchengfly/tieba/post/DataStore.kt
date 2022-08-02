package com.huanchengfly.tieba.post

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object DataStoreConst {
    const val DATA_STORE_NAME = "app_preferences"

    val KEY_THEME = stringPreferencesKey("theme")
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = DataStoreConst.DATA_STORE_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "settings"))
    }
)

fun DataStore<Preferences>.getInt(key: String, defaultValue: Int): Int {
    var resultValue = defaultValue

    runBlocking {
        BaseApplication.INSTANCE.dataStore.data.first {
            resultValue = it[intPreferencesKey(key)] ?: resultValue
            true
        }
    }

    return resultValue
}

fun DataStore<Preferences>.getString(key: String, defaultValue: String? = null): String? {
    var resultValue = defaultValue

    runBlocking {
        BaseApplication.INSTANCE.dataStore.data.first {
            resultValue = it[stringPreferencesKey(key)] ?: resultValue
            true
        }
    }

    return resultValue
}

fun DataStore<Preferences>.getStringSet(
    key: String,
    defaultValues: MutableSet<String>? = null
): MutableSet<String>? {
    var resultValue = defaultValues

    runBlocking {
        BaseApplication.INSTANCE.dataStore.data.first {
            resultValue = it[stringSetPreferencesKey(key)]?.toMutableSet() ?: resultValue
            true
        }
    }

    return resultValue
}

fun DataStore<Preferences>.getBoolean(key: String, defaultValue: Boolean): Boolean {
    var resultValue = defaultValue

    runBlocking {
        BaseApplication.INSTANCE.dataStore.data.first {
            resultValue = it[booleanPreferencesKey(key)] ?: resultValue
            true
        }
    }

    return resultValue
}

fun DataStore<Preferences>.getFloat(key: String, defaultValue: Float): Float {
    var resultValue = defaultValue

    runBlocking {
        BaseApplication.INSTANCE.dataStore.data.first {
            resultValue = it[floatPreferencesKey(key)] ?: resultValue
            true
        }
    }

    return resultValue
}

fun DataStore<Preferences>.getLong(key: String, defaultValue: Long): Long {
    var resultValue = defaultValue

    runBlocking {
        BaseApplication.INSTANCE.dataStore.data.first {
            resultValue = it[longPreferencesKey(key)] ?: resultValue
            true
        }
    }

    return resultValue
}