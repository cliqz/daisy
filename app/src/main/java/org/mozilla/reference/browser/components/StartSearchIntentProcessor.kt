/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.content.Intent
import mozilla.components.browser.session.SessionManager
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.utils.SafeIntent
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.browser.isFreshTab

class StartSearchIntentProcessor(
    private val tabsUseCases: TabsUseCases,
    private val sessionManager: SessionManager
) {
    fun process(intent: Intent): Boolean {
        val safeIntent = SafeIntent(intent)
        val openToSearch = safeIntent.getBooleanExtra(BrowserActivity.EXTRA_OPEN_TO_SEARCH, false)
        return if (openToSearch) {
            sessionManager.selectedSession?.let { selectedSession ->
                if (!selectedSession.url.isFreshTab()) {
                    tabsUseCases.addTab("", selectTab = true)
                }
            } ?: tabsUseCases.addTab("", selectTab = true)
            true
        } else {
            false
        }
    }
}
