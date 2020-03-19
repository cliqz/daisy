/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.topsites.ui

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.BrowserDirection
import org.mozilla.reference.browser.database.model.TopSite
import org.mozilla.reference.browser.library.history.usecases.HistoryUseCases

class TopSitesPresenter(
    private val context: Context,
    private val view: View,
    private val historyUseCases: HistoryUseCases
) {

    interface View {
        fun updateTopSitesData(topSites: List<TopSite>)
    }

    init {
        fetchTopSites()
    }

    fun fetchTopSites() = GlobalScope.launch(Dispatchers.Main) {
        val historyInfo = withContext(Dispatchers.IO) {
            historyUseCases.getTopSites.invoke()
        }
        view.updateTopSitesData(historyInfo)
    }

    fun onTopSiteClicked(topSite: TopSite) {
        (context as BrowserActivity).openToBrowserAndLoad(
            searchTermOrUrl = topSite.url,
            newTab = false,
            from = BrowserDirection.FromFreshTab,
            private = false
        )
    }

    fun openInNewTab(topSite: TopSite) {
        (context as BrowserActivity).openToBrowserAndLoad(
            searchTermOrUrl = topSite.url,
            newTab = true,
            from = BrowserDirection.FromFreshTab,
            private = false
        )
    }

    fun openInPrivateTab(topSite: TopSite) {
        (context as BrowserActivity).openToBrowserAndLoad(
            searchTermOrUrl = topSite.url,
            newTab = true,
            from = BrowserDirection.FromFreshTab,
            private = true
        )
    }
}
