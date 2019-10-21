package com.cliqz.browser.news.domain

import com.cliqz.browser.news.data.NewsItem
import com.cliqz.browser.news.data.Result
import com.cliqz.browser.news.data.source.NewsRepository

class GetNewsUseCase(private val newsRepository: NewsRepository) {

    suspend operator fun invoke(): Result<List<NewsItem>> {
        return newsRepository.getNews()
    }
}
