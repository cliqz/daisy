package com.cliqz.browser.news.ui

import com.cliqz.browser.news.data.NewsItem
import com.cliqz.browser.news.data.Result.Success
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class NewsFeatureTest {

    private val newsView: NewsView = mockk(relaxed = true)
    private val presenter: NewsPresenter = mockk(relaxed = true)

    private val coroutineScope = TestCoroutineScope()

    private lateinit var feature: NewsFeature

    @Before
    fun setup() {
        feature = NewsFeature(newsView, coroutineScope, mockk(), mockk())
        feature.presenter = presenter
    }

    @After
    fun tearDown() {
        coroutineScope.cleanupTestCoroutines()
    }

    /**
     * Derived from FindInPageFeatureTest
     */
    @Test
    fun `Start is forwarded to interactor`() {
        val emptyResult: Success<List<NewsItem>> = Success(listOf())
        coEvery { presenter.getNews() } returns emptyResult
        feature.start()
        verify { presenter.start() }
    }

    @Test
    fun `Stop is forwarded to interactor`() {
        feature.stop()
        verify { presenter.stop() }
    }

    @Test
    fun `Data is forwarded to view through interactor`() {
        val emptyResult: Success<List<NewsItem>> = Success(listOf())
        coEvery { presenter.getNews() } returns emptyResult
        feature.start()
        verify { newsView.displayNews(emptyResult.data) }
    }
}
