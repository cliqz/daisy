package com.cliqz.browser.news.data.source

import com.cliqz.browser.news.data.NewsItem
import com.cliqz.browser.news.data.Result

interface NewsRepository {

    suspend fun getNews(forceUpdate: Boolean = false): Result<List<NewsItem>>
}
