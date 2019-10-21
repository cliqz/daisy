package com.cliqz.browser.freshtab

import android.view.View
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.toolbar.Toolbar
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class FreshTabFeatureTest {

    /**
     * Derived from AwesomeBarFeatureTest
     */
    @Test
    fun `Feature connects Toolbar and EngineView with FreshTab`() = runBlockingTest {
        val toolbar: Toolbar = mockk()
        val freshTab: FreshTab = mockk()
        val engineView: EngineView = mockk()

        var urlCommitListener: ((String) -> Boolean)? = null
        every { toolbar.setOnUrlCommitListener(any()) } answers {
            @Suppress("UNCHECKED_CAST")
            urlCommitListener = args[0] as ((String) -> Boolean)
        }

        var editListener: Toolbar.OnEditListener? = null
        every { toolbar.setOnEditListener(any()) } answers {
            editListener = args[0] as Toolbar.OnEditListener
        }

        var freshTabVisibility: Int? = null
        every { freshTab.visibility  = any() } answers {
            freshTabVisibility = args[0] as Int
        }

        var engineViewVisibility: Int? = null
        every { engineView.asView().visibility  = any() } answers {
            engineViewVisibility = args[0] as Int
        }

        FreshTabFeature(toolbar, freshTab, engineView)

        assertNotNull(urlCommitListener)

        assertNotNull(editListener)

        editListener!!.onStartEditing()

        assertTrue(freshTabVisibility == View.GONE)
        assertTrue(engineViewVisibility == View.VISIBLE)

        editListener!!.onCancelEditing()

        assertTrue(freshTabVisibility == View.VISIBLE)
        assertTrue(engineViewVisibility == View.GONE)
    }
}
