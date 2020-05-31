/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui.addfolder

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_edit_bookmark.*
import mozilla.components.support.ktx.android.view.hideKeyboard
import mozilla.components.support.ktx.android.view.showKeyboard
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ViewModelFactory
import org.mozilla.reference.browser.ext.application
import org.mozilla.reference.browser.ext.nav
import org.mozilla.reference.browser.ext.navController
import org.mozilla.reference.browser.library.LibraryToolbar
import org.mozilla.reference.browser.library.bookmarks.ui.BookmarksSharedViewModel
import org.mozilla.reference.browser.storage.HistoryDatabase.Companion.bookmarksRootFolder

class AddBookmarkFolderFragment : Fragment(R.layout.fragment_edit_bookmark) {

    private lateinit var addBookmarkFolderViewModel: AddBookmarkFolderViewModel

    private val sharedViewModel: BookmarksSharedViewModel by activityViewModels()

    @Suppress("ComplexMethod")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bookmark_url_label.visibility = View.GONE
        bookmark_url_edit.visibility = View.GONE
        bookmark_title_edit.showKeyboard()
        toolbar.register(object : LibraryToolbar.Observer {
            override fun confirm() {
                addFolder()
            }

            override fun close() {
                navController().popBackStack()
            }

            override fun add() {
                // no-op
            }

            override fun delete() {
                // no-op
            }

            override fun openAll(newTab: Boolean, private: Boolean) {
                // no-op
            }

            override fun searchOpened() {
                // no-op
            }

            override fun searchClosed() {
                // no-op
            }

            override fun searchQueryChanged(query: String) {
                // no-op
            }
        })

        context?.let {
            toolbar.updateToolbar(R.menu.bookmarks_add_folder, it.getString(R.string.bookmark_add_folder_title))
        }

        addBookmarkFolderViewModel = ViewModelProviders.of(this,
            ViewModelFactory.getInstance(view.context.application)).get(AddBookmarkFolderViewModel::class.java)

        addBookmarkFolderViewModel.tree.observe(viewLifecycleOwner, Observer { tree ->
            sharedViewModel.selectedFolder = tree
            bookmark_parent_folder_selector.text = sharedViewModel.selectedFolder!!.title
        })
    }

    override fun onResume() {
        super.onResume()

        if (sharedViewModel.selectedFolder != null) {
            bookmark_parent_folder_selector.text = if (sharedViewModel.selectedFolder!!.guid == bookmarksRootFolder) {
                getString(R.string.bookmarks_folder_root)
            } else {
                sharedViewModel.selectedFolder!!.title
            }
        } else {
            addBookmarkFolderViewModel.fetchBookmarks(bookmarksRootFolder)
        }
        bookmark_parent_folder_selector.setOnClickListener {
            nav(
                R.id.addBookmarkFolderFragment,
                AddBookmarkFolderFragmentDirections
                    .actionAddBookmarkFolderFragmentToSelectBookmarkFolderFragment()
            )
        }
    }

    override fun onPause() {
        super.onPause()
        bookmark_title_edit.hideKeyboard()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bookmarks_add_folder, menu)
    }

    private fun addFolder() {
        if (bookmark_title_edit.text.isNullOrBlank()) {
            bookmark_title_edit.error = getString(R.string.bookmark_empty_title_error)
            return
        }
        this.view?.hideKeyboard()
        val title = bookmark_title_edit.text.toString()
        addBookmarkFolderViewModel.addBookmarkFolder(sharedViewModel.selectedFolder!!.guid, title)
        navController().popBackStack()
    }
}
