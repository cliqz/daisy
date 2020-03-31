/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.cliqz.browser.news.ui

import androidx.annotation.VisibleForTesting
import com.cliqz.browser.news.domain.GetNewsUseCase
import kotlinx.coroutines.CoroutineScope
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.support.base.feature.LifecycleAwareFeature

interface NewsInteractor {

    fun onNewsItemClicked(url: String)
}

class NewsFeature(
    private val newsView: NewsView,
    scope: CoroutineScope,
    newsInteractor: NewsInteractor,
    newsUseCase: GetNewsUseCase,
    icons: BrowserIcons? = null
) : LifecycleAwareFeature {

    @VisibleForTesting
    internal var presenter = DefaultNewsPresenter(
        newsView,
        scope,
        newsInteractor,
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
