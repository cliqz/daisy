package org.mozilla.reference.browser.history.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.concept.storage.VisitInfo
import org.mozilla.reference.browser.history.usecases.HistoryUseCases

/**
 * @author Ravjit Uppal
 */
class HistoryViewModel(private val historyUseCases: HistoryUseCases) : ViewModel() {

    private val historyItems = MutableLiveData<List<VisitInfo>>().apply { value = emptyList() }

    init {
        fetchHistoryItems()
    }

    fun getHistoryItems(): LiveData<List<VisitInfo>> {
        return historyItems
    }

    private fun fetchHistoryItems() {
        viewModelScope.launch(Dispatchers.IO) {
            historyItems.postValue(historyUseCases.getHistory())
        }
    }
}