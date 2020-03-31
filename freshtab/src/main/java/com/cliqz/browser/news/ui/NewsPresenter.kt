/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.cliqz.browser.news.ui

import com.cliqz.browser.news.data.NewsItem
import com.cliqz.browser.news.data.Result
import com.cliqz.browser.news.domain.GetNewsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import mozilla.components.browser.icons.BrowserIcons

class DefaultNewsPresenter(
    private val newsView: NewsView,
    private val scope: CoroutineScope,
    private val newsInteractor: NewsInteractor,
    private val getNewsUseCase: GetNewsUseCase,
    private val icons: BrowserIcons? = null
) : NewsPresenter {

    fun start() {
        newsView.presenter = this
        val result = scope.async(Dispatchers.IO) {
            getNews()
        }
        scope.launch {
            result.await().run {
                if (this is Result.Success) {
                    newsView.displayNews(data, icons)
                } else {
                    newsView.hideNews()
                }
            }
        }
    }

    fun stop() {
        // no-op
    }

    override suspend fun getNews(): Result<List<NewsItem>> {
        return getNewsUseCase.invoke()
    }

    override fun onOpenInNormalTab(item: NewsItem) {
        newsInteractor.onNewsItemClicked(item.url)
    }
}

interface NewsPresenter {

    suspend fun getNews(): Result<List<NewsItem>>

    fun onOpenInNormalTab(item: NewsItem)

    interface View {

        fun displayNews(newsList: List<NewsItem>, icons: BrowserIcons?)

        fun hideNews()
    }
}
