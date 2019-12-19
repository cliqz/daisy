package org.mozilla.reference.browser.utils

/**
 * Borrowed from https://blog.mindorks.com/how-to-create-a-singleton-class-in-kotlin
 */
open class SingletonHolder<out T : Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        return instance ?: synchronized(this) {
            instance ?: creator!!(arg).also { instance = it }
        }
    }
}
