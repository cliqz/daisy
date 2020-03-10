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

/**
 * @author Sam Macbeth
 */
class CliqzExtensionFeature {
    private val logger = Logger("cliqz-privacy")

    private val APP_NAME = "cliqz"
    private val PRIVACY_EXTENSION_ID = "privacy@cliqz.com"
    private val PRIVACY_EXTENSION_URL = "resource://android/assets/extensions/cliqz/"

    private val extensionController = WebExtensionController(PRIVACY_EXTENSION_ID, PRIVACY_EXTENSION_URL)
    private val messageHandler = CliqzBackgroundMessageHandler(this)

    fun install(engine: Engine) {
        extensionController.registerBackgroundMessageHandler(messageHandler, APP_NAME)
        extensionController.install(engine)
    }

    fun callActionSync(module: String, action: String, vararg args: Any): JSONObject? {
        return messageHandler.callAction(module, action, args)
    }

    fun callActionAsync(module: String, action: String, vararg args: Any): Deferred<JSONObject?> {
        return GlobalScope.async {
            callActionSync(module, action, args)
        }
    }

    private class CliqzBackgroundMessageHandler(private val parent: CliqzExtensionFeature): MessageHandler {

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


        fun callAction(module: String, action: String, vararg args: Any): JSONObject? {
            val message = JSONObject()
            val latch = CountDownLatch(1)
            var messageId: Int
            synchronized(this) {
                messageId = messageCtr++
                message.put("id", messageId)
                message.put("module", module)
                message.put("action", action)
                message.put("args", JSONArray())
                awaitingResults.put(messageId, latch)
            }
            // wait for port connection with extension to be ready
            ready.await()

            // send message to extension
            parent.logger.debug("Send extension message: " + message.toString())
            parent.extensionController.sendBackgroundMessage(message, parent.APP_NAME)

            latch.await()

            val result = results.get(messageId)
            results.remove(messageId)
            if (result != null && result.has("error")) {
                throw RuntimeException("Action threw an error: " + result.get("error"))
            } else if (result != null) {
                return result.getJSONObject("result")
            }
            return null
        }
    }

}
