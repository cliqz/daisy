package org.mozilla.reference.browser.browser

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.cliqz.browser.freshtab.FreshTab
import com.cliqz.browser.freshtab.FreshTabFeature
import com.cliqz.browser.news.domain.GetNewsUseCase
import com.cliqz.browser.news.ui.NewsFeature
import com.cliqz.browser.news.ui.NewsView
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.concept.awesomebar.AwesomeBar
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.support.base.feature.LifecycleAwareFeature
import org.mozilla.reference.browser.ext.preferences
import org.mozilla.reference.browser.history.usecases.HistoryUseCases
import org.mozilla.reference.browser.topsites.ui.TopSitesFeature
import org.mozilla.reference.browser.topsites.ui.TopSitesView

class FreshTabIntegration(
    private val context: Context,
    awesomeBar: AwesomeBar,
    private val toolbar: BrowserToolbar,
    freshTab: FreshTab,
    engineView: EngineView,
    sessionManager: SessionManager
) : LifecycleAwareFeature, SessionManager.Observer {

    private var newsFeature: NewsFeature? = null
    private var topSitesFeature: TopSitesFeature? = null

    init {
        FreshTabFeature(awesomeBar, toolbar, freshTab, engineView, sessionManager)
        sessionManager.register(this)
    }

    override fun onSessionAdded(session: Session) {
        super.onSessionAdded(session)
        topSitesFeature?.updateTopSites()
    }

    override fun start() {
        if (context.preferences().shouldShowNewsView) {
            newsFeature?.start()
            topSitesFeature?.start()
        } else {
            newsFeature?.hideNews()
        }
    }

    override fun stop() {
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
            icons)
        return this
    }

    fun addTopSitesFeature(
        topSitesView: TopSitesView,
        loadUrlUseCase: SessionUseCases.LoadUrlUseCase,
        getTopSitesUseCase: HistoryUseCases.GetTopSitesUseCase,
        browserIcons: BrowserIcons
    ) : FreshTabIntegration {
        topSitesFeature = TopSitesFeature(
            topSitesView,
            loadUrlUseCase,
            getTopSitesUseCase,
            browserIcons)
        return this
    }
}
