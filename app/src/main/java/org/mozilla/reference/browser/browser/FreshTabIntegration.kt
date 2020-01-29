/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Context
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleCoroutineScope
import com.cliqz.browser.freshtab.FreshTab
import com.cliqz.browser.news.ui.NewsFeature
import com.cliqz.browser.news.ui.NewsView
import com.cliqz.browser.news.domain.GetNewsUseCase
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.browser.toolbar.display.DisplayToolbar
import mozilla.components.concept.awesomebar.AwesomeBar
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.support.base.feature.LifecycleAwareFeature
import org.mozilla.reference.browser.ext.preferences

class FreshTabIntegration(
    private val context: Context,
    private val awesomeBar: AwesomeBar,
    private val toolbar: BrowserToolbar,
    private val freshTab: FreshTab,
    private val engineView: EngineView,
    private val sessionManager: SessionManager
) : LifecycleAwareFeature, Session.Observer, Toolbar.OnEditListener {

    @VisibleForTesting
    val currentUrl: String
        get() = sessionManager.selectedSession?.url ?: ""

    private var toolbarText: String = ""

    private var inputStarted = false

    private var newsFeature: NewsFeature? = null

    init {
        toolbar.setOnEditListener(this)
        sessionManager.register(object : SessionManager.Observer {
            override fun onSessionAdded(session: Session) {
                updateVisibility()
                addSession(session)
            }

            override fun onSessionsRestored() {
                updateVisibility()
                sessionManager.sessions.forEach { addSession(it) }
            }

            override fun onSessionSelected(session: Session) {
                updateVisibility()
            }

            override fun onSessionRemoved(session: Session) {
                updateVisibility()
                session.unregister(this@FreshTabIntegration)
            }
        }, freshTab)
        sessionManager.sessions.forEach {
            addSession(it)
        }
        updateVisibility()
    }

    override fun start() {
        if (context.preferences().shouldShowNewsView) {
            newsFeature?.start()
        } else {
            newsFeature?.hideNews()
        }
    }

    override fun stop() {
        // Show display mode if there's no change in url
        if (currentUrl == toolbarText) {
            toolbar.displayMode()
        }
        newsFeature?.stop()
    }

    fun addNewsFeature(
        newsView: NewsView,
        lifecycleScope: LifecycleCoroutineScope,
        loadUrl: SessionUseCases.DefaultLoadUrlUseCase,
        newsUseCase: GetNewsUseCase,
        icons: BrowserIcons
    ): FreshTabIntegration {
        newsFeature = NewsFeature(
            context,
            newsView,
            toolbar,
            lifecycleScope,
            loadUrl,
            newsUseCase,
            icons
        )
        return this
    }

    override fun onTextChanged(text: String) {
        toolbarText = text
        if (inputStarted) {
            if (text.isNotBlank()) {
                freshTab.visibility = View.GONE
                awesomeBar.asView().visibility = View.VISIBLE
            } else {
                freshTab.visibility = View.VISIBLE
                awesomeBar.asView().visibility = View.GONE
            }
            awesomeBar.onInputChanged(text)
        }
    }

    override fun onStartEditing() {
        inputStarted = true
        awesomeBar.onInputStarted()
        engineView.asView().visibility = View.GONE
    }

    override fun onStopEditing() {
        inputStarted = false
        awesomeBar.onInputCancelled()
        awesomeBar.asView().visibility = View.GONE
        updateVisibility()
    }

    override fun onCancelEditing(): Boolean {
        return true
    }

    private fun addSession(session: Session) {
        session.register(this, freshTab)
    }

    override fun onUrlChanged(session: Session, url: String) {
        updateVisibility()
    }

    @VisibleForTesting
    fun updateVisibility() {
        if (currentUrl.isFreshTab()) {
            freshTab.visibility = View.VISIBLE
            engineView.asView().visibility = View.GONE
            toolbar.display.indicators = emptyList()
        } else {
            freshTab.visibility = View.GONE
            engineView.asView().visibility = View.VISIBLE
            toolbar.display.indicators = listOf(DisplayToolbar.Indicators.SECURITY)
        }
    }

    companion object {
        const val NEW_TAB_URL = "about:blank"
    }
}

fun CharSequence.isFreshTab() = (this == FreshTabIntegration.NEW_TAB_URL) || (this == "")
