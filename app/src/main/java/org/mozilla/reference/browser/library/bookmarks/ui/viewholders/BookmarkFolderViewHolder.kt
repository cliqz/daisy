/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui.viewholders

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import kotlinx.android.synthetic.main.two_line_list_item_with_action_layout.view.*
import mozilla.components.concept.storage.BookmarkNode
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.library.LibraryItemView
import org.mozilla.reference.browser.library.SelectionHolder
import org.mozilla.reference.browser.library.bookmarks.ui.BookmarkViewInteractor

class BookmarkFolderViewHolder(
    view: LibraryItemView,
    interactor: BookmarkViewInteractor,
    private val selectionHolder: SelectionHolder<BookmarkNode>
) : BookmarkNodeViewHolder(view, interactor) {

    override fun bind(item: BookmarkNode) {
        containerView.toggleActionButton(
            showActionButton = selectionHolder.selectedItems.isEmpty()
        )
        setupMenu(item)

        containerView.titleView.text = item.title
        containerView.urlView.visibility = View.GONE
        containerView.favicon.setImageDrawable(
            AppCompatResources.getDrawable(containerView.context, R.drawable.ic_folder_icon))
        containerView.changeSelected(item in selectionHolder.selectedItems)
        setSelectionListeners(item, selectionHolder)
    }
}
