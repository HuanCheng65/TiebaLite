package com.huanchengfly.tieba.post.fragments.intro;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.XmlRes;

import com.huanchengfly.tieba.post.fragments.preference.PreferencesFragment;

public class IntroSettingsFragment extends PreferencesFragment {
    private String spName;
    private @XmlRes
    int res;

    private IntroSettingsFragment() {
    }

    public static IntroSettingsFragment newInstance(@XmlRes int res, String spName) {
        IntroSettingsFragment fragment = new IntroSettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("res", res);
        bundle.putString("spName", spName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            spName = bundle.getString("spName");
            res = bundle.getInt("res");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(spName);
        addPreferencesFromResource(res);
    }
}
