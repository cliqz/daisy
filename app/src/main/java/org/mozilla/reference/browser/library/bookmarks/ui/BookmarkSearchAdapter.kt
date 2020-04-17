/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.two_line_list_item_layout.view.*
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.concept.storage.BookmarkNode
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.inflate
import org.mozilla.reference.browser.ext.loadIntoView

class BookmarkSearchAdapter(
    private val browserIcons: BrowserIcons
) : RecyclerView.Adapter<BookmarkSearchAdapter.ViewHolder>() {

    private var searchResult: List<BookmarkNode> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.two_line_list_item_layout))

    override fun getItemCount() = searchResult.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(searchResult[position])
    }

    fun setData(data: List<BookmarkNode>) {
        searchResult = data
        notifyDataSetChanged()
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(bookmarkNode: BookmarkNode) {
            containerView.title_view.text = if (!bookmarkNode.title.isNullOrBlank())
                bookmarkNode.title else containerView.resources.getString(R.string.history_title_untitled)
            containerView.url_view.text = bookmarkNode.url
            browserIcons.loadIntoView(containerView.favicon, bookmarkNode.url!!)
        }
    }
}
