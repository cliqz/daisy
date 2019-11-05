package org.mozilla.reference.browser.utils

import android.util.DisplayMetrics

/**
 * Coverts a float value in density independent pixels (dp)
 */
fun Float.toDp(displayMetrics: DisplayMetrics) = this / displayMetrics.density
