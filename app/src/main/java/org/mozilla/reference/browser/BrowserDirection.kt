/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import androidx.annotation.IdRes

/**
 * This file is from the Fenix project.
 *
 * Used with [BrowserActivity.openBrowser] to indicate which fragment
 * the browser is being opened from.
 *
 * @property fragmentId ID of the fragment opening the browser in the navigation graph.
 * An ID of `0` indicates a global action with no corresponding opening fragment.
 */
enum class BrowserDirection(@IdRes val fragmentId: Int) {
    FromFreshTab(R.id.freshTabFragment),
    FromSearch(R.id.searchFragment),
    FromHistory(R.id.historyFragment)
}
