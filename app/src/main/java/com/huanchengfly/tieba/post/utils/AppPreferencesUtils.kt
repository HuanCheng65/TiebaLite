package com.huanchengfly.tieba.post.utils

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    var toolbarPrimaryColor by DataStoreDelegates.boolean(
        defaultValue = false,
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

    var oksignWorkId by DataStoreDelegates.string()

    var oksignSlowMode by DataStoreDelegates.boolean(
        defaultValue = true,
        key = "oksign_slow_mode"
    )

    var oksignUseOfficialOksign by DataStoreDelegates.boolean(
        defaultValue = false,
        key = "oksign_use_official_oksign"
    )

    var postOrReplyWarning by DataStoreDelegates.boolean(defaultValue = true)

    var radius by DataStoreDelegates.int(defaultValue = 8)

    var signDay by DataStoreDelegates.int(defaultValue = -1, key = "sign_day")

    var showBlockTip by DataStoreDelegates.boolean(defaultValue = true)

    var showBothUsernameAndNickname by DataStoreDelegates.boolean(
        defaultValue = false,
        key = "show_both_username_and_nickname"
    )

    var showExperimentalFeatures by DataStoreDelegates.boolean(defaultValue = false)

    var showShortcutInThread by DataStoreDelegates.boolean(defaultValue = true)

    var showTopForumInNormalList by DataStoreDelegates.boolean(
        defaultValue = true,
        key = "show_top_forum_in_normal_list"
    )

    var statusBarDarker by DataStoreDelegates.boolean(
        defaultValue = true,
        key = "status_bar_darker"
    )

    var theme by DataStoreDelegates.string(defaultValue = ThemeUtil.THEME_BLUE)

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
            private var state by mutableStateOf(defaultValue)
            private var stateInitialized = false

            override fun getValue(thisRef: AppPreferencesUtils, property: KProperty<*>): Int {
                if (!stateInitialized) {
                    stateInitialized = true
                    state = thisRef.preferencesDataStore.getInt(key ?: property.name, defaultValue)
                }
                return state
            }

            override fun setValue(
                thisRef: AppPreferencesUtils,
                property: KProperty<*>,
                value: Int
            ) {
                state = value
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
            private var state by mutableStateOf(defaultValue)
            private var stateInitialized = false

            override fun getValue(thisRef: AppPreferencesUtils, property: KProperty<*>): String? {
                if (!stateInitialized) {
                    stateInitialized = true
                    state = thisRef.preferencesDataStore.getString(key ?: property.name)
                        ?: defaultValue
                }
                return state
            }

            override fun setValue(
                thisRef: AppPreferencesUtils,
                property: KProperty<*>,
                value: String?
            ) {
                state = value
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
            private var state by mutableStateOf(defaultValue)
            private var stateInitialized = false

            override fun getValue(thisRef: AppPreferencesUtils, property: KProperty<*>): Float {
                if (!stateInitialized) {
                    stateInitialized = true
                    state =
                        thisRef.preferencesDataStore.getFloat(key ?: property.name, defaultValue)
                }
                return state
            }

            override fun setValue(
                thisRef: AppPreferencesUtils,
                property: KProperty<*>,
                value: Float
            ) {
                state = value
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
            private var state by mutableStateOf(defaultValue)
            private var stateInitialized = false

            override fun getValue(thisRef: AppPreferencesUtils, property: KProperty<*>): Boolean {
                if (!stateInitialized) {
                    stateInitialized = true
                    state =
                        thisRef.preferencesDataStore.getBoolean(key ?: property.name, defaultValue)
                }
                return state
            }

            override fun setValue(
                thisRef: AppPreferencesUtils,
                property: KProperty<*>,
                value: Boolean
            ) {
                state = value
                MainScope().launch(Dispatchers.IO) {
                    state = value
                    thisRef.preferencesDataStore.edit {
                        it[booleanPreferencesKey(key ?: property.name)] = value
                    }
                }
            }
        }
    }

}

val Context.appPreferences: AppPreferencesUtils
    get() = AppPreferencesUtils(this)