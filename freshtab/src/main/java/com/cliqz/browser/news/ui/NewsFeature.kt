package com.cliqz.browser.news.ui

import android.widget.ImageView
import androidx.annotation.VisibleForTesting
import com.cliqz.browser.news.data.Result.Success
import com.cliqz.browser.news.domain.GetNewsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import mozilla.components.feature.session.SessionUseCases.LoadUrlUseCase
import mozilla.components.support.base.feature.LifecycleAwareFeature

class NewsFeature(
    private val newsView: NewsView,
    private val scope: CoroutineScope,
    loadUrlUseCase: LoadUrlUseCase,
    newsUseCase: GetNewsUseCase,
    onNewsItemSelected: (() -> Unit)? = null,
    loadNewsItemIcon: ((view: ImageView, url: String) -> Unit)? = null
) : LifecycleAwareFeature {

    @VisibleForTesting
    internal var presenter = NewsPresenter(
        newsView,
        loadUrlUseCase,
        newsUseCase,
        onNewsItemSelected,
        loadNewsItemIcon
    )

    override fun start() {
        presenter.start()
        val result = scope.async(Dispatchers.IO) { presenter.getNews() }
        scope.launch {
            result.await().run {
                if (this is Success) {
                    newsView.displayNews(data)
                } else {
                    newsView.hideNews()
                }
            }
        }
    }

    override fun stop() {
        presenter.stop()
    }
}
