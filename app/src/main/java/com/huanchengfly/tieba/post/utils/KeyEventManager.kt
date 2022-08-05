package com.huanchengfly.tieba.post.utils

import android.view.KeyEvent
import android.view.View

private class KeyEventManager<V : View> : View.OnKeyListener {
    private val keyEventMapping = mutableMapOf<Int, ((V) -> Unit)>()

    override fun onKey(v: View, keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_UP && keyEventMapping.containsKey(keyCode)) {
            keyEventMapping[keyCode]?.invoke(v as V)
            return true
        }
        return false
    }

    fun bindKeyEvent(keyCode: Int, action: (V) -> Unit) {
        keyEventMapping[keyCode] = action
    }
}

fun <V : View> V.bindKeyEvent(keyCode: Int, action: (V) -> Unit) {
    val keyEventManager: KeyEventManager<V>
    if (onKeyListener !is KeyEventManager<*>) {
        keyEventManager = KeyEventManager()
        setOnKeyListener(keyEventManager)
    } else {
        keyEventManager = onKeyListener as KeyEventManager<V>
    }
    keyEventManager.bindKeyEvent(keyCode, action)
}

fun <V : View> V.bindKeyEvent(keyCodes: List<Int>, action: (V) -> Unit) {
    val keyEventManager = getKeyEventManager()
    keyCodes.forEach {
        keyEventManager.bindKeyEvent(it, action)
    }
}

private val View.onKeyListener: View.OnKeyListener?
    get() {
        return runCatching {
            val viewClazz = View::class.java
            val listenerInfoClazz = Class.forName("android.view.View.ListenerInfo")
            val getListenerInfoMethod = viewClazz.getDeclaredMethod("getListenerInfo")
            val mOnKeyListenerField = listenerInfoClazz.getDeclaredField("mOnKeyListener")
            getListenerInfoMethod.isAccessible = true
            mOnKeyListenerField.isAccessible = true
            val listenerInfo = getListenerInfoMethod.invoke(this)
            mOnKeyListenerField.get(listenerInfo) as View.OnKeyListener?
        }.getOrNull()
    }

private fun <V : View> V.getKeyEventManager(): KeyEventManager<V> {
    return if (onKeyListener !is KeyEventManager<*>) {
        KeyEventManager<V>().also { setOnKeyListener(it) }
    } else {
        onKeyListener as KeyEventManager<V>
    }
}