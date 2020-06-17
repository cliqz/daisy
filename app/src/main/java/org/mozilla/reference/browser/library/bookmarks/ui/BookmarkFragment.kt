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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_bookmark.view.*
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.BrowserDirection
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ViewModelFactory
import org.mozilla.reference.browser.ext.application
import org.mozilla.reference.browser.ext.nav
import org.mozilla.reference.browser.ext.openToBrowserAndLoad
import org.mozilla.reference.browser.storage.HistoryDatabase.Companion.bookmarksRootFolder

class BookmarkFragment @JvmOverloads constructor(
    private val initialBookmarkViewModel: BookmarkViewModel? = null
) : Fragment(), UserInteractionHandler {

    private lateinit var bookmarkViewModel: BookmarkViewModel

    private lateinit var bookmarkView: BookmarkView
    private lateinit var bookmarkInteractor: BookmarkViewInteractor

    private val sharedViewModel: BookmarksSharedViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        bookmarkViewModel = initialBookmarkViewModel ?: ViewModelProviders.of(this,
            ViewModelFactory.getInstance(context.application)).get(BookmarkViewModel::class.java)

        bookmarkInteractor = BookmarkViewInteractor(
            bookmarkViewModel,
            ::openBookmarkItem,
            ::expandBookmarkFolder,
            ::onBackPressed,
            ::navigateToAddFolder,
            ::navigateToEditBookmark
        )
    }

    override fun onResume() {
        super.onResume()
        bookmarkViewModel.tree.observe(this, Observer { tree ->
            bookmarkView.updateEmptyState(userHasBookmarks = tree != null && !tree.children.isNullOrEmpty())
            bookmarkView.update(
                newViewMode = bookmarkViewModel.viewMode,
                newBookmarkNode = tree,
                newSelectedItems = bookmarkViewModel.selectedItems.value!!)
            sharedViewModel.selectedFolder = tree
        })

        bookmarkViewModel.selectedItems.observe(this, Observer { selectedItems ->
            if (selectedItems.isEmpty() && bookmarkViewModel.viewMode == ViewMode.Editing) {
                bookmarkViewModel.viewMode = ViewMode.Normal
            } else if (selectedItems.isNotEmpty() && bookmarkViewModel.viewMode == ViewMode.Normal) {
                bookmarkViewModel.viewMode = ViewMode.Editing
            }
            bookmarkView.update(
                newViewMode = bookmarkViewModel.viewMode,
                newSelectedItems = selectedItems,
                newBookmarkNode = bookmarkViewModel.tree.value)
        })

        val currentGuid = BookmarkFragmentArgs.fromBundle(requireArguments()).currentRoot.ifEmpty {
            bookmarksRootFolder
        }
        loadRootFolder(currentGuid)
    }

    private fun loadRootFolder(currentGuid: String) {
        bookmarkViewModel.fetchBookmarks(currentGuid)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bookmark, container, false)
        bookmarkView = BookmarkView(view.bookmark_layout, this, bookmarkViewModel, bookmarkInteractor)
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

    private fun expandBookmarkFolder(folder: BookmarkNode) {
        findNavController().nav(R.id.bookmarkFragment,
            BookmarkFragmentDirections.actionBookmarkFragmentSelf(folder.guid))
    }

    private fun navigateToAddFolder() {
        findNavController().nav(R.id.bookmarkFragment,
            BookmarkFragmentDirections.actionBookmarkFragmentToAddBookmarkFolderFragment())
    }

    fun navigateToEditBookmark(bookmarkNode: BookmarkNode) {
        findNavController().nav(R.id.bookmarkFragment,
            BookmarkFragmentDirections.actionBookmarkFragmentToEditBookmarkFragment(bookmarkNode.guid))
    }

    override fun onBackPressed(): Boolean {
        if (bookmarkView.onBackPressed()) {
            return true
        }
        findNavController().navigateUp()
        return true
    }
}
