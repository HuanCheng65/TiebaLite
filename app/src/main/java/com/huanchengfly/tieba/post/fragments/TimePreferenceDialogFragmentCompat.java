package com.huanchengfly.tieba.post.fragments;

import android.content.Context;
import android.view.View;
import android.widget.TimePicker;

import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.huanchengfly.tieba.post.components.prefs.TimePickerPreference;

public class TimePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat implements DialogPreference.TargetFragment {
    TimePicker timePicker = null;

    @Override
    protected View onCreateDialogView(Context context) {
        timePicker = new TimePicker(context);
        return (timePicker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        timePicker.setIs24HourView(true);
        TimePickerPreference pref = (TimePickerPreference) getPreference();
        timePicker.setCurrentHour(pref.hour);
        timePicker.setCurrentMinute(pref.minute);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            TimePickerPreference pref = (TimePickerPreference) getPreference();
            pref.hour = timePicker.getCurrentHour();
            pref.minute = timePicker.getCurrentMinute();

            String value = TimePickerPreference.timeToString(pref.hour, pref.minute);
            if (pref.callChangeListener(value)) pref.persistStringValue(value);
        }
    }

    @Override
    public Preference findPreference(CharSequence charSequence) {
        return getPreference();
    }
}