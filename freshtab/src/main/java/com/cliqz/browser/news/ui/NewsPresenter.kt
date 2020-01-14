package com.cliqz.browser.news.ui

import android.content.Context
import android.content.SharedPreferences
import com.cliqz.browser.news.data.NewsItem
import com.cliqz.browser.news.data.Result
import com.cliqz.browser.news.domain.GetNewsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.feature.session.SessionUseCases.LoadUrlUseCase

class DefaultNewsPresenter(
    private val context: Context,
    private val newsView: NewsView,
    private val toolbar: Toolbar,
    private val scope: CoroutineScope,
    private val loadUrlUseCase: LoadUrlUseCase,
    private val getNewsUseCase: GetNewsUseCase,
    private val icons: BrowserIcons? = null
) : NewsPresenter {

    override var isNewsViewExpanded: Boolean
        get() = isNewsViewExpanded(context)
        set(value) {
            setNewsViewExpanded(context, value)
        }

    fun start() {
        newsView.presenter = this
        val result = scope.async(Dispatchers.IO) {
            getNews()
        }
        scope.launch {
            result.await().run {
                if (this is Result.Success) {
                    newsView.displayNews(data, isNewsViewExpanded, icons)
                } else {
                    newsView.hideNews()
                }
            }
        }
    }

    fun stop() {
        newsView.presenter = null
    }

    override suspend fun getNews(): Result<List<NewsItem>> {
        return getNewsUseCase.invoke()
    }

    override fun toggleNewsViewClicked() {
        isNewsViewExpanded = !isNewsViewExpanded
        newsView.toggleNewsView(isNewsViewExpanded)
    }

    override fun onOpenInNormalTab(item: NewsItem) {
        loadUrlUseCase.invoke(item.url)
        toolbar.displayMode()
    }

    private fun isNewsViewExpanded(context: Context): Boolean {
        return preferences(context).getBoolean(PREF_NEWS_EXPANDED, false)
    }

    private fun setNewsViewExpanded(context: Context, value: Boolean) {
        preferences(context).edit().putBoolean(PREF_NEWS_EXPANDED, value).apply()
    }

    private fun preferences(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    companion object {
        internal const val PREFERENCE_NAME = "news_feature"
        internal const val PREF_NEWS_EXPANDED = "news_expanded"
    }
}

interface NewsPresenter {

    var isNewsViewExpanded: Boolean

    suspend fun getNews(): Result<List<NewsItem>>

    fun onOpenInNormalTab(item: NewsItem)

    fun toggleNewsViewClicked()

    interface View {

        fun displayNews(newsList: List<NewsItem>, isNewsViewExpanded: Boolean, icons: BrowserIcons?)

        fun hideNews()

        fun toggleNewsView(isNewsViewExpanded: Boolean)
    }
}
