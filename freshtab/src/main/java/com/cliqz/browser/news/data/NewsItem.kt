package com.cliqz.browser.news.data

data class NewsItem(
    val url: String,
    val title: String,
    val description: String,
    val domain: String,
    val shortTitle: String,
    val media: String,
    val breakingLabel: String?,
    val breaking: Boolean,
    val isLocalNews: Boolean,
    val localLabel: String?
)
