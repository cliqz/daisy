/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.component_bookmark.view.bookmarks_list
import kotlinx.android.synthetic.main.component_bookmark.view.bookmark_search_list
import kotlinx.android.synthetic.main.component_bookmark.view.empty_view
import kotlinx.android.synthetic.main.component_bookmark.view.toolbar
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.getQuantityString
import org.mozilla.reference.browser.library.LibraryPageView
import org.mozilla.reference.browser.library.LibraryToolbar

class BookmarkView(
    containerView: ViewGroup,
    private val bookmarkViewModel: BookmarkViewModel,
    private val interactor: BookmarkViewInteractor
) : LibraryPageView(containerView), UserInteractionHandler {

    val view: View = LayoutInflater.from(containerView.context)
            .inflate(R.layout.component_bookmark, containerView, true)

    private val bookmarkAdapter: BookmarkAdapter = BookmarkAdapter(interactor)

    private val bookmarkSearchAdapter: BookmarkSearchAdapter = BookmarkSearchAdapter(
        interactor,
        containerView.context.components.core.icons)
    private var searchItem: MenuItem? = null

    init {
        view.bookmarks_list.adapter = bookmarkAdapter
        view.bookmark_search_list.adapter = bookmarkSearchAdapter
        view.toolbar.register(object : LibraryToolbar.Observer {
            override fun close() {
                if (view.bookmark_search_list.visibility == View.VISIBLE) {
                    view.bookmark_search_list.visibility = View.GONE
                    searchItem?.collapseActionView()
                } else {
                    interactor.exitView()
                }
            }

            override fun delete() {
                interactor.onDeleteSome(bookmarkViewModel.selectedItems)
                bookmarkViewModel.viewMode = ViewMode.Normal
            }

            override fun openAll(private: Boolean) {
                interactor.open(bookmarkViewModel.selectedItems, private)
            }

            override fun searchOpened() {
                // no-op
            }

            override fun searchClosed() {
                view.bookmark_search_list.visibility = View.GONE
            }

            override fun searchQueryChanged(query: String) {
                if (query.isBlank()) return
                view.bookmark_search_list.visibility = View.VISIBLE
                bookmarkSearchAdapter.setData(bookmarkViewModel.searchBookmarks(query))
            }
        })
        update(ViewMode.Normal, emptyList(), emptySet())
    }

    fun update(newViewMode: ViewMode, bookmarkList: List<BookmarkNode>, newSelectedItems: Set<BookmarkNode>) {
        bookmarkAdapter.updateData(bookmarkList, newViewMode, newSelectedItems)

        if (newViewMode == ViewMode.Normal) {
            setUiForNormalMode(context.getString(R.string.bookmark_screen_title), view.bookmarks_list, view.toolbar)
        } else {
            setUiForEditingMode(
                context.getQuantityString(
                    R.plurals.bookmark_items_selected,
                    bookmarkAdapter.selectedItems.size,
                    bookmarkAdapter.selectedItems.size
                ),
                view.bookmarks_list,
                view.toolbar
            )
        }
    }

    override fun onBackPressed(): Boolean {
        return if (view.bookmark_search_list.isVisible) {
            view.bookmark_search_list.visibility = View.GONE
            searchItem?.collapseActionView()
            true
        } else {
            interactor.onBackPressed()
        }
    }

    fun updateEmptyState(userHasBookmarks: Boolean) {
        view.bookmarks_list.isVisible = userHasBookmarks
        view.empty_view.isVisible = !userHasBookmarks
    }
}
