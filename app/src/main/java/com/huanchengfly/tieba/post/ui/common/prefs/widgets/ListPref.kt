package com.huanchengfly.tieba.post.ui.common.prefs.widgets

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.huanchengfly.tieba.post.R
import com.huanchengfly.tieba.post.ui.common.prefs.LocalPrefsDataStore
import com.huanchengfly.tieba.post.ui.widgets.compose.Dialog
import com.huanchengfly.tieba.post.ui.widgets.compose.DialogNegativeButton
import com.huanchengfly.tieba.post.ui.widgets.compose.picker.ListSinglePicker
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import kotlinx.coroutines.launch

/**
 * Preference that shows a list of entries in a Dialog where a single entry can be selected at one time.
 *
 * @param key Key used to identify this Pref in the DataStore
 * @param title Main text which describes the Pref. Shown above the summary and in the Dialog.
 * @param modifier Modifier applied to the Text aspect of this Pref
 * @param summary Used to give some more information about what this Pref is for
 * @param defaultValue Default selected key if this Pref hasn't been saved already. Otherwise the value from the dataStore is used.
 * @param onValueChange Will be called with the selected key when an item is selected
 * @param useSelectedAsSummary If true, uses the current selected item as the summary
 * @param dialogBackgroundColor Background color of the Dialog
 * @param contentColor Preferred content color passed to dialog's children
 * @param textColor Text colour of the [title], [summary] and [entries]
 * @param selectionColor Colour of the radiobutton of the selected item
 * @param buttonColor Colour of the cancel button
 * @param enabled If false, this Pref cannot be clicked and the Dialog cannot be shown.
 * @param entries Map of keys to values for entries that should be shown in the Dialog.
 */
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun ListPref(
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    defaultValue: String? = null,
    onValueChange: ((String) -> Unit)? = null,
    useSelectedAsSummary: Boolean = false,
    textColor: Color = MaterialTheme.colors.onBackground,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    entries: Map<String, String> = emptyMap(), //TODO: Change to List?
    icons: Map<String, @Composable () -> Unit> = emptyMap(),
) {
    val dialogState = rememberDialogState()
    val selectionKey = stringPreferencesKey(key)
    val scope = rememberCoroutineScope()

    val datastore = LocalPrefsDataStore.current
    val prefs by remember { datastore.data }.collectAsState(initial = null)

    var selected = defaultValue
    prefs?.get(selectionKey)?.also { selected = it } // starting value if it exists in datastore

    fun edit(current: Pair<String, String>) = run {
        scope.launch {
            try {
                datastore.edit { preferences ->
                    preferences[selectionKey] = current.first
                }
                onValueChange?.invoke(current.first)
            } catch (e: Exception) {
                Log.e("ListPref", "Could not write pref $key to database. ${e.printStackTrace()}")
            }
        }
    }

    TextPref(
        title = title,
        summary = when {
            useSelectedAsSummary && selected != null -> entries[selected]
            useSelectedAsSummary && selected == null -> "Not Set"
            else -> summary
        },
        leadingIcon = leadingIcon,
        modifier = modifier,
        textColor = textColor,
        enabled = true,
        onClick = { if (enabled) dialogState.show() },
    )

    Dialog(
        dialogState = dialogState,
        title = { Text(text = title) },
        buttons = {
            DialogNegativeButton(text = stringResource(id = R.string.button_cancel))
        }
    ) {
        ListSinglePicker(
            itemTitles = entries.map { it.value },
            itemValues = entries.map { it.key },
            selectedPosition = entries.keys.indexOf(selected),
            onItemSelected = { _, title, value, _ ->
                edit(current = value to title)
                dismiss()
            },
            itemIcons = icons,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
//    if (showDialog) {
//        AlertDialog(
//            onDismissRequest = { showDialog = false },
//            text = {
//                Column() {
//                    Text(modifier = Modifier.padding(vertical = 16.dp), text = title)
//                    LazyColumn {
//                        items(entryList) { current ->
//
//                            val isSelected = selected == current.first
//                            val onSelected = {
//                                edit(current)
//                            }
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .selectable(
//                                        selected = isSelected,
//                                        onClick = { if (!isSelected) onSelected() }
//                                    ),
//                                verticalAlignment = CenterVertically,
//                            ) {
//                                RadioButton(
//                                    selected = isSelected,
//                                    onClick = { if (!isSelected) onSelected() },
//                                    colors = RadioButtonDefaults.colors(selectedColor = selectionColor)
//                                )
//                                Text(
//                                    text = current.second,
//                                    style = MaterialTheme.typography.body2,
//                                    color = textColor
//                                )
//                            }
//                        }
//                    }
//                }
//            },
//            confirmButton = {
//                TextButton(
//                    onClick = { showDialog = false },
//                ) {
//                    Text("Cancel", style = MaterialTheme.typography.body1, color = buttonColor)
//                }
//
//            },
//            backgroundColor = dialogBackgroundColor,
//            contentColor = contentColor,
//            properties = DialogProperties(
//                usePlatformDefaultWidth = true
//            ),
//        )
//    }
}