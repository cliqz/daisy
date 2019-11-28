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
    historyViewModel: HistoryViewModel,
    private val interactor: HistoryInteractor
) : LibraryPageView(containerView), BackHandler {

    private var viewMode = ViewMode.Normal
    private var selectedItems = mutableSetOf<HistoryItem>()

    val view: View = LayoutInflater.from(containerView.context)
        .inflate(R.layout.component_history, containerView, true)

    val historyAdapter: HistoryAdapter = HistoryAdapter(interactor, historyViewModel)

    init {
        view.history_list.adapter = historyAdapter
    }

    fun update(newViewMode: ViewMode, newSelectedItems: Set<HistoryItem>) {
        if (viewMode != newViewMode) {
            interactor.onModeSwitched()
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
            setUiForNormalMode(context.getString(R.string.history_screen_title), view.history_list)
        } else {
            setUiForEditingMode(
                context.getString(R.string.history_multiple_selected, historyAdapter.selectedItems.size),
                view.history_list
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
