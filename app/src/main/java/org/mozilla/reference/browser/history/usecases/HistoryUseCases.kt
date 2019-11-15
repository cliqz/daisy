package org.mozilla.reference.browser.history.usecases

import mozilla.components.concept.storage.HistoryStorage
import mozilla.components.concept.storage.VisitInfo

/**
 * @author Ravjit Uppal
 */
class HistoryUseCases(historyStorage: HistoryStorage) {

    class GetHistoryUseCase(private val historyStorage: HistoryStorage) {
        suspend operator fun invoke(): List<VisitInfo> {
            return historyStorage.getDetailedVisits(0)
        }
    }

    class DeleteHistoryUseCase(private val historyStorage: HistoryStorage) {
        suspend operator fun invoke(visitInfo: VisitInfo) {
            historyStorage.deleteVisit(visitInfo.url, visitInfo.visitTime)
        }
    }

    class ClearAllHistoryUseCase(private val historyStorage: HistoryStorage) {
        suspend operator fun invoke() {
            historyStorage.deleteEverything()
        }
    }

    val getHistory: GetHistoryUseCase by lazy { GetHistoryUseCase(historyStorage) }
    val deleteHistory: DeleteHistoryUseCase by lazy { DeleteHistoryUseCase(historyStorage) }
    val clearAllHistory: ClearAllHistoryUseCase by lazy { ClearAllHistoryUseCase(historyStorage) }
}