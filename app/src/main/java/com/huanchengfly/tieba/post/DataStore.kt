package com.huanchengfly.tieba.post

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

object DataStoreConst {
    const val DATA_STORE_NAME = "app_data"
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DataStoreConst.DATA_STORE_NAME)