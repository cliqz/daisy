package org.mozilla.reference.browser.components

import android.content.Intent
import com.cliqz.browser.freshtab.isFreshTab
import mozilla.components.browser.session.SessionManager
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.utils.SafeIntent
import org.mozilla.reference.browser.BrowserActivity

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
