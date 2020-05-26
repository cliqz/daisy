/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import android.util.AttributeSet
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.addons.AddonManager
import mozilla.components.feature.addons.update.DefaultAddonUpdater
import mozilla.components.service.fxa.manager.FxaAccountManager
import mozilla.components.support.rustlog.RustLog
import mozilla.components.support.test.any
import mozilla.components.support.test.mock
import mozilla.components.support.test.whenever
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mozilla.reference.browser.components.BackgroundServices
import org.mozilla.reference.browser.components.Core
import org.mozilla.reference.browser.components.News
import org.mozilla.reference.browser.components.Search
import org.mozilla.reference.browser.components.Services
import org.mozilla.reference.browser.components.TestEngineView
import org.mozilla.reference.browser.components.UseCases
import org.mozilla.reference.browser.components.Utilities
import org.mozilla.reference.browser.storage.HistoryDatabase
import org.mozilla.reference.browser.ext.whenever
import org.mozilla.reference.browser.utils.ClipboardHandler

class TestBrowserApplication : BrowserApplication() {

    // Mocking Core
    private val engine = mock<Engine>().also {
        val settings = DefaultSettings()
        whenever(it.createView(any(), any<AttributeSet>()))
                .then { invocation ->
                    val context = invocation.arguments[0] as Context
                    val attrs = invocation.arguments[1] as AttributeSet
                    return@then TestEngineView(context, attrs)
                }
        whenever(it.settings).thenReturn(settings)
    }

    private val core by lazy {
        mock<Core>().also {
            val client = mock<Client>()
            doReturnMock<AddonManager>().`when`(it).addonManager
            doReturnMock<DefaultAddonUpdater>().`when`(it).addonUpdater
            doReturn(engine).whenever(it).engine
            doReturn(SessionManager(engine)).`when`(it).sessionManager
            doReturn(client).whenever(it).client
            doReturn(BrowserStore()).whenever(it).store
            doReturnMock<BrowserIcons>().whenever(it).icons
            val historyDatabase = HistoryDatabase(this)
            doReturn(historyDatabase).whenever(it).historyStorage
            doReturn(historyDatabase).whenever(it).bookmarksStorage
        }
    }

    private val backgroundServices = mock<BackgroundServices>().also {
        doReturnMock<FxaAccountManager>().whenever(it).accountManager
    }

    // Finally, override the components
    private val mockedComponents by lazy {
        mock<Components>().also {
            val realSearch = spy(Search(this))
            val realNews = spy(News(core.client, this))
            val realUseCases = spy(UseCases(
                    this,
                    core.sessionManager,
                    core.store,
                    core.engine.settings,
                    realSearch.searchEngineManager,
                    core.client,
                    core.historyStorage,
                    core.bookmarksStorage,
                    realNews.newsRepository
            ))
            val realUtils = spy(Utilities(
                    this,
                    core.sessionManager,
                    realUseCases.sessionUseCases,
                    realUseCases.searchUseCases,
                    realUseCases.tabsUseCases
            ))
            val realServices = spy(Services(
                    this,
                    backgroundServices.accountManager,
                    realUseCases.tabsUseCases
            ))
            val realClipboardHandler = spy(ClipboardHandler(this))
            whenever(it.core).thenReturn(core)
            whenever(it.search).thenReturn(realSearch)
            whenever(it.news).thenReturn(realNews)
            whenever(it.useCases).thenReturn(realUseCases)
            whenever(it.backgroundServices).thenReturn(backgroundServices)
            whenever(it.utils).thenReturn(realUtils)
            whenever(it.services).thenReturn(realServices)
            whenever(it.clipboardHandler).thenReturn(realClipboardHandler)
        }
    }

    override val components: Components
        get() = mockedComponents

    override fun onCreate() {
        // RustLog has a static instance that crash if instantiated multiple times, we disable it
        // before being enabled again in BrowserApplication::onCreate()
        RustLog.disable()
        super.onCreate()
    }
}

private inline fun <reified T : Any> doReturnMock() = doReturn(Mockito.mock(T::class.java))