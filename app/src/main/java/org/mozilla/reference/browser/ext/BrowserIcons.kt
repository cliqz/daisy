package org.mozilla.reference.browser.ext

import android.widget.ImageView
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.icons.IconRequest

/**
 * Borrowed from Fenix project
 */
fun BrowserIcons.loadIntoView(view: ImageView, url: String) = loadIntoView(view, IconRequest(url))