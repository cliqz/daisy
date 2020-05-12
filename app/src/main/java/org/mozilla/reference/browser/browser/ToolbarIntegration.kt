/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import kotlinx.android.synthetic.main.browser_toolbar_popup_window.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.browser.domains.autocomplete.ShippedDomainsProvider
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.BrowserMenuItem
import mozilla.components.browser.menu.item.BrowserMenuItemToolbar
import mozilla.components.browser.menu.item.BrowserMenuSwitch
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.browser.toolbar.display.DisplayToolbar
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
import org.mozilla.reference.browser.concepts.HistoryStorage
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.isFreshTab
import org.mozilla.reference.browser.ext.nav
import org.mozilla.reference.browser.ext.share
import org.mozilla.reference.browser.settings.SettingsActivity
import org.mozilla.reference.browser.settings.deletebrowsingdata.DeleteBrowsingData
import org.mozilla.reference.browser.view.DaisySnackbar

@Suppress("TooManyFunctions")
class ToolbarIntegration(
    private val context: Context,
    toolbar: BrowserToolbar,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val lifecycleOwner: LifecycleOwner,
    private val historyStorage: HistoryStorage,
    private val sessionManager: SessionManager,
    sessionUseCases: SessionUseCases,
    searchUseCases: SearchUseCases,
    tabsUseCases: TabsUseCases,
    webAppUseCases: WebAppUseCases,
    sessionId: String? = null,
    private val parentView: View,
    private val navController: NavController
) : LifecycleAwareFeature, UserInteractionHandler {

    private var isCurrentUrlBookmarked = false
    private var isBookmarkedJob: Job? = null

    private val session: Session? get() = sessionManager.selectedSession

    private val shippedDomainsProvider = ShippedDomainsProvider().also {
        it.initialize(context)
    }

    private fun hasSessionAndUrl() =
        sessionManager.selectedSession != null &&
            !sessionManager.selectedSession!!.isFreshTab()

    private val menuToolbar by lazy {
        val forward = BrowserMenuItemToolbar.TwoStateButton(
            primaryImageResource = mozilla.components.ui.icons.R.drawable.mozac_ic_forward,
            primaryImageTintResource = R.color.icons,
            primaryContentDescription = context.getString(R.string.toolbar_menu_item_forward),
            isInPrimaryState = {
                sessionManager.selectedSession?.canGoForward == true
            },
            secondaryImageTintResource = R.color.disabled_icons,
            disableInSecondaryState = true
        ) {
            sessionUseCases.goForward.invoke()
        }

        val refresh = BrowserMenuItemToolbar.TwoStateButton(
            primaryImageResource = mozilla.components.ui.icons.R.drawable.mozac_ic_refresh,
            primaryContentDescription = context.getString(R.string.toolbar_menu_item_refresh),
            primaryImageTintResource = R.color.icons,
            isInPrimaryState = {
                session?.loading == false
            },
            secondaryImageResource = mozilla.components.ui.icons.R.drawable.mozac_ic_stop,
            secondaryContentDescription = context.getString(R.string.toolbar_menu_item_stop),
            secondaryImageTintResource = R.color.icons,
            disableInSecondaryState = false
        ) {
            if (session?.loading == true) {
                sessionUseCases.stopLoading.invoke()
            } else {
                sessionUseCases.reload.invoke()
            }
        }

        val share = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_share,
            iconTintColorResource = R.color.icons,
            contentDescription = context.getString(R.string.toolbar_menu_item_share),
            isEnabled = ::hasSessionAndUrl) {
            val url = sessionManager.selectedSession?.url ?: ""
            context.share(url)
        }

        registerSessionForBookmarkUpdates()
        val bookmark = BrowserMenuItemToolbar.TwoStateButton(
            primaryImageResource = R.drawable.ic_bookmark,
            primaryImageTintResource = R.color.icons,
            primaryContentDescription = context.getString(R.string.toolbar_menu_item_bookmark),
            isInPrimaryState = { !isCurrentUrlBookmarked },
            secondaryImageResource = R.drawable.ic_bookmarked,
            secondaryImageTintResource = -1,
            secondaryContentDescription = context.getString(R.string.toolbar_menu_item_remove_bookmark)
        ) {
            sessionManager.selectedSession?.let {
                lifecycleScope.launch {
                    isCurrentUrlBookmarked = !isCurrentUrlBookmarked
                    bookmarkTapped(it)
                }
            }
        }

        BrowserMenuItemToolbar(listOf(forward, refresh, bookmark, share))
    }

    private val menuItems: List<BrowserMenuItem> by lazy {
        listOf(
            menuToolbar,
            SimpleBrowserMenuItem(context.getString(R.string.toolbar_menu_item_new_tab)) {
                tabsUseCases.addTab.invoke("")
                openFreshTabFragment()
            },
            SimpleBrowserMenuItem(context.getString(R.string.toolbar_menu_item_forget_tab)) {
                tabsUseCases.addPrivateTab.invoke("about:privatebrowsing", selectTab = true)
            },
            BrowserMenuSwitch(context.getString(R.string.toolbar_menu_item_request_desktop_site), {
                sessionManager.selectedSessionOrThrow.desktopMode
            }) { checked ->
                sessionUseCases.requestDesktopSite.invoke(checked)
            }.apply {
                visible = ::hasSessionAndUrl
            },

            SimpleBrowserMenuItem(context.getString(R.string.toolbar_menu_item_add_to_homescreen)) {
                MainScope().launch { webAppUseCases.addToHomescreen() }
            }.apply {
                visible = {
                    webAppUseCases.isPinningSupported() && hasSessionAndUrl()
                }
            },

            SimpleBrowserMenuItem(context.getString(R.string.toolbar_menu_item_find_in_page)) {
                FindInPageIntegration.launch?.invoke()
            }.apply {
                visible = ::hasSessionAndUrl
            },

            SimpleBrowserMenuItem(context.getString(R.string.toolbar_menu_item_report_issue)) {
                tabsUseCases.addTab.invoke(
                    "https://cliqz.com/en/support")
            },

            SimpleBrowserMenuItem(context.getString(R.string.toolbar_menu_item_settings)) {
                openSettingsActivity(context)
            },

            SimpleBrowserMenuItem(context.getString(R.string.toolbar_menu_item_history)) {
                openHistoryFragment()
            },

            SimpleBrowserMenuItem(context.getString(R.string.toolbar_menu_item_bookmarks)) {
                openBookmarkFragment()
            },

            SimpleBrowserMenuItem(context.getString(R.string.toolbar_menu_item_clear_data)) {
                val deleteBrowsingData = DeleteBrowsingData(
                    context,
                    lifecycleScope,
                    tabsUseCases,
                    sessionManager,
                    ::openFreshTabFragment)
                deleteBrowsingData.askToDelete()
            }
        )
    }

    private val menuBuilder = BrowserMenuBuilder(menuItems)

    init {
        toolbar.display.indicators = listOf(DisplayToolbar.Indicators.SECURITY)
        toolbar.display.displayIndicatorSeparator = true
        toolbar.display.menuBuilder = menuBuilder

        toolbar.display.onUrlClicked = {
            openSearchFragment()
            false
        }

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
            @SuppressLint("InflateParams")
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

            customView.copy.isVisible = selectedSession != null && !selectedSession.isFreshTab()
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

    private fun registerSessionForBookmarkUpdates() {
        val observer = object : Session.Observer {
            override fun onUrlChanged(session: Session, url: String) {
                isCurrentUrlBookmarked = false
                updateIsCurrentUrlBookmarked(url)
            }
        }

        sessionManager.selectedSession?.url?.let { updateIsCurrentUrlBookmarked(it) }
        sessionManager.selectedSession?.register(observer, lifecycleOwner)
    }

    private fun updateIsCurrentUrlBookmarked(url: String) {
        isBookmarkedJob?.cancel()
        isBookmarkedJob = lifecycleScope.launch {
            isCurrentUrlBookmarked = historyStorage.isBookmark(url)
        }
    }

    private suspend fun bookmarkTapped(session: Session) = withContext(Dispatchers.IO) {
        val isExistingBookmark = historyStorage.isBookmark(session.url)
        if (isExistingBookmark) {
            historyStorage.deleteBookmark(session.url)
        } else {
            // Save bookmark
            historyStorage.addBookmark(session.url, session.title)
            withContext(Dispatchers.Main) {
                showBookmarkAddedSnackbar()
            }
        }
    }

    private fun showBookmarkAddedSnackbar() {
        DaisySnackbar.make(
            view = parentView,
            duration = DaisySnackbar.LENGTH_LONG
        )
            .setText(context.getString(R.string.bookmark_added_snackbar_text))
            .setAction(context.getString(R.string.bookmark_added_snackbar_edit_action)) {
                openBookmarkEditFragment()
            }
            .show()
    }

    private fun openSettingsActivity(context: Context) {
        val intent = Intent(context, SettingsActivity::class.java)
        context.startActivity(intent)
    }

    private fun openBookmarkEditFragment() {
        // to-do go to bookmark edit fragment
    }

    private fun openHistoryFragment() {
        val direction = BrowserFragmentDirections.actionBrowserFragmentToHistoryFragment()
        navController.nav(R.id.browserFragment, direction)
    }

    private fun openBookmarkFragment() {
        val direction = BrowserFragmentDirections.actionBrowserFragmentToBookmarkFragment()
        navController.nav(R.id.browserFragment, direction)
    }

    private fun openFreshTabFragment() {
        val direction = BrowserFragmentDirections.actionBrowserFragmentToFreshTabFragment()
        navController.nav(R.id.browserFragment, direction)
    }

    private fun openSearchFragment() {
        val direction = BrowserFragmentDirections.actionBrowserFragmentToSearchFragment(
            sessionId = sessionManager.selectedSession?.id
        )
        navController.nav(R.id.browserFragment, direction)
    }
}
