/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.SearchResult
import org.mozilla.reference.browser.library.bookmarks.usecases.BookmarkUseCases

class BookmarkViewModel(private val bookmarkUseCases: BookmarkUseCases) : ViewModel() {

    var bookmarkList = listOf<BookmarkNode>()
    val bookmarkItemsLiveData = MutableLiveData<List<BookmarkNode>>()

    var viewMode = ViewMode.Normal

    val selectedItems = mutableSetOf<BookmarkNode>()
    val selectedItemsLiveData = MutableLiveData<MutableSet<BookmarkNode>>()

    init {
        fetchBookmarks()
    }

    private fun fetchBookmarks() {
        viewModelScope.launch(Dispatchers.IO) {
            bookmarkList = bookmarkUseCases.getBookmarks()
            withContext(Dispatchers.Main) {
                bookmarkItemsLiveData.value = bookmarkList
            }
        }
    }

    fun addToSelectedItems(item: BookmarkNode) {
        selectedItems.add(item)
        selectedItemsLiveData.value = selectedItems
    }

    fun removeFromSelectedItems(item: BookmarkNode) {
        selectedItems.remove(item)
        selectedItemsLiveData.value = selectedItems
    }

    fun clearSelectedItems() {
        selectedItems.clear()
        selectedItemsLiveData.value = selectedItems
    }

    fun deleteBookmarkItem(item: BookmarkNode) {
        viewModelScope.launch(Dispatchers.IO) {
            bookmarkUseCases.deleteBookmark(item)
            invalidate()
        }
    }

    fun deleteMultipleBookmarkItem(itemList: Set<BookmarkNode>) {
        viewModelScope.launch(Dispatchers.IO) {
            bookmarkUseCases.deleteMultipleBookmarks(itemList)
            invalidate()
            withContext(Dispatchers.Main) {
                clearSelectedItems()
            }
        }
    }

    fun searchBookmarks(query: String?): List<SearchResult> {
        return bookmarkUseCases.searchBookmarks(query ?: "")
    }

    private fun invalidate() {
        fetchBookmarks()
    }
}

enum class ViewMode {
    Normal, Editing
}
