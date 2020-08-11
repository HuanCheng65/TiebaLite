package com.huanchengfly.tieba.post.fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.google.android.material.snackbar.Snackbar;
import com.huanchengfly.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.api.LiteApi;
import com.huanchengfly.tieba.api.interfaces.CommonAPICallback;
import com.huanchengfly.tieba.api.models.NewUpdateBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.activities.BlockListActivity;
import com.huanchengfly.tieba.post.activities.LoginActivity;
import com.huanchengfly.tieba.post.components.prefs.TimePickerPreference;
import com.huanchengfly.tieba.post.models.database.Account;
import com.huanchengfly.tieba.post.models.database.Block;
import com.huanchengfly.tieba.post.utils.AccountUtil;
import com.huanchengfly.tieba.post.utils.DialogUtil;
import com.huanchengfly.tieba.post.utils.DisplayUtil;
import com.huanchengfly.tieba.post.utils.TiebaUtil;
import com.huanchengfly.tieba.post.utils.Util;
import com.huanchengfly.tieba.post.utils.VersionUtil;
import com.huanchengfly.utils.GlideCacheUtil;
import com.lapism.searchview.database.SearchHistoryTable;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends PreferencesFragment {
    public static final String TAG = "SettingsFragment";

    private Account loginInfo;

    public SettingsFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        loginInfo = AccountUtil.getLoginInfo(getAttachContext());
        List<Account> accounts = AccountUtil.getAllAccounts();
        List<String> usernameList = new ArrayList<>();
        List<String> idList = new ArrayList<>();
        for (Account account : accounts) {
            usernameList.add(account.getNameShow());
            idList.add(String.valueOf(account.getId()));
        }
        ListPreference accountsPreference = findPreference("switch_account");
        accountsPreference.setEntries(usernameList.toArray(new String[0]));
        accountsPreference.setEntryValues(idList.toArray(new String[0]));
        if (loginInfo != null) {
            accountsPreference.setValue(String.valueOf(loginInfo.getId()));
            accountsPreference.setSummary("已登录账号 " + loginInfo.getNameShow());
        } else {
            accountsPreference.setSummary("未登录");
        }
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName("settings");
        addPreferencesFromResource(R.xml.preferences);
        ListPreference accountsPreference = findPreference("switch_account");
        accountsPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (AccountUtil.switchUser(getAttachContext(), Integer.valueOf((String) newValue))) {
                refresh();
                Toast.makeText(getAttachContext(), R.string.toast_switch_success, Toast.LENGTH_SHORT).show();
            }
            return false;
        });
        findPreference("copy_bduss").setOnPreferenceClickListener(preference -> {
            Account account = AccountUtil.getLoginInfo(getAttachContext());
            if (account != null) {
                TiebaUtil.copyText(getAttachContext(), account.getBduss());
            }
            return true;
        });
        findPreference("clear_search_history").setOnPreferenceClickListener(preference -> {
            new SearchHistoryTable(getAttachContext()).clearDatabase();
            if (getView() != null)
                Util.createSnackbar(getView(), R.string.toast_clear_success, Snackbar.LENGTH_SHORT).show();
            return true;
        });
        findPreference("exit_account").setEnabled(AccountUtil.isLoggedIn(getAttachContext()));
        findPreference("exit_account").setOnPreferenceClickListener(preference -> {
            DialogUtil.build(getAttachContext())
                    .setMessage(R.string.title_dialog_exit_account)
                    .setPositiveButton(R.string.button_sure_default, (dialog, which) -> {
                        AccountUtil.exit(getAttachContext());
                        refresh();
                        if (AccountUtil.getLoginInfo(getAttachContext()) == null) {
                            getAttachContext().startActivity(new Intent(getAttachContext(), LoginActivity.class));
                        }
                    })
                    .setNegativeButton(R.string.button_cancel, null)
                    .create()
                    .show();
            return true;
        });
        findPreference("black_list").setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getAttachContext(), BlockListActivity.class).putExtra("category", Block.CATEGORY_BLACK_LIST));
            return true;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            findPreference("follow_system_night").setEnabled(true);
            findPreference("follow_system_night").setSummary(null);
        } else {
            findPreference("follow_system_night").setEnabled(false);
            findPreference("follow_system_night").setSummary(R.string.summary_follow_system_night_disabled);
        }
        findPreference("show_top_forum_in_normal_list").setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary(R.string.summary_show_top_forum_in_normal_list_changed);
            return true;
        });
        findPreference("status_bar_darker").setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary(R.string.summary_status_bar_darker_changed);
            return true;
        });
        findPreference("hideExplore").setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary(R.string.summary_change_need_restart);
            return true;
        });
        findPreference("white_list").setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getAttachContext(), BlockListActivity.class).putExtra("category", Block.CATEGORY_WHITE_LIST));
            return true;
        });
        TimePickerPreference timePickerPreference = findPreference("auto_sign_time");
        timePickerPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary(getAttachContext().getString(R.string.summary_auto_sign_time, (String) newValue));
            return true;
        });
        timePickerPreference.setSummary(getAttachContext().getString(R.string.summary_auto_sign_time, getPreferenceManager().getSharedPreferences().getString("auto_sign_time", "09:00")));
        Preference clearCache = findPreference("clear_cache");
        clearCache.setSummary(getAttachContext().getString(R.string.tip_cache, GlideCacheUtil.getInstance().getCacheSize(getAttachContext())));
        clearCache.setOnPreferenceClickListener(preference -> {
            GlideCacheUtil.getInstance().clearImageAllCache(getAttachContext());
            if (getView() != null)
                Util.createSnackbar(getView(), R.string.toast_clear_cache_success, Snackbar.LENGTH_SHORT).show();
            preference.setSummary(getAttachContext().getString(R.string.tip_cache, "0.0B"));
            return true;
        });
        EditTextPreference littleTaliPreference = findPreference("little_tail");
        String littleTali = getPreferenceManager().getSharedPreferences().getString("little_tail", "");
        if (littleTali.isEmpty()) {
            littleTaliPreference.setSummary(R.string.tip_no_little_tail);
        } else {
            littleTaliPreference.setSummary(littleTali);
            littleTaliPreference.setText(littleTali);
        }
        littleTaliPreference.setOnPreferenceChangeListener((preference, value) -> {
            if (value instanceof String) {
                String tail = (String) value;
                if (tail.isEmpty()) {
                    littleTaliPreference.setSummary(R.string.tip_no_little_tail);
                } else {
                    littleTaliPreference.setSummary(tail);
                    littleTaliPreference.setText(tail);
                }
            }
            return true;
        });
        Preference aboutPreference = findPreference("about");
        LiteApi.getInstance().newCheckUpdate(new CommonAPICallback<NewUpdateBean>() {
            @Override
            public void onSuccess(NewUpdateBean data) {
                if (data.isHasUpdate()) {
                    aboutPreference.setSummary(getAttachContext().getString(R.string.tip_new_version, data.getResult().getVersionName()));
                }
            }

            @Override
            public void onFailure(int code, String error) {
            }
        });
        SwitchPreference useCustomTabs = findPreference("use_custom_tabs");
        useCustomTabs.setEnabled(!getPreferenceManager().getSharedPreferences().getBoolean("use_webview", true));
        findPreference("use_webview").setOnPreferenceChangeListener((preference, newValue) -> {
            useCustomTabs.setEnabled(!(boolean) newValue);
            return true;
        });
        initListPreference("dark_theme", "dark");
        aboutPreference.setSummary(getString(R.string.tip_about, VersionUtil.getVersionName(getAttachContext())));
        refresh();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDivider(ThemeUtils.tintDrawable(ContextCompat.getDrawable(getAttachContext(), R.drawable.drawable_divider_8dp), ThemeUtils.getColorByAttr(getAttachContext(), R.attr.colorDivider)));
        setDividerHeight(DisplayUtil.dp2px(getAttachContext(), 8));
    }

    private void initSwitchPreference(String key) {
        initSwitchPreference(key, false);
    }

    private void initSwitchPreference(SwitchPreference switchPreference) {
        initSwitchPreference(switchPreference, false);
    }

    private void initSwitchPreference(String key, boolean defValue) {
        SwitchPreference switchPreference = findPreference(key);
        initSwitchPreference(switchPreference, defValue);
    }

    private void initSwitchPreference(SwitchPreference switchPreference, boolean defValue) {
        boolean value = getPreferenceManager().getSharedPreferences().getBoolean(switchPreference.getKey(), defValue);
        switchPreference.setChecked(value);
    }

    private void initListPreference(String key, String defValue) {
        ListPreference listPreference = findPreference(key);
        String value = getPreferenceManager().getSharedPreferences().getString(key, defValue);
        listPreference.setValue(value);
    }
}