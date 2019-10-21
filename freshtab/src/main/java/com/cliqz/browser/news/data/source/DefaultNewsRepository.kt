package com.cliqz.browser.news.data.source

import com.cliqz.browser.news.data.NewsItem
import com.cliqz.browser.news.data.Result
import com.cliqz.browser.news.data.Result.Success
import com.cliqz.browser.news.data.source.remote.NewsRemoteDataSource

class DefaultNewsRepository(
    private val newsRemoteDataSource: NewsRemoteDataSource
) : NewsRepository {

    private var cachedNews: List<NewsItem> = emptyList()

    override suspend fun getNews(forceUpdate: Boolean): Result<List<NewsItem>> {
        // TODO: Logic to do force update after some time interval

        // Return with cache if available
        if (cachedNews.isNotEmpty()) {
            return Success(cachedNews)
        }
        val news = fetchNewsFromRemoteOrLocal()
        cacheNews((news as Success).data)
        return Success(cachedNews)
    }

    private suspend fun fetchNewsFromRemoteOrLocal(): Result<List<NewsItem>> {
        // TODO: We can have a local news repository (in db/prefs) populating the newsview
        //  even when app is offline
        return newsRemoteDataSource.getNews()
    }

    private fun cacheNews(newsList: List<NewsItem>) {
        cachedNews = newsList
    }

    companion object {
        // For singleton instantiation
        @Volatile
        private var instance: NewsRepository? = null

        fun getInstance(newsRemoteDataSource: NewsRemoteDataSource) =
            instance ?: synchronized(this) {
                instance ?: DefaultNewsRepository(newsRemoteDataSource).also { instance = it }
            }
    }
}
