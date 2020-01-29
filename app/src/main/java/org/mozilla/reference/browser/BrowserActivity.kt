/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import androidx.fragment.app.Fragment
import mozilla.components.browser.session.Session
import mozilla.components.browser.tabstray.BrowserTabsTray
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.tabstray.TabsTray
import mozilla.components.feature.intent.ext.EXTRA_SESSION_ID
import mozilla.components.lib.crash.Crash
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.utils.SafeIntent
import org.mozilla.reference.browser.R.color.navigationBarColor
import org.mozilla.reference.browser.R.color.statusBarColor
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.browser.CrashIntegration
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.isCrashReportActive
import org.mozilla.reference.browser.ext.setSystemBarsTheme
import org.mozilla.reference.browser.tabs.TabsTouchHelper

/**
 * Activity that holds the [BrowserFragment].
 */
open class BrowserActivity : AppCompatActivity() {

    private lateinit var crashIntegration: CrashIntegration

    private val sessionId: String?
        get() = SafeIntent(intent).getStringExtra(EXTRA_SESSION_ID)

    /**
     * Returns a new instance of [BrowserFragment] to display.
     */
    open fun createBrowserFragment(sessionId: String?, openToSearch: Boolean): Fragment =
        BrowserFragment.create(sessionId, openToSearch)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSystemBarsTheme(statusBarColor, navigationBarColor)

        if (savedInstanceState == null) {
            val openToSearch = components.utils.startSearchIntentProcessor.process(intent)
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.container, createBrowserFragment(sessionId, openToSearch))
                commit()
            }
        }

        if (isCrashReportActive) {
            crashIntegration = CrashIntegration(this, components.analytics.crashReporter) { crash ->
                onNonFatalCrash(crash)
            }
            lifecycle.addObserver(crashIntegration)
        }

        NotificationManager.checkAndNotifyPolicy(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent ?: return

        if (components.utils.startSearchIntentProcessor.process(intent)) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.container, createBrowserFragment(null, openToSearch = true))
                commit()
            }
        } else if (components.utils.tabIntentProcessor.matches(intent)) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.container, createBrowserFragment(null, openToSearch = false))
                commit()
            }
        }
    }

    override fun onBackPressed() {
        supportFragmentManager.fragments.forEach {
            if (it is UserInteractionHandler && it.onBackPressed()) {
                return
            }
        }

        super.onBackPressed()

        removeSessionIfNeeded()
    }

    /**
     * If needed remove the current session.
     *
     * If a session is a custom tab or was opened from an external app then the session gets removed once you go back
     * to the third-party app.
     *
     * Eventually we may want to move this functionality into one of our feature components.
     */
    private fun removeSessionIfNeeded() {
        val sessionManager = components.core.sessionManager
        val sessionId = sessionId

        val session = (if (sessionId != null) {
            sessionManager.findSessionById(sessionId)
        } else {
            sessionManager.selectedSession
        }) ?: return

        if (session.source == Session.Source.ACTION_VIEW || session.source == Session.Source.CUSTOM_TAB) {
            sessionManager.remove(session)
        }
    }

    override fun onUserLeaveHint() {
        supportFragmentManager.fragments.forEach {
            if (it is UserInteractionHandler && it.onHomePressed()) {
                return
            }
        }

        super.onUserLeaveHint()
    }

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? =
        when (name) {
            EngineView::class.java.name -> components.core.engine.createView(context, attrs).asView()
            TabsTray::class.java.name -> {
                BrowserTabsTray(context, attrs).also { tray ->
                    TabsTouchHelper(tray.tabsAdapter).attachToRecyclerView(tray)
                }
            }
            else -> super.onCreateView(parent, name, context, attrs)
        }

    private fun onNonFatalCrash(crash: Crash) {
        Snackbar.make(findViewById(android.R.id.content), R.string.crash_report_non_fatal_message, LENGTH_LONG)
            .setAction(R.string.crash_report_non_fatal_action) {
                crashIntegration.sendCrashReport(crash)
            }.show()
    }

    companion object {
        const val EXTRA_OPEN_TO_SEARCH = "OPEN_TO_SEARCH"
    }
}
