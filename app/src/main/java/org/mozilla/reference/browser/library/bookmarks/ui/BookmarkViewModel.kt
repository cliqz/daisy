/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.SearchResult
import org.mozilla.reference.browser.ext.notifyObserver
import org.mozilla.reference.browser.library.bookmarks.usecases.BookmarkUseCases

class BookmarkViewModel(private val bookmarkUseCases: BookmarkUseCases) : ViewModel() {

    private val _bookmarkList = MutableLiveData<List<BookmarkNode>>()
    val bookmarkList: LiveData<List<BookmarkNode>> = _bookmarkList

    var viewMode = ViewMode.Normal

    private val _selectedItems = MutableLiveData<MutableSet<BookmarkNode>>()
    val selectedItems: LiveData<MutableSet<BookmarkNode>> = _selectedItems

    init {
        _bookmarkList.value = listOf()
        _selectedItems.value = mutableSetOf()
        fetchBookmarks()
    }

    private fun fetchBookmarks() {
        viewModelScope.launch(Dispatchers.IO) {
            val bookmarkList = bookmarkUseCases.getBookmarks()
            withContext(Dispatchers.Main) {
                _bookmarkList.value = bookmarkList
            }
        }
    }

    fun addToSelectedItems(item: BookmarkNode) {
        _selectedItems.value?.add(item)
        _selectedItems.notifyObserver()
    }

    fun removeFromSelectedItems(item: BookmarkNode) {
        _selectedItems.value?.remove(item)
        _selectedItems.notifyObserver()
    }

    fun clearSelectedItems() {
        _selectedItems.value?.clear()
        _selectedItems.notifyObserver()
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
