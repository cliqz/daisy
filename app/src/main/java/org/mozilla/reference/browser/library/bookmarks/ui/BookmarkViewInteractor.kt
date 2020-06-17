/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui

import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType
import org.mozilla.reference.browser.library.MultiSelectionInteractor

@Suppress("TooManyFunctions")
class BookmarkViewInteractor(
    private val bookmarkViewModel: BookmarkViewModel,
    private val openToBrowser: (items: Set<BookmarkNode>, private: Boolean) -> Unit,
    private val expandBookmarkFolder: (item: BookmarkNode) -> Unit,
    private val onBackPressed: () -> Boolean,
    private val navigateToAddFolder: () -> Unit,
    private val navigateToEditBookmark: (item: BookmarkNode) -> Unit
) : MultiSelectionInteractor<BookmarkNode> {

    override fun open(items: Set<BookmarkNode>, newTab: Boolean, private: Boolean) {
        when (items.first().type) {
            BookmarkNodeType.ITEM -> {
                openToBrowser.invoke(items, private)
            }
            BookmarkNodeType.FOLDER -> {
                expandBookmarkFolder.invoke(items.first())
            }
            BookmarkNodeType.SEPARATOR -> throw IllegalStateException("Cannot open separators")
        }
    }

    override fun select(item: BookmarkNode) {
        bookmarkViewModel.addToSelectedItems(item)
    }

    override fun deselect(item: BookmarkNode) {
        bookmarkViewModel.removeFromSelectedItems(item)
    }

    override fun onDeleteSome(items: Set<BookmarkNode>) {
        bookmarkViewModel.deleteMultipleBookmarkItem(items)
    }

    override fun onBackPressed(): Boolean {
        if (bookmarkViewModel.viewMode == ViewMode.Editing) {
            bookmarkViewModel.clearSelectedItems()
            return true
        }
        return false
    }

    fun navigateToAddFolder() {
        navigateToAddFolder.invoke()
    }

    fun onEdit(item: BookmarkNode) {
        navigateToEditBookmark.invoke(item)
    }

    fun onOpenInNormalTab(item: BookmarkNode) {
        open(setOf(item), false)
    }

    fun onOpenInPrivateTab(item: BookmarkNode) {
        openToBrowser.invoke(setOf(item), true)
    }

    fun onDelete(item: BookmarkNode) {
        bookmarkViewModel.deleteBookmarkItem(item)
    }

    fun exitView(): Boolean {
        return onBackPressed.invoke()
    }
}
