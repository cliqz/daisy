/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType
import org.mozilla.reference.browser.library.LibraryItemView
import org.mozilla.reference.browser.library.SelectionHolder
import org.mozilla.reference.browser.library.bookmarks.ui.viewholders.BookmarkFolderViewHolder
import org.mozilla.reference.browser.library.bookmarks.ui.viewholders.BookmarkItemViewHolder
import org.mozilla.reference.browser.library.bookmarks.ui.viewholders.BookmarkNodeViewHolder

class BookmarkAdapter(
    private val bookmarkInteractor: BookmarkViewInteractor,
    private val bookmarkViewModel: BookmarkViewModel
) : RecyclerView.Adapter<BookmarkNodeViewHolder>(), SelectionHolder<BookmarkNode> {

    private var viewMode = ViewMode.Normal

    private var bookmarkList: List<BookmarkNode> = listOf()

    override val selectedItems: Set<BookmarkNode> get() = bookmarkViewModel.selectedItems

    fun updateData(
        bookmarkList: List<BookmarkNode>,
        viewMode: ViewMode,
        selectedItems: Set<BookmarkNode>
    ) {
        val diffUtil = DiffUtil.calculateDiff(
            BookmarkDiffUtil(
                this.bookmarkList,
                bookmarkList,
                this.viewMode,
                viewMode,
                this.selectedItems,
                selectedItems
            )
        )

        this.bookmarkList = bookmarkList
        this.viewMode = viewMode

        diffUtil.dispatchUpdatesTo(this)
    }

    private class BookmarkDiffUtil(
        val old: List<BookmarkNode>,
        val new: List<BookmarkNode>,
        val oldMode: ViewMode,
        val newMode: ViewMode,
        val oldSelectedItems: Set<BookmarkNode>,
        val newSelectedItems: Set<BookmarkNode>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            old[oldItemPosition].guid == new[newItemPosition].guid

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldMode == newMode &&
                old[oldItemPosition] in oldSelectedItems == new[newItemPosition] in newSelectedItems &&
                old[oldItemPosition].title == new[newItemPosition].title &&
                old[oldItemPosition].url == new[newItemPosition].url

        override fun getOldListSize(): Int = old.size
        override fun getNewListSize(): Int = new.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkNodeViewHolder {
        val view = LibraryItemView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        return when (viewType) {
            BookmarkNodeType.ITEM.ordinal -> BookmarkItemViewHolder(view, bookmarkInteractor, this)
            BookmarkNodeType.FOLDER.ordinal -> BookmarkFolderViewHolder(view, bookmarkInteractor, this)
            else -> throw IllegalStateException("ViewType $viewType does not match to a ViewHolder")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return bookmarkList[position].type.ordinal
    }

    override fun onBindViewHolder(holder: BookmarkNodeViewHolder, position: Int) {
        holder.bind(bookmarkList[position])
    }

    override fun getItemCount() = bookmarkList.count()
}
