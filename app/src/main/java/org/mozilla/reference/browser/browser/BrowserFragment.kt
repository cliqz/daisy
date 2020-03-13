/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_browser.awesomeBar
import kotlinx.android.synthetic.main.fragment_browser.engineView
import kotlinx.android.synthetic.main.fragment_browser.freshTab
import kotlinx.android.synthetic.main.fragment_browser.fresh_tab_toolbar
import kotlinx.android.synthetic.main.fragment_browser.toolbar
import kotlinx.android.synthetic.main.fragment_browser.view.readerViewAppearanceButton
import kotlinx.android.synthetic.main.fragment_browser.view.readerViewBar
import kotlinx.android.synthetic.main.fragment_browser.view.toolbar
import kotlinx.android.synthetic.main.news_layout.newsView
import kotlinx.android.synthetic.main.top_sites_layout.topSitesView
import mozilla.components.feature.awesomebar.AwesomeBarFeature
import mozilla.components.feature.session.ThumbnailsFeature
import mozilla.components.feature.syncedtabs.SyncedTabsStorageSuggestionProvider
import mozilla.components.feature.tabs.toolbar.TabsToolbarFeature
import mozilla.components.feature.toolbar.WebExtensionToolbarFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.tabs.TabsTrayFragment

/**
 * Fragment used for browsing the web within the main app.
 */
class BrowserFragment : BaseBrowserFragment(), UserInteractionHandler {
    private val thumbnailsFeature = ViewBoundFeatureWrapper<ThumbnailsFeature>()
    private val readerViewFeature = ViewBoundFeatureWrapper<ReaderViewIntegration>()
    private val freshTabIntegration = ViewBoundFeatureWrapper<FreshTabIntegration>()
    private val webExtToolbarFeature = ViewBoundFeatureWrapper<WebExtensionToolbarFeature>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AwesomeBarFeature(awesomeBar, toolbar, engineView)
            .addSearchProvider(
                requireContext(),
                requireComponents.search.searchEngineManager,
                requireComponents.useCases.searchUseCases.defaultSearch,
                requireComponents.core.client)
            .addSessionProvider(
                requireComponents.core.sessionManager,
                requireComponents.useCases.tabsUseCases.selectTab)
            .addHistoryProvider(
                requireComponents.core.historyStorage,
                requireComponents.useCases.sessionUseCases.loadUrl)
            .addClipboardProvider(requireContext(), requireComponents.useCases.sessionUseCases.loadUrl)

        freshTabIntegration.set(
            feature = FreshTabIntegration(
                requireContext(),
                awesomeBar,
                toolbar,
                freshTab,
                fresh_tab_toolbar,
                engineView,
                requireComponents.core.sessionManager,
                sessionId)
            .addNewsFeature(
                newsView,
                lifecycleScope,
                requireComponents.useCases.sessionUseCases.loadUrl,
                requireComponents.useCases.getNewsUseCase,
                requireComponents.core.icons)
            .addTopSitesFeature(
                topSitesView,
                requireComponents.useCases.sessionUseCases.loadUrl,
                requireComponents.useCases.tabsUseCases,
                requireComponents.useCases.historyUseCases,
                requireComponents.core.icons
            ),
            owner = this,
            view = view
        )

        backButtonHandler.add(freshTabIntegration)

        // We cannot really add a `addSyncedTabsProvider` to `AwesomeBarFeature` coz that would create
        // a dependency on feature-syncedtabs (which depends on Sync).
        awesomeBar.addProviders(
            SyncedTabsStorageSuggestionProvider(
                requireComponents.backgroundServices.syncedTabs,
                requireComponents.useCases.tabsUseCases.addTab,
                requireComponents.core.icons
            )
        )

        TabsToolbarFeature(
            toolbar = toolbar,
            sessionId = sessionId,
            sessionManager = requireComponents.core.sessionManager,
            showTabs = ::showTabs)

        thumbnailsFeature.set(
                feature = ThumbnailsFeature(requireContext(),
                        engineView,
                        requireComponents.core.sessionManager),
                owner = this,
                view = view
        )

        readerViewFeature.set(
            feature = ReaderViewIntegration(
                requireContext(),
                requireComponents.core.engine,
                requireComponents.core.sessionManager,
                view.toolbar,
                view.readerViewBar,
                view.readerViewAppearanceButton
            ),
            owner = this,
            view = view
        )

        webExtToolbarFeature.set(
            feature = WebExtensionToolbarFeature(
                view.toolbar,
                requireContext().components.core.store
            ),
            owner = this,
            view = view
        )
    }

    private fun showTabs() {
        // For now we are performing manual fragment transactions here. Once we can use the new
        // navigation support library we may want to pass navigation graphs around.
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, TabsTrayFragment())
            commit()
        }
    }

    override fun onBackPressed(): Boolean =
        readerViewFeature.onBackPressed() || super.onBackPressed()

    companion object {
        fun create(sessionId: String? = null, openToSearch: Boolean = false): BrowserFragment {
            return BrowserFragment().apply {
                arguments = Bundle().apply {
                    putSessionId(sessionId)
                    setOpenToSearch(openToSearch)
                }
            }
        }
    }
}
