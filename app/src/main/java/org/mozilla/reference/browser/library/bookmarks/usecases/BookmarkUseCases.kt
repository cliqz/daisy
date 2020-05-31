/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.usecases

import mozilla.components.concept.storage.BookmarkInfo
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarksStorage

class BookmarkUseCases(bookmarksStorage: BookmarksStorage) {

    class GetBookmarksUseCase(private val bookmarksStorage: BookmarksStorage) {
        suspend operator fun invoke(id: String): BookmarkNode? = bookmarksStorage.getTree(id)
    }

    class DeleteBookmarkUseCase(private val bookmarksStorage: BookmarksStorage) {
        suspend operator fun invoke(bookmarkItem: BookmarkNode) {
            bookmarksStorage.deleteNode(bookmarkItem.guid)
        }
    }

    class DeleteMultipleBookmarkUseCase(private val bookmarksStorage: BookmarksStorage) {
        suspend operator fun invoke(bookmarkItemList: Set<BookmarkNode>) {
            bookmarkItemList.forEach { bookmarkItem ->
                bookmarksStorage.deleteNode(bookmarkItem.guid)
            }
        }
    }

    class SearchBookmarksUseCase(private val bookmarksStorage: BookmarksStorage) {
        suspend operator fun invoke(query: String): List<BookmarkNode> {
            return bookmarksStorage.searchBookmarks(query, limit = Integer.MAX_VALUE)
        }
    }

    class AddBookmarkFolderUseCase(private val bookmarksStorage: BookmarksStorage) {
        suspend operator fun invoke(parentGuid: String, title: String): String {
            return bookmarksStorage.addFolder(parentGuid, title)
        }
    }

    class EditBookmarkFolderUseCase(private val bookmarksStorage: BookmarksStorage) {
        suspend operator fun invoke(guid: String, bookmarkInfo: BookmarkInfo) {
            return bookmarksStorage.updateNode(guid, bookmarkInfo)
        }
    }

    val getBookmarks by lazy { GetBookmarksUseCase(bookmarksStorage) }
    val deleteBookmark by lazy { DeleteBookmarkUseCase(bookmarksStorage) }
    val deleteMultipleBookmarks by lazy { DeleteMultipleBookmarkUseCase(bookmarksStorage) }
    val searchBookmarks by lazy { SearchBookmarksUseCase(bookmarksStorage) }
    val addFolder by lazy { AddBookmarkFolderUseCase(bookmarksStorage) }
    val editBookmark by lazy { EditBookmarkFolderUseCase(bookmarksStorage) }
}
