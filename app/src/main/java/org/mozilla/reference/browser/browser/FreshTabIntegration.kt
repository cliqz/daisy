/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Context
import android.os.Handler
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
import org.mozilla.reference.browser.freshtab.FreshTabToolbar

class FreshTabIntegration(
    private val context: Context,
    private val awesomeBar: AwesomeBar,
    private val toolbar: BrowserToolbar,
    private val freshTab: FreshTab,
    private val freshTabToolbar: FreshTabToolbar,
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
        toolbar.edit.setOnEditFocusChangeListener { hasFocus: Boolean ->
            if (!hasFocus) toolbar.displayMode()
        }
        freshTab.setOnTouchListener { _, _ ->
            if (toolbar.hasFocus()) {
                updateVisibility()
                true
            }
            false
        }

        freshTabToolbar.setSearchBarClickListener(View.OnClickListener {
            freshTabToolbar.setExpanded(false)
            Handler().postDelayed({
                toolbar.editMode()
                toolbar.visibility = View.VISIBLE
                freshTabToolbar.visibility = View.GONE
            }, FRESH_TAB_TOOLBAR_EXPAND_INTERACTION_DELAY)
        })

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
        if (toolbarText == text) return
        toolbarText = text
        if (inputStarted) {
            if (text.isNotBlank()) {
                freshTab.visibility = View.GONE
                freshTabToolbar.visibility = View.GONE
                awesomeBar.asView().visibility = View.VISIBLE
                engineView.asView().visibility = View.GONE
            } else {
                if (currentUrl.isFreshTab()) {
                    freshTab.visibility = View.VISIBLE
                } else {
                    engineView.asView().visibility = View.VISIBLE
                }
                awesomeBar.asView().visibility = View.GONE
            }
            awesomeBar.onInputChanged(text)
        }
    }

    override fun onStartEditing() {
        inputStarted = true
        awesomeBar.onInputStarted()
    }

    override fun onStopEditing() {
        inputStarted = false
        awesomeBar.onInputCancelled()
        awesomeBar.asView().visibility = View.GONE
    }

    override fun onCancelEditing(): Boolean {
        updateVisibility()
        return true
    }

    private fun addSession(session: Session) {
        session.register(this, freshTab)
    }

    override fun onUrlChanged(session: Session, url: String) {
        updateVisibility()
        toolbarText = url
    }

    @VisibleForTesting
    fun updateVisibility() {
        if (currentUrl.isFreshTab()) {
            freshTab.visibility = View.VISIBLE
            freshTabToolbar.visibility = View.VISIBLE
            engineView.asView().visibility = View.GONE
            toolbar.display.indicators = emptyList()
            toolbar.visibility = View.GONE
        } else {
            freshTab.visibility = View.GONE
            freshTabToolbar.visibility = View.GONE
            engineView.asView().visibility = View.VISIBLE
            toolbar.display.indicators = listOf(DisplayToolbar.Indicators.SECURITY)
            toolbar.visibility = View.VISIBLE
        }
    }

    companion object {
        const val NEW_TAB_URL = "about:blank"

        const val FRESH_TAB_TOOLBAR_EXPAND_INTERACTION_DELAY = 400L
    }
}

fun CharSequence.isFreshTab() = (this == FreshTabIntegration.NEW_TAB_URL) || (this == "")
