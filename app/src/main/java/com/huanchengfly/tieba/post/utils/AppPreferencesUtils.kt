package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class AppPreferencesUtils(context: Context) {
    private val preferences: SharedPreferences =
            context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    //PreferenceManager.getDefaultSharedPreferences(context)

    var loadPictureWhenScroll by SharedPreferenceDelegates.boolean(true)
    var littleTail by SharedPreferenceDelegates.string(key = "little_tail")

    private object SharedPreferenceDelegates {
        fun int(defaultValue: Int = 0) = object : ReadWriteProperty<AppPreferencesUtils, Int> {
            override fun getValue(thisRef: AppPreferencesUtils, property: KProperty<*>): Int {
                return thisRef.preferences.getInt(property.name, defaultValue)
            }

            override fun setValue(
                    thisRef: AppPreferencesUtils,
                    property: KProperty<*>,
                    value: Int
            ) {
                thisRef.preferences.edit().putInt(property.name, value).apply()
            }
        }

        fun long(defaultValue: Long = 0L) =
                object : ReadWriteProperty<AppPreferencesUtils, Long> {
                    override fun getValue(
                            thisRef: AppPreferencesUtils,
                            property: KProperty<*>
                    ): Long {
                        return thisRef.preferences.getLong(property.name, defaultValue)
                    }

                    override fun setValue(
                            thisRef: AppPreferencesUtils,
                            property: KProperty<*>,
                            value: Long
                    ) {
                        thisRef.preferences.edit().putLong(property.name, value).apply()
                    }
                }

        fun boolean(defaultValue: Boolean = false) =
                object : ReadWriteProperty<AppPreferencesUtils, Boolean> {
                    override fun getValue(
                            thisRef: AppPreferencesUtils,
                            property: KProperty<*>
                    ): Boolean {
                        return thisRef.preferences.getBoolean(property.name, defaultValue)
                    }

                    override fun setValue(
                            thisRef: AppPreferencesUtils,
                            property: KProperty<*>,
                            value: Boolean
                    ) {
                        thisRef.preferences.edit().putBoolean(property.name, value).apply()
                    }
                }

        fun float(defaultValue: Float = 0.0f) =
                object : ReadWriteProperty<AppPreferencesUtils, Float> {
                    override fun getValue(
                            thisRef: AppPreferencesUtils,
                            property: KProperty<*>
                    ): Float {
                        return thisRef.preferences.getFloat(property.name, defaultValue)
                    }

                    override fun setValue(
                            thisRef: AppPreferencesUtils,
                            property: KProperty<*>,
                            value: Float
                    ) {
                        thisRef.preferences.edit().putFloat(property.name, value).apply()
                    }
                }

        fun string(
                defaultValue: String? = null,
                key: String? = null
        ) =
                object : ReadWriteProperty<AppPreferencesUtils, String?> {
                    override fun getValue(
                            thisRef: AppPreferencesUtils,
                            property: KProperty<*>
                    ): String? {
                        return thisRef.preferences.getString(key ?: property.name, defaultValue)
                    }

                    override fun setValue(
                            thisRef: AppPreferencesUtils,
                            property: KProperty<*>,
                            value: String?
                    ) {
                        thisRef.preferences.edit().putString(key ?: property.name, value).apply()
                    }
                }

        fun stringSet(defaultValue: Set<String>? = null) =
                object : ReadWriteProperty<AppPreferencesUtils, Set<String>?> {
                    override fun getValue(
                            thisRef: AppPreferencesUtils,
                            property: KProperty<*>
                    ): Set<String>? {
                        return thisRef.preferences.getStringSet(property.name, defaultValue)
                    }

                    override fun setValue(
                            thisRef: AppPreferencesUtils,
                            property: KProperty<*>,
                            value: Set<String>?
                    ) {
                        thisRef.preferences.edit().putStringSet(property.name, value).apply()
                    }
                }
    }

    val Context.appPreferences: AppPreferencesUtils
        get() = AppPreferencesUtils(this)
}