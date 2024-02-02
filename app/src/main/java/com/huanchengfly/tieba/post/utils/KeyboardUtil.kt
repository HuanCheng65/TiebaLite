package com.huanchengfly.tieba.post.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun showKeyboard(context: Context, view: View): Boolean {
    view.requestFocus()
    val mInputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    return mInputManager.showSoftInput(view, 0)
}

fun hideKeyboard(context: Context, view: View): Boolean {
    val mInputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    return mInputManager.hideSoftInputFromWindow(view.windowToken, 0)
}