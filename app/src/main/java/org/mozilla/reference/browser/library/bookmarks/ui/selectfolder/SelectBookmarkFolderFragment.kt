/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui.selectfolder

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_select_bookmark_folder.*
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ViewModelFactory
import org.mozilla.reference.browser.ext.application
import org.mozilla.reference.browser.ext.navController
import org.mozilla.reference.browser.library.LibraryToolbar
import org.mozilla.reference.browser.library.bookmarks.ui.BookmarksSharedViewModel
import org.mozilla.reference.browser.storage.HistoryDatabase.Companion.bookmarksRootFolder

class SelectBookmarkFolderFragment : Fragment(R.layout.fragment_select_bookmark_folder) {

    private lateinit var selectBookmarkFolderViewModel: SelectBookmarkViewModel
    private lateinit var adapter: SelectBookmarkFolderAdapter

    private val sharedViewModel: BookmarksSharedViewModel by activityViewModels()

    @Suppress("ComplexMethod")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.register(object : LibraryToolbar.Observer {
            override fun confirm() {
                exitFragment()
            }

            override fun close() {
                exitFragment()
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
            toolbar.updateToolbar(R.menu.bookmarks_add_folder, it.getString(R.string.bookmark_select_folder_title))
        }

        selectBookmarkFolderViewModel = ViewModelProviders.of(this,
            ViewModelFactory.getInstance(view.context.application)).get(SelectBookmarkViewModel::class.java)
        selectBookmarkFolderViewModel.tree.observe(viewLifecycleOwner, Observer { tree ->
            adapter.updateData(tree)
        })

        adapter = SelectBookmarkFolderAdapter(sharedViewModel)
        bookmark_folders.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        selectBookmarkFolderViewModel.fetchBookmarks(bookmarksRootFolder)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bookmarks_add_folder, menu)
    }

    private fun exitFragment() {
        if (sharedViewModel.selectedFolder == null) {
            sharedViewModel.selectedFolder = selectBookmarkFolderViewModel.tree.value
        }
        navController().popBackStack()
    }
}
