package org.mozilla.reference.browser.components

import android.content.Context
import com.cliqz.browser.news.data.source.DefaultNewsRepository
import com.cliqz.browser.news.data.source.remote.NewsLocalDataSource
import com.cliqz.browser.news.data.source.remote.NewsRemoteDataSource
import mozilla.components.concept.fetch.Client

class News(private val client: Client, private val context: Context) {

    val newsRepository by lazy {
        val newsRemoteDataSource = NewsRemoteDataSource(client)
        val newsLocalDataSource = NewsLocalDataSource(context)
        DefaultNewsRepository.getInstance(newsRemoteDataSource, newsLocalDataSource)
    }
}
