package org.mozilla.reference.browser.history.usecases

import mozilla.components.browser.storage.sync.PlacesHistoryStorage
import mozilla.components.concept.storage.VisitInfo

/**
 * @author Ravjit Uppal
 */
class HistoryUseCases(historyStorage: PlacesHistoryStorage) {

    class GetHistoryUseCase(private val historyStorage: PlacesHistoryStorage) {
        suspend operator fun invoke(): List<VisitInfo> {
            return historyStorage.getDetailedVisits(0)
        }
    }

    class GetTopSitesUseCase(private val historyStorage: PlacesHistoryStorage) {
        suspend operator fun invoke(): List<VisitInfo> {
            return historyStorage.getTopSitesVisits(5)
        }
    }

    val getHistory: GetHistoryUseCase by lazy { GetHistoryUseCase(historyStorage) }
    val getTopSites: GetTopSitesUseCase by lazy {GetTopSitesUseCase(historyStorage)}
}