package com.cliqz.browser.freshtab

import android.view.View
import mozilla.components.browser.session.Session
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.toolbar.Toolbar

class FreshTabFeature(
    toolbar: Toolbar,
    private val freshTab: FreshTab,
    private val engineView: EngineView,
    private val selectedSession: Session? = null
) {
    init {
        showFreshTab()
        toolbar.setOnUrlCommitListener {
            hideFreshTab()
            true
        }
        toolbar.setOnEditListener(ToolbarEditListener(
                ::showFreshTab,
                ::hideFreshTab
        ))
    }

    private fun showFreshTab() {
        if (selectedSession == null || selectedSession.isFreshTab()) {
            freshTab.visibility = View.VISIBLE
            engineView.asView().visibility = View.GONE
        }
    }

    private fun hideFreshTab() {
        if (selectedSession == null || selectedSession.isFreshTab()) {
            freshTab.visibility = View.GONE
            engineView.asView().visibility = View.VISIBLE
        }
    }
}

internal class ToolbarEditListener(
    private val showFreshTab: () -> Unit,
    private val hideFreshTab: () -> Unit
) : Toolbar.OnEditListener {

    override fun onStartEditing() {
        hideFreshTab()
    }

    override fun onCancelEditing(): Boolean {
        showFreshTab()
        return true
    }
}

private fun Session.isFreshTab() = this.url == "about:blank"
