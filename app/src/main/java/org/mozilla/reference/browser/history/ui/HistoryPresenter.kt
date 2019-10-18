package org.mozilla.reference.browser.history.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.concept.storage.VisitInfo
import org.mozilla.reference.browser.history.usecases.HistoryUseCases

/**
 * @author Ravjit Uppal
 */
class HistoryPresenter(
    private var view: View?,
    private val historyUseCases: HistoryUseCases
) {
    interface View {
        fun renderHistory(historyItems: List<VisitInfo>)
    }

    fun onCreate() = GlobalScope.launch(Dispatchers.Main) {
        val historyInfo = withContext(Dispatchers.IO) { historyUseCases.getHistory() }
        view?.renderHistory(historyInfo)
    }

    fun onDestroy() {
        view = null
    }
}