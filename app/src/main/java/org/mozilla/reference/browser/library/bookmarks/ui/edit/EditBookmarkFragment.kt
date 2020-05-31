/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui.edit

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_edit_bookmark.*
import mozilla.components.concept.storage.BookmarkNodeType
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ViewModelFactory
import org.mozilla.reference.browser.ext.application
import org.mozilla.reference.browser.ext.nav
import org.mozilla.reference.browser.ext.navController
import org.mozilla.reference.browser.library.LibraryToolbar
import org.mozilla.reference.browser.library.bookmarks.ui.BookmarksSharedViewModel
import org.mozilla.reference.browser.storage.HistoryDatabase.Companion.bookmarksRootFolder

class EditBookmarkFragment : Fragment(R.layout.fragment_edit_bookmark) {

    private val args by navArgs<EditBookmarkFragmentArgs>()

    private lateinit var editBookmarkViewModel: EditBookmarkViewModel

    private val sharedViewModel: BookmarksSharedViewModel by activityViewModels()

    @Suppress("ComplexMethod")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.register(object : LibraryToolbar.Observer {
            override fun confirm() {
                updateBookmark()
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

        editBookmarkViewModel = ViewModelProviders.of(this,
            ViewModelFactory.getInstance(view.context.application)).get(EditBookmarkViewModel::class.java)

        editBookmarkViewModel.tree.observe(viewLifecycleOwner, Observer { tree ->
            when (tree?.type) {
                BookmarkNodeType.FOLDER -> {
                    context?.let {
                        toolbar.updateToolbar(R.menu.bookmarks_add_folder,
                            it.getString(R.string.bookmark_edit_folder_title))
                    }
                    bookmark_url_label.visibility = View.GONE
                    bookmark_url_edit.visibility = View.GONE
                }
                BookmarkNodeType.ITEM -> {
                    context?.let {
                        toolbar.updateToolbar(R.menu.bookmarks_add_folder,
                            it.getString(R.string.bookmark_edit_bookmark_title))
                    }
                }
                else -> throw IllegalArgumentException()
            }
            tree.let {
                bookmark_title_edit.setText(it.title)
                bookmark_url_edit.setText(it.url)
            }

            if (sharedViewModel.selectedFolder != null) {
                bookmark_parent_folder_selector.text =
                    if (sharedViewModel.selectedFolder!!.guid == bookmarksRootFolder) {
                    getString(R.string.bookmarks_folder_root)
                } else {
                    sharedViewModel.selectedFolder!!.title
                }
            } else {
                editBookmarkViewModel.fetchParentBookmark(bookmarksRootFolder)
            }

            bookmark_parent_folder_selector.setOnClickListener {
                nav(
                    R.id.editBookmarkFragment,
                    EditBookmarkFragmentDirections
                        .actionEditBookmarkFragmentToSelectBookmarkFolderFragment()
                )
            }
        })

        editBookmarkViewModel.parentTree.observe(viewLifecycleOwner, Observer { tree ->
            sharedViewModel.selectedFolder = tree
            bookmark_parent_folder_selector.text = sharedViewModel.selectedFolder!!.title
        })

        editBookmarkViewModel.fetchBookmarks(args.guidToEdit)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bookmarks_add_folder, menu)
    }

    private fun updateBookmark() {
        val title = bookmark_title_edit.text.toString()
        val url = bookmark_url_edit.text.toString()
        val parentGuid = sharedViewModel.selectedFolder?.guid
            ?: editBookmarkViewModel.tree.value!!.parentGuid
        editBookmarkViewModel.editBookmark(
            args.guidToEdit,
            parentGuid,
            title,
            url
        )
        navController().popBackStack()
    }
}
