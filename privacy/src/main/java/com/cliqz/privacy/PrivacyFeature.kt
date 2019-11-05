package com.cliqz.privacy

import mozilla.components.concept.engine.Engine
import mozilla.components.support.base.log.logger.Logger

/**
 * @author Sam Macbeth
 */
object PrivacyFeature {
    private val logger = Logger("cliqz-privacy")

    private const val PRIVACY_EXTENSION_ID = "privacy@cliqz.com"
    private const val PRIVACY_EXTENSION_URL = "resource://android/assets/extensions/cliqz/"

    fun install(engine: Engine) {
        engine.installWebExtension(PRIVACY_EXTENSION_ID, PRIVACY_EXTENSION_URL,
            onSuccess = {
                logger.debug("Installed Cliqz extension: ${it.id}")
            },
            onError = { ext, throwable ->
                logger.error("Failed to install Cliqz extension: $ext", throwable)
            }
        )
    }
}