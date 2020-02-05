/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.library.history.ui

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.isVisible
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.component_history.view.empty_view
import kotlinx.android.synthetic.main.component_history.view.history_list
import kotlinx.android.synthetic.main.component_history.view.history_search_list
import kotlinx.android.synthetic.main.component_history.view.toolbar
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.distinct
import org.mozilla.reference.browser.ext.getQuantityString
import org.mozilla.reference.browser.library.LibraryPageView
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
        containerView.context.components.core.icons)
    private var searchItem: MenuItem? = null

    init {
        view.history_list.adapter = historyAdapter
        view.history_search_list.adapter = historySearchAdapter
        createToolbarMenu()
        setupToolbarListeners()
        update(ViewMode.Normal, emptySet())
    }

    private fun createToolbarMenu() {
        val layout = when (historyViewModel.viewMode) {
            ViewMode.Normal -> R.menu.history_menu
            ViewMode.Editing -> R.menu.history_multi_select_menu
        }
        view.toolbar.menu.clear()
        view.toolbar.inflateMenu(layout)
    }

    @Suppress("PrivateResource", "ComplexMethod")
    private fun setupToolbarListeners() {
        view.toolbar.setNavigationIcon(R.drawable.mozac_ic_back)
        view.toolbar.setNavigationContentDescription(R.string.abc_action_bar_up_description)
        view.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.close -> {
                    if (view.history_search_list.visibility == View.VISIBLE) {
                        view.history_search_list.visibility = View.GONE
                        searchItem?.collapseActionView()
                    } else {
                        interactor.exitView()
                    }
                    true
                }
                R.id.delete -> {
                    historyViewModel.deleteMultipleHistoryItem(historyViewModel.selectedItems)
                    historyViewModel.viewMode = ViewMode.Normal
                    true
                }
                R.id.history_search -> {
                    false
                }
                else -> throw IllegalArgumentException("Invalid menu item")
            }
        }
        view.toolbar.setNavigationOnClickListener {
            interactor.exitEditingMode()
        }
        searchItem = view.toolbar.menu.findItem(R.id.history_search)
        searchItem?.let {
            val searchView = it.actionView as SearchView
            searchView.setOnCloseListener {
                view.history_search_list.visibility = View.GONE
                true
            }
            searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    view.history_search_list.visibility = View.GONE
                    return true
                }
            })
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    if (query.isNullOrBlank()) return true
                    view.history_search_list.visibility = View.VISIBLE
                    historySearchAdapter.setData(historyViewModel.searchHistory(query))
                    return true
                }
            })
        }
    }

    private fun onModeSwitched() {
        view.toolbar.invalidate()
        createToolbarMenu()
        setupToolbarListeners()
    }

    fun submitList(pagedList: PagedList<HistoryItem>?) {
        historyAdapter.submitList(pagedList)
    }

    fun update(newViewMode: ViewMode, newSelectedItems: Set<HistoryItem>) {
        if (viewMode != newViewMode) {
            onModeSwitched()
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
            setUiForNormalMode(context.getString(R.string.history_screen_title), view.history_list, view.toolbar)
        } else {
            setUiForEditingMode(
                context.getQuantityString(
                    R.plurals.history_items_selected,
                    historyAdapter.selectedItems.size,
                    historyAdapter.selectedItems.size
                ),
                view.history_list,
                view.toolbar
            )
        }
        viewMode = newViewMode
        selectedItems = newSelectedItems.toSet()
    }

    override fun onBackPressed(): Boolean {
        if (view.history_search_list.visibility == View.VISIBLE) {
            view.history_search_list.visibility = View.GONE
            searchItem?.collapseActionView()
            return true
        } else {
            return interactor.onBackPressed()
        }
    }

    fun updateEmptyState(userHasHistory: Boolean) {
        view.history_list.isVisible = userHasHistory
        view.empty_view.isVisible = !userHasHistory
    }
}
