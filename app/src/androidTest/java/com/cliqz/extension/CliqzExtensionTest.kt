package com.cliqz.extension

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mozilla.reference.browser.components.Core

/**
 * @author Sam Macbeth
 */
class CliqzExtensionTest {

    lateinit var core: Core

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        core = Core(context)
    }

    @After
    fun tearDown() {
        core.cliqz.runtime.shutdown()
    }

    @Test
    fun getModuleStatus() = runBlockingTest {
        val api = CliqzAPI(core.cliqz, this)
        val status = api.getModuleStatus()
        // check modules are enabled by default
        Assert.assertNotNull("Status should not be null", status)
        Assert.assertTrue("Adblocker should be enabled", status!!.adblockerEnabled)
        Assert.assertTrue("Antitracking should be enabled", status.antitrackingEnabled)
        Assert.assertTrue("Anolysis should be enabled", status.anolysisEnabled)
        Assert.assertTrue("Humanweb should be enabled", status.humanwebEnabled)
        Assert.assertTrue("Insights should be enabled", status.insightsEnabled)
    }

    @Test
    fun moduleEnableDisable() = runBlockingTest {
        val api = CliqzAPI(core.cliqz, this)
        api.setModuleEnabled(api.ANTITRACKING_MODULE, false)
        var status = api.getModuleStatus()
        Assert.assertFalse("antitracking should be disabled", status!!.antitrackingEnabled)
        api.setModuleEnabled(api.ANTITRACKING_MODULE, true)
        status = api.getModuleStatus()
        Assert.assertTrue("antitracking should be enabled", status!!.antitrackingEnabled)
    }

    @Test
    fun getBlockingStats() = runBlockingTest {
        val api = CliqzAPI(core.cliqz, this)
        var stats = api.getBlockingStats(StatsPeriod.ALL_TIME)
        Assert.assertNotNull("Stats is not null", stats)
        Assert.assertEquals("Initially no pages", stats!!.pages, 0)
    }

}