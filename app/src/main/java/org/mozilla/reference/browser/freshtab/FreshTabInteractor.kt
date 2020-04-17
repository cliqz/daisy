/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.freshtab

import com.cliqz.browser.news.ui.NewsInteractor
import org.mozilla.reference.browser.freshtab.toolbar.SearchBarInteractor
import org.mozilla.reference.browser.freshtab.toolbar.TabsToolbarInteractor
import org.mozilla.reference.browser.freshtab.toolbar.ToolbarMenuInteractor

@Suppress("TooManyFunctions")
class FreshTabInteractor(
    private val freshTabController: FreshTabController
) : ToolbarMenuInteractor, SearchBarInteractor, NewsInteractor, TabsToolbarInteractor {

    override fun onTabsCounterClicked() {
        freshTabController.handleTabsCounterClicked()
    }

    override fun onForwardClicked() {
        freshTabController.handleForwardClicked()
    }

    override fun onNewTabClicked() {
        freshTabController.handleMenuNewTabClicked()
    }

    override fun onNewForgetTabClicked() {
        freshTabController.handleMenuNewForgetTabClicked()
    }

    override fun onReportIssueClicked() {
        freshTabController.handleMenuReportIssueClicked()
    }

    override fun onSettingsClicked() {
        freshTabController.handleMenuSettingsClicked()
    }

    override fun onHistoryClicked() {
        freshTabController.handleMenuHistoryClicked()
    }

    override fun onBookmarksClicked() {
        freshTabController.handleBookmarksClicked()
    }

    override fun onClearDataClicked() {
        freshTabController.handleMenuClearDataClicked()
    }

    override fun onSearchBarClicked() {
        freshTabController.handleSearchBarClicked()
    }

    override fun onNewsItemClicked(url: String) {
        freshTabController.handleNewsItemClicked(url)
    }
}
