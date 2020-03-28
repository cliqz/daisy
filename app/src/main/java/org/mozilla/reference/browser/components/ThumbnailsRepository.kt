/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import androidx.core.graphics.scale
import androidx.core.util.AtomicFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.withOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.lang.ref.WeakReference
import java.util.Locale

/**
 * The [ThumbnailsRepository] observes the [SessionManager] in order to intercept sessions being
 * added or removed. When a [Session] is added, it subscribe itself as a session observer to detect
 * [Session.thumbnail] changes and store the bitmap (scaled) to the app cache.
 * The [ThumbnailsRepository] unsubscribe from a [Session] when it gets removed from the
 * [SessionManager].
 *
 * @param context The context used to get the cache directory
 * @param scope The coroutine scope on which the repository performs IO actions (default:
 *              `CoroutineScope(Dispatchers.IO)`)
 */
class ThumbnailsRepository(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : Session.Observer {

    class CancelOnDetach(private val job: Job) : View.OnAttachStateChangeListener {
        override fun onViewDetachedFromWindow(v: View?) = job.cancel()

        override fun onViewAttachedToWindow(v: View?) = Unit
    }

    // This is done like this for testing purposes, we need to be able to replace the observer
    // with one that call a spied repository instance
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    class Observer(private val repository: ThumbnailsRepository) : SessionManager.Observer {
        override fun onSessionAdded(session: Session) {
            registerIfNeeded(session)
        }

        override fun onSessionRemoved(session: Session) {
            session.unregister(repository)
            synchronized(repository.checksumsMap) {
                repository.checksumsMap.remove(session.id)
            }
            repository.removeThumbnail(session.id)
        }

        override fun onAllSessionsRemoved() {
            synchronized(repository.checksumsMap) {
                repository.checksumsMap.clear()
            }
            repository.removeAllThumbnails()
        }

        override fun onSessionSelected(session: Session) {
            registerIfNeeded(session)
        }

        private fun registerIfNeeded(session: Session) {
            synchronized(repository.checksumsMap) {
                repository.checksumsMap[session.id] ?: session.register(repository)
            }
        }
    }

    private val cachePath = context.cacheDir.resolve(THUMBNAILS_FOLDER_NAME).also {
        it.mkdirs()
    }

    private val checksumsMap = mutableMapOf<String, Int>()

    /**
     * Create a [SessionManager.Observer] instance tied to this [ThumbnailsRepository]
     */
    fun asSessionManagerObserver() = Observer(this)

    override fun onThumbnailChanged(session: Session, bitmap: Bitmap?) {
        if (bitmap == null || bitmap.isRecycled) {
            return
        }

        val newChecksum = calculateChecksum(session)
        synchronized(checksumsMap) {
            val oldChecksum = checksumsMap[session.id]
            if (oldChecksum != newChecksum) {
                checksumsMap[session.id] = newChecksum
                storeThumbnail(session.id, bitmap)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun storeThumbnail(id: String, bitmap: Bitmap) = scope.launch {
        val scaledBitmap = bitmap.scale(bitmap.width / 2, bitmap.height / 2)
        getThumbnailFile(cachePath, id).withOutputStream { stream ->
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, DEFAULT_QUALITY, stream)
        }
        scaledBitmap.recycle()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun removeThumbnail(id: String) = scope.launch {
        getThumbnailFile(cachePath, id).delete()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun removeAllThumbnails() = scope.launch {
        cachePath.listFiles().forEach { it.delete() }
    }

    /**
     * Fetch the thumbnail from the disk if it exists
     *
     * @param session the session we want the thumbnail for
     * @return a deferred optional Bitmap
     */
    fun getThumbnailAsync(session: Session) = scope.async {
        try {
            getThumbnailFile(cachePath, session.id).openRead().use {
                BitmapFactory.decodeStream(it)
            }
        } catch (_: FileNotFoundException) {
            null
        }
    }

    /**
     * Load the thumbnail into the given view
     *
     * @param view the [ImageView] in which we set the thumbnail to
     * @param session the [Session] for which we want to load the thumbnail
     * @param placeholder a placeholder to display while loading, default null
     * @param error a drawable to display in case of error, default null
     */
    @MainThread
    fun loadIntoView(
        view: ImageView,
        session: Session,
        placeholder: Drawable? = null,
        error: Drawable? = null
    ): Job = scope.launch(Dispatchers.Main) {
        internalLoadIntoView(WeakReference(view), session, placeholder, error)
    }

    // Inspired by mozilla.components.browser.icons.BrowserIcons
    @WorkerThread
    private suspend fun internalLoadIntoView(
        view: WeakReference<ImageView>,
        session: Session,
        placeholder: Drawable?,
        error: Drawable?
    ) {
        // If we previously started loading into the view, cancel the job.
        val existingJob = view.get()?.getTag(R.id.mozac_browser_thumbnail_tag_job) as? Job
        existingJob?.cancel()

        view.get()?.setImageDrawable(placeholder)

        // Create a loading job
        val deferredIcon = getThumbnailAsync(session)

        view.get()?.setTag(R.id.mozac_browser_thumbnail_tag_job, deferredIcon)
        val onAttachStateChangeListener = CancelOnDetach(deferredIcon).also {
            view.get()?.addOnAttachStateChangeListener(it)
        }

        try {
            deferredIcon.await()?.let {
                view.get()?.setImageBitmap(it)
            }
        } catch (e: CancellationException) {
            view.get()?.setImageDrawable(error)
        } finally {
            view.get()?.removeOnAttachStateChangeListener(onAttachStateChangeListener)
            view.get()?.setTag(R.id.mozac_browser_thumbnail_tag_job, null)
        }
    }

    companion object {
        const val DEFAULT_QUALITY = 42 // It really doesn't matter for PNGs
        const val THUMBNAILS_FOLDER_NAME = "page_thumbnails"
        const val THUMBNAIL_FILE_NAME_FORMAT = "thumbnail_%s.png"
    }
}

// We just use the url hashCode, this should be unique enough
private fun calculateChecksum(session: Session): Int = "${session.id} + ${session.url}".hashCode()

private fun getThumbnailFile(cachePath: File, id: String) =
    AtomicFile(cachePath.resolve(
            ThumbnailsRepository.THUMBNAIL_FILE_NAME_FORMAT.format(
                    Locale.ROOT, id
            )
    ))
