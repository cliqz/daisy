/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.android.synthetic.main.browser_toolbar_popup_window.view.copy
import kotlinx.android.synthetic.main.browser_toolbar_popup_window.view.paste
import kotlinx.android.synthetic.main.browser_toolbar_popup_window.view.paste_and_go
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
import mozilla.components.feature.search.SearchUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.feature.toolbar.ToolbarAutocompleteFeature
import mozilla.components.feature.toolbar.ToolbarFeature
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.ktx.kotlin.isUrl
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.addons.AddonsActivity
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.share
import org.mozilla.reference.browser.freshtab.FreshTabToolbar
import org.mozilla.reference.browser.library.history.ui.HistoryFragment
import org.mozilla.reference.browser.settings.SettingsActivity
import org.mozilla.reference.browser.tabs.SyncedTabsActivity
import org.mozilla.reference.browser.settings.deletebrowsingdata.DeleteBrowsingData

class ToolbarIntegration(
    context: Context,
    toolbar: BrowserToolbar,
    freshTabToolbar: FreshTabToolbar,
    coroutineScope: LifecycleCoroutineScope,
    private val historyStorage: HistoryStorage,
    sessionManager: SessionManager,
    sessionUseCases: SessionUseCases,
    searchUseCases: SearchUseCases,
    tabsUseCases: TabsUseCases,
    webAppUseCases: WebAppUseCases,
    sessionId: String? = null,
    private val fragmentManager: FragmentManager?,
    toolbarEditMode: Boolean = false,
    showFreshTab: () -> Unit
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
                showFreshTab.invoke()
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

            SimpleBrowserMenuItem("Synced Tabs") {
                openSyncedTabsActivity(context)
            },

            SimpleBrowserMenuItem("Report issue") {
                tabsUseCases.addTab.invoke(
                    "https://cliqz.com/en/support")
            },

            SimpleBrowserMenuItem("Settings") {
                openSettingsActivity(context)
            },

            SimpleBrowserMenuItem(context.getString(R.string.menu_item_history)) {
                openHistoryFragment()
            },

            SimpleBrowserMenuItem(context.getString(R.string.menu_item_clear_data)) {
                val deleteBrowsingData = DeleteBrowsingData(
                    context,
                    coroutineScope,
                    tabsUseCases,
                    sessionManager,
                    showFreshTab)
                deleteBrowsingData.askToDelete()
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

        freshTabToolbar.setMenuBuilder(menuBuilder)

        val iconColor = ContextCompat.getColor(context, R.color.icons)
        toolbar.display.colors = toolbar.display.colors.copy(
            menu = iconColor,
            securityIconSecure = iconColor,
            securityIconInsecure = iconColor
        )

        toolbar.display.hint = context.getString(R.string.toolbar_hint)
        toolbar.edit.hint = context.getString(R.string.toolbar_hint)

        ToolbarAutocompleteFeature(toolbar).apply {
            addHistoryStorageProvider(historyStorage)
            addDomainProvider(shippedDomainsProvider)
        }

        toolbar.display.setUrlBackground(context.resources.getDrawable(R.drawable.url_background, context.theme))

        toolbar.display.setOnUrlLongClickListener {
            val clipboard = context.components.clipboardHandler
            val customView = LayoutInflater.from(context)
                .inflate(R.layout.browser_toolbar_popup_window, null)
            val popupWindow = PopupWindow(
                customView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                context.resources.getDimensionPixelSize(R.dimen.context_menu_height),
                true
            )

            val selectedSession = sessionManager.selectedSession

            popupWindow.elevation =
                context.resources.getDimension(R.dimen.mozac_browser_menu_elevation)

            customView.copy.isVisible = selectedSession != null && !selectedSession.url.isFreshTab()
            customView.paste.isVisible = !clipboard.text.isNullOrEmpty()
            customView.paste_and_go.isVisible = !clipboard.text.isNullOrEmpty()

            customView.copy.setOnClickListener {
                popupWindow.dismiss()
                clipboard.text = selectedSession?.url

                Toast.makeText(
                    context,
                    R.string.browser_toolbar_url_copied_to_clipboard_toast,
                    Toast.LENGTH_SHORT
                ).show()
            }

            customView.paste.setOnClickListener {
                popupWindow.dismiss()
                toolbar.url = clipboard.text!!
                toolbar.editMode()
            }

            customView.paste_and_go.setOnClickListener {
                popupWindow.dismiss()
                clipboard.text?.let {
                    toolbar.url = it
                    if (it.isUrl()) {
                        sessionUseCases.loadUrl.invoke(it)
                    } else {
                        searchUseCases.defaultSearch.invoke(it)
                    }
                }
            }

            popupWindow.showAsDropDown(
                toolbar,
                toolbar.context.resources.getDimensionPixelSize(R.dimen.context_menu_x_offset),
                0,
                Gravity.START
            )
            true
        }
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

    private fun openSyncedTabsActivity(context: Context) {
        val intent = Intent(context, SyncedTabsActivity::class.java)
        context.startActivity(intent)
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
