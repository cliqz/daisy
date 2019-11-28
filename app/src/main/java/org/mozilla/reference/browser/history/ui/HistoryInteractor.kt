package org.mozilla.reference.browser.history.ui

import org.mozilla.reference.browser.history.data.HistoryItem
import org.mozilla.reference.browser.library.MultiSelectionInteractor

/**
 * Borrowed from the Fenix project
 */
class HistoryInteractor(
    private val historyViewModel: HistoryViewModel,
    private val openToBrowser: (item: HistoryItem) -> Unit,
    private val deleteAll: () -> Unit,
    private val invalidateOptionsMenu: () -> Unit
) : MultiSelectionInteractor<HistoryItem> {

    override fun open(item: HistoryItem) {
        openToBrowser.invoke(item)
    }

    override fun select(item: HistoryItem) {
        historyViewModel.addToSelectedItems(item)
    }

    override fun deselect(item: HistoryItem) {
        historyViewModel.removeFromSelectedItems(item)
    }

    override fun onDeleteSome(items: Set<HistoryItem>) {
        historyViewModel.deleteMultipleHistoryItem(items)
    }

    fun onModeSwitched() {
        invalidateOptionsMenu.invoke()
    }

    override fun onBackPressed(): Boolean {
        if (historyViewModel.viewMode == ViewMode.Editing) {
            historyViewModel.clearSelectedItems()
            return true
        }
        return false
    }

    fun delete(item: HistoryItem) {
        historyViewModel.deleteHistoryItem(item)
    }

    fun onDeleteAll() {
        deleteAll.invoke()
    }
}
