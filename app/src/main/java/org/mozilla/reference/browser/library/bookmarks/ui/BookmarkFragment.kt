/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_bookmark.view.*
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.BrowserDirection
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.openToBrowserAndLoad

class BookmarkFragment @JvmOverloads constructor(
    private val initialBookmarkViewModel: BookmarkViewModel? = null
) : Fragment(), UserInteractionHandler {

    private lateinit var bookmarkViewModel: BookmarkViewModel

    private lateinit var bookmarkView: BookmarkView
    private lateinit var bookmarkInteractor: BookmarkViewInteractor

    override fun onAttach(context: Context) {
        super.onAttach(context)

        bookmarkViewModel = initialBookmarkViewModel ?: BookmarkViewModel(
            context.components.useCases.bookmarkUseCases
        )

        bookmarkInteractor = BookmarkViewInteractor(
            bookmarkViewModel,
            ::openBookmarkItem,
            ::onBackPressed
        )
    }

    override fun onResume() {
        super.onResume()
        bookmarkViewModel.bookmarkItemsLiveData.observe(this, Observer { bookmarkList ->
            bookmarkView.updateEmptyState(userHasBookmarks = bookmarkList.isNotEmpty())
            bookmarkView.update(bookmarkViewModel.viewMode, bookmarkList, bookmarkViewModel.selectedItems)
        })

        bookmarkViewModel.selectedItemsLiveData.observe(this, Observer { selectedItems ->
            if (selectedItems.isEmpty() && bookmarkViewModel.viewMode == ViewMode.Editing) {
                bookmarkViewModel.viewMode = ViewMode.Normal
            } else if (selectedItems.isNotEmpty() && bookmarkViewModel.viewMode == ViewMode.Normal) {
                bookmarkViewModel.viewMode = ViewMode.Editing
            }
            bookmarkView.update(bookmarkViewModel.viewMode, bookmarkViewModel.bookmarkList, selectedItems)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bookmark, container, false)
        bookmarkView = BookmarkView(view.bookmark_layout, bookmarkViewModel, bookmarkInteractor)
        return view
    }

    private fun openBookmarkItem(items: Set<BookmarkNode>, private: Boolean) {
        items.forEach { bookmarkItem ->
            context?.openToBrowserAndLoad(
                searchTermOrUrl = bookmarkItem.url!!,
                newTab = items.size != 1,
                from = BrowserDirection.FromBookmark,
                private = private
            )
        }
    }

    override fun onBackPressed(): Boolean {
        if (bookmarkView.onBackPressed()) {
            return true
        }
        findNavController().navigateUp()
        return true
    }
}
