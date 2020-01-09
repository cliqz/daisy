/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.cliqz.dat.DatFeature
import com.cliqz.privacy.PrivacyFeature
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.session.storage.SessionStorage
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.storage.sync.PlacesHistoryStorage
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineSession.TrackingProtectionPolicy
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.addons.AddonManager
import mozilla.components.feature.addons.amo.AddonCollectionProvider
import mozilla.components.feature.addons.update.AddonUpdater
import mozilla.components.feature.addons.update.DefaultAddonUpdater
import mozilla.components.feature.downloads.DownloadsUseCases
import mozilla.components.feature.media.MediaFeature
import mozilla.components.feature.media.RecordingDevicesNotificationFeature
import mozilla.components.feature.media.state.MediaStateMachine
import mozilla.components.feature.session.HistoryDelegate
import mozilla.components.feature.webnotifications.WebNotificationFeature
import org.mozilla.reference.browser.AppRequestInterceptor
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.EngineProvider
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.R.string.pref_key_remote_debugging
import org.mozilla.reference.browser.R.string.pref_key_tracking_protection_normal
import org.mozilla.reference.browser.R.string.pref_key_tracking_protection_private
import org.mozilla.reference.browser.ext.getPreferenceKey
import java.util.concurrent.TimeUnit

private const val DAY_IN_MINUTES = 24 * 60L

/**
 * Component group for all core browser functionality.
 */
class Core(private val context: Context) {
    /**
     * The browser engine component initialized based on the build
     * configuration (see build variants).
     */
    val engine: Engine by lazy {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val defaultSettings = DefaultSettings(
            requestInterceptor = AppRequestInterceptor(context),
            remoteDebuggingEnabled = prefs.getBoolean(context.getPreferenceKey(pref_key_remote_debugging), false),
            testingModeEnabled = prefs.getBoolean(context.getPreferenceKey(R.string.pref_key_testing_mode), false),
            trackingProtectionPolicy = createTrackingProtectionPolicy(prefs),
            historyTrackingDelegate = HistoryDelegate(historyStorage)
        )
        val engine = EngineProvider.createEngine(context, defaultSettings)
        DatFeature.install(engine)
        PrivacyFeature.install(engine)
        engine
    }
    /**
     * The [Client] implementation (`concept-fetch`) used for HTTP requests.
     */
    val client: Client by lazy {
        EngineProvider.createClient(context)
    }

    /**
     * The [BrowserStore] holds the global [BrowserState].
     */
    val store by lazy {
        BrowserStore()
    }

    /**
     * The session manager component provides access to a centralized registry of
     * all browser sessions (i.e. tabs). It is initialized here to persist and restore
     * sessions from the [SessionStorage], and with a default session (about:blank) in
     * case all sessions/tabs are closed.
     */
    val sessionManager by lazy {
        val sessionStorage = SessionStorage(context, engine)

        SessionManager(engine, store).apply {
            sessionStorage.restore()?.let { snapshot -> restore(snapshot) }

            if (size == 0) {
                add(Session(""), selected = true)
            }
            sessionStorage.autoSave(this)
                .periodicallyInForeground(interval = 30, unit = TimeUnit.SECONDS)
                .whenGoingToBackground()
                .whenSessionsChange()

            // Install the "icons" WebExtension to automatically load icons for every visited website.
            icons.install(engine, sessionManager = this)

            // Show an ongoing notification when recording devices (camera, microphone) are used by web content
            RecordingDevicesNotificationFeature(context, sessionManager = this)
                .enable()

            MediaStateMachine.start(this)

            // Enable media features like showing an ongoing notification with media controls when
            // media in web content is playing.
            MediaFeature(context).enable()

            WebNotificationFeature(context, engine, icons, R.drawable.ic_notification,
                BrowserActivity::class.java)
        }
    }

    /**
     * Contains use cases related to the downloads feature.
     */
    val downloadsUseCases: DownloadsUseCases by lazy { DownloadsUseCases(store) }

    /**
     * The storage component to persist browsing history (with the exception of
     * private sessions).
     */
    val historyStorage by lazy { PlacesHistoryStorage(context) }

    /**
     * Icons component for loading, caching and processing website icons.
     */
    val icons by lazy { BrowserIcons(context, client) }

    // Addons
    val addonManager by lazy {
        val addonUpdater = DefaultAddonUpdater(context, AddonUpdater.Frequency(1, TimeUnit.DAYS))
        AddonManager(store, engine, addonCollectionProvider, addonUpdater)
    }

    val addonCollectionProvider by lazy {
        AddonCollectionProvider(
            context = context,
            client = client,
            maxCacheAgeInMinutes = DAY_IN_MINUTES
        )
    }

    /**
     * Constructs a [TrackingProtectionPolicy] based on current preferences.
     *
     * @param prefs the shared preferences to use when reading tracking
     * protection settings.
     * @param normalMode whether or not tracking protection should be enabled
     * in normal browsing mode, defaults to the current preference value.
     * @param privateMode whether or not tracking protection should be enabled
     * in private browsing mode, default to the current preference value.
     * @return the constructed tracking protection policy based on preferences.
     */
    fun createTrackingProtectionPolicy(
        prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context),
        normalMode: Boolean = prefs.getBoolean(context.getPreferenceKey(pref_key_tracking_protection_normal), true),
        privateMode: Boolean = prefs.getBoolean(context.getPreferenceKey(pref_key_tracking_protection_private), true)
    ): TrackingProtectionPolicy {

        val trackingPolicy = TrackingProtectionPolicy.recommended()
        return when {
            normalMode && privateMode -> trackingPolicy
            normalMode && !privateMode -> trackingPolicy.forRegularSessionsOnly()
            !normalMode && privateMode -> trackingPolicy.forPrivateSessionsOnly()
            else -> TrackingProtectionPolicy.none()
        }
    }
}
