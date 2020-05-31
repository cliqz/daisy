/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui.viewholders

import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import mozilla.components.browser.menu.BrowserMenu
import mozilla.components.concept.storage.BookmarkNode
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.library.LibraryItemView
import org.mozilla.reference.browser.library.SelectionHolder
import org.mozilla.reference.browser.library.bookmarks.ui.BookmarkItemMenu
import org.mozilla.reference.browser.library.bookmarks.ui.BookmarkViewInteractor

/**
 * Base class for bookmark node view holders.
 */
abstract class BookmarkNodeViewHolder(
    final override val containerView: LibraryItemView,
    private val interactor: BookmarkViewInteractor
) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    var overflowView: ImageButton = containerView.actionButton

    init {
        setActionButtonIconForBookmarkLibraryItemView(overflowView)
    }

    abstract fun bind(item: BookmarkNode)

    protected fun setSelectionListeners(item: BookmarkNode, selectionHolder: SelectionHolder<BookmarkNode>) {
        containerView.setSelectionInteractor(item, selectionHolder, interactor)
    }

    protected fun setupMenu(item: BookmarkNode) {
        val bookmarkItemMenu = BookmarkItemMenu(containerView.context, item) {
            when (it) {
                BookmarkItemMenu.Item.OpenInNewTab -> interactor.onOpenInNormalTab(item)
                BookmarkItemMenu.Item.OpenInPrivateTab -> interactor.onOpenInPrivateTab(item)
                BookmarkItemMenu.Item.Delete -> interactor.onDelete(item)
                BookmarkItemMenu.Item.Edit -> interactor.onEdit(item)
            }
        }

        overflowView.setOnClickListener {
            bookmarkItemMenu.menuBuilder.build(containerView.context).show(
                anchor = it,
                orientation = BrowserMenu.Orientation.DOWN
            )
        }
    }

    private fun setActionButtonIconForBookmarkLibraryItemView(overflowView: ImageButton) {
        overflowView.setImageDrawable(
            ContextCompat.getDrawable(containerView.context, R.drawable.mozac_ic_menu))
    }
}
