/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.usecases

import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType
import mozilla.components.support.ktx.android.org.json.tryGetString
import org.mozilla.reference.browser.concepts.HistoryStorage
import org.mozilla.reference.browser.storage.HistoryDatabase.HistoryKeys

class BookmarkUseCases(historyStorage: HistoryStorage) {

    class GetBookmarksUseCase(private val historyStorage: HistoryStorage) {
        suspend operator fun invoke(): List<BookmarkNode> {
            val bookmarkList = mutableListOf<BookmarkNode>()
            val result = historyStorage.getBookmarks()
            for (i in 0 until result.length()) {
                val item = result.getJSONObject(i)
                bookmarkList.add(BookmarkNode(
                    type = BookmarkNodeType.ITEM,
                    guid = "",
                    parentGuid = null,
                    url = item.tryGetString(HistoryKeys.URL),
                    title = item.tryGetString(HistoryKeys.TIME),
                    position = null,
                    children = null))
            }
            return bookmarkList
        }
    }

    class DeleteBookmarkUseCase(private val historyStorage: HistoryStorage) {
        suspend operator fun invoke(bookmarkItem: BookmarkNode) {
            // Delete bookmark
        }
    }

    class DeleteMultipleBookmarkUseCase(private val historyStorage: HistoryStorage) {
        suspend operator fun invoke(bookmarkItemList: Set<BookmarkNode>) {
            // Delete bookmark items
        }
    }

    class SearchBookmarksUseCase(private val historyStorage: HistoryStorage) {
        operator fun invoke(query: String): List<BookmarkNode> {
            val bookmarkList = mutableListOf<BookmarkNode>()
            // Create search query for bookmark
            return bookmarkList
        }
    }

    val getBookmarks by lazy { GetBookmarksUseCase(historyStorage) }
    val deleteBookmark by lazy { DeleteBookmarkUseCase(historyStorage) }
    val deleteMultipleBookmarks by lazy { DeleteMultipleBookmarkUseCase(historyStorage) }
    val searchBookmarks by lazy { SearchBookmarksUseCase(historyStorage) }
}
