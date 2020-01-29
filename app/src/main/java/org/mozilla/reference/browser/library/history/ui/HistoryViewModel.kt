/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.library.history.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.feature.session.SessionUseCases
import org.mozilla.reference.browser.library.history.data.HistoryItem
import org.mozilla.reference.browser.library.history.usecases.HistoryUseCases

class HistoryViewModel(
    private val historyUseCases: HistoryUseCases,
    private val sessionUseCases: SessionUseCases
) : ViewModel() {

    private lateinit var historyItems: LiveData<PagedList<HistoryItem>>

    var viewMode = ViewMode.Normal

    val selectedItems = mutableSetOf<HistoryItem>()
    val selectedItemsLiveData = MutableLiveData<MutableSet<HistoryItem>>()

    init {
        fetchHistoryItems()
    }

    fun addToSelectedItems(item: HistoryItem) {
        selectedItems.add(item)
        selectedItemsLiveData.value = selectedItems
    }

    fun removeFromSelectedItems(item: HistoryItem) {
        selectedItems.remove(item)
        selectedItemsLiveData.value = selectedItems
    }

    fun clearSelectedItems() {
        selectedItems.clear()
        selectedItemsLiveData.value = selectedItems
    }

    fun getHistoryItems(): LiveData<PagedList<HistoryItem>> {
        return historyItems
    }

    fun openHistoryItem(item: HistoryItem) {
        sessionUseCases.loadUrl(item.url)
    }

    fun deleteMultipleHistoryItem(itemList: Set<HistoryItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            historyUseCases.deleteMultipleHistoryUseCase(itemList)
            invalidate()
            withContext(Dispatchers.Main) {
                clearSelectedItems()
            }
        }
    }

    fun deleteHistoryItem(item: HistoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            historyUseCases.deleteHistory(item)
            invalidate()
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            historyUseCases.clearAllHistory()
            invalidate()
        }
    }

    private fun invalidate() {
        historyItems.value?.dataSource?.invalidate()
    }

    private fun fetchHistoryItems() {
        historyItems = historyUseCases.getPagedHistory()
    }
}

enum class ViewMode {
    Normal, Editing
}
