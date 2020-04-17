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
import mozilla.components.concept.storage.SearchResult
import org.mozilla.reference.browser.library.history.data.HistoryItem
import org.mozilla.reference.browser.library.history.usecases.HistoryUseCases

@Suppress("TooManyFunctions")
class HistoryViewModel(
    private val historyUseCases: HistoryUseCases
) : ViewModel() {

    var historyItems: LiveData<PagedList<HistoryItem>> = historyUseCases.getPagedHistory()
        private set

    var viewMode = ViewMode.Normal

    val selectedItems = mutableSetOf<HistoryItem>()
    val selectedItemsLiveData = MutableLiveData<MutableSet<HistoryItem>>()

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

    fun searchHistory(query: String?): List<SearchResult> {
        return historyUseCases.searchHistory(query ?: "")
    }

    private fun invalidate() {
        historyItems.value?.dataSource?.invalidate()
    }
}

enum class ViewMode {
    Normal, Editing
}
