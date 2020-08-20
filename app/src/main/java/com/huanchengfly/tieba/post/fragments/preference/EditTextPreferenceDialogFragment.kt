package com.huanchengfly.tieba.post.fragments.preference

import android.os.Bundle
import android.view.View
import androidx.preference.EditTextPreferenceDialogFragmentCompat
import androidx.preference.PreferenceDialogFragmentCompat
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils

class EditTextPreferenceDialogFragment : EditTextPreferenceDialogFragmentCompat() {
    companion object {
        fun newInstance(key: String): EditTextPreferenceDialogFragment {
            val fragment = EditTextPreferenceDialogFragment()
            val b = Bundle(1)
            b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        ThemeUtils.refreshUI(view.context)
    }
}