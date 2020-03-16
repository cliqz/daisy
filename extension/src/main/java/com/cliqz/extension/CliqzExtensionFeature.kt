package com.cliqz.extension

import kotlinx.coroutines.*
import mozilla.components.browser.engine.gecko.webextension.GeckoWebExtension
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.webextension.MessageHandler
import mozilla.components.concept.engine.webextension.Port
import mozilla.components.support.base.log.logger.Logger
import org.json.JSONArray
import org.json.JSONObject
import org.mozilla.geckoview.GeckoRuntime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch

data class ModuleStatus(
        val adblockerEnabled: Boolean,
        val anolysisEnabled: Boolean,
        val antitrackingEnabled: Boolean,
        val autoconsentEnabled: Boolean,
        val cookieMonsterEnabled: Boolean,
        val humanwebEnabled: Boolean,
        val insightsEnabled: Boolean
)

enum class StatsPeriod(val period: String) {
    ALL_TIME(""),
    DAY("day"),
    WEEK("week"),
    MONTH("month")
}

data class TrackerInfo(
        val name: String,
        val category: String,
        val wtm: String
)

data class BlockingStats(
        val adsBlocked: Int,
        val cookiesBlocked: Int,
        val dataSaved: Int,
        val day: String,
        val fingerprintsRemoved: Int,
        val loadTime: Int,
        val pages: Int,
        val timeSaved: Int,
        val trackers: List<String>,
        val trackersDetailed: List<TrackerInfo>,
        val trackersDetected: Int
)

/**
 * @author Sam Macbeth
 */
class CliqzExtensionFeature(runtime: GeckoRuntime) {
    private val logger = Logger("cliqz-extension")

    private val appName = "cliqz"
    private val privacyExtensionID = "cliqz@cliqz.com"
    private val privacyExtensionUrl = "resource://android/assets/extensions/cliqz/"

    private var extension = GeckoWebExtension(privacyExtensionID, privacyExtensionUrl, runtime.webExtensionController, allowContentMessaging = true)
    private val messageHandler = CliqzBackgroundMessageHandler(this)

    init {
        extension.registerBackgroundMessageHandler(appName, messageHandler)
        runtime.registerWebExtension(extension.nativeExtension)
    }

    private fun callActionSync(module: String, action: String, vararg args: Any?): Any? {
        return messageHandler.callAction(module, action, *args)
    }

    fun callActionAsync(module: String, action: String, vararg args: Any?): Deferred<Any?> {
        return GlobalScope.async {
            callActionSync(module, action, *args)
        }
    }

    /**
     * Gets the current enabled status of Cliqz modules running in the extension.
     */
    suspend fun getModuleStatus(): ModuleStatus? {
        val moduleStatus = callActionAsync("core", "status").await().let { (it as JSONObject).getJSONObject("modules") }

        fun isModuleEnabled(module: String) = moduleStatus.has(module) && moduleStatus.getJSONObject(module).getBoolean("isEnabled")

        return ModuleStatus(
                isModuleEnabled("adblocker"),
                isModuleEnabled("anolysis"),
                isModuleEnabled("antitracking"),
                isModuleEnabled("autoconsent"),
                isModuleEnabled("cookie-monster"),
                isModuleEnabled("human-web-lite"),
                isModuleEnabled("insights")
        )
    }


    /**
     * Set the enabled status of an extension module.
     */
    suspend fun setModuleEnabled(module: String, enabled: Boolean) {
        if (enabled) {
            callActionAsync("core", "enableModule", module).await()
        }
    }

    /**
     * Get aggregated privacy stats over the given {StatsPeriod}.
     */
    suspend fun getBlockingStats(period: StatsPeriod): BlockingStats? {
        return callActionAsync("insights", "getDashboardStats", period.period).await().let { (it as JSONObject) }.let { res ->
            BlockingStats(
                    res.getInt("adsBlocked"),
                    res.getInt("cookiesBlocked"),
                    res.getInt("dataSaved"),
                    res.getString("day"),
                    res.getInt("fingerprintsRemoved"),
                    res.getInt("loadTime"),
                    res.getInt("pages"),
                    res.getInt("timeSaved"),
                    res.getJSONArray("trackers").let {
                        List(it.length()) { i -> it.getString(i) }
                    },
                    res.getJSONArray("trackersDetailed").let {
                        List(it.length()) { i ->
                            val tracker = it.getJSONObject(i)
                            TrackerInfo(tracker.getString("name"), tracker.getString("cat"), tracker.getString("wtm"))
                        }
                    },
                    res.getInt("trackersDetected")
            )
        }
    }

    private class CliqzBackgroundMessageHandler(private val parent: CliqzExtensionFeature) : MessageHandler {

        var messageCtr = 0
        var ready = CountDownLatch(1)
        val awaitingResults = ConcurrentHashMap<Int, CountDownLatch>()
        val results = ConcurrentHashMap<Int, JSONObject>()
        var port: Port? = null

        override fun onMessage(message: Any, source: EngineSession?): Any? {
            parent.logger.debug("message from extension:" + message.toString())
            if (message == "ready") {
                parent.logger.debug("extension is ready")
                ready.countDown()
            }
            return null
        }

        override fun onPortMessage(message: Any, port: Port) {
            parent.logger.debug("message on port:" + message.toString())
            if (message is JSONObject) {
                val id = message.getInt("id")
                results[id] = message
                awaitingResults[id]?.countDown()
                awaitingResults.remove(id)
            }
            super.onPortMessage(message, port)
        }

        override fun onPortConnected(port: Port) {
            parent.logger.debug("port was connected")
            this.port = port
            val config = JSONObject("""{
                "settings": {
                    "channel": "MA60",
                    "telemetry": {
                        "demographics": {
                            "brand": "cliqz",
                            "name": "browser",
                            "platform": "android"
                        }
                    }
                },
                "prefs": {
                    "showConsoleLogs": true
                }
            }""".trimMargin())
            val message = JSONObject(mapOf(
                    "action" to "startApp",
                    "debug" to BuildConfig.DEBUG,
                    "config" to config
            ))
            port.postMessage(message)
        }

        override fun onPortDisconnected(port: Port) {
            this.port = null
        }

        fun callAction(module: String, action: String, vararg args: Any?): Any? {
            val message = JSONObject()
            val latch = CountDownLatch(1)
            var messageId: Int
            synchronized(this) {
                messageId = messageCtr++
                message.put("id", messageId)
                message.put("module", module)
                message.put("action", action)
                message.put("args", JSONArray(args))
                awaitingResults.put(messageId, latch)
            }
            // wait for port connection with extension to be ready
            ready.await()

            // send message to extension
            parent.logger.debug("Send extension message: " + message.toString())
            port!!.postMessage(message)

            latch.await()

            val result = results.get(messageId)
            results.remove(messageId)
            if (result != null && result.has("error")) {
                throw RuntimeException("Action threw an error: " + result.get("error"))
            } else if (result != null) {
                return result.get("result")
            }
            return null
        }
    }

}
