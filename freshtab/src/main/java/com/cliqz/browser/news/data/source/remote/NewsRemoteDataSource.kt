package com.cliqz.browser.news.data.source.remote

import com.cliqz.browser.news.data.NewsItem
import com.cliqz.browser.news.data.Result
import com.cliqz.browser.news.data.Result.Error
import com.cliqz.browser.news.data.Result.Success
import com.cliqz.browser.news.data.source.NewsDataSource
import java.io.IOException
import java.util.Locale
import mozilla.components.concept.fetch.Client
import mozilla.components.concept.fetch.Headers
import mozilla.components.concept.fetch.isSuccess
import mozilla.components.concept.fetch.MutableHeaders
import mozilla.components.concept.fetch.Request
import mozilla.components.concept.fetch.Response
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

class NewsRemoteDataSource(private val client: Client) : NewsDataSource {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getNews(): Result<List<NewsItem>> {
        val url = getNewsUrl()
        val headers = MutableHeaders(
            Headers.Names.CONTENT_TYPE to CONTENT_TYPE_JSON
        )
        val request = Request(
            url = url,
            method = Request.Method.PUT,
            headers = headers,
            body = Request.Body.fromString(NEWS_PAYLOAD)
        )

        try {
            val response = client.fetch(request)
            if (response.isSuccess) {
                return Success(response.toNewsList())
            }
            return Error(Exception("Error fetching news. Response code:${response.status}"))
        } catch (e: Exception) {
            when (e) {
                // Known exceptions. Handle them.
                is IOException, is JSONException -> return Error(e)
                else -> throw e
            }
        }
    }

    override suspend fun saveNews(newsList: List<NewsItem>) {
        // no-op
    }

    private fun getNewsUrl(): String {
        val locale = Locale.getDefault().toString().replace("_", "-")
        val parts = locale.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var lang: String? = null
        var newsEdition = "intl"
        if (parts.isNotEmpty()) {
            lang = parts[0].toLowerCase(Locale.getDefault())
        }
        val sb = StringBuilder(NEWS_URL)
        sb.append("&locale=").append(locale)
        if (lang != null) {
            sb.append("&lang=").append(lang)
            if (lang == Locale.GERMAN.language || lang == Locale.FRENCH.language) {
                newsEdition = lang
            }
        }
        sb.append("&edition=").append(newsEdition)

        /* Get country based result.
        val country: String?
        country = preferenceManager.getCountryChoice().countryCode
        if (country != null) {
            sb.append("&country=").append(country)
        } */
        sb.append("&count=").append(Int.MAX_VALUE)
        sb.append("&platform=1")
        /* Get location based result.
        if (locationCache.getLastLocation() != null) {
            sb.append("&loc=").append(locationCache.getLastLocation().getLatitude()).append(",")
                    .append(locationCache.getLastLocation().getLongitude())
        }*/
        return sb.toString()
    }

    companion object {
        private const val CONTENT_TYPE_JSON = "application/json"
        private const val NEWS_PAYLOAD =
                "{\"q\":\"\",\"results\":[{\"url\":\"rotated-top-news.cliqz.com\",\"snippet\":{}}]}"
        private const val NEWS_URL = "https://api.cliqz.com/api/v2/rich-header?path=/v2/map"
    }
}

@Throws(JSONException::class)
private fun Response.toNewsList(): List<NewsItem> {
    val responseBody = use { body.string() }
    val result = responseBody.run {
        JSONTokener(responseBody).nextValue() as JSONObject
    }
    val newsList = ArrayList<NewsItem>()
    val data = result.getJSONArray("results").getJSONObject(0)
        .getJSONObject("snippet").getJSONObject("extra")
    val articles = data.getJSONArray("articles")
    for (i in 0 until articles.length()) {
        val article = articles.getJSONObject(i)

        val url = article.optString("url", "")
        val title = article.optString("title", "")
        val description = article.optString("description", "")
        val domain = article.optString("domain", "")
        val shortTitle = article.optString("short_title", "")
        val media = article.optString("media", "")
        val breaking = article.optBoolean("breaking", false)
        val breakingLabel = article.optString("breaking_label", "")
        val isLocalNews = article.has("local_news")
        val localLabel = article.optString("local_label", "")
        newsList.add(NewsItem(url, title, description, domain, shortTitle, media,
            breakingLabel, breaking, isLocalNews, localLabel))
    }
    return newsList
}
