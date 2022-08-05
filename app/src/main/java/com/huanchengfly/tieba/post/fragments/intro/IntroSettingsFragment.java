package com.huanchengfly.tieba.post.fragments.intro;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.XmlRes;

import com.huanchengfly.tieba.post.DataStorePreference;
import com.huanchengfly.tieba.post.fragments.preference.PreferencesFragment;

public class IntroSettingsFragment extends PreferencesFragment {
    private @XmlRes
    int res;

    private IntroSettingsFragment() {
    }

    public static IntroSettingsFragment newInstance(@XmlRes int res) {
        IntroSettingsFragment fragment = new IntroSettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("res", res);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            res = bundle.getInt("res");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setPreferenceDataStore(new DataStorePreference());
        addPreferencesFromResource(res);
    }
}
