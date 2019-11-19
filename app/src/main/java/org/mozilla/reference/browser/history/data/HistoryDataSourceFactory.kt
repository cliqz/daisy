package org.mozilla.reference.browser.history.data

import androidx.paging.DataSource

class HistoryDataSourceFactory(
    private val historyProvider: PagedHistoryProvider
) : DataSource.Factory<Int, HistoryItem>() {

    override fun create(): DataSource<Int, HistoryItem> {
        return HistoryDataSource(historyProvider)
    }
}
