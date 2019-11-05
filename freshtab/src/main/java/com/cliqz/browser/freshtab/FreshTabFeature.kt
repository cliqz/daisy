package com.cliqz.browser.freshtab

import android.view.View
import androidx.annotation.VisibleForTesting
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.concept.engine.EngineView
import mozilla.components.browser.toolbar.BrowserToolbar

class FreshTabFeature(
    private val toolbar: BrowserToolbar,
    private val freshTab: FreshTab,
    private val engineView: EngineView,
    private val sessionManager: SessionManager
) : SessionManager.Observer, Session.Observer {

    @VisibleForTesting
    val currentUrl : String?
        get() = sessionManager.selectedSession?.url

    private var isUrlBarActive = toolbar.isFocused || toolbar.isInEditMode

    init {
        toolbar.setOnEditFocusChangeListener {
            isUrlBarActive = it || toolbar.isInEditMode
            updateVisibility()
        }
        sessionManager.register(this, freshTab)
        sessionManager.sessions.forEach { addSession(it) }
        updateVisibility()
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
        if (isUrlBarActive) {
            freshTab.visibility = View.GONE
        } else {
            val showFreshTab = currentUrl == null || currentUrl!!.isFreshTab()
            if (showFreshTab) {
                freshTab.visibility = View.VISIBLE
                engineView.asView().visibility = View.GONE
                toolbar.displaySiteSecurityIcon = false
            } else {
                freshTab.visibility = View.GONE
                engineView.asView().visibility = View.VISIBLE
                toolbar.displaySiteSecurityIcon = true
            }
        }
    }

    companion object {
        const val NEW_TAB_URL = "about:blank"
    }
}

fun CharSequence.isFreshTab() = (this == FreshTabFeature.NEW_TAB_URL) || (this == "")
