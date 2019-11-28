package org.mozilla.reference.browser.history.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.component_history.view.*
import mozilla.components.support.base.feature.BackHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.distinct
import org.mozilla.reference.browser.history.data.HistoryItem
import org.mozilla.reference.browser.library.LibraryPageView

/**
 * Borrowed from the Fenix project
 */
class HistoryView(
    containerView: ViewGroup,
    private val historyViewModel: HistoryViewModel,
    private val interactor: HistoryInteractor
) : LibraryPageView(containerView), BackHandler {

    private var viewMode = ViewMode.Normal
    private var selectedItems = mutableSetOf<HistoryItem>()

    val view: View = LayoutInflater.from(containerView.context)
        .inflate(R.layout.component_history, containerView, true)

    val historyAdapter: HistoryAdapter = HistoryAdapter(interactor, historyViewModel)

    init {
        view.history_list.adapter = historyAdapter
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

    private fun setupToolbarListeners() {
        view.toolbar.setNavigationIcon(R.drawable.mozac_ic_back)
        view.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.close -> {
                    interactor.exitView()
                    true
                }
                R.id.delete -> {
                    historyViewModel.deleteMultipleHistoryItem(historyViewModel.selectedItems)
                    historyViewModel.viewMode = ViewMode.Normal
                    true
                }
                else -> throw IllegalArgumentException("Invalid menu item")
            }
        }
        view.toolbar.setNavigationOnClickListener {
            interactor.exitEditingMode()
        }
    }

    private fun onModeSwitched() {
        view.toolbar.invalidate()
        createToolbarMenu()
    }

    fun update(newViewMode: ViewMode, newSelectedItems: Set<HistoryItem>) {
        if (viewMode != newViewMode) {
            onModeSwitched()
            historyAdapter.updateViewMode(newViewMode)

            // Deselect all the previously selected items
            selectedItems.forEach {
                historyAdapter.notifyItemChanged(it.id)
            }
            (view.history_list.layoutManager as LinearLayoutManager).apply {
                historyAdapter.notifyItemRangeChanged(0, findFirstVisibleItemPosition())
                historyAdapter.notifyItemRangeChanged(findLastVisibleItemPosition(), historyAdapter.itemCount)
            }
        }

        if (newViewMode == ViewMode.Editing) {
            selectedItems.distinct(newSelectedItems).forEach {
                historyAdapter.notifyItemChanged(it.id)
            }
        }

        if (newViewMode == ViewMode.Normal) {
            setUiForNormalMode(context.getString(R.string.history_screen_title), view.history_list, view.toolbar)
        } else {
            setUiForEditingMode(
                context.getString(R.string.history_multiple_selected, historyAdapter.selectedItems.size),
                view.history_list,
                view.toolbar
            )
        }
        viewMode = newViewMode
        selectedItems = newSelectedItems.toMutableSet()
    }

    override fun onBackPressed(): Boolean {
        return interactor.onBackPressed()
    }

    fun updateEmptyState(userHasHistory: Boolean) {
        view.history_list.isVisible = userHasHistory
        view.empty_view.isVisible = !userHasHistory
    }
}
