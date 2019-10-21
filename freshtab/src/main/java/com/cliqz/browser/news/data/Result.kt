package com.cliqz.browser.news.data

import com.cliqz.browser.news.data.Result.Success
import java.lang.Exception

/**
 * A generic class that holds data and its loading state
 * Derived from 'Exposing Network Status' on developer.android.com:
 * https://developer.android.com/jetpack/docs/guide#addendum
 */
sealed class Result<out R> {

    class Success<out T>(val data: T) : Result<T>()
    class Error(exception: Exception, val message: String = exception.localizedMessage) :
            Result<Nothing>()
}

val Result<*>.succeeded
    get() = this is Success && data != null
