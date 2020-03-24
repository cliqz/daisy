/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.cliqz.extension

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mozilla.reference.browser.Components
import org.mozilla.reference.browser.components.Core
import org.mozilla.reference.browser.ext.application

@ExperimentalCoroutinesApi
class CliqzExtensionTest {

    lateinit var components: Components
    lateinit var core: Core

    @Before
    fun setUp() {
        components = InstrumentationRegistry.getInstrumentation().targetContext.application.components
        core = components.core
    }

    @Test
    fun getModuleStatus() = runBlockingTest {
        val api = CliqzAPI(core.cliqz, this)
        val status = api.getModuleStatus()
        // check modules are enabled by default
        assertNotNull("Status should not be null", status)
        assertTrue("Adblocker should be enabled", status!!.adblockerEnabled)
        assertTrue("Antitracking should be enabled", status.antitrackingEnabled)
        assertTrue("Anolysis should be enabled", status.anolysisEnabled)
        assertTrue("Humanweb should be enabled", status.humanwebEnabled)
        assertTrue("Insights should be enabled", status.insightsEnabled)
    }

    @Test
    fun moduleEnableDisable() = runBlockingTest {
        val api = CliqzAPI(core.cliqz, this)
        api.setModuleEnabled(ANTITRACKING_MODULE, false)
        var status = api.getModuleStatus()
        assertFalse("antitracking should be disabled", status!!.antitrackingEnabled)
        api.setModuleEnabled(ANTITRACKING_MODULE, true)
        status = api.getModuleStatus()
        assertTrue("antitracking should be enabled", status!!.antitrackingEnabled)
    }

    @Test
    fun getBlockingStats() = runBlockingTest {
        val api = CliqzAPI(core.cliqz, this)
        val stats = api.getBlockingStats(StatsPeriod.ALL_TIME)
        assertNotNull("Stats is not null", stats)
        assertEquals("Initially no pages", 0, stats!!.pages)
    }
}
