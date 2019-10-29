package com.cliqz.browser.news.data.source

import androidx.annotation.VisibleForTesting
import com.cliqz.browser.news.data.NewsItem
import com.cliqz.browser.news.data.Result
import com.cliqz.browser.news.data.Result.Success
import com.cliqz.browser.news.data.Result.Error
import com.cliqz.browser.news.data.source.remote.NewsLocalDataSource
import com.cliqz.browser.news.data.source.remote.NewsRemoteDataSource

class DefaultNewsRepository(
    private val newsRemoteDataSource: NewsRemoteDataSource,
    private val newsLocalDataSource: NewsLocalDataSource
) : NewsRepository {

    private var cachedNews: List<NewsItem> = emptyList()

    private var lastCachedOn = 0L

    override suspend fun getNews(): Result<List<NewsItem>> {
        // Return with cache if available
        if (cachedNews.isNotEmpty() && !hasCacheExpired()) {
            return Success(cachedNews)
        }
        val newsList = fetchNewsFromRemoteOrLocal()
        (newsList as? Success)?.let { cacheNews(it.data) }
        return Success(cachedNews)
    }

    private suspend fun fetchNewsFromRemoteOrLocal(): Result<List<NewsItem>> {
        when (val remoteNewsList = newsRemoteDataSource.getNews()) {
            is Error -> {
                // TODO: Log that fetching from remote source failed
            }
            is Success -> {
                refreshLocalDataSource(remoteNewsList.data)
                return remoteNewsList
            }
        }

        val localNewsList = newsLocalDataSource.getNews()
        if (localNewsList is Success) {
            return localNewsList
        }
        return Error(Exception("Error fetching from remote and local news source"))
    }

    private fun cacheNews(newsList: List<NewsItem>) {
        cachedNews = newsList
        lastCachedOn = System.currentTimeMillis()
    }

    @VisibleForTesting
    fun hasCacheExpired(): Boolean {
        return lastCachedOn != 0L && (System.currentTimeMillis() - lastCachedOn) > CACHE_PERIOD
    }

    private suspend fun refreshLocalDataSource(news: List<NewsItem>) {
        newsLocalDataSource.saveNews(news)
    }

    companion object {
        const val CACHE_PERIOD = 30 * 60 * 1000L // 30 minutes

        // For singleton instantiation
        @Volatile
        private var instance: NewsRepository? = null

        fun getInstance(newsRemoteDataSource: NewsRemoteDataSource,
                        newsLocalDataSource: NewsLocalDataSource) =
            instance ?: synchronized(this) {
                instance ?: DefaultNewsRepository(newsRemoteDataSource, newsLocalDataSource).also { instance = it }
            }
    }
}
