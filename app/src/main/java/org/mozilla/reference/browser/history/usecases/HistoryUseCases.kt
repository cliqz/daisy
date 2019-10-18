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

	val getHistory: GetHistoryUseCase by lazy { GetHistoryUseCase(historyStorage) }
}