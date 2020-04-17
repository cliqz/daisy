/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui

import mozilla.components.concept.storage.BookmarkNode
import org.mozilla.reference.browser.library.MultiSelectionInteractor

class BookmarkViewInteractor(
    private val bookmarkViewModel: BookmarkViewModel,
    private val openToBrowser: (items: Set<BookmarkNode>, private: Boolean) -> Unit,
    private val onBackPressed: () -> Boolean
) : MultiSelectionInteractor<BookmarkNode> {

    override fun open(items: Set<BookmarkNode>, private : Boolean) {
        openToBrowser.invoke(items, false)
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
        }
        return false
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
