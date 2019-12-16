package org.mozilla.reference.browser.library.history.data

import kotlinx.coroutines.runBlocking
import mozilla.components.concept.storage.HistoryStorage
import mozilla.components.concept.storage.VisitInfo

class PagedHistoryProvider(
    private val historyStorage: HistoryStorage
) {

    fun getHistory(
        offset: Long,
        numberOfItems: Long,
        onComplete: (List<VisitInfo>) -> Unit
    ) {
        runBlocking {
            val history = historyStorage.getVisitsPaginated(
                offset,
                numberOfItems
            )
            onComplete(history)
        }
    }
}
