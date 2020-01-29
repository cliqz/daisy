/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ext

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.EXTRA_TEXT
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.view.ContextThemeWrapper
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import mozilla.components.support.base.log.Log.Priority.WARN
import mozilla.components.support.base.log.Log
import org.mozilla.reference.browser.BrowserApplication
import org.mozilla.reference.browser.Components
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.utils.PreferenceHelper

/**
 * Get the BrowserApplication object from a context.
 */
val Context.application: BrowserApplication
    get() = applicationContext as BrowserApplication

/**
 * Get the requireComponents of this application.
 */
val Context.components: Components
    get() = application.components

fun Context.asActivity() = (this as? ContextThemeWrapper)?.baseContext as? Activity
    ?: this as? Activity

fun Context.getPreferenceKey(@StringRes resourceId: Int): String =
    resources.getString(resourceId)

/**
 *  Shares content via [ACTION_SEND] intent.
 *
 * @param text the data to be shared  [EXTRA_TEXT]
 * @param subject of the intent [EXTRA_TEXT]
 * @return true it is able to share false otherwise.
 */
fun Context.share(text: String, subject: String = ""): Boolean {
    return try {
        val intent = Intent(ACTION_SEND).apply {
            type = "text/plain"
            putExtra(EXTRA_SUBJECT, subject)
            putExtra(EXTRA_TEXT, text)
            flags = FLAG_ACTIVITY_NEW_TASK
        }

        val shareIntent = Intent.createChooser(intent, getString(R.string.menu_share_with)).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
        }

        startActivity(shareIntent)
        true
    } catch (e: ActivityNotFoundException) {
        Log.log(WARN, message = "No activity to share to found", throwable = e, tag = "Reference-Browser")
        false
    }
}

fun Context.preferences() = PreferenceHelper.getInstance(this)

/**
 * Shortcut to get quantity aware strings from the context.
 *
 * @param pluralId the plural id
 * @param quantity the quantity you want the string for
 * @see android.content.res.Resources.getQuantityString
 */
fun Context.getQuantityString(@PluralsRes pluralId: Int, quantity: Int) =
    resources.getQuantityString(pluralId, quantity)

/**
 * Shortcut to get quantity aware strings from the context.
 *
 * @param pluralId the plural id
 * @param quantity the quantity you want the string for
 * @param formatArgs the arguments used for substitution
 * @see android.content.res.Resources.getQuantityString
 */
@Suppress("SpreadOperator")
fun Context.getQuantityString(
    @PluralsRes pluralId: Int,
    quantity: Int,
    vararg formatArgs: Any
) = resources.getQuantityString(pluralId, quantity, *formatArgs)
