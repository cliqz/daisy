/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.library.history.usecases

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import mozilla.components.concept.storage.HistoryStorage
import mozilla.components.concept.storage.VisitInfo
import org.mozilla.reference.browser.library.history.data.HistoryDataSourceFactory
import org.mozilla.reference.browser.library.history.data.HistoryItem
import org.mozilla.reference.browser.library.history.data.PagedHistoryProvider

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

    class DeleteMultipleHistoryUseCase(private val historyStorage: HistoryStorage) {
        suspend operator fun invoke(historyItemList: Set<HistoryItem>) {
            historyItemList.forEach { historyItem ->
                historyStorage.deleteVisit(historyItem.url, historyItem.visitTime)
            }
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

    val getHistory by lazy { GetHistoryUseCase(historyStorage) }
    val getPagedHistory by lazy { GetPagedHistoryUseCase(historyStorage) }
    val deleteMultipleHistoryUseCase by lazy { DeleteMultipleHistoryUseCase(historyStorage) }
    val deleteHistory by lazy { DeleteHistoryUseCase(historyStorage) }
    val clearAllHistory by lazy { ClearAllHistoryUseCase(historyStorage) }

    companion object {
        private const val PAGE_SIZE = 25
    }
}
