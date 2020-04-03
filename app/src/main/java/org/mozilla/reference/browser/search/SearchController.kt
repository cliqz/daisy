/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.search

import android.content.Context
import androidx.navigation.NavController
import mozilla.components.browser.session.Session
import mozilla.components.concept.awesomebar.AwesomeBar
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.BrowserDirection
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.openToBrowserAndLoad

interface SearchController {
    fun handleUrlCommitted(url: String)
    fun handleEditingCancelled()
    fun handleTextChanged(text: String)
    fun handleEditingStarted()
    fun handleEditingStopped()
    fun handleUrlTapped(url: String)
    fun handleSearchTermsTapped(searchTerms: String)
    fun handleExistingSessionSelected(session: Session)
}

class DefaultSearchController(
    private val context: Context,
    private val awesomeBar: AwesomeBar,
    private val navController: NavController,
    private val session: Session?,
    private val showAwesomeBar: () -> Unit,
    private val hideAwesomeBar: () -> Unit
) : SearchController {

    private var inputStarted = false

    override fun handleUrlCommitted(url: String) {
        if (url.isNotBlank()) {
            context.openToBrowserAndLoad(
                searchTermOrUrl = url,
                newTab = session == null,
                from = BrowserDirection.FromSearch,
                private = session?.private ?: false,
                engine = context.components.search.searchEngineManager.defaultSearchEngine
            )
        }
    }

    override fun handleEditingCancelled() {
        navController.navigateUp()
    }

    override fun handleTextChanged(text: String) {
        if (inputStarted) {
            awesomeBar.onInputChanged(text)
        }
    }

    override fun handleEditingStarted() {
        showAwesomeBar.invoke()
        awesomeBar.onInputStarted()
        inputStarted = true
    }

    override fun handleEditingStopped() {
        hideAwesomeBar.invoke()
        awesomeBar.onInputCancelled()
        inputStarted = false
    }

    override fun handleUrlTapped(url: String) {
        context.openToBrowserAndLoad(
            searchTermOrUrl = url,
            newTab = false,
            private = false,
            from = BrowserDirection.FromSearch
        )
    }

    override fun handleSearchTermsTapped(searchTerms: String) {
        context.openToBrowserAndLoad(
            searchTermOrUrl = searchTerms,
            newTab = false,
            private = false,
            from = BrowserDirection.FromSearch
        )
    }

    override fun handleExistingSessionSelected(session: Session) {
        context.components.core.sessionManager.select(session)
        (context as BrowserActivity).openBrowser(from = BrowserDirection.FromSearch)
    }
}
