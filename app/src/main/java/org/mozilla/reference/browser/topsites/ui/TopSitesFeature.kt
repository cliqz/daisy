package org.mozilla.reference.browser.topsites.ui

import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.feature.session.SessionUseCases.LoadUrlUseCase
import mozilla.components.support.base.feature.LifecycleAwareFeature
import org.mozilla.reference.browser.history.usecases.HistoryUseCases.GetTopSitesUseCase

/**
 * @author Ravjit Uppal
 */
class TopSitesFeature(
    private val topSitesView: TopSitesView,
    private val loadUrlUseCase: LoadUrlUseCase,
    private val getTopSitesUseCase: GetTopSitesUseCase,
    private val browserIcons: BrowserIcons
) : LifecycleAwareFeature {

    override fun start() {
        topSitesView.init(loadUrlUseCase, getTopSitesUseCase,  browserIcons)
    }

    override fun stop() {
    }

    fun updateTopSites() {
        topSitesView.updateTopSites()
    }

}