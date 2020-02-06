/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.cliqz.browser.news.ui

import androidx.annotation.VisibleForTesting
import com.cliqz.browser.news.domain.GetNewsUseCase
import kotlinx.coroutines.CoroutineScope
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.feature.session.SessionUseCases.LoadUrlUseCase
import mozilla.components.support.base.feature.LifecycleAwareFeature

class NewsFeature(
    private val newsView: NewsView,
    toolbar: Toolbar,
    scope: CoroutineScope,
    loadUrlUseCase: LoadUrlUseCase,
    newsUseCase: GetNewsUseCase,
    icons: BrowserIcons? = null
) : LifecycleAwareFeature {

    @VisibleForTesting
    internal var presenter = DefaultNewsPresenter(
        newsView,
        toolbar,
        scope,
        loadUrlUseCase,
        newsUseCase,
        icons
    )

    override fun start() {
        presenter.start()
    }

    fun hideNews() {
        newsView.hideNews()
    }

    override fun stop() {
        presenter.stop()
    }
}
