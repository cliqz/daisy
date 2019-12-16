/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.content.Context
import com.cliqz.browser.news.domain.GetNewsUseCase
import com.cliqz.browser.news.data.source.NewsRepository
import mozilla.components.browser.search.SearchEngineManager
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Settings
import mozilla.components.concept.fetch.Client
import mozilla.components.concept.storage.HistoryStorage
import mozilla.components.feature.contextmenu.ContextMenuUseCases
import mozilla.components.feature.pwa.WebAppUseCases
import mozilla.components.feature.search.SearchUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.session.SettingsUseCases
import mozilla.components.feature.tabs.TabsUseCases
import org.mozilla.reference.browser.library.history.usecases.HistoryUseCases

/**
 * Component group for all use cases. Use cases are provided by feature
 * modules and can be triggered by UI interactions.
 */
class UseCases(
    private val context: Context,
    private val sessionManager: SessionManager,
    private val store: BrowserStore,
    private val engineSettings: Settings,
    private val searchEngineManager: SearchEngineManager,
    private val client: Client,
    private val historyStorage: HistoryStorage,
    private val newsRepository: NewsRepository
) {
    /**
     * Use cases that provide engine interactions for a given browser session.
     */
    val sessionUseCases by lazy { SessionUseCases(sessionManager) }

    /**
     * Use cases that provide tab management.
     */
    val tabsUseCases: TabsUseCases by lazy { TabsUseCases(sessionManager) }

    /**
     * Use cases that provide search engine integration.
     */
    val searchUseCases by lazy { SearchUseCases(context, searchEngineManager, sessionManager) }

    /**
     * Use cases that provide settings management.
     */
    val settingsUseCases by lazy { SettingsUseCases(engineSettings, sessionManager) }

    /**
     * Use cases that provide shortcut and progressive web app management.
     */
    val webAppUseCases by lazy { WebAppUseCases(context, sessionManager, client) }

    /**
     * Uses cases that provides context menu
     */
    val contextMenuUseCases: ContextMenuUseCases by lazy { ContextMenuUseCases(sessionManager, store) }

    /**
     * Uses cases that provides history integration
     */
    val historyUseCases by lazy { HistoryUseCases(historyStorage) }

    val getNewsUseCase: GetNewsUseCase by lazy { GetNewsUseCase(newsRepository) }
}
