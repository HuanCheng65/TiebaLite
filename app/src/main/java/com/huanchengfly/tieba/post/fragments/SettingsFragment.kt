package com.huanchengfly.tieba.post.fragments

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.google.android.material.snackbar.Snackbar
import com.huanchengfly.theme.utils.ThemeUtils
import com.huanchengfly.tieba.api.LiteApi.Companion.instance
import com.huanchengfly.tieba.api.interfaces.CommonAPICallback
import com.huanchengfly.tieba.api.models.NewUpdateBean
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.activities.BlockListActivity
import com.huanchengfly.tieba.post.activities.LoginActivity
import com.huanchengfly.tieba.post.components.prefs.TimePickerPreference
import com.huanchengfly.tieba.post.models.database.Account
import com.huanchengfly.tieba.post.models.database.Block
import com.huanchengfly.tieba.post.utils.*
import com.huanchengfly.utils.GlideCacheUtil
import com.lapism.searchview.database.SearchHistoryTable
import java.util.*

class SettingsFragment : PreferencesFragment() {
    private var loginInfo: Account? = null
    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        loginInfo = AccountUtil.getLoginInfo(attachContext)
        val accounts = AccountUtil.getAllAccounts()
        val usernameList: MutableList<String> = ArrayList()
        val idList: MutableList<String> = ArrayList()
        for (account in accounts) {
            usernameList.add(account.nameShow)
            idList.add(account.id.toString())
        }
        val accountsPreference = findPreference<ListPreference>("switch_account")
        accountsPreference!!.entries = usernameList.toTypedArray()
        accountsPreference.entryValues = idList.toTypedArray()
        if (loginInfo != null) {
            accountsPreference.value = loginInfo!!.id.toString()
            accountsPreference.summary = "已登录账号 " + loginInfo!!.nameShow
        } else {
            accountsPreference.summary = "未登录"
        }
    }

    @SuppressLint("ApplySharedPref")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = "settings"
        addPreferencesFromResource(R.xml.preferences)
        val accountsPreference = findPreference<ListPreference>("switch_account")
        accountsPreference!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any? ->
            if (AccountUtil.switchUser(attachContext, Integer.valueOf((newValue as String?)!!))) {
                refresh()
                Toast.makeText(attachContext, R.string.toast_switch_success, Toast.LENGTH_SHORT).show()
            }
            false
        }
        findPreference<Preference>("copy_bduss")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val account = AccountUtil.getLoginInfo(attachContext)
            if (account != null) {
                TiebaUtil.copyText(attachContext, account.bduss)
            }
            true
        }
        findPreference<Preference>("clear_search_history")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            SearchHistoryTable(attachContext).clearDatabase()
            if (view != null) Util.createSnackbar(view!!, R.string.toast_clear_success, Snackbar.LENGTH_SHORT).show()
            true
        }
        findPreference<Preference>("exit_account")!!.isEnabled = AccountUtil.isLoggedIn(attachContext)
        findPreference<Preference>("exit_account")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            DialogUtil.build(attachContext)
                    .setMessage(R.string.title_dialog_exit_account)
                    .setPositiveButton(R.string.button_sure_default) { _: DialogInterface?, _: Int ->
                        AccountUtil.exit(attachContext)
                        refresh()
                        if (AccountUtil.getLoginInfo(attachContext) == null) {
                            attachContext.startActivity(Intent(attachContext, LoginActivity::class.java))
                        }
                    }
                    .setNegativeButton(R.string.button_cancel, null)
                    .create()
                    .show()
            true
        }
        findPreference<Preference>("black_list")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(attachContext, BlockListActivity::class.java).putExtra("category", Block.CATEGORY_BLACK_LIST))
            true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            findPreference<Preference>("follow_system_night")!!.isEnabled = true
            findPreference<Preference>("follow_system_night")!!.summary = null
        } else {
            findPreference<Preference>("follow_system_night")!!.isEnabled = false
            findPreference<Preference>("follow_system_night")!!.setSummary(R.string.summary_follow_system_night_disabled)
        }
        findPreference<Preference>("show_top_forum_in_normal_list")!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference, _: Any? ->
            preference.setSummary(R.string.summary_show_top_forum_in_normal_list_changed)
            true
        }
        findPreference<Preference>("status_bar_darker")!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference, _: Any? ->
            preference.setSummary(R.string.summary_status_bar_darker_changed)
            true
        }
        findPreference<Preference>("hideExplore")!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference, _: Any? ->
            preference.setSummary(R.string.summary_change_need_restart)
            true
        }
        findPreference<Preference>("white_list")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(attachContext, BlockListActivity::class.java).putExtra("category", Block.CATEGORY_WHITE_LIST))
            true
        }
        val timePickerPreference = findPreference<TimePickerPreference>("auto_sign_time")
        timePickerPreference!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference, newValue: Any? ->
            preference.summary = attachContext.getString(R.string.summary_auto_sign_time, newValue as String?)
            true
        }
        timePickerPreference.summary = attachContext.getString(R.string.summary_auto_sign_time, preferenceManager.sharedPreferences.getString("auto_sign_time", "09:00"))
        val clearCache = findPreference<Preference>("clear_cache")
        clearCache!!.summary = attachContext.getString(R.string.tip_cache, GlideCacheUtil.getInstance().getCacheSize(attachContext))
        clearCache.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference: Preference ->
            GlideCacheUtil.getInstance().clearImageAllCache(attachContext)
            if (view != null) Util.createSnackbar(view!!, R.string.toast_clear_cache_success, Snackbar.LENGTH_SHORT).show()
            preference.summary = attachContext.getString(R.string.tip_cache, "0.0B")
            true
        }
        val littleTaliPreference = findPreference<EditTextPreference>("little_tail")
        val littleTali = preferenceManager.sharedPreferences.getString("little_tail", "")
        if (littleTali!!.isEmpty()) {
            littleTaliPreference!!.setSummary(R.string.tip_no_little_tail)
        } else {
            littleTaliPreference!!.summary = littleTali
            littleTaliPreference.text = littleTali
        }
        littleTaliPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, value: Any? ->
            if (value is String) {
                if (value.isEmpty()) {
                    littleTaliPreference.setSummary(R.string.tip_no_little_tail)
                } else {
                    littleTaliPreference.summary = value
                    littleTaliPreference.text = value
                }
            }
            true
        }
        val aboutPreference = findPreference<Preference>("about")
        instance!!.newCheckUpdate(object : CommonAPICallback<NewUpdateBean?> {
            override fun onSuccess(data: NewUpdateBean?) {
                if (data != null) {
                    if (data.isHasUpdate == true) {
                        aboutPreference!!.summary = attachContext.getString(R.string.tip_new_version, data.result?.versionName)
                    }
                }
            }

            override fun onFailure(code: Int, error: String) {}
        })
        val useCustomTabs = findPreference<SwitchPreference>("use_custom_tabs")
        useCustomTabs!!.isEnabled = !preferenceManager.sharedPreferences.getBoolean("use_webview", true)
        findPreference<Preference>("use_webview")!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any? ->
            useCustomTabs.isEnabled != newValue as Boolean
            true
        }
        initListPreference("dark_theme", "dark")
        aboutPreference!!.summary = getString(R.string.tip_about, VersionUtil.getVersionName(attachContext))
        refresh()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDivider(ThemeUtils.tintDrawable(ContextCompat.getDrawable(attachContext, R.drawable.drawable_divider_8dp), ThemeUtils.getColorByAttr(attachContext, R.attr.colorDivider)))
        setDividerHeight(DisplayUtil.dp2px(attachContext, 8f))
    }

    private fun initSwitchPreference(key: String, defValue: Boolean = false) {
        val switchPreference = findPreference<SwitchPreference>(key)
        initSwitchPreference(switchPreference, defValue)
    }

    private fun initSwitchPreference(switchPreference: SwitchPreference?, defValue: Boolean = false) {
        val value = preferenceManager.sharedPreferences.getBoolean(switchPreference!!.key, defValue)
        switchPreference.isChecked = value
    }

    private fun initListPreference(key: String, defValue: String) {
        val listPreference = findPreference<ListPreference>(key)
        val value = preferenceManager.sharedPreferences.getString(key, defValue)
        listPreference!!.value = value
    }

    companion object {
        const val TAG = "SettingsFragment"
    }
}