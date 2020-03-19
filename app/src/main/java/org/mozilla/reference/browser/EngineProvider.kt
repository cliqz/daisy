/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import com.cliqz.extension.CliqzExtensionFeature
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.browser.engine.gecko.fetch.GeckoViewFetchClient
import mozilla.components.browser.engine.gecko.glean.GeckoAdapter
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.webcompat.WebCompatFeature
import mozilla.components.lib.crash.handler.CrashHandlerService
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.reference.browser.ext.isCrashReportActive
import java.io.File

object EngineProvider {

    private var runtime: GeckoRuntime? = null
    private var cliqz: CliqzExtensionFeature? = null

    @Synchronized
    private fun getOrCreateRuntime(context: Context): GeckoRuntime {
        if (runtime == null) {
            val builder = GeckoRuntimeSettings.Builder()

            if (isCrashReportActive) {
                builder.crashHandler(CrashHandlerService::class.java)
            }

            // Allow for exfiltrating Gecko metrics through the Glean SDK.
            builder.telemetryDelegate(GeckoAdapter())

            // About config it's no longer enabled by default
            builder.aboutConfigEnabled(true)

            // copy gecko config to cache dir
            val geckoViewConfigPath = "geckoview-config.yaml"
            val configAssets = context.assets.open(geckoViewConfigPath)
            val configFile = File(context.cacheDir, geckoViewConfigPath)
            configFile.createNewFile()
            configAssets.copyTo(configFile.outputStream())
            configAssets.close()
            builder.configFilePath(configFile.absolutePath)

            runtime = GeckoRuntime.create(context, builder.build())
        }

        return runtime!!
    }

    fun createEngine(context: Context, defaultSettings: DefaultSettings): Engine {
        val runtime = getOrCreateRuntime(context)
        createCliqz(context)
        return GeckoEngine(context, defaultSettings, runtime).also {
            WebCompatFeature.install(it)
        }
    }

    fun createCliqz(context: Context): CliqzExtensionFeature {
        if (cliqz == null) {
            cliqz = CliqzExtensionFeature(getOrCreateRuntime(context))
        }
        return cliqz!!
    }

    fun createClient(context: Context): Client {
        val runtime = getOrCreateRuntime(context)
        return GeckoViewFetchClient(context, runtime)
    }
}
