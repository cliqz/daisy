package org.mozilla.reference.browser.history.data

import androidx.paging.ItemKeyedDataSource
import mozilla.components.concept.storage.VisitInfo

class HistoryDataSource(
    private val historyProvider: PagedHistoryProvider
) : ItemKeyedDataSource<Int, HistoryItem>() {

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<HistoryItem>
    ) {
        historyProvider.getHistory(INITIAL_OFFSET, params.requestedLoadSize.toLong()) {
            callback.onResult(it.convertToHistoryItems(INITIAL_OFFSET.toInt()))
        }
    }

    override fun loadAfter(
        params: LoadParams<Int>,
        callback: LoadCallback<HistoryItem>
    ) {
        historyProvider.getHistory(params.key.toLong(), params.requestedLoadSize.toLong()) {
            callback.onResult(it.convertToHistoryItems(params.key))
        }
    }

    override fun loadBefore(
        params: LoadParams<Int>,
        callback: LoadCallback<HistoryItem>
    ) {
        // no op
    }

    override fun getKey(item: HistoryItem): Int {
        return item.id + 1
    }

    companion object {
        private const val INITIAL_OFFSET = 0L

        fun List<VisitInfo>.convertToHistoryItems(offset: Int): List<HistoryItem> {
            return mapIndexed { index, visitInfo ->
                // TODO: Provide a title when there is none.
                HistoryItem(
                    offset + index,
                    visitInfo.title ?: "",
                    visitInfo.url,
                    visitInfo.visitTime,
                    visitInfo.visitType
                )
            }
        }
    }
}
