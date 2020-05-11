/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui.viewholders

import mozilla.components.concept.storage.BookmarkNode
import org.mozilla.reference.browser.library.LibraryItemView
import org.mozilla.reference.browser.library.SelectionHolder
import org.mozilla.reference.browser.library.bookmarks.ui.BookmarkViewInteractor

class BookmarkFolderViewHolder(
    view: LibraryItemView,
    interactor: BookmarkViewInteractor,
    private val selectionHolder: SelectionHolder<BookmarkNode>
) : BookmarkNodeViewHolder(view, interactor) {

    override fun bind(item: BookmarkNode) {
        // to-do
    }
}
