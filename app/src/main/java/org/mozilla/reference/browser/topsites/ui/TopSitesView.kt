/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.topsites.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.top_site_context_menu.view.open_in_new_tab
import kotlinx.android.synthetic.main.top_site_context_menu.view.open_in_private_tab
import mozilla.components.browser.icons.BrowserIcons
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

    private var topSiteMenu: TopSiteMenu
    lateinit var popupWindow: PopupWindow

    init {
        isNestedScrollingEnabled = false
        layoutManager = GridLayoutManager(context, NUM_COLUMNS)
        topSiteMenu = TopSiteMenu(context) { menuItem, topSite ->
            popupWindow.dismiss()
            when (menuItem) {
                is TopSiteMenu.Item.OpenInNewTab -> presenter.openInNewTab(topSite)
                is TopSiteMenu.Item.OpenInPrivateTab -> presenter.openInPrivateTab(topSite)
            }
        }
    }

    fun initialize(
        historyUseCases: HistoryUseCases,
        browserIcons: BrowserIcons
    ) {
        presenter = TopSitesPresenter(context, this, historyUseCases)
        topSitesAdapter = TopSitesAdapter(browserIcons,
            presenter::onTopSiteClicked,
            ::onTopSiteLongClicked
        )
        adapter = topSitesAdapter
    }

    private fun onTopSiteLongClicked(topSite: TopSite, itemView: View): Boolean {
        topSiteMenu.topSite = topSite
        popupWindow = PopupWindow(
            topSiteMenu.view,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true)
        popupWindow.elevation =
            context.resources.getDimension(R.dimen.mozac_browser_menu_elevation)
        val xOffset = (itemView.width * contextMenuPercentXOffset).toInt()
        val yOffset = -(itemView.height *
            contextMenuPercentYOffset -
            context.resources.getDimensionPixelSize(R.dimen.top_site_context_menu_extra_y_offset)).toInt()
        popupWindow.showAsDropDown(itemView, xOffset, yOffset, Gravity.START)
        return true
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

    class TopSiteMenu(
        context: Context,
        private val onItemTapped: (Item, TopSite) -> Unit
    ) {

        lateinit var topSite: TopSite

        sealed class Item {
            object OpenInNewTab : Item()
            object OpenInPrivateTab : Item()
        }

        val view: View = View.inflate(context, R.layout.top_site_context_menu, null)

        init {
            view.open_in_new_tab.setOnClickListener {
                onItemTapped.invoke(Item.OpenInNewTab, topSite)
            }
            view.open_in_private_tab.setOnClickListener {
                onItemTapped.invoke(Item.OpenInPrivateTab, topSite)
            }
        }
    }

    companion object {
        private const val NUM_COLUMNS = 4
        private const val contextMenuPercentXOffset = 0.25f
        private const val contextMenuPercentYOffset = 0.50f
    }
}
