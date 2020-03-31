/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.topsites.ui

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.reference.browser.BrowserDirection
import org.mozilla.reference.browser.storage.model.TopSite
import org.mozilla.reference.browser.ext.openToBrowserAndLoad
import org.mozilla.reference.browser.library.history.usecases.HistoryUseCases

class TopSitesPresenter(
    private val context: Context,
    private val view: View,
    private val scope: CoroutineScope,
    private val historyUseCases: HistoryUseCases
) {

    interface View {
        fun updateTopSitesData(topSites: List<TopSite>)
    }

    init {
        fetchTopSites()
    }

    fun fetchTopSites() = scope.launch(Dispatchers.IO) {
        val historyInfo = historyUseCases.getTopSites.invoke()
        withContext(Dispatchers.Main) {
            view.updateTopSitesData(historyInfo)
        }
    }

    fun onTopSiteClicked(topSite: TopSite) {
        context.openToBrowserAndLoad(
            searchTermOrUrl = topSite.url,
            newTab = false,
            from = BrowserDirection.FromFreshTab,
            private = false
        )
    }

    fun openInNewTab(topSite: TopSite) {
        context.openToBrowserAndLoad(
            searchTermOrUrl = topSite.url,
            newTab = true,
            from = BrowserDirection.FromFreshTab,
            private = false
        )
    }

    fun openInPrivateTab(topSite: TopSite) {
        context.openToBrowserAndLoad(
            searchTermOrUrl = topSite.url,
            newTab = true,
            from = BrowserDirection.FromFreshTab,
            private = true
        )
    }

    fun removeFromTopSite(topSite: TopSite) = scope.launch(Dispatchers.IO) {
        historyUseCases.removeFromTopSites.invoke(topSite)
        fetchTopSites()
    }
}
