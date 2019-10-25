package org.mozilla.reference.browser.browser

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.cliqz.browser.freshtab.FreshTab
import com.cliqz.browser.freshtab.FreshTabFeature
import com.cliqz.browser.news.ui.NewsFeature
import com.cliqz.browser.news.ui.NewsView
import com.cliqz.browser.news.domain.GetNewsUseCase
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.support.base.feature.LifecycleAwareFeature
import org.mozilla.reference.browser.ext.preferences

class FreshTabIntegration(
    private val context: Context,
    toolbar: BrowserToolbar,
    freshTab: FreshTab,
    engineView: EngineView,
    sessionManager: SessionManager
) : LifecycleAwareFeature {

    private var newsFeature: NewsFeature? = null

    init {
        FreshTabFeature(toolbar, freshTab, engineView, sessionManager)
    }

    override fun start() {
        if (context.preferences().shouldShowNewsView) {
            newsFeature?.start()
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
            lifecycleScope,
            loadUrl,
            newsUseCase,
            icons)
        return this
    }
}
