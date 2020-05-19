/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_browser.engineView
import kotlinx.android.synthetic.main.fragment_browser.toolbar
import kotlinx.android.synthetic.main.fragment_browser.view.readerViewAppearanceButton
import kotlinx.android.synthetic.main.fragment_browser.view.readerViewBar
import kotlinx.android.synthetic.main.fragment_browser.view.toolbar
import mozilla.components.feature.session.ThumbnailsFeature
import mozilla.components.feature.tabs.toolbar.TabsToolbarFeature
import mozilla.components.feature.toolbar.WebExtensionToolbarFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.nav
import org.mozilla.reference.browser.ext.requireComponents

/**
 * Fragment used for browsing the web within the main app.
 */
class BrowserFragment : BaseBrowserFragment(), UserInteractionHandler {
    private val thumbnailsFeature = ViewBoundFeatureWrapper<ThumbnailsFeature>()
    private val readerViewFeature = ViewBoundFeatureWrapper<ReaderViewIntegration>()
    private val webExtToolbarFeature = ViewBoundFeatureWrapper<WebExtensionToolbarFeature>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TabsToolbarFeature(
            toolbar = toolbar,
            sessionId = sessionId,
            sessionManager = requireComponents.core.sessionManager,
            showTabs = ::showTabsTrayFragment)

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
                requireComponents.core.store,
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

    private fun showTabsTrayFragment() {
        val direction = BrowserFragmentDirections.actionBrowserFragmentToTabsTrayFragment()
        nav(R.id.browserFragment, direction)
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
