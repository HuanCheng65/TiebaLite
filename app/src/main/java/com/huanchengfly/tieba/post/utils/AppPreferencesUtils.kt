package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.huanchengfly.tieba.post.*
import com.huanchengfly.tieba.post.utils.ThemeUtil.TRANSLUCENT_THEME_LIGHT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


open class AppPreferencesUtils(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val preferencesDataStore: DataStore<Preferences> = context.dataStore

    var autoSign by DataStoreDelegates.boolean(defaultValue = false, key = "auto_sign")

    var autoSignTime by DataStoreDelegates.string(
        defaultValue = "09:00",
        key = "auto_sign_time"
    )

    var checkCIUpdate by DataStoreDelegates.boolean(
        defaultValue = false
    )

    var collectThreadSeeLz by DataStoreDelegates.boolean(
        defaultValue = true,
        key = "collect_thread_see_lz"
    )

    var customPrimaryColor by DataStoreDelegates.string(key = "custom_primary_color")

    var customStatusBarFontDark by DataStoreDelegates.boolean(
        defaultValue = false,
        key = "custom_status_bar_font_dark"
    )

    var customToolbarPrimaryColor by DataStoreDelegates.boolean(
        defaultValue = true,
        key = "custom_toolbar_primary_color"
    )

    var defaultSortType by DataStoreDelegates.string(
        key = "default_sort_type",
        defaultValue = "0"
    )

    var darkTheme by DataStoreDelegates.string(key = "dark_theme", defaultValue = "dark")

    var followSystemNight by DataStoreDelegates.boolean(
        defaultValue = true,
        key = "follow_system_night"
    )

    var fontScale by DataStoreDelegates.float(defaultValue = 1.0f)

    var forumFabFunction by DataStoreDelegates.string(defaultValue = "post")

    var hideExplore by DataStoreDelegates.boolean(defaultValue = false)

    var hideForumIntroAndStat by DataStoreDelegates.boolean(defaultValue = false)

    var homePageScroll by DataStoreDelegates.boolean(defaultValue = false)

    var imageLoadType by DataStoreDelegates.string(
        key = "image_load_type",
        defaultValue = "0"
    )

    var listItemsBackgroundIntermixed by DataStoreDelegates.boolean(defaultValue = true)

    var listSingle by DataStoreDelegates.boolean(defaultValue = false)

    var littleTail by DataStoreDelegates.string(key = "little_tail")

    var loadPictureWhenScroll by DataStoreDelegates.boolean(defaultValue = true)

    var oldTheme by DataStoreDelegates.string(key = "old_theme")

    var oksignSlowMode by DataStoreDelegates.boolean(
        defaultValue = true,
        key = "oksign_slow_mode"
    )

    var postOrReplyWarning by DataStoreDelegates.boolean(defaultValue = true)

    var radius by DataStoreDelegates.int(defaultValue = 8)

    var signDay by DataStoreDelegates.int(defaultValue = -1, key = "sign_day")

    var showBothUsernameAndNickname by DataStoreDelegates.boolean(
        defaultValue = false,
        key = "show_both_username_and_nickname"
    )

    var showShortcutInThread by DataStoreDelegates.boolean(defaultValue = true)

    var showTopForumInNormalList by DataStoreDelegates.boolean(
        defaultValue = true,
        key = "show_top_forum_in_normal_list"
    )

    var statusBarDarker by DataStoreDelegates.boolean(
        defaultValue = true,
        key = "status_bar_darker"
    )

    var theme by DataStoreDelegates.string(defaultValue = ThemeUtil.THEME_WHITE)

    var translucentBackgroundAlpha by DataStoreDelegates.int(
        defaultValue = 255,
        key = "translucent_background_alpha"
    )

    var translucentBackgroundBlur by DataStoreDelegates.int(key = "translucent_background_blur")

    var translucentBackgroundTheme by DataStoreDelegates.int(
        defaultValue = TRANSLUCENT_THEME_LIGHT,
        key = "translucent_background_theme"
    )

    var translucentThemeBackgroundPath by DataStoreDelegates.string(key = "translucent_theme_background_path")

    var translucentPrimaryColor by DataStoreDelegates.string(key = "translucent_primary_color")

    var useCustomTabs by DataStoreDelegates.boolean(
        defaultValue = true,
        key = "use_custom_tabs"
    )

    var useWebView by DataStoreDelegates.boolean(defaultValue = true, key = "use_webview")

    private object DataStoreDelegates {
        fun int(
            defaultValue: Int = 0,
            key: String? = null
        ) = object : ReadWriteProperty<AppPreferencesUtils, Int> {
            override fun getValue(thisRef: AppPreferencesUtils, property: KProperty<*>): Int {
                return thisRef.preferencesDataStore.getInt(key ?: property.name, defaultValue)
            }

            override fun setValue(
                thisRef: AppPreferencesUtils,
                property: KProperty<*>,
                value: Int
            ) {
                MainScope().launch(Dispatchers.IO) {
                    thisRef.preferencesDataStore.edit {
                        it[intPreferencesKey(key ?: property.name)] = value
                    }
                }
            }
        }

        fun string(
            defaultValue: String? = null,
            key: String? = null
        ) = object : ReadWriteProperty<AppPreferencesUtils, String?> {
            override fun getValue(thisRef: AppPreferencesUtils, property: KProperty<*>): String? {
                return thisRef.preferencesDataStore.getString(key ?: property.name)
                    ?: defaultValue
            }

            override fun setValue(
                thisRef: AppPreferencesUtils,
                property: KProperty<*>,
                value: String?
            ) {
                MainScope().launch(Dispatchers.IO) {
                    thisRef.preferencesDataStore.edit {
                        if (value == null) {
                            it.remove(stringPreferencesKey(key ?: property.name))
                        } else {
                            it[stringPreferencesKey(key ?: property.name)] = value
                        }
                    }
                }
            }
        }

        fun float(
            defaultValue: Float = 0F,
            key: String? = null
        ) = object : ReadWriteProperty<AppPreferencesUtils, Float> {
            override fun getValue(thisRef: AppPreferencesUtils, property: KProperty<*>): Float {
                return thisRef.preferencesDataStore.getFloat(key ?: property.name, defaultValue)
            }

            override fun setValue(
                thisRef: AppPreferencesUtils,
                property: KProperty<*>,
                value: Float
            ) {
                MainScope().launch(Dispatchers.IO) {
                    thisRef.preferencesDataStore.edit {
                        it[floatPreferencesKey(key ?: property.name)] = value
                    }
                }
            }
        }

        fun boolean(
            defaultValue: Boolean = false,
            key: String? = null
        ) = object : ReadWriteProperty<AppPreferencesUtils, Boolean> {
            override fun getValue(thisRef: AppPreferencesUtils, property: KProperty<*>): Boolean {
                return thisRef.preferencesDataStore.getBoolean(key ?: property.name, defaultValue)
            }

            override fun setValue(
                thisRef: AppPreferencesUtils,
                property: KProperty<*>,
                value: Boolean
            ) {
                MainScope().launch(Dispatchers.IO) {
                    thisRef.preferencesDataStore.edit {
                        it[booleanPreferencesKey(key ?: property.name)] = value
                    }
                }
            }
        }
    }

    private object SharedPreferenceDelegates {
        fun int(
            defaultValue: Int = 0,
            key: String? = null
        ) = object : ReadWriteProperty<AppPreferencesUtils, Int> {
            override fun getValue(thisRef: AppPreferencesUtils, property: KProperty<*>): Int {
                return thisRef.preferences.getInt(key ?: property.name, defaultValue)
            }

            override fun setValue(
                thisRef: AppPreferencesUtils,
                property: KProperty<*>,
                value: Int
            ) {
                thisRef.preferences.edit().putInt(key ?: property.name, value).apply()
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

        fun boolean(
            defaultValue: Boolean = false,
            key: String? = null
        ) =
            object : ReadWriteProperty<AppPreferencesUtils, Boolean> {
                override fun getValue(
                    thisRef: AppPreferencesUtils,
                    property: KProperty<*>
                ): Boolean {
                    return thisRef.preferences.getBoolean(key ?: property.name, defaultValue)
                }

                override fun setValue(
                    thisRef: AppPreferencesUtils,
                    property: KProperty<*>,
                    value: Boolean
                ) {
                    thisRef.preferences.edit().putBoolean(key ?: property.name, value).apply()
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
}

val Context.appPreferences: AppPreferencesUtils
    get() = AppPreferencesUtils(this)