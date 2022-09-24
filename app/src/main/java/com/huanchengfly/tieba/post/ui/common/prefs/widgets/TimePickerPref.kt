package com.huanchengfly.tieba.post.ui.common.prefs.widgets

import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.huanchengfly.tieba.post.getString
import com.huanchengfly.tieba.post.ui.common.prefs.LocalPrefsDataStore
import com.huanchengfly.tieba.post.ui.widgets.compose.TimePickerDialog
import com.huanchengfly.tieba.post.ui.widgets.compose.rememberDialogState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalTime

/**
 * Preference which shows a TextField in a Dialog
 *
 * @param key Key used to identify this Pref in the DataStore
 * @param title Main text which describes the Pref
 * @param modifier Modifier applied to the Text aspect of this Pref
 * @param summary Used to give some more information about what this Pref is for
 * @param dialogTitle Title shown in the dialog. No title if null.
 * @param dialogMessage Summary shown underneath [dialogTitle]. No summary if null.
 * @param defaultValue Default value that will be set in the TextField when the dialog is shown for the first time.
 * @param onValueSaved Will be called with new TextField value when the confirm button is clicked. It is NOT called every time the value changes. Use [onValueChange] for that.
 * @param onValueChange Will be called every time the TextField value is changed.
 * @param textColor Text colour of the [title] and [summary]
 * @param enabled If false, this Pref cannot be clicked.
 */
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun TimePickerPerf(
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    summary: @Composable (value: String) -> String? = { null },
    dialogTitle: String? = null,
    dialogMessage: String? = null,
    defaultValue: String = "07:00",
    onValueSaved: (String) -> Unit = {},
    onValueChange: (String) -> Unit = {},
    textColor: Color = MaterialTheme.colors.onBackground,
    leadingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val dialogState = rememberDialogState()
    val selectionKey = stringPreferencesKey(key)
    val scope = rememberCoroutineScope()

    val datastore = LocalPrefsDataStore.current
    val prefs by remember { datastore.data }.collectAsState(initial = null)

    //value should only change when save button is clicked
    var value by remember { mutableStateOf(datastore.getString(key, defaultValue)) }
    //value of the TextField which changes every time the text is modified
    var timeVal by remember { mutableStateOf(value) }

    var dialogSize by remember { mutableStateOf(Size.Zero) }


    LaunchedEffect(datastore.data) {
        datastore.data.collectLatest { pref ->
            pref[selectionKey]?.also {
                value = it
            }
        }
    }

    fun edit() = run {
        scope.launch {
            try {
                datastore.edit { preferences ->
                    preferences[selectionKey] = timeVal
                }
                onValueSaved(timeVal)
            } catch (e: Exception) {
                Log.e(
                    "EditTextPref",
                    "Could not write pref $key to database. ${e.printStackTrace()}"
                )
            }
        }
    }

    TextPref(
        title = title,
        modifier = modifier,
        summary = summary(value),
        textColor = textColor,
        enabled = enabled,
        leadingIcon = leadingIcon,
        onClick = { if (enabled) dialogState.show() },
    )

    if (dialogState.show) {
        //reset
        LaunchedEffect(null) {
            timeVal = value
        }

        TimePickerDialog(
            dialogState = dialogState,
            modifier = Modifier.onGloballyPositioned {
                dialogSize = it.size.toSize()
            },
            currentTime = LocalTime.parse(timeVal),
            onConfirm = {
                timeVal = it.toString()
                onValueChange(it.toString())
                edit()
            },
            onValueChange = {
                timeVal = it.toString()
                onValueChange(it.toString())
            },
            title = {
                if (dialogTitle != null) {
                    Text(text = dialogTitle)
                }
            },
        ) {
            if (dialogMessage != null) {
                Text(text = dialogMessage)
            }
        }
    }
}
