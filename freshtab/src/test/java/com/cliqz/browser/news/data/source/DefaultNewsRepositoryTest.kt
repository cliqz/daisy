package com.cliqz.browser.news.data.source

import com.cliqz.browser.news.data.NewsItem
import com.cliqz.browser.news.data.Result.Success
import com.cliqz.browser.news.data.source.remote.NewsRemoteDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class DefaultNewsRepositoryTest {

    private val remoteNewsDataSource = mockk<NewsRemoteDataSource>()

    private val newsRepository = spyk(DefaultNewsRepository(remoteNewsDataSource, mockk(relaxed = true)))

    @Test
    fun `getNews second and subsequent api calls return cache`() = runBlockingTest {

        coEvery { remoteNewsDataSource.getNews() } returns Success(listOf(
                NewsItem(
                        url = "https://www.cnbc.com/amp/2019/10/15/catalonia-protests-expected-to-continue-after-separatists-jailed.html",
                        title = "Spain braced for more protests after imprisonment of Catalan leaders provokes outrage",
                        description = "Spain is braced for more protests in Catalonia as anger mounts over jail terms meted out to pro-independence leaders in the region.",
                        domain = "cnbc.com",
                        shortTitle = "Spain braced for more protests after imprisonment of Catalan leaders provokes outrage",
                        media = "https://image.cnbcfm.com/api/v1/image/106181517-1571123242715gettyimages-1181066665.jpeg?v=1571123278",
                        breaking = false,
                        breakingLabel = null,
                        isLocalNews = false,
                        localLabel = null
                )
        ))
        val initial = newsRepository.getNews() as Success

        coEvery { remoteNewsDataSource.getNews() } returns Success(listOf())

        val second = newsRepository.getNews() as Success

        coVerify(exactly = 1) { remoteNewsDataSource.getNews() }
        assertEquals(second.data, initial.data)
    }

    @Test
    fun `getNews makes a network api call when cache expires`() = runBlockingTest {

        coEvery { remoteNewsDataSource.getNews() } returns Success(listOf(
                NewsItem(
                        url = "https://www.cnbc.com/amp/2019/10/15/catalonia-protests-expected-to-continue-after-separatists-jailed.html",
                        title = "Spain braced for more protests after imprisonment of Catalan leaders provokes outrage",
                        description = "Spain is braced for more protests in Catalonia as anger mounts over jail terms meted out to pro-independence leaders in the region.",
                        domain = "cnbc.com",
                        shortTitle = "Spain braced for more protests after imprisonment of Catalan leaders provokes outrage",
                        media = "https://image.cnbcfm.com/api/v1/image/106181517-1571123242715gettyimages-1181066665.jpeg?v=1571123278",
                        breaking = false,
                        breakingLabel = null,
                        isLocalNews = false,
                        localLabel = null
                )
        ))

        val initial = newsRepository.getNews() as Success

        assertTrue(!newsRepository.hasCacheExpired())

        coEvery { remoteNewsDataSource.getNews() } returns Success(listOf())
        every { newsRepository.hasCacheExpired() } returns true

        val second = newsRepository.getNews() as Success

        coVerify(exactly = 2) { remoteNewsDataSource.getNews() }
        assertNotEquals(second.data, initial.data)
    }
}