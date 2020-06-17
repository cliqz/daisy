/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.concept.storage.BookmarkInfo
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType
import org.mozilla.reference.browser.library.bookmarks.usecases.BookmarkUseCases

class EditBookmarkViewModel(private val bookmarkUseCases: BookmarkUseCases) : ViewModel() {

    private val _tree = MutableLiveData<BookmarkNode?>()
    val tree: LiveData<BookmarkNode?> = _tree

    private val _parentTree = MutableLiveData<BookmarkNode?>()
    val parentTree: LiveData<BookmarkNode?> = _parentTree

    fun fetchBookmarks(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val tree = bookmarkUseCases.getBookmarks.invoke(id)
            withContext(Dispatchers.Main) {
                _tree.value = tree
            }
        }
    }

    fun fetchParentBookmark(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val tree = bookmarkUseCases.getBookmarks.invoke(id)
            withContext(Dispatchers.Main) {
                _parentTree.value = tree
            }
        }
    }

    fun editBookmark(guid: String, parentGuid: String?, title: String, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            bookmarkUseCases.editBookmark(
                guid,
                BookmarkInfo(
                    parentGuid,
                    tree.value?.position,
                    title,
                    if (tree.value?.type == BookmarkNodeType.ITEM) url else null
                )
            )
        }
    }
}
