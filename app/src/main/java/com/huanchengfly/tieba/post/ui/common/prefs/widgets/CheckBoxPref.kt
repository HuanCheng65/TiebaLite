package com.huanchengfly.tieba.post.ui.common.prefs.widgets

import android.util.Log
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.huanchengfly.tieba.post.ui.common.prefs.LocalPrefsDataStore
import kotlinx.coroutines.launch

/**
 * Simple preference with a trailing [Checkbox]
 *
 * @param key Key used to identify this Pref in the DataStore
 * @param title Main text which describes the Pref
 * @param modifier Modifier applied to the Text aspect of this Pref
 * @param summary Used to give some more information about what this Pref is for
 * @param defaultChecked If the checkbox should be checked by default. Only used if a value for this [key] doesn't already exist in the DataStore
 * @param onCheckedChange Will be called with the new state when the state changes
 * @param textColor Text colour of the [title] and [summary]
 * @param enabled If false, this Pref cannot be checked/unchecked
 * @param leadingIcon Icon which is positioned at the start of the Pref
 */
@ExperimentalMaterialApi
@Composable
fun CheckBoxPref(
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    defaultChecked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    textColor: Color = MaterialTheme.colors.onBackground,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {

    val selectionKey = booleanPreferencesKey(key)
    val scope = rememberCoroutineScope()

    val datastore = LocalPrefsDataStore.current
    val prefs by remember { datastore.data }.collectAsState(initial = null)

    var checked = defaultChecked
    prefs?.get(selectionKey)?.also { checked = it }

    fun edit(newState: Boolean) = run {
        scope.launch {
            try {
                datastore.edit { preferences ->
                    preferences[selectionKey] = newState
                }
                checked = newState
                onCheckedChange?.invoke(newState)
            } catch (e: Exception) {
                Log.e(
                    "CheckBoxPref",
                    "Could not write pref $key to database. ${e.printStackTrace()}"
                )
            }
        }
    }

    TextPref(
        title = title,
        modifier = modifier,
        textColor = textColor,
        summary = summary,
        leadingIcon = leadingIcon,
        enabled = enabled,
        darkenOnDisable = true,
        onClick = {
            checked = !checked
            edit(checked)
        }
    ) {
        Checkbox(
            checked = checked,
            enabled = enabled,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary),
            onCheckedChange = {
                edit(it)
            }
        )
    }
}