/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import mozilla.components.browser.session.Session
import mozilla.components.support.test.any
import mozilla.components.support.test.mock
import mozilla.components.support.test.robolectric.testContext
import mozilla.components.support.test.whenever
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import java.io.File
import java.util.Locale

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ThumbnailsRepositoryTest {

    private lateinit var repository: ThumbnailsRepository
    private lateinit var thumbnailsDir: File
    private val scope = TestCoroutineScope()
    private lateinit var testThumbnailPath: File

    @Before
    fun setUp() {
        thumbnailsDir = testContext.cacheDir.resolve(ThumbnailsRepository.THUMBNAILS_FOLDER_NAME)
        testThumbnailPath = thumbnailsDir.resolve(
                ThumbnailsRepository.THUMBNAIL_FILE_NAME_FORMAT.format(Locale.ROOT, DEFAULT_SESSION_ID)
        )
        repository = spy(ThumbnailsRepository(testContext, scope))
        val observer = ThumbnailsRepository.Observer(repository)
        whenever(repository.asSessionManagerObserver()).thenReturn(observer)
    }

    @After
    fun tearDown() {
        assertTrue("The thumbnails folder must be removed", thumbnailsDir.deleteRecursively())
    }

    @Test
    fun `should create the thumbnails cache directory`() {
        assertTrue(thumbnailsDir.isDirectory)
    }

    @Test
    fun `storeThumbnail should be called`() {
        val session = Session(DEFAULT_URL)
        repository.asSessionManagerObserver().onSessionAdded(session)
        session.thumbnail = THUMBNAIL

        verify(repository).onThumbnailChanged(session, THUMBNAIL)
        verify(repository).storeThumbnail(session.id, THUMBNAIL)
    }

    @Test
    fun `a thumbnail should be stored`() = runBlockingTest {
        repository.storeThumbnail(DEFAULT_SESSION_ID, THUMBNAIL).join()
        assertTrue("A thumbnail for session $DEFAULT_SESSION_ID should exists", testThumbnailPath.isFile)
    }

    @Test
    fun `removeThumbnail should be called`() {
        val session = Session(DEFAULT_URL)
        repository.asSessionManagerObserver().onSessionRemoved(session)
        verify(repository).removeThumbnail(session.id)
    }

    @Test
    fun `a thumbnail should be removed`() {
        createTestThumbnail()
        runBlockingTest {
            repository.removeThumbnail(DEFAULT_SESSION_ID).join()
            assertFalse("The thumbnail file should have been deleted", testThumbnailPath.exists())
        }
    }

    @Test
    fun `removeAllThumbnails should be called`() {
        repository.asSessionManagerObserver().onAllSessionsRemoved()
        verify(repository).removeAllThumbnails()
    }

    @Test
    fun `every thumbnail should be removed when all the sessions are removed`() {
        createTestThumbnail()
        runBlockingTest {
            repository.removeAllThumbnails().join()
            assertTrue("The thumbnails cache should be empty", thumbnailsDir.listFiles().isEmpty())
        }
    }

    @Test
    fun `storeThumbnail should be called only once if the session didn't change`() {
        val session = Session(DEFAULT_URL)
        repository.asSessionManagerObserver().onSessionAdded(session)

        session.thumbnail = THUMBNAIL
        session.thumbnail = THUMBNAIL

        verify(repository, times(1)).storeThumbnail(session.id, THUMBNAIL)
    }

    @Test
    fun `storeThumbnail should be called twice if the session changed`() {
        val session = Session(DEFAULT_URL)
        repository.asSessionManagerObserver().onSessionAdded(session)

        session.thumbnail = THUMBNAIL
        session.url = "https://cliqz.com"
        session.thumbnail = THUMBNAIL

        verify(repository, times(2)).storeThumbnail(session.id, THUMBNAIL)
    }

    @Test
    fun `getThumbnailOrNull should return null is there is no thumbnail`() = runBlockingTest {
        val thumbnail = repository.getThumbnailAsync(DEFAULT_SESSION_ID).await()
        assertNull("The thumbnail should be null", thumbnail)
    }

    @Test
    fun `getThumbnailOrNull should return a thumbnail for a persisted session`() = runBlockingTest {
        repository.storeThumbnail(DEFAULT_SESSION_ID, THUMBNAIL).join()
        val thumbnail = repository.getThumbnailAsync(DEFAULT_SESSION_ID)
        assertNotNull("The thumbnail should not be null", thumbnail)
    }

    @Test
    fun `loadIntoView should set the thumbnail to the given ImageView`() = runBlockingTest {
        repository.storeThumbnail(DEFAULT_SESSION_ID, THUMBNAIL).join()
        val view: ImageView = mock()
        repository.loadIntoView(view, DEFAULT_SESSION_ID)
        verify(view).setImageBitmap(any())
    }

    private fun createTestThumbnail() =
            assertTrue("A file should have been created", testThumbnailPath.createNewFile())

    companion object {
        const val DEFAULT_SESSION_ID = "abc"
        const val DEFAULT_URL = "https://example.org"
        val THUMBNAIL: Bitmap = ThumbnailsRepository::class.java.getResourceAsStream("/thumbnail.jpg")!!
                .use { BitmapFactory.decodeStream(it) }
    }
}