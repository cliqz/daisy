package org.mozilla.reference.browser.history.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.feature.session.SessionUseCases
import org.mozilla.reference.browser.history.data.HistoryItem
import org.mozilla.reference.browser.history.usecases.HistoryUseCases

/**
 * @author Ravjit Uppal
 */
class HistoryViewModel(
    private val historyUseCases: HistoryUseCases,
    private val sessionUseCases: SessionUseCases
) : ViewModel() {

    private lateinit var historyItems: LiveData<PagedList<HistoryItem>>

    init {
        fetchHistoryItems()
    }

    fun getHistoryItems(): LiveData<PagedList<HistoryItem>> {
        return historyItems
    }

    fun openHistoryItem(item: HistoryItem) {
        sessionUseCases.loadUrl(item.url)
    }

    fun deleteHistoryItem(item: HistoryItem) {
        viewModelScope.launch(Dispatchers.IO) {
            historyUseCases.deleteHistory(item)
            historyItems.value?.dataSource?.invalidate()
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            historyUseCases.clearAllHistory()
            historyItems.value?.dataSource?.invalidate()
        }
    }

    private fun fetchHistoryItems() {
        historyItems = historyUseCases.getPagedHistory()
    }
}
