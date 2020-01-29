/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.library.history.ui

import org.mozilla.reference.browser.library.history.data.HistoryItem
import org.mozilla.reference.browser.library.MultiSelectionInteractor

/**
 * Borrowed from the Fenix project
 */
class HistoryInteractor(
    private val historyViewModel: HistoryViewModel,
    private val openToBrowser: (item: HistoryItem) -> Unit,
    private val deleteAll: () -> Unit,
    private val onBackPressed: () -> Boolean
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

    fun exitEditingMode(): Boolean {
        if (historyViewModel.viewMode == ViewMode.Editing) {
            historyViewModel.clearSelectedItems()
            return true
        }
        return exitView()
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

    fun exitView(): Boolean {
        return onBackPressed.invoke()
    }
}
