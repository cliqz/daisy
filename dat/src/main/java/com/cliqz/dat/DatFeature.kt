package com.cliqz.dat

import mozilla.components.concept.engine.Engine
import mozilla.components.support.base.log.logger.Logger

/**
 * @author Sam Macbeth
 */
object DatFeature {
    private val logger = Logger("dat")

    private const val DAT_EXTENSION_ID = "dat@cliqz.com"
    private const val DAT_EXTENSION_URL = "resource://android/assets/extensions/dat/"

    fun install(engine: Engine) {
        engine.installWebExtension(DAT_EXTENSION_ID, DAT_EXTENSION_URL,
            onSuccess = {
                logger.debug("Installed Dat extension: ${it.id}")
            },
            onError = { ext, throwable ->
                logger.error("Failed to install Dat extension: $ext", throwable)
            }
        )
    }
}
