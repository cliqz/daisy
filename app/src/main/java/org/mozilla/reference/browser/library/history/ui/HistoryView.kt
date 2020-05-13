/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.library.history.ui

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.component_history.view.*
import kotlinx.android.synthetic.main.library_toolbar.view.*
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.distinct
import org.mozilla.reference.browser.ext.getQuantityString
import org.mozilla.reference.browser.library.LibraryPageView
import org.mozilla.reference.browser.library.LibraryToolbar
import org.mozilla.reference.browser.library.history.data.HistoryItem

/**
 * Borrowed from the Fenix project
 */
class HistoryView(
    containerView: ViewGroup,
    private val historyViewModel: HistoryViewModel,
    private val interactor: HistoryInteractor
) : LibraryPageView(containerView), UserInteractionHandler {

    private var viewMode = ViewMode.Normal
    private var selectedItems = setOf<HistoryItem>()

    val view: View = LayoutInflater.from(containerView.context)
        .inflate(R.layout.component_history, containerView, true)

    private val historyAdapter: HistoryAdapter = HistoryAdapter(interactor, historyViewModel)
    private val historySearchAdapter: HistorySearchAdapter = HistorySearchAdapter(
        interactor,
        containerView.context.components.core.icons
    )
    private var searchItem: MenuItem? = null

    init {
        view.search.hint = context.getString(R.string.history_search_hint)
        view.history_list.adapter = historyAdapter
        view.history_search_list.adapter = historySearchAdapter
        view.toolbar.register(object : LibraryToolbar.Observer {
            override fun close() {
                if (view.history_search_list.visibility == View.VISIBLE) {
                    view.history_search_list.visibility = View.GONE
                    searchItem?.collapseActionView()
                } else {
                    interactor.exitView()
                }
            }

            override fun delete() {
                historyViewModel.deleteMultipleHistoryItem(historyViewModel.selectedItems)
                historyViewModel.viewMode = ViewMode.Normal
            }

            override fun openAll(newTab: Boolean, private: Boolean) {
                interactor.open(historyViewModel.selectedItems, newTab, private)
            }

            override fun searchOpened() {
                // no-op
            }

            override fun searchClosed() {
                view.history_search_list.visibility = View.GONE
            }

            override fun searchQueryChanged(query: String) {
                if (query.isBlank()) return
                view.history_search_list.visibility = View.VISIBLE
                historySearchAdapter.setData(historyViewModel.searchHistory(query))
            }
        })
        update(ViewMode.Normal, emptySet())
    }

    fun submitList(pagedList: PagedList<HistoryItem>?) {
        historyAdapter.submitList(pagedList)
    }

    fun update(newViewMode: ViewMode, newSelectedItems: Set<HistoryItem>) {
        if (viewMode != newViewMode) {
            historyAdapter.updateViewMode(newViewMode)

            // Deselect all the previously selected items
            selectedItems.forEach {
                val position = it.id + 1
                historyAdapter.notifyItemChanged(position)
            }
            (view.history_list.layoutManager as LinearLayoutManager).apply {
                historyAdapter.notifyItemRangeChanged(0, findFirstVisibleItemPosition())
                historyAdapter.notifyItemRangeChanged(findLastVisibleItemPosition(), historyAdapter.itemCount)
            }
        }

        if (newViewMode == ViewMode.Editing) {
            selectedItems.distinct(newSelectedItems).forEach {
                val position = it.id + 1
                historyAdapter.notifyItemChanged(position)
            }
        }

        if (newViewMode == ViewMode.Normal) {
            setUiForNormalMode(
                context.getString(R.string.history_screen_title),
                view.history_list,
                view.toolbar,
                R.menu.history_menu)
        } else {
            setUiForEditingMode(
                context.getQuantityString(
                    R.plurals.history_items_selected,
                    historyAdapter.selectedItems.size,
                    historyAdapter.selectedItems.size
                ),
                view.history_list,
                view.toolbar,
                R.menu.library_multi_select_menu
            )
        }
        viewMode = newViewMode
        selectedItems = newSelectedItems.toSet()
    }

    override fun onBackPressed(): Boolean {
        return if (view.history_search_list.visibility == View.VISIBLE) {
            view.history_search_list.visibility = View.GONE
            searchItem?.collapseActionView()
            true
        } else {
            interactor.onBackPressed()
        }
    }

    fun updateEmptyState(userHasHistory: Boolean) {
        view.history_list.isVisible = userHasHistory
        view.empty_view.isVisible = !userHasHistory
    }
}
