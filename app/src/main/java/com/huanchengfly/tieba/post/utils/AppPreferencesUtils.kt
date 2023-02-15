package com.huanchengfly.tieba.post.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.huanchengfly.tieba.post.dataStore
import com.huanchengfly.tieba.post.getBoolean
import com.huanchengfly.tieba.post.getFloat
import com.huanchengfly.tieba.post.getInt
import com.huanchengfly.tieba.post.getLong
import com.huanchengfly.tieba.post.getString
import com.huanchengfly.tieba.post.utils.ThemeUtil.TRANSLUCENT_THEME_LIGHT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


open class AppPreferencesUtils(context: Context) {
    private val preferencesDataStore: DataStore<Preferences> = context.dataStore
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var userLikeLastRequestUnix by DataStoreDelegates.long(defaultValue = 0L)

    var appIcon by DataStoreDelegates.string(
        defaultValue = Icons.DEFAULT_ICON,
        key = AppIconUtil.PREF_KEY_APP_ICON
    )

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

    var darkTheme by DataStoreDelegates.string(key = "dark_theme", defaultValue = "grey_dark")

    var enableNewUi by DataStoreDelegates.boolean(defaultValue = false)

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
            private var prefValue = defaultValue
            private var initialized = false

            override fun getValue(thisRef: AppPreferencesUtils, property: KProperty<*>): Int {
                val finalKey = key ?: property.name
                if (!initialized) {
                    initialized = true
                    prefValue = thisRef.preferencesDataStore.getInt(finalKey, defaultValue)
                    thisRef.coroutineScope.launch {
                        thisRef.preferencesDataStore.data
                            .map { it[intPreferencesKey(finalKey)] }
                            .distinctUntilChanged()
                            .collect {
                                prefValue = it ?: defaultValue
                            }
                    }
                }
                return prefValue
            }

            override fun setValue(
                thisRef: AppPreferencesUtils,
                property: KProperty<*>,
                value: Int
            ) {
                prefValue = value
                thisRef.coroutineScope.launch {
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
            private var prefValue = defaultValue
            private var initialized = false

            override fun getValue(thisRef: AppPreferencesUtils, property: KProperty<*>): String? {
                val finalKey = key ?: property.name
                if (!initialized) {
                    initialized = true
                    prefValue = thisRef.preferencesDataStore.getString(finalKey)
                        ?: defaultValue
                    thisRef.coroutineScope.launch {
                        thisRef.preferencesDataStore.data
                            .map { it[stringPreferencesKey(finalKey)] }
                            .distinctUntilChanged()
                            .collect {
                                prefValue = it ?: defaultValue
                            }
                    }
                }
                return prefValue
            }

            override fun setValue(
                thisRef: AppPreferencesUtils,
                property: KProperty<*>,
                value: String?
            ) {
                prefValue = value
                thisRef.coroutineScope.launch {
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
            private var prefValue = defaultValue
            private var initialized = false

            override fun getValue(thisRef: AppPreferencesUtils, property: KProperty<*>): Float {
                val finalKey = key ?: property.name
                if (!initialized) {
                    initialized = true
                    prefValue =
                        thisRef.preferencesDataStore.getFloat(finalKey, defaultValue)
                    thisRef.coroutineScope.launch {
                        thisRef.preferencesDataStore.data
                            .map { it[floatPreferencesKey(finalKey)] }
                            .distinctUntilChanged()
                            .collect {
                                prefValue = it ?: defaultValue
                            }
                    }
                }
                return prefValue
            }

            override fun setValue(
                thisRef: AppPreferencesUtils,
                property: KProperty<*>,
                value: Float
            ) {
                prefValue = value
                thisRef.coroutineScope.launch {
                    thisRef.preferencesDataStore.edit {
                        it[floatPreferencesKey(key ?: property.name)] = value
                    }
                }
            }
        }

        fun long(
            defaultValue: Long = 0L,
            key: String? = null
        ) = object : ReadWriteProperty<AppPreferencesUtils, Long> {
            private var prefValue = defaultValue
            private var initialized = false

            override fun getValue(thisRef: AppPreferencesUtils, property: KProperty<*>): Long {
                val finalKey = key ?: property.name
                if (!initialized) {
                    initialized = true
                    prefValue =
                        thisRef.preferencesDataStore.getLong(finalKey, defaultValue)
                    thisRef.coroutineScope.launch {
                        thisRef.preferencesDataStore.data
                            .map { it[longPreferencesKey(finalKey)] }
                            .distinctUntilChanged()
                            .collect {
                                prefValue = it ?: defaultValue
                            }
                    }
                }
                return prefValue
            }

            override fun setValue(
                thisRef: AppPreferencesUtils,
                property: KProperty<*>,
                value: Long
            ) {
                prefValue = value
                thisRef.coroutineScope.launch {
                    thisRef.preferencesDataStore.edit {
                        it[longPreferencesKey(key ?: property.name)] = value
                    }
                }
            }
        }

        fun boolean(
            defaultValue: Boolean = false,
            key: String? = null
        ) = object : ReadWriteProperty<AppPreferencesUtils, Boolean> {
            private var prefValue = defaultValue
            private var initialized = false

            override fun getValue(thisRef: AppPreferencesUtils, property: KProperty<*>): Boolean {
                val finalKey = key ?: property.name
                if (!initialized) {
                    initialized = true
                    prefValue =
                        thisRef.preferencesDataStore.getBoolean(finalKey, defaultValue)
                    thisRef.coroutineScope.launch {
                        thisRef.preferencesDataStore.data
                            .map { it[booleanPreferencesKey(finalKey)] }
                            .distinctUntilChanged()
                            .collect {
                                prefValue = it ?: defaultValue
                            }
                    }
                }
                return prefValue
            }

            override fun setValue(
                thisRef: AppPreferencesUtils,
                property: KProperty<*>,
                value: Boolean
            ) {
                prefValue = value
                thisRef.coroutineScope.launch {
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