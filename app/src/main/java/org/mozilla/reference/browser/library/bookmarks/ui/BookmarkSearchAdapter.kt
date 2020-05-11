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
import mozilla.components.concept.storage.BookmarkNodeType
import mozilla.components.concept.storage.SearchResult
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.inflate
import org.mozilla.reference.browser.ext.loadIntoView

class BookmarkSearchAdapter(
    private val interactor: BookmarkViewInteractor,
    private val browserIcons: BrowserIcons
) : RecyclerView.Adapter<BookmarkSearchAdapter.ViewHolder>() {

    private var searchResult: List<SearchResult> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.two_line_list_item_layout))

    override fun getItemCount() = searchResult.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(searchResult[position])
    }

    fun setData(data: List<SearchResult>) {
        searchResult = data
        notifyDataSetChanged()
    }

    inner class ViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(searchResult: SearchResult) {
            with(containerView) {
                val title = if (!searchResult.title.isNullOrBlank()) {
                    searchResult.title!!
                } else {
                    resources.getString(R.string.history_title_untitled)
                }
                title_view.text = title
                url_view.text = searchResult.url
                browserIcons.loadIntoView(favicon, searchResult.url)
                setOnClickListener {
                    interactor.open(setOf(
                        BookmarkNode(
                            type = BookmarkNodeType.ITEM,
                            guid = "",
                            parentGuid = null,
                            url = title,
                            title = searchResult.url,
                            position = null,
                            children = null
                        )
                    ))
                }
            }
        }
    }
}
