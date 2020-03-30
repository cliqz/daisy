/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.freshtab

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.cliqz.browser.news.domain.GetNewsUseCase
import com.cliqz.browser.news.ui.NewsFeature
import com.cliqz.browser.news.ui.NewsInteractor
import com.cliqz.browser.news.ui.NewsView
import kotlinx.coroutines.CoroutineScope
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.session.SessionManager
import mozilla.components.support.base.feature.LifecycleAwareFeature
import org.mozilla.reference.browser.ext.preferences
import org.mozilla.reference.browser.freshtab.toolbar.SearchBarInteractor
import org.mozilla.reference.browser.freshtab.toolbar.ToolbarFeature
import org.mozilla.reference.browser.freshtab.toolbar.ToolbarMenuInteractor
import org.mozilla.reference.browser.library.history.usecases.HistoryUseCases
import org.mozilla.reference.browser.topsites.ui.TopSitesFeature
import org.mozilla.reference.browser.topsites.ui.TopSitesView

class FreshTabIntegration(
    private val context: Context,
    private val sessionManager: SessionManager,
    private val freshTabToolbar: FreshTabToolbar
) : LifecycleAwareFeature {

    private var newsFeature: NewsFeature? = null
    private var topSitesFeature: TopSitesFeature? = null
    private var toolbarFeature: ToolbarFeature? = null

    override fun start() {
        topSitesFeature?.start()
        if (context.preferences().shouldShowNewsView) {
            newsFeature?.start()
        } else {
            newsFeature?.hideNews()
        }
    }

    override fun stop() {
        newsFeature?.stop()
    }

    fun addToolbarFeature(
        freshTabInteractor: FreshTabInteractor,
        scope: CoroutineScope
    ): FreshTabIntegration {
        toolbarFeature = ToolbarFeature(
            context,
            scope,
            sessionManager,
            freshTabToolbar,
            freshTabInteractor as ToolbarMenuInteractor,
            freshTabInteractor as SearchBarInteractor
        )
        return this
    }

    fun addNewsFeature(
        newsView: NewsView,
        lifecycleScope: LifecycleCoroutineScope,
        newsInteractor: NewsInteractor,
        newsUseCase: GetNewsUseCase,
        icons: BrowserIcons
    ): FreshTabIntegration {
        newsFeature = NewsFeature(
            newsView,
            lifecycleScope,
            newsInteractor,
            newsUseCase,
            icons
        )
        return this
    }

    fun addTopSitesFeature(
        topSitesView: TopSitesView,
        historyUseCases: HistoryUseCases,
        browserIcons: BrowserIcons
    ): FreshTabIntegration {
        topSitesFeature = TopSitesFeature(
            topSitesView,
            historyUseCases,
            browserIcons)
        return this
    }

    companion object {
        const val FRESH_TAB_TOOLBAR_EXPAND_INTERACTION_DELAY = 200L
    }
}
