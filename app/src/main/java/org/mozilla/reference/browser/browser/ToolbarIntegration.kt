/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleCoroutineScope
import com.cliqz.browser.freshtab.isFreshTab
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mozilla.components.browser.domains.autocomplete.ShippedDomainsProvider
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.BrowserMenuItem
import mozilla.components.browser.menu.item.BrowserMenuItemToolbar
import mozilla.components.browser.menu.item.BrowserMenuSwitch
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.browser.toolbar.display.DisplayToolbar
import mozilla.components.concept.storage.HistoryStorage
import mozilla.components.feature.pwa.WebAppUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.feature.toolbar.ToolbarAutocompleteFeature
import mozilla.components.feature.toolbar.ToolbarFeature
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.addons.AddonsActivity
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.share
import org.mozilla.reference.browser.library.history.ui.HistoryFragment
import org.mozilla.reference.browser.settings.SettingsActivity
import org.mozilla.reference.browser.settings.deletebrowsingdata.DeleteBrowsingData

class ToolbarIntegration(
    context: Context,
    toolbar: BrowserToolbar,
    coroutineScope: LifecycleCoroutineScope,
    private val historyStorage: HistoryStorage,
    sessionManager: SessionManager,
    sessionUseCases: SessionUseCases,
    tabsUseCases: TabsUseCases,
    webAppUseCases: WebAppUseCases,
    sessionId: String? = null,
    private val fragmentManager: FragmentManager?,
    toolbarEditMode: Boolean = false
) : LifecycleAwareFeature, UserInteractionHandler {

    private val shippedDomainsProvider = ShippedDomainsProvider().also {
        it.initialize(context)
    }

    private val menuToolbar by lazy {
        val forward = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_forward,
            iconTintColorResource = R.color.icons,
            contentDescription = "Forward",
            isEnabled = { sessionManager.selectedSession?.canGoForward == true }) {
            sessionUseCases.goForward.invoke()
        }

        val refresh = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_refresh,
            iconTintColorResource = R.color.icons,
            contentDescription = "Refresh") {
            sessionUseCases.reload.invoke()
        }

        val stop = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_stop,
            iconTintColorResource = R.color.icons,
            contentDescription = "Stop") {
            sessionUseCases.stopLoading.invoke()
        }

        BrowserMenuItemToolbar(listOf(forward, refresh, stop))
    }

    private val menuItems: List<BrowserMenuItem> by lazy {
        val hasSessionAndUrl = {
            sessionManager.selectedSession != null &&
                    !sessionManager.selectedSession!!.url.isFreshTab()
        }
        listOf(
            menuToolbar,
            SimpleBrowserMenuItem("New Tab") {
                tabsUseCases.addTab.invoke("")
            },
            SimpleBrowserMenuItem(context.getString(R.string.menu_item_forget_tab)) {
                tabsUseCases.addPrivateTab.invoke("about:privatebrowsing", selectTab = true)
            },
            SimpleBrowserMenuItem("Share") {
                val url = sessionManager.selectedSession?.url ?: ""
                context.share(url)
            }.apply {
                visible = hasSessionAndUrl
            },
            BrowserMenuSwitch("Request desktop site", {
                sessionManager.selectedSessionOrThrow.desktopMode
            }) { checked ->
                sessionUseCases.requestDesktopSite.invoke(checked)
            }.apply {
                visible = hasSessionAndUrl
            },

            SimpleBrowserMenuItem("Add to homescreen") {
                MainScope().launch { webAppUseCases.addToHomescreen() }
            }.apply {
                visible = {
                    webAppUseCases.isPinningSupported() && hasSessionAndUrl()
                }
            },

            SimpleBrowserMenuItem("Find in Page") {
                FindInPageIntegration.launch?.invoke()
            }.apply {
                visible = hasSessionAndUrl
            },

            SimpleBrowserMenuItem("Add-ons") {
                val intent = Intent(context, AddonsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            },

            SimpleBrowserMenuItem("Report issue") {
                tabsUseCases.addTab.invoke(
                    "https://github.com/mozilla-mobile/reference-browser/issues/new")
            },

            SimpleBrowserMenuItem("Settings") {
                openSettingsActivity(context)
            },

            SimpleBrowserMenuItem(context.getString(R.string.menu_item_history)) {
                openHistoryFragment()
            },

            SimpleBrowserMenuItem(context.getString(R.string.menu_item_clear_data)) {
                DeleteBrowsingData(context, coroutineScope, tabsUseCases, sessionManager).askToDelete()
            }
        )
    }

    private val menuBuilder = BrowserMenuBuilder(menuItems)

    init {
        toolbar.display.indicators = listOf(DisplayToolbar.Indicators.SECURITY)
        toolbar.display.displayIndicatorSeparator = true
        toolbar.display.menuBuilder = menuBuilder
        if (toolbarEditMode) {
            toolbar.editMode()
        }
        toolbar.display.hint = context.getString(R.string.toolbar_hint)
        toolbar.edit.hint = context.getString(R.string.toolbar_hint)

        ToolbarAutocompleteFeature(toolbar).apply {
            addHistoryStorageProvider(historyStorage)
            addDomainProvider(shippedDomainsProvider)
        }

        toolbar.display.setUrlBackground(context.resources.getDrawable(R.drawable.url_background, context.theme))
    }

    private val toolbarFeature: ToolbarFeature = ToolbarFeature(
        toolbar,
        context.components.core.store,
        context.components.useCases.sessionUseCases.loadUrl,
        { searchTerms -> context.components.useCases.searchUseCases.defaultSearch.invoke(searchTerms) },
        sessionId
    )

    override fun start() {
        toolbarFeature.start()
    }

    override fun stop() {
        toolbarFeature.stop()
    }

    override fun onBackPressed(): Boolean {
        return toolbarFeature.onBackPressed()
    }

    private fun openSettingsActivity(context: Context) {
        val intent = Intent(context, SettingsActivity::class.java)
        context.startActivity(intent)
    }

    private fun openHistoryFragment() {
        fragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, HistoryFragment())
            commit()
        }
    }
}
