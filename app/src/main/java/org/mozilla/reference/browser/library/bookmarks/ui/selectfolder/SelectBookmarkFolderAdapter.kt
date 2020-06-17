/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui.selectfolder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.two_line_list_item_with_action_layout.view.*
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType
import mozilla.components.support.ktx.android.util.dpToPx
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.library.LibraryItemView
import org.mozilla.reference.browser.library.bookmarks.ui.BookmarksSharedViewModel
import org.mozilla.reference.browser.storage.HistoryDatabase.Companion.bookmarksRootFolder

class SelectBookmarkFolderAdapter(
    private val sharedViewModel: BookmarksSharedViewModel
) : ListAdapter<SelectBookmarkFolderAdapter.BookmarkNodeWithDepth,
    SelectBookmarkFolderAdapter.BookmarkFolderViewHolder>(DiffCallback) {

    fun updateData(tree: BookmarkNode?) {
        val updatedData = tree
            ?.convertToFolderDepthTree()
            .orEmpty()
        submitList(updatedData)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookmarkFolderViewHolder {
        val view = LibraryItemView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        return BookmarkFolderViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: BookmarkFolderViewHolder,
        position: Int
    ) {
        val item = getItem(position)

        holder.bind(item, selected = (item.node == sharedViewModel.selectedFolder)) { node ->
            val lastSelectedItemPosition = getSelectedItemIndex()

            sharedViewModel.toggleSelection(node)

            notifyItemChanged(position)
            lastSelectedItemPosition
                ?.takeIf { it != position }
                ?.let { notifyItemChanged(it) }
        }
    }

    class BookmarkFolderViewHolder(
        val view: LibraryItemView
    ) : RecyclerView.ViewHolder(view), LayoutContainer {

        override val containerView get() = view

        init {
            view.toggleActionButton(showActionButton = false)
            view.urlView.visibility = View.GONE
        }

        fun bind(folder: BookmarkNodeWithDepth, selected: Boolean, onSelect: (BookmarkNode) -> Unit) {
            view.changeSelected(selected)
            view.favicon.setImageDrawable(
                AppCompatResources.getDrawable(
                    containerView.context,
                    R.drawable.ic_folder_icon
                )
            )
            view.titleView.text = if (folder.node.guid == bookmarksRootFolder) {
                view.context.getString(R.string.bookmarks_folder_root)
            } else {
                folder.node.title
            }
            view.setOnClickListener {
                onSelect(folder.node)
            }
            val pxToIndent = dpsToIndent.dpToPx(view.context.resources.displayMetrics)
            val padding = pxToIndent * if (folder.depth > maxDepth) maxDepth else folder.depth
            view.setPadding(padding, 0, 0, 0)
        }
    }

    data class BookmarkNodeWithDepth(val depth: Int, val node: BookmarkNode, val parent: String?)

    private fun BookmarkNode.convertToFolderDepthTree(depth: Int = 0): List<BookmarkNodeWithDepth> {
        val newList = listOf(BookmarkNodeWithDepth(depth, this, this.parentGuid))
        return newList + children
            ?.filter { it.type == BookmarkNodeType.FOLDER }
            ?.flatMap { it.convertToFolderDepthTree(depth = depth + 1) }
            .orEmpty()
    }

    private fun getSelectedItemIndex(): Int? {
        val selectedNode = sharedViewModel.selectedFolder
        val selectedNodeIndex = currentList.indexOfFirst { it.node == selectedNode }

        return selectedNodeIndex.takeIf { it != -1 }
    }

    companion object {
        private const val maxDepth = 10
        private const val dpsToIndent = 10
    }
}

private object DiffCallback :
    DiffUtil.ItemCallback<SelectBookmarkFolderAdapter.BookmarkNodeWithDepth>() {

    override fun areItemsTheSame(
        oldItem: SelectBookmarkFolderAdapter.BookmarkNodeWithDepth,
        newItem: SelectBookmarkFolderAdapter.BookmarkNodeWithDepth
    ) = oldItem.node.guid == newItem.node.guid

    override fun areContentsTheSame(
        oldItem: SelectBookmarkFolderAdapter.BookmarkNodeWithDepth,
        newItem: SelectBookmarkFolderAdapter.BookmarkNodeWithDepth
    ) = oldItem == newItem
}

private fun BookmarksSharedViewModel.toggleSelection(node: BookmarkNode?) {
    selectedFolder = if (selectedFolder == node) null else node
}
