package org.mozilla.reference.browser.browser

import android.view.View
import android.widget.ImageView
import androidx.lifecycle.LifecycleCoroutineScope
import com.cliqz.browser.freshtab.FreshTab
import com.cliqz.browser.freshtab.FreshTabFeature
import com.cliqz.browser.news.ui.NewsFeature
import com.cliqz.browser.news.ui.NewsView
import com.cliqz.browser.news.domain.GetNewsUseCase
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.icons.IconRequest
import mozilla.components.browser.session.Session
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.support.base.feature.LifecycleAwareFeature

class FreshTabIntegration(
    toolbar: Toolbar,
    private val freshTab: FreshTab,
    private val engineView: EngineView,
    selectedSession: Session? = null
) : LifecycleAwareFeature {

    private var feature: NewsFeature? = null
    private var icons: BrowserIcons? = null

    init {
        FreshTabFeature(toolbar, freshTab, engineView, selectedSession)
    }

    override fun start() {
        feature?.start()
    }

    override fun stop() {
        feature?.stop()
    }

    fun addNewsFeature(
        newsView: NewsView,
        lifecycleScope: LifecycleCoroutineScope,
        loadUrl: SessionUseCases.DefaultLoadUrlUseCase,
        newsUseCase: GetNewsUseCase,
        icons: BrowserIcons
    ): FreshTabIntegration {
        feature = NewsFeature(
            newsView,
            lifecycleScope,
            loadUrl,
            newsUseCase,
            ::onNewsItemSelected,
            ::loadNewsItemIcon)
        this.icons = icons
        return this
    }

    private fun onNewsItemSelected() {
        freshTab.visibility = View.GONE
        engineView.asView().visibility = View.VISIBLE
    }

    private fun loadNewsItemIcon(view: ImageView, url: String) {
        icons?.loadIntoView(view, IconRequest(url))
    }
}
