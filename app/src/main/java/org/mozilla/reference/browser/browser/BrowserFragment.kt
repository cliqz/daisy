/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_browser.*
import kotlinx.android.synthetic.main.fragment_browser.view.*
import mozilla.components.feature.awesomebar.AwesomeBarFeature
import mozilla.components.feature.session.ThumbnailsFeature
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
                engineView,
                requireComponents.core.sessionManager)
                .addNewsFeature(
                    newsView,
                    lifecycleScope,
                    requireComponents.useCases.sessionUseCases.loadUrl,
                    requireComponents.useCases.getNewsUseCase,
                    requireComponents.core.icons
                ),
                owner = this,
                view = view
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
