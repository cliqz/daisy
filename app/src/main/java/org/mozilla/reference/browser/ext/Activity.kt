package org.mozilla.reference.browser.ext

import android.app.Activity
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import mozilla.components.support.ktx.android.view.setNavigationBarTheme
import mozilla.components.support.ktx.android.view.setStatusBarTheme

fun Activity.setWindowTheme(@ColorRes statusBarColor: Int, @ColorRes navigationBarColor: Int) {
    window.run {
        setStatusBarTheme(
            ContextCompat.getColor(context, statusBarColor))
        setNavigationBarTheme(
            ContextCompat.getColor(context, navigationBarColor))
    }
}
