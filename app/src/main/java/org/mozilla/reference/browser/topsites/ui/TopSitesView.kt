/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.topsites.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.feature.session.SessionUseCases
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.database.model.TopSite
import org.mozilla.reference.browser.library.history.usecases.HistoryUseCases

class TopSitesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr), TopSitesPresenter.View {

    private lateinit var topSitesAdapter: TopSitesAdapter
    private lateinit var presenter: TopSitesPresenter

    init {
        isNestedScrollingEnabled = false
        layoutManager = GridLayoutManager(context, NUM_COLUMNS)
        addItemDecoration(GridSpacingDecoration(
            context.resources.getDimension(R.dimen.margin_padding_size_large).toInt()
        ))
    }

    fun init(
        loadUrlUseCase: SessionUseCases.LoadUrlUseCase,
        getTopSitesUseCase: HistoryUseCases.GetTopSitesUseCase,
        browserIcons: BrowserIcons
    ) {
        presenter = TopSitesPresenter(this, loadUrlUseCase, getTopSitesUseCase)
        topSitesAdapter = TopSitesAdapter(browserIcons) {
            presenter.onTopSiteClicked(it)
        }
        adapter = topSitesAdapter
    }

    fun updateTopSites() {
        presenter.fetchTopSites()
    }

    override fun updateTopSitesData(topSites: List<TopSite>) {
        if (topSites.isNotEmpty()) {
            topSitesAdapter.topSites = topSites
        } else {
            visibility = View.GONE
        }
    }

    companion object {
        private const val NUM_COLUMNS = 4
    }
}
