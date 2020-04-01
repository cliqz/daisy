/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.freshtab.toolbar

import mozilla.components.browser.session.SessionManager
import mozilla.components.feature.tabs.toolbar.TabCounterToolbarButton
import org.mozilla.reference.browser.freshtab.FreshTabToolbar

interface TabsToolbarInteractor {

    fun onTabsCounterClicked()
}

/**
 * Feature implementation for connecting a tabs tray implementation with a toolbar implementation.
 */
class TabsToolbarFeature(
    toolbar: FreshTabToolbar,
    sessionManager: SessionManager,
    tabsToolbarInteractor: TabsToolbarInteractor
) {
    init {
        run {
            val tabsAction = TabCounterToolbarButton(
                sessionManager,
                tabsToolbarInteractor::onTabsCounterClicked
            )
            toolbar.addBrowserAction(tabsAction)
        }
    }
}
