/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ext

import android.app.Activity
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import mozilla.components.support.ktx.android.view.setNavigationBarTheme
import mozilla.components.support.ktx.android.view.setStatusBarTheme

/**
 * Colors the status bar and the navigation bar.
 */
fun Activity.setSystemBarsTheme(@ColorRes statusBarColor: Int, @ColorRes navigationBarColor: Int) {
    window.run {
        setStatusBarTheme(
            ContextCompat.getColor(context, statusBarColor))
        setNavigationBarTheme(
            ContextCompat.getColor(context, navigationBarColor))
    }
}
