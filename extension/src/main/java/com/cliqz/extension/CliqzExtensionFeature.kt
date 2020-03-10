package com.cliqz.extension

import kotlinx.coroutines.*
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.webextension.MessageHandler
import mozilla.components.concept.engine.webextension.Port
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.webextensions.WebExtensionController
import org.json.JSONArray
import org.json.JSONObject
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

/**
 * @author Sam Macbeth
 */
class CliqzExtensionFeature {
    private val logger = Logger("cliqz-privacy")

    private val appName = "cliqz"
    private val privacyExtensionID = "privacy@cliqz.com"
    private val privacyExtensionUrl = "resource://android/assets/extensions/cliqz/"

    private val extensionController = WebExtensionController(privacyExtensionID, privacyExtensionUrl)
    private val messageHandler = CliqzBackgroundMessageHandler(this)

    fun install(engine: Engine) {
        extensionController.registerBackgroundMessageHandler(messageHandler, appName)
        extensionController.install(engine)
    }

    private fun callActionSync(module: String, action: String, vararg args: Any?): Any? {
        return messageHandler.callAction(module, action, *args)
    }

    fun callActionAsync(module: String, action: String, vararg args: Any?): Deferred<Any?> {
        return GlobalScope.async {
            callActionSync(module, action, *args)
        }
    }

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


    suspend fun setModuleEnabled(module: String, enabled: Boolean) {
        if (enabled) {
            callActionAsync("core", "enableModule", module).await()
        }
    }

    private class CliqzBackgroundMessageHandler(private val parent: CliqzExtensionFeature) : MessageHandler {

        var messageCtr = 0
        var ready = CountDownLatch(1)
        val awaitingResults = ConcurrentHashMap<Int, CountDownLatch>()
        val results = ConcurrentHashMap<Int, JSONObject>()


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
            parent.extensionController.sendBackgroundMessage(message, parent.appName)

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
