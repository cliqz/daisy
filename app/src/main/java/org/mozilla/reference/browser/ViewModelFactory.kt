/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.mozilla.reference.browser.library.history.ui.HistoryViewModel

/**
 * Custom ViewModel factory that takes care of instantiating view models with non-default constructors
 */
class ViewModelFactory(private val applicationContext: BrowserApplication) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass == HistoryViewModel::class.java) {
            return HistoryViewModel(
                applicationContext.components.useCases.historyUseCases,
                applicationContext.components.useCases.sessionUseCases) as T
        }
        throw IllegalArgumentException("Unknown model class $modelClass")
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
