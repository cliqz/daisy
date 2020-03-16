package com.cliqz.extension

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.mozilla.reference.browser.components.Core

/**
 * @author Sam Macbeth
 */
class CliqzExtensionTest {

    lateinit var core: Core

    @BeforeClass
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        core = Core(context)
    }

    @Test
    fun getModuleStatus() {
        runBlocking {
            val status = core.cliqz.getModuleStatus()
            // check modules are enabled by default
            assert(status!!.adblockerEnabled)
            assert(status.antitrackingEnabled)
            assert(status.anolysisEnabled)
            assert(status.humanwebEnabled)
            assert(status.insightsEnabled)
        }
    }
}