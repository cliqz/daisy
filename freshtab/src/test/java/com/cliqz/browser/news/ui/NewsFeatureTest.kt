package com.cliqz.browser.news.ui

import com.cliqz.browser.news.data.NewsItem
import com.cliqz.browser.news.data.Result.Success
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class NewsFeatureTest {

    private val newsView: NewsView = mockk(relaxed = true)

    private val coroutineScope = TestCoroutineScope()

    private lateinit var feature: NewsFeature
    private lateinit var presenter: DefaultNewsPresenter

    @Before
    fun setup() {
        presenter = spyk(DefaultNewsPresenter(mockk(), newsView, coroutineScope, mockk(), mockk()))
        feature = NewsFeature(mockk(), newsView, coroutineScope, mockk(), mockk())
        feature.presenter = presenter
        coEvery { presenter.isNewsViewExpanded } returns true
    }

    @After
    fun tearDown() {
        coroutineScope.cleanupTestCoroutines()
    }

    /**
     * Derived from FindInPageFeatureTest
     */
    @Test
    fun `Start is forwarded to presenter`() {
        val emptyResult: Success<List<NewsItem>> = Success(listOf())
        coEvery { presenter.getNews() } returns emptyResult
        feature.start()
        verify { presenter.start() }
    }

    @Test
    fun `Stop is forwarded to presenter`() {
        feature.stop()
        verify { presenter.stop() }
    }

    @Test
    fun `Data is forwarded to view through presenter`() {
        val emptyResult: Success<List<NewsItem>> = Success(listOf())
        coEvery { presenter.getNews() } returns emptyResult
        feature.start()
        verify { newsView.displayNews(any(), any()) }
    }
}
