/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.freshtab.toolbar

import android.content.Context
import android.os.Handler
import android.view.View
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.BrowserMenuItem
import mozilla.components.browser.menu.item.BrowserMenuItemToolbar
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
import mozilla.components.browser.session.SessionManager
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.isFreshTab
import org.mozilla.reference.browser.freshtab.FreshTabIntegration
import org.mozilla.reference.browser.freshtab.FreshTabToolbar

interface ToolbarMenuInteractor {

    fun onForwardClicked()

    fun onNewTabClicked()

    fun onNewForgetTabClicked()

    fun onReportIssueClicked()

    fun onSettingsClicked()

    fun onHistoryClicked()

    fun onClearDataClicked()
}

interface SearchBarInteractor {

    fun onSearchBarClicked()
}

class ToolbarFeature(
    private val context: Context,
    private val sessionManager: SessionManager,
    private val freshTabToolbar: FreshTabToolbar,
    private val toolbarMenuInteractor: ToolbarMenuInteractor,
    private val searchBarInteractor: SearchBarInteractor
) {

    private val menuToolbar by lazy {
        val forward = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_forward,
            iconTintColorResource = R.color.icons,
            contentDescription = context.getString(R.string.toolbar_menu_item_forward),
            isEnabled = { sessionManager.selectedSession?.isFreshTab() == false },
            listener = toolbarMenuInteractor::onForwardClicked
        )

        val refresh = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_refresh,
            iconTintColorResource = R.color.icons,
            contentDescription = context.getString(R.string.toolbar_menu_item_refresh),
            isEnabled = { false },
            listener = {}
        )

        val stop = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_stop,
            iconTintColorResource = R.color.icons,
            contentDescription = context.getString(R.string.toolbar_menu_item_stop),
            isEnabled = { false },
            listener = {}
        )

        BrowserMenuItemToolbar(listOf(forward, refresh, stop))
    }

    private val menuItems: List<BrowserMenuItem> by lazy {
        listOf(
            menuToolbar,
            SimpleBrowserMenuItem(context.getString(R.string.toolbar_menu_item_new_tab)) {
                toolbarMenuInteractor.onNewTabClicked()
            },

            SimpleBrowserMenuItem(context.getString(R.string.toolbar_menu_item_forget_tab)) {
                toolbarMenuInteractor.onNewForgetTabClicked()
            },

            SimpleBrowserMenuItem(context.getString(R.string.toolbar_menu_item_report_issue)) {
                toolbarMenuInteractor.onReportIssueClicked()
            },

            SimpleBrowserMenuItem(context.getString(R.string.toolbar_menu_item_settings)) {
                toolbarMenuInteractor.onSettingsClicked()
            },

            SimpleBrowserMenuItem(context.getString(R.string.toolbar_menu_item_history)) {
                toolbarMenuInteractor.onHistoryClicked()
            },

            SimpleBrowserMenuItem(context.getString(R.string.toolbar_menu_item_clear_data)) {
                toolbarMenuInteractor.onClearDataClicked()
            }
        )
    }

    private val menuBuilder = BrowserMenuBuilder(menuItems)

    init {
        freshTabToolbar.setMenuBuilder(menuBuilder)

        freshTabToolbar.setSearchBarClickListener(View.OnClickListener {
            freshTabToolbar.setExpanded(false)
            Handler().postDelayed({
                searchBarInteractor.onSearchBarClicked()
            }, FreshTabIntegration.FRESH_TAB_TOOLBAR_EXPAND_INTERACTION_DELAY)
        })
    }
}
