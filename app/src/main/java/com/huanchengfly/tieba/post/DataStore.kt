package com.huanchengfly.tieba.post

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object DataStoreConst {
    const val DATA_STORE_NAME = "app_preferences"
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = DataStoreConst.DATA_STORE_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "settings"))
    }
)

fun DataStore<Preferences>.putString(key: String, value: String? = null) {
    MainScope().launch(Dispatchers.IO) {
        BaseApplication.INSTANCE.dataStore.edit {
            if (value == null) {
                it.remove(stringPreferencesKey(key))
            } else {
                it[stringPreferencesKey(key)] = value
            }
        }
    }
}

fun DataStore<Preferences>.putBoolean(key: String, value: Boolean) {
    MainScope().launch(Dispatchers.IO) {
        BaseApplication.INSTANCE.dataStore.edit {
            it[booleanPreferencesKey(key)] = value
        }
    }
}

fun DataStore<Preferences>.putInt(key: String, value: Int) {
    MainScope().launch(Dispatchers.IO) {
        BaseApplication.INSTANCE.dataStore.edit {
            it[intPreferencesKey(key)] = value
        }
    }
}

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

fun DataStore<Preferences>.getString(key: String): String? {
    var resultValue: String? = null

    runBlocking {
        BaseApplication.INSTANCE.dataStore.data.first {
            resultValue = it[stringPreferencesKey(key)]
            true
        }
    }

    return resultValue
}

fun DataStore<Preferences>.getString(key: String, defaultValue: String): String {
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

class DataStorePreference : PreferenceDataStore() {
    override fun putString(key: String, value: String?) {
        MainScope().launch(Dispatchers.IO) {
            BaseApplication.INSTANCE.dataStore.edit {
                if (value == null) {
                    it.remove(stringPreferencesKey(key))
                } else {
                    it[stringPreferencesKey(key)] = value
                }
            }
        }
    }

    override fun putStringSet(key: String, values: MutableSet<String>?) {
        MainScope().launch(Dispatchers.IO) {
            BaseApplication.INSTANCE.dataStore.edit {
                if (values == null) {
                    it.remove(stringSetPreferencesKey(key))
                } else {
                    it[stringSetPreferencesKey(key)] = values
                }
            }
        }
    }

    override fun putInt(key: String, value: Int) {
        MainScope().launch(Dispatchers.IO) {
            BaseApplication.INSTANCE.dataStore.edit {
                it[intPreferencesKey(key)] = value
            }
        }
    }

    override fun putLong(key: String, value: Long) {
        MainScope().launch(Dispatchers.IO) {
            BaseApplication.INSTANCE.dataStore.edit {
                it[longPreferencesKey(key)] = value
            }
        }
    }

    override fun putFloat(key: String, value: Float) {
        MainScope().launch(Dispatchers.IO) {
            BaseApplication.INSTANCE.dataStore.edit {
                it[floatPreferencesKey(key)] = value
            }
        }
    }

    override fun putBoolean(key: String, value: Boolean) {
        MainScope().launch(Dispatchers.IO) {
            BaseApplication.INSTANCE.dataStore.edit {
                it[booleanPreferencesKey(key)] = value
            }
        }
    }

    override fun getString(key: String, defValue: String?): String? {
        return BaseApplication.INSTANCE.dataStore.getString(key) ?: defValue
    }

    override fun getStringSet(
        key: String,
        defValues: MutableSet<String>?
    ): MutableSet<String>? {
        return BaseApplication.INSTANCE.dataStore.getStringSet(key, defValues)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return BaseApplication.INSTANCE.dataStore.getInt(key, defValue)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return BaseApplication.INSTANCE.dataStore.getLong(key, defValue)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return BaseApplication.INSTANCE.dataStore.getFloat(key, defValue)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return BaseApplication.INSTANCE.dataStore.getBoolean(key, defValue)
    }
}