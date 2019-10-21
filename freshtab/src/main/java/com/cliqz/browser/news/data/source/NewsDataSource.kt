package com.cliqz.browser.news.data.source

import com.cliqz.browser.news.data.NewsItem
import com.cliqz.browser.news.data.Result

interface NewsDataSource {

    suspend fun getNews(): Result<List<NewsItem>>
}
