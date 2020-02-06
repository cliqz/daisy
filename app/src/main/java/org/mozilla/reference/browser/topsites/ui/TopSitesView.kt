/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.topsites.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.GridView
import android.widget.LinearLayout
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.feature.session.SessionUseCases
import org.mozilla.reference.browser.database.model.TopSite
import org.mozilla.reference.browser.library.history.usecases.HistoryUseCases

class TopSitesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridView(context, attrs, defStyleAttr), TopSitesPresenter.View {

    private lateinit var topSitesAdapter: TopSitesAdapter
    private lateinit var presenter: TopSitesPresenter

    fun init(
        loadUrlUseCase: SessionUseCases.LoadUrlUseCase,
        getTopSitesUseCase: HistoryUseCases.GetTopSitesUseCase,
        browserIcons: BrowserIcons
    ) {
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        gravity = Gravity.CENTER
        topSitesAdapter = TopSitesAdapter(browserIcons)
        adapter = topSitesAdapter
        numColumns = TOP_SITES_COUNT
        presenter = TopSitesPresenter(this, loadUrlUseCase, getTopSitesUseCase)
        setOnItemClickListener { _, _, position, _ ->
            if (topSitesAdapter.topSites.size >= position + 1) {
                presenter.onTopSiteClicked(topSitesAdapter.topSites[position].url)
            }
        }
    }

    fun updateTopSites() {
        presenter.fetchTopSites()
    }

    override fun updateTopSitesData(topSites: List<TopSite>) {
        topSitesAdapter.topSites = topSites
    }

    companion object {
        private const val TOP_SITES_COUNT = 5
    }
}
