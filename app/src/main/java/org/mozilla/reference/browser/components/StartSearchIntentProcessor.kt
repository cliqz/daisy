package org.mozilla.reference.browser.components

import com.cliqz.browser.freshtab.isFreshTab
import mozilla.components.browser.session.SessionManager
import mozilla.components.feature.tabs.TabsUseCases

class StartSearchIntentProcessor(
    private val tabsUseCases: TabsUseCases,
    private val sessionManager: SessionManager
) {
    fun process(openToSearch: Boolean) {
        if (openToSearch) {
            sessionManager.selectedSession?.let { selectedSession ->
                if (!selectedSession.url.isFreshTab()) {
                    tabsUseCases.addTab("", selectTab = true)
                }
            } ?: tabsUseCases.addTab("", selectTab = true)
        }
    }
}
