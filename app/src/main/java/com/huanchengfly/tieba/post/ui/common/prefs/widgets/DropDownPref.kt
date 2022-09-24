package com.huanchengfly.tieba.post.ui.common.prefs.widgets

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.huanchengfly.tieba.post.ui.common.prefs.LocalPrefsDataStore
import kotlinx.coroutines.launch

/**
 * Preference that shows a list of entries in a DropDown
 *
 * @param key Key used to identify this Pref in the DataStore
 * @param title Main text which describes the Pref
 * @param modifier Modifier applied to the Text aspect of this Pref
 * @param summary Used to give some more information about what this Pref is for
 * @param defaultValue Default selected key if this Pref hasn't been saved already. Otherwise the value from the dataStore is used.
 * @param onValueChange Will be called with the selected key when an item is selected
 * @param useSelectedAsSummary If true, uses the current selected item as the summary. Equivalent of useSimpleSummaryProvider in androidx.
 * @param dropdownBackgroundColor Color of the dropdown menu
 * @param textColor Text colour of the [title] and [summary]
 * @param enabled If false, this Pref cannot be clicked and the dropdown menu will not show.
 * @param entries Map of keys to values for entries that should be shown in the DropDown.
 */
@ExperimentalMaterialApi
@Composable
fun DropDownPref(
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    defaultValue: String? = null,
    onValueChange: ((String) -> Unit)? = null,
    useSelectedAsSummary: Boolean = false,
    dropdownBackgroundColor: Color? = null,
    textColor: Color = MaterialTheme.colors.onBackground,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    entries: Map<String, String> = mapOf()
) {

    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectionKey = stringPreferencesKey(key)
    val scope = rememberCoroutineScope()

    val datastore = LocalPrefsDataStore.current
    val prefs by remember { datastore.data }.collectAsState(initial = null)

    var value = defaultValue
    prefs?.get(selectionKey)?.also { value = it } // starting value if it exists in datastore

    fun edit(item: Map.Entry<String, String>) = run {
        scope.launch {
            try {
                datastore.edit { preferences ->
                    preferences[selectionKey] = item.key
                }
                expanded = false
                onValueChange?.invoke(item.key)
            } catch (e: Exception) {
                Log.e("DropDownPref", "Could not write pref $key to database. ${e.printStackTrace()}")
            }
        }
    }

    Column {
        TextPref(
            title = title,
            modifier = modifier,
            summary = when {
                useSelectedAsSummary && value != null -> entries[value]
                useSelectedAsSummary && value == null -> "Not Set"
                else -> summary
            },
            textColor = textColor,
            leadingIcon = leadingIcon,
            enabled = enabled,
            onClick = {
                expanded = true
            },
        )

        Box(
            modifier = Modifier
                .padding(start = 16.dp)
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = if (dropdownBackgroundColor != null) Modifier.background(dropdownBackgroundColor) else Modifier
            ) {
                entries.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            edit(item)
                        }
                    ) {
                        Text(
                            text = item.value,
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }
        }
    }
}