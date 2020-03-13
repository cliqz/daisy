/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.topsites.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.feature.session.SessionUseCases.LoadUrlUseCase
import mozilla.components.feature.tabs.TabsUseCases
import org.mozilla.reference.browser.database.model.TopSite
import org.mozilla.reference.browser.library.history.usecases.HistoryUseCases

class TopSitesPresenter(
    private val view: View,
    private val loadUrlUseCase: LoadUrlUseCase,
    private val tabsUseCases: TabsUseCases,
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
        loadUrlUseCase.invoke(topSite.url)
    }

    fun openInNewTab(topSite: TopSite) {
        tabsUseCases.addTab.invoke(topSite.url)
    }

    fun openInPrivateTab(topSite: TopSite) {
        tabsUseCases.addPrivateTab.invoke(topSite.url)
    }
}
