/* This Source Code Form is subject to the terms of the Mozilla Public
   License, v. 2.0. If a copy of the MPL was not distributed with this
   file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.library.history.data

import androidx.paging.DataSource

class HistoryDataSourceFactory(
    private val historyProvider: PagedHistoryProvider
) : DataSource.Factory<Int, HistoryItem>() {

    override fun create(): DataSource<Int, HistoryItem> {
        return HistoryDataSource(historyProvider)
    }
}
