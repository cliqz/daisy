/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.search.awesomebar

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.browser.awesomebar.BrowserAwesomeBar
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.search.SearchEngine
import mozilla.components.browser.search.SearchEngineManager
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.concept.awesomebar.AwesomeBar
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.fetch.Client
import mozilla.components.concept.storage.HistoryStorage
import mozilla.components.feature.awesomebar.provider.ClipboardSuggestionProvider
import mozilla.components.feature.awesomebar.provider.HistoryStorageSuggestionProvider
import mozilla.components.feature.awesomebar.provider.SearchSuggestionProvider
import mozilla.components.feature.awesomebar.provider.SessionSuggestionProvider
import mozilla.components.feature.search.SearchUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.ktx.android.view.hideKeyboard

/**
 * Interface for the AwesomeBarView Interactor. This interface is implemented by objects that want
 * to respond to user interaction on the AwesomeBarView
 */
interface AwesomeBarInteractor {

    /**
     * Called whenever a suggestion containing a URL is tapped
     * @param url the url the suggestion was providing
     */
    fun onUrlTapped(url: String)

    /**
     * Called whenever a search engine suggestion is tapped
     * @param searchTerms the query contained by the search suggestion
     */
    fun onSearchTermsTapped(searchTerms: String)

    /**
     * Called whenever an existing session is selected from the sessionSuggestionProvider
     */
    fun onExistingSessionSelected(session: Session)
}

/**
 * Configures the BrowserAwesomeBar
 */
class AwesomeBarFeature(
    private val awesomeBar: BrowserAwesomeBar,
    val interactor: AwesomeBarInteractor
) {

    internal var isKeyboardDismissedProgrammatically: Boolean = false

    private val loadUrlUseCase = object : SessionUseCases.LoadUrlUseCase {
        override fun invoke(url: String, flags: EngineSession.LoadUrlFlags) {
            interactor.onUrlTapped(url)
        }
    }

    private val searchUseCase = object : SearchUseCases.SearchUseCase {
        override fun invoke(searchTerms: String, searchEngine: SearchEngine?) {
            interactor.onSearchTermsTapped(searchTerms)
        }
    }

    private val selectTabUseCase = object : TabsUseCases.SelectTabUseCase {
        override fun invoke(session: Session) {
            interactor.onExistingSessionSelected(session)
        }
    }

    init {
        val recyclerListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING ->
                        if (!isKeyboardDismissedProgrammatically) {
                            awesomeBar.hideKeyboard()
                            isKeyboardDismissedProgrammatically = true
                        }
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        isKeyboardDismissedProgrammatically = false
                        awesomeBar.requestFocus()
                    }
                }
            }
        }
        awesomeBar.addOnScrollListener(recyclerListener)
    }

    /**
     * Add a [AwesomeBar.SuggestionProvider] for "Open tabs" to the [AwesomeBar].
     */
    fun addSessionProvider(
        sessionManager: SessionManager,
        icons: BrowserIcons? = null
    ): AwesomeBarFeature {
        awesomeBar.addProviders(
            SessionSuggestionProvider(
                sessionManager,
                selectTabUseCase,
                icons,
                excludeSelectedSession = true
            )
        )
        return this
    }

    /**
     * Adds a [AwesomeBar.SuggestionProvider] for search engine suggestions to the [AwesomeBar].
     */
    fun addSearchProvider(
        searchEngine: SearchEngine,
        fetchClient: Client,
        limit: Int = 15,
        mode: SearchSuggestionProvider.Mode = SearchSuggestionProvider.Mode.SINGLE_SUGGESTION
    ): AwesomeBarFeature {
        awesomeBar.addProviders(
            SearchSuggestionProvider(
                searchEngine,
                searchUseCase,
                fetchClient,
                limit,
                mode
            )
        )
        return this
    }

    /**
     * Adds a [AwesomeBar.SuggestionProvider] for search engine suggestions to the [AwesomeBar].
     * If the default search engine is to be used for fetching search engine suggestions then
     * this method is preferable over [addSearchProvider], as it will lazily load the default
     * search engine using the provided [SearchEngineManager].
     */
    @Suppress("LongParameterList")
    fun addSearchProvider(
        context: Context,
        searchEngineManager: SearchEngineManager,
        fetchClient: Client,
        limit: Int = 15,
        mode: SearchSuggestionProvider.Mode = SearchSuggestionProvider.Mode.SINGLE_SUGGESTION
    ): AwesomeBarFeature {
        awesomeBar.addProviders(
            SearchSuggestionProvider(
                context,
                searchEngineManager,
                searchUseCase,
                fetchClient,
                limit,
                mode
            )
        )
        return this
    }

    /**
     * Add a [AwesomeBar.SuggestionProvider] for browsing history to the [AwesomeBar].
     */
    fun addHistoryProvider(
        historyStorage: HistoryStorage,
        icons: BrowserIcons? = null
    ): AwesomeBarFeature {
        awesomeBar.addProviders(
            HistoryStorageSuggestionProvider(
                historyStorage,
                loadUrlUseCase,
                icons
            )
        )
        return this
    }

    fun addClipboardProvider(
        context: Context
    ): AwesomeBarFeature {
        awesomeBar.addProviders(
            ClipboardSuggestionProvider(context, loadUrlUseCase)
        )
        return this
    }
}
