/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import mozilla.components.browser.search.SearchEngine
import mozilla.components.browser.session.Session
import mozilla.components.browser.state.state.WebExtensionState
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.tabstray.TabsTray
import mozilla.components.feature.intent.ext.EXTRA_SESSION_ID
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.ktx.kotlin.isUrl
import mozilla.components.support.ktx.kotlin.toNormalizedUrl
import mozilla.components.support.utils.SafeIntent
import mozilla.components.support.webextensions.WebExtensionPopupFeature
import org.mozilla.reference.browser.R.color.navigationBarColor
import org.mozilla.reference.browser.R.color.statusBarColor
import org.mozilla.reference.browser.addons.WebExtensionActionPopupActivity
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.ext.alreadyOnDestination
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.isFreshTab
import org.mozilla.reference.browser.ext.nav
import org.mozilla.reference.browser.ext.setSystemBarsTheme
import org.mozilla.reference.browser.freshtab.FreshTabFragmentDirections
import org.mozilla.reference.browser.library.history.ui.HistoryFragmentDirections
import org.mozilla.reference.browser.search.SearchFragmentDirections
import org.mozilla.reference.browser.tabs.TabsTouchHelper
import org.mozilla.reference.browser.tabstray.BrowserTabsTray

/**
 * Activity that holds the [BrowserFragment].
 */
@Suppress("TooManyFunctions")
open class BrowserActivity : AppCompatActivity() {

    private val sessionId: String?
        get() = SafeIntent(intent).getStringExtra(EXTRA_SESSION_ID)

    private val webExtensionPopupFeature by lazy {
        WebExtensionPopupFeature(components.core.store, ::openPopup)
    }

    private val navHost by lazy {
        supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
    }

    /**
     * Returns a new instance of [BrowserFragment] to display.
     */
    open fun createBrowserFragment(sessionId: String?, openToSearch: Boolean): Fragment =
        BrowserFragment.create(sessionId, openToSearch)

    @Suppress("ComplexMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSystemBarsTheme(statusBarColor, navigationBarColor)

        if (savedInstanceState == null) {
            val openToSearch = components.utils.startSearchIntentProcessor.process(intent)
            val selectedSession = components.core.sessionManager.selectedSession
            val direction = when {
                openToSearch -> {
                    NavGraphDirections.actionWidgetSearch(sessionId = null)
                }
                sessionId != null -> {
                    FreshTabFragmentDirections
                        .actionFreshTabFragmentToBrowserFragmentNewStartDestination(sessionId)
                }
                selectedSession != null && !selectedSession.isFreshTab() -> {
                    FreshTabFragmentDirections
                        .actionFreshTabFragmentToBrowserFragmentNewStartDestination(sessionId = null)
                }
                else -> null
            }
            direction?.let {
                navHost.navController.navigate(it)
            } ?: run {
                // On new install, there is no selected session.
                if (selectedSession == null) {
                    components.useCases.tabsUseCases.addTab.invoke("")
                }
            }
        }

        NotificationManager.checkAndNotifyPolicy(this)
        lifecycle.addObserver(webExtensionPopupFeature)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent ?: return

        if (components.utils.startSearchIntentProcessor.process(intent)) {
            val direction = NavGraphDirections.actionWidgetSearch(sessionId = null)
            navHost.navController.navigate(direction)
        } else if (components.utils.tabIntentProcessor.matches(intent)) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.container, createBrowserFragment(null, openToSearch = false))
                commit()
            }
        }
    }

    override fun onBackPressed() {
        supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.forEach {
            if (it is UserInteractionHandler && it.onBackPressed()) {
                return
            }
        }

        super.onBackPressed()

        removeSessionIfNeeded()
    }

    /**
     * If needed remove the current session.
     *
     * If a session is a custom tab or was opened from an external app then the session gets removed once you go back
     * to the third-party app.
     *
     * Eventually we may want to move this functionality into one of our feature components.
     */
    private fun removeSessionIfNeeded() {
        val sessionManager = components.core.sessionManager
        val sessionId = sessionId

        val session = (if (sessionId != null) {
            sessionManager.findSessionById(sessionId)
        } else {
            sessionManager.selectedSession
        }) ?: return

        if (session.source == Session.Source.ACTION_VIEW || session.source == Session.Source.CUSTOM_TAB) {
            sessionManager.remove(session)
        }
    }

    override fun onUserLeaveHint() {
        supportFragmentManager.fragments.forEach {
            if (it is UserInteractionHandler && it.onHomePressed()) {
                return
            }
        }

        super.onUserLeaveHint()
    }

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? =
        when (name) {
            EngineView::class.java.name -> components.core.engine.createView(context, attrs).asView()
            TabsTray::class.java.name -> {
                BrowserTabsTray(context, attrs).also { tray ->
                    TabsTouchHelper(tray.tabsAdapter).attachToRecyclerView(tray)
                }
            }
            else -> super.onCreateView(parent, name, context, attrs)
        }

    private fun openPopup(webExtensionState: WebExtensionState) {
        val intent = Intent(this, WebExtensionActionPopupActivity::class.java)
        intent.putExtra("web_extension_id", webExtensionState.id)
        intent.putExtra("web_extension_name", webExtensionState.name)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    @Suppress("LongParameterList")
    fun openToBrowserAndLoad(
        searchTermOrUrl: String,
        newTab: Boolean,
        from: BrowserDirection,
        private: Boolean,
        sessionId: String? = null,
        engine: SearchEngine? = null
    ) {
        openBrowser(from, sessionId)
        val fromFreshTab = from == BrowserDirection.FromFreshTab
        load(searchTermOrUrl, newTab, private, fromFreshTab, engine)
    }

    fun openBrowser(from: BrowserDirection, sessionId: String? = null) {
        if (navHost.navController.alreadyOnDestination(R.id.browserFragment)) {
            return
        }
        @IdRes val fragmentId = if (from.fragmentId != 0) from.fragmentId else null
        val direction = getNavDirections(from, sessionId)
        if (direction != null) {
            navHost.navController.nav(fragmentId, direction)
        }
    }

    private fun getNavDirections(
        from: BrowserDirection,
        sessionId: String?
    ): NavDirections? = when (from) {
        BrowserDirection.FromFreshTab ->
            FreshTabFragmentDirections.actionFreshTabFragmentToBrowserFragment(sessionId)
        BrowserDirection.FromSearch ->
            SearchFragmentDirections.actionSearchFragmentToBrowserFragment(sessionId)
        BrowserDirection.FromHistory ->
            HistoryFragmentDirections.actionHistoryFragmentToBrowserFragment(sessionId)
    }

    private fun load(
        searchTermOrURL: String,
        newTab: Boolean,
        private: Boolean,
        fromFreshTab: Boolean,
        engine: SearchEngine? = null
    ) {
        if (searchTermOrURL.isUrl()) {
            loadUrl(searchTermOrURL, newTab, private, fromFreshTab)
        } else {
            searchTerm(searchTermOrURL, newTab, private, engine)
        }
    }

    @Suppress("ComplexMethod")
    private fun loadUrl(
        url: String,
        newTab: Boolean,
        private: Boolean,
        fromFreshTab: Boolean
    ) {
        val loadUrlUseCase = if (fromFreshTab) {
            components.core.sessionManager.selectedSession?.let {
                components.useCases.tabsUseCases.removeTab.invoke(it)
            }
            if (private) {
                components.useCases.tabsUseCases.addPrivateTab
            } else {
                components.useCases.tabsUseCases.addTab
            }
        } else {
            if (newTab) {
                if (private) {
                    components.useCases.tabsUseCases.addPrivateTab
                } else {
                    components.useCases.tabsUseCases.addTab
                }
            } else components.useCases.sessionUseCases.loadUrl
        }
        loadUrlUseCase.invoke(url.toNormalizedUrl())
    }

    private fun searchTerm(
        searchTerm: String,
        newTab: Boolean,
        private: Boolean,
        engine: SearchEngine? = null
    ) {
        val searchUseCase: (String) -> Unit = { searchTerms ->
            if (newTab) {
                components.useCases.searchUseCases.newTabSearch
                    .invoke(
                        searchTerms,
                        Session.Source.USER_ENTERED,
                        true,
                        private,
                        searchEngine = engine
                    )
            } else components.useCases.searchUseCases.defaultSearch.invoke(searchTerms, engine)
        }
        searchUseCase.invoke(searchTerm)
    }

    companion object {
        const val EXTRA_OPEN_TO_SEARCH = "OPEN_TO_SEARCH"
    }
}
