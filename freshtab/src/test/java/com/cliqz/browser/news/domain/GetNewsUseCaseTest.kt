package com.cliqz.browser.news.domain

import com.cliqz.browser.news.data.NewsItem
import com.cliqz.browser.news.data.Result.Error
import com.cliqz.browser.news.data.Result.Success
import com.cliqz.browser.news.data.source.NewsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class GetNewsUseCaseTest {

    private val newsRepository = mockk<NewsRepository>()

    private val useCase = GetNewsUseCase(newsRepository)

    @Test
    fun `GetNewsUseCase should return empty data`() = runBlockingTest {
        coEvery { newsRepository.getNews() } returns Success(listOf())
        val result = useCase()

        coVerify { newsRepository.getNews() }

        assertTrue(result is Success)
        assertTrue((result as Success).data.isEmpty())
    }

    @Test
    fun `GetNewsUseCase should return some data`() = runBlockingTest {
        coEvery { newsRepository.getNews() } returns Success(listOf(
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
                ),
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
                ),
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

        val result = useCase()

        coVerify { newsRepository.getNews() }

        assertTrue(result is Success)
        assertEquals((result as Success).data.size, 3)
    }

    @Test
    fun `GetNewsUseCase should return error`() = runBlockingTest {
        coEvery { newsRepository.getNews() } returns Error(Exception("Test exception"))

        val result = useCase()

        coVerify { newsRepository.getNews() }

        assertTrue(result is Error)
    }

}