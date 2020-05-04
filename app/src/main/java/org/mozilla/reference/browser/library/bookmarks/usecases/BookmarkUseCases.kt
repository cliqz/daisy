/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.usecases

import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.SearchResult
import org.mozilla.reference.browser.concepts.HistoryStorage

class BookmarkUseCases(historyStorage: HistoryStorage) {

    class GetBookmarksUseCase(private val historyStorage: HistoryStorage) {
        suspend operator fun invoke(): List<BookmarkNode> = historyStorage.getBookmarks()
    }

    class DeleteBookmarkUseCase(private val historyStorage: HistoryStorage) {
        suspend operator fun invoke(bookmarkItem: BookmarkNode) {
            bookmarkItem.url?.let { historyStorage.deleteBookmark(it) }
        }
    }

    class DeleteMultipleBookmarkUseCase(private val historyStorage: HistoryStorage) {
        suspend operator fun invoke(bookmarkItemList: Set<BookmarkNode>) {
            bookmarkItemList.forEach { bookmarkItem ->
                bookmarkItem.url?.let { historyStorage.deleteBookmark(it) }
            }
        }
    }

    class SearchBookmarksUseCase(private val historyStorage: HistoryStorage) {
        operator fun invoke(query: String): List<SearchResult> {
            return historyStorage.searchBookmarks(query)
        }
    }

    val getBookmarks by lazy { GetBookmarksUseCase(historyStorage) }
    val deleteBookmark by lazy { DeleteBookmarkUseCase(historyStorage) }
    val deleteMultipleBookmarks by lazy { DeleteMultipleBookmarkUseCase(historyStorage) }
    val searchBookmarks by lazy { SearchBookmarksUseCase(historyStorage) }
}
