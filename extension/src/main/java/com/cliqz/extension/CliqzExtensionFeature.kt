/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.cliqz.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
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

val demographics = mapOf(
        "brand" to "cliqz",
        "name" to "browser",
        "platform" to "android"
)

class CliqzExtensionFeature(val runtime: GeckoRuntime) {
    private val logger = Logger("cliqz-extension")

    private val appName = "cliqz"
    private val privacyExtensionID = "cliqz@cliqz.com"
    private val privacyExtensionUrl = "resource://android/assets/extensions/cliqz/"

    private var extension = GeckoWebExtension(privacyExtensionID, privacyExtensionUrl,
            runtime.webExtensionController, allowContentMessaging = true)
    private val messageHandler = CliqzBackgroundMessageHandler(this)

    val extensionConfig: JSONObject by lazy {
        val settings = JSONObject(mapOf(
                "telemetry" to mapOf(
                        "demographics" to demographics
                ),
                "ADBLOCKER_PLATFORM" to "desktop"
        ))
        val prefs = JSONObject(mapOf(
                "showConsoleLogs" to BuildConfig.DEBUG
        ))
        JSONObject(mapOf(
                "settings" to settings,
                "prefs" to prefs
        ))
    }

    init {
        extension.registerBackgroundMessageHandler(appName, messageHandler)
        runtime.registerWebExtension(extension.nativeExtension)
    }

    fun callActionAsync(scope: CoroutineScope, module: String, action: String, vararg args: Any?): Deferred<Any?> {
        return scope.async {
            messageHandler.callAction(module, action, *args)
        }
    }

    private class CliqzBackgroundMessageHandler(private val parent: CliqzExtensionFeature) : MessageHandler {

        var messageCtr = 0
        var ready = CountDownLatch(1)
        val awaitingResults = ConcurrentHashMap<Int, CountDownLatch>()
        val results = ConcurrentHashMap<Int, JSONObject>()
        var port: Port? = null

        override fun onMessage(message: Any, source: EngineSession?): Any? {
            parent.logger.debug("message from extension:$message")
            if (message == "ready") {
                parent.logger.debug("extension is ready")
                ready.countDown()
            }
            return null
        }

        override fun onPortMessage(message: Any, port: Port) {
            parent.logger.debug("message on port:$message")
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
            val message = JSONObject(mapOf(
                    "action" to "startApp",
                    "debug" to BuildConfig.DEBUG,
                    "config" to parent.extensionConfig
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
            parent.logger.debug("Send extension message: $message")
            port!!.postMessage(message)

            latch.await()

            val result = results[messageId]
            results.remove(messageId)
            if (result != null && result.has("error")) {
                throw ActionError(result.get("error").toString())
            } else if (result != null) {
                return result.get("result")
            }
            return null
        }
    }

    class ActionError(error: String) : java.lang.RuntimeException(error)
}
