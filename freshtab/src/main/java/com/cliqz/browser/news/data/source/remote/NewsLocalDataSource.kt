package com.cliqz.browser.news.data.source.remote

import android.content.Context
import com.cliqz.browser.news.data.NewsItem
import com.cliqz.browser.news.data.Result
import com.cliqz.browser.news.data.Result.Success
import com.cliqz.browser.news.data.Result.Error
import com.cliqz.browser.news.data.source.NewsDataSource
import java.io.FileNotFoundException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class NewsLocalDataSource(private val context: Context) : NewsDataSource {

    @Suppress("UNCHECKED_CAST", "TooGenericExceptionCaught")
    override suspend fun getNews(): Result<List<NewsItem>> {
        return try {
            Success(readObjectFromFile() as? List<NewsItem> ?: emptyList())
        } catch (e: Exception) {
            when (e) {
                // File won't exist for the first time the app is run. Do nothing.
                is FileNotFoundException -> Error(e)
                else -> throw e
            }
        }
    }

    override suspend fun saveNews(newsList: List<NewsItem>) {
        writeObjectToFile(newsList as Any)
    }

    private fun writeObjectToFile(data: Any) {
        context.deleteFile(NEWS_FILE_NAME)
        val fos = context.openFileOutput(NEWS_FILE_NAME, Context.MODE_PRIVATE)
        val oss = ObjectOutputStream(fos)
        oss.writeObject(data)
        oss.close()
    }

    private fun readObjectFromFile(): Any {
        val fis = context.openFileInput(NEWS_FILE_NAME)
        val ois = ObjectInputStream(fis)
        val data = ois.readObject()
        ois.close()
        return data
    }

    companion object {
        private const val NEWS_FILE_NAME = "news_list"
    }
}
