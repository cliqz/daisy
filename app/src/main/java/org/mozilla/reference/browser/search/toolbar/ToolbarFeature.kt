/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.search.toolbar

import mozilla.components.browser.domains.autocomplete.ShippedDomainsProvider
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.concept.storage.HistoryStorage
import mozilla.components.feature.toolbar.ToolbarAutocompleteFeature
import mozilla.components.support.ktx.android.util.dpToPx
import org.mozilla.reference.browser.R

/**
 * Interface for the Toolbar Interactor. This interface is implemented by objects that want
 * to respond to user interaction on the [BrowserToolbar]
 */
interface ToolbarInteractor {

    /**
     * Called when a user hits the return key while [BrowserToolbar] has focus.
     * @param url the text inside the [BrowserToolbar] when committed
     */
    fun onUrlCommitted(url: String)

    /**
     * Called when a user removes focus from the [BrowserToolbar]
     */
    fun onEditingCanceled()

    /**
     * Called whenever the text inside the [BrowserToolbar] changes
     * @param text the current text displayed by [BrowserToolbar]
     */
    fun onTextChanged(text: String)

    /**
     * Called when a user focuses the [BrowserToolbar]
     */
    fun onEditingStarted()
}

/**
 * View that contains and configures the BrowserToolbar to only be used in its editing mode.
 */
class ToolbarFeature(
    private val browserToolbar: BrowserToolbar,
    private val interactor: ToolbarInteractor,
    private val historyStorage: HistoryStorage?,
    private val isPrivate: Boolean
) {

    init {
        browserToolbar.apply {

            elevation = TOOLBAR_ELEVATION_IN_DP.dpToPx(resources.displayMetrics).toFloat()

            setOnUrlCommitListener {
                interactor.onUrlCommitted(it)
                false
            }

            edit.hint = context.getString(R.string.toolbar_hint)

            private = isPrivate

            setOnEditListener(object : mozilla.components.concept.toolbar.Toolbar.OnEditListener {
                override fun onStartEditing() {
                    interactor.onEditingStarted()
                }
                override fun onCancelEditing(): Boolean {
                    interactor.onEditingCanceled()
                    // We need to return false to not show display mode
                    return false
                }
                override fun onTextChanged(text: String) {
                    url = text
                    this@ToolbarFeature.interactor.onTextChanged(text)
                }
            })

            editMode()
        }

        ToolbarAutocompleteFeature(browserToolbar).apply {
            addDomainProvider(ShippedDomainsProvider().also {
                it.initialize(browserToolbar.context)
            })
            historyStorage?.also(::addHistoryStorageProvider)
        }
    }

    fun updateText(query: String) {
        browserToolbar.apply {
            url = query
            editMode()
        }
    }

    companion object {
        private const val TOOLBAR_ELEVATION_IN_DP = 16
    }
}
