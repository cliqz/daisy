package com.cliqz.browser.freshtab

import android.view.View
import androidx.annotation.VisibleForTesting
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.concept.engine.EngineView
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.browser.toolbar.display.DisplayToolbar
import mozilla.components.concept.awesomebar.AwesomeBar
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.support.base.feature.LifecycleAwareFeature

class FreshTabFeature(
    private val awesomeBar: AwesomeBar,
    private val toolbar: BrowserToolbar,
    private val freshTab: FreshTab,
    private val engineView: EngineView,
    private val sessionManager: SessionManager
) : SessionManager.Observer, Session.Observer, Toolbar.OnEditListener, LifecycleAwareFeature {

    @VisibleForTesting
    val currentUrl: String?
        get() = sessionManager.selectedSession?.url

    private var toolbarText: String = ""

    private var inputStarted = false

    init {
        toolbar.setOnEditListener(this)
        sessionManager.register(this, freshTab)
        sessionManager.sessions.forEach { addSession(it) }
        updateVisibility()
    }

    override fun start() {
        // no-op
    }

    override fun stop() {
        // Show display mode if there's no change in url
        if (sessionManager.selectedSession != null &&
                sessionManager.selectedSession!!.url == toolbarText) {
            toolbar.displayMode()
        }
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
        awesomeBar.onInputCancelled()
        awesomeBar.asView().visibility = View.GONE
        updateVisibility()
        inputStarted = false
    }

    override fun onCancelEditing(): Boolean {
        return true
    }

    private fun addSession(session: Session) {
        session.register(this, freshTab)
    }

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
        session.unregister(this)
    }

    override fun onUrlChanged(session: Session, url: String) {
        updateVisibility()
    }

    @VisibleForTesting
    fun updateVisibility() {
            val showFreshTab = currentUrl == null || currentUrl!!.isFreshTab()
            if (showFreshTab) {
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

fun CharSequence.isFreshTab() = (this == FreshTabFeature.NEW_TAB_URL) || (this == "")
