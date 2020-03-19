/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.freshtab

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.NavController
import mozilla.components.browser.session.SessionManager
import mozilla.components.feature.tabs.TabsUseCases
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.BrowserDirection
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.nav
import org.mozilla.reference.browser.settings.SettingsActivity
import org.mozilla.reference.browser.settings.deletebrowsingdata.DeleteBrowsingData

interface FreshTabController {
    fun handleSearchBarClicked()
    fun handleForwardClicked()
    fun handleMenuNewTabClicked()
    fun handleMenuNewForgetTabClicked()
    fun handleMenuReportIssueClicked()
    fun handleMenuSettingsClicked()
    fun handleMenuHistoryClicked()
    fun handleMenuClearDataClicked()
    fun handleNewsItemClicked(url: String)
}

class DefaultFreshTabController(
    private val context: Context,
    private val sessionManager: SessionManager,
    private val tabsUseCases: TabsUseCases,
    private val navController: NavController,
    private val coroutineScope: LifecycleCoroutineScope
) : FreshTabController {

    override fun handleSearchBarClicked() {
        // Remove existing session and add a new session.
        sessionManager.selectedSession?.let {
            tabsUseCases.removeTab.invoke(it)
            tabsUseCases.addTab.invoke("", selectTab = true)
        }
        val direction = FreshTabFragmentDirections.actionFreshTabFragmentToSearchFragment(
            sessionId = sessionManager.selectedSession?.id
        )
        navController.nav(R.id.freshTabFragment, direction)
    }

    override fun handleForwardClicked() {
        (context as BrowserActivity).openBrowser(from = BrowserDirection.FromFreshTab)
    }

    override fun handleMenuNewTabClicked() {
        tabsUseCases.addTab.invoke("", selectTab = true)
        val direction = FreshTabFragmentDirections.actionFreshTabFragmentToFreshTabFragment()
        navController.nav(R.id.freshTabFragment, direction)
    }

    override fun handleMenuNewForgetTabClicked() {
        tabsUseCases.addPrivateTab.invoke("about:privatebrowsing")
        val direction = FreshTabFragmentDirections.actionFreshTabFragmentToBrowserFragment(null)
        navController.nav(R.id.freshTabFragment, direction)
    }

    override fun handleMenuReportIssueClicked() {
        tabsUseCases.addTab.invoke("https://cliqz.com/en/support")
        (context as BrowserActivity).openBrowser(from = BrowserDirection.FromFreshTab)
    }

    override fun handleMenuSettingsClicked() {
        val intent = Intent(context, SettingsActivity::class.java)
        context.startActivity(intent)
    }

    override fun handleMenuHistoryClicked() {
        val direction = FreshTabFragmentDirections.actionFreshTabFragmentToHistoryFragment()
        navController.nav(R.id.freshTabFragment, direction)
    }

    override fun handleMenuClearDataClicked() {
        val deleteBrowsingData = DeleteBrowsingData(
            context, coroutineScope, tabsUseCases, sessionManager) {}
        deleteBrowsingData.askToDelete()
    }

    override fun handleNewsItemClicked(url: String) {
        // Remove existing session and add a new session.
        sessionManager.selectedSession?.let {
            tabsUseCases.removeTab.invoke(it)
            tabsUseCases.addTab.invoke(url, selectTab = true)
        }
        (context as BrowserActivity).openToBrowserAndLoad(
            searchTermOrUrl = url,
            newTab = false,
            from = BrowserDirection.FromFreshTab,
            private = false
        )
    }
}
