/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.mozilla.reference.browser.library.bookmarks.ui.BookmarkViewModel
import org.mozilla.reference.browser.library.bookmarks.ui.addfolder.AddBookmarkFolderViewModel
import org.mozilla.reference.browser.library.bookmarks.ui.edit.EditBookmarkViewModel
import org.mozilla.reference.browser.library.bookmarks.ui.selectfolder.SelectBookmarkViewModel
import org.mozilla.reference.browser.library.history.ui.HistoryViewModel
import java.lang.IllegalArgumentException

/**
 * Custom ViewModel factory that takes care of instantiating view models with non-default constructors
 */
class ViewModelFactory(private val applicationContext: BrowserApplication) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST", "ComplexMethod")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val useCases = applicationContext.components.useCases
        return when (modelClass) {
            HistoryViewModel::class.java -> with(useCases) {
                HistoryViewModel(historyUseCases) as T
            }
            BookmarkViewModel::class.java -> with(useCases) {
                BookmarkViewModel(bookmarkUseCases) as T
            }
            AddBookmarkFolderViewModel::class.java -> with(useCases) {
                AddBookmarkFolderViewModel(bookmarkUseCases) as T
            }
            SelectBookmarkViewModel::class.java -> with(useCases) {
                SelectBookmarkViewModel(bookmarkUseCases) as T
            }
            EditBookmarkViewModel::class.java -> with(useCases) {
                EditBookmarkViewModel(bookmarkUseCases) as T
            }
            else -> throw IllegalArgumentException("Unknown model class $modelClass")
        }
    }

    companion object {
        private var INSTANCE: ViewModelFactory? = null
        fun getInstance(context: BrowserApplication): ViewModelFactory {
            if (INSTANCE == null) {
                INSTANCE = ViewModelFactory(context)
            }
            return INSTANCE!!
        }
    }
}
