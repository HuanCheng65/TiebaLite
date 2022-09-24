package com.huanchengfly.tieba.post.ui.common.prefs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.huanchengfly.tieba.post.collectPreferenceAsState

/**
 * Receiver scope which is used by [PrefsScreen].
 */
interface PrefsScope {
    /**
     * Adds a single Pref
     *
     * @param content the content of the item
     */
    fun prefsItem(content: @Composable PrefsScope.() -> Unit)

    /**
     * Adds a group of Prefs with a title.
     *
     * @param title Group header text. Will be shown above the list of Prefs
     * @param items All the prefs in this group
     */
    fun prefsGroup(title: String, items: PrefsScope.() -> Unit)

    /**
     * Adds a group of Prefs. This overload is intended for passing in a [GroupHeader] if you want more control over the header.
     *
     * @param header Group header. Will be shown above the list of Prefs
     * @param items All the prefs in this group
     */
    fun prefsGroup(header: @Composable PrefsScope.() -> Unit, items: PrefsScope.() -> Unit)
}

internal class PrefsScopeImpl : PrefsScope {

    private var _headerIndexes: MutableList<Int> = mutableListOf()
    val headerIndexes: List<Int> get() = _headerIndexes

    private var _footerIndexes: MutableList<Int> = mutableListOf()
    val footerIndexes: List<Int> get() = _footerIndexes

    private var _prefsItems: MutableList<PrefsItem> = mutableListOf()
    val prefsItems: List<PrefsItem> get() = _prefsItems

    override fun prefsItem(content: @Composable PrefsScope.() -> Unit) {
        _prefsItems.add(
            PrefsItem(
                content = { @Composable { content() } }
            )
        )
    }

    override fun prefsGroup(title: String, items: PrefsScope.() -> Unit) {
        // Exceptions to when divider should be drawn
        // - if last item
        // - if next item starts a new group
        // - if current current item is header of new group
        // - if current item is end of group

        // add header index so we know where each group starts
        _headerIndexes.add(this.prefsItems.size)

        this.prefsItem {
            GroupHeader(title)
        }

        // add all children to hierarchy
        this.apply(items)

        this.prefsItem { Spacer(modifier = Modifier.height(16.dp)) }

        // add totalSize -2/-1 to footerIndexes as that is the index of the last item added and the spacer respectively
        _footerIndexes.add(this.prefsItems.size - 2)
        _footerIndexes.add(this.prefsItems.size - 1)
    }

    override fun prefsGroup(
        header: @Composable PrefsScope.() -> Unit,
        items: PrefsScope.() -> Unit
    ) {
        // add header index so we know where each group starts
        _headerIndexes.add(this.prefsItems.size)

        this.prefsItem {
            header()
        }

        // add all children to hierarchy
        this.apply(items)

        this.prefsItem { Spacer(modifier = Modifier.height(16.dp)) }

        // add totalSize -2/-1 to footerIndexes as that is the index of the last item added and the spacer respectively
        _footerIndexes.add(this.prefsItems.size - 2)
        _footerIndexes.add(this.prefsItems.size - 1)
    }

    fun getPrefsItem(index: Int): @Composable () -> Unit {
        val prefsItem = prefsItems[index]
        return prefsItem.content.invoke(this, index)
    }
}

internal class PrefsItem(
    val content: PrefsScope.(index: Int) -> @Composable () -> Unit
)

@Composable
fun depend(key: String): Boolean {
    return LocalPrefsDataStore.current.collectPreferenceAsState(
        key = booleanPreferencesKey(key),
        defaultValue = true
    ).value
}

@Composable
fun dependNot(key: String): Boolean {
    return !LocalPrefsDataStore.current.collectPreferenceAsState(
        key = booleanPreferencesKey(key),
        defaultValue = false
    ).value
}