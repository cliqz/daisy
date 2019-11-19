package org.mozilla.reference.browser.history.usecases

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import mozilla.components.concept.storage.HistoryStorage
import mozilla.components.concept.storage.VisitInfo
import org.mozilla.reference.browser.history.data.HistoryDataSourceFactory
import org.mozilla.reference.browser.history.data.HistoryItem
import org.mozilla.reference.browser.history.data.PagedHistoryProvider

/**
 * @author Ravjit Uppal
 */
class HistoryUseCases(historyStorage: HistoryStorage) {

    class GetPagedHistoryUseCase(private val historyStorage: HistoryStorage) {
        operator fun invoke(): LiveData<PagedList<HistoryItem>> {
            val historyProvider = PagedHistoryProvider(historyStorage)
            val historyDataSourceFactory = HistoryDataSourceFactory(historyProvider)
            return LivePagedListBuilder(historyDataSourceFactory, PAGE_SIZE)
                    .build()
        }
    }

    class GetHistoryUseCase(private val historyStorage: HistoryStorage) {
        suspend operator fun invoke(): List<VisitInfo> {
            return historyStorage.getDetailedVisits(0)
        }
    }

    class DeleteHistoryUseCase(private val historyStorage: HistoryStorage) {
        suspend operator fun invoke(historyItem: HistoryItem) {
            historyStorage.deleteVisit(historyItem.url, historyItem.visitTime)
        }
    }

    class ClearAllHistoryUseCase(private val historyStorage: HistoryStorage) {
        suspend operator fun invoke() {
            historyStorage.deleteEverything()
        }
    }

    val getHistory: GetHistoryUseCase by lazy { GetHistoryUseCase(historyStorage) }
    val getPagedHistory: GetPagedHistoryUseCase by lazy { GetPagedHistoryUseCase(historyStorage) }
    val deleteHistory: DeleteHistoryUseCase by lazy { DeleteHistoryUseCase(historyStorage) }
    val clearAllHistory: ClearAllHistoryUseCase by lazy { ClearAllHistoryUseCase(historyStorage) }

    companion object {
        private const val PAGE_SIZE = 25
    }
}
