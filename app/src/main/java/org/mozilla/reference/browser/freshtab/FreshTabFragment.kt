/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.freshtab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cliqz.browser.news.ui.NewsInteractor
import kotlinx.android.synthetic.main.fragment_fresh_tab.*
import kotlinx.android.synthetic.main.news_layout.*
import kotlinx.android.synthetic.main.top_sites_layout.*
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.requireComponents

class FreshTabFragment : Fragment(), UserInteractionHandler {

    private val freshTabIntegration = ViewBoundFeatureWrapper<FreshTabIntegration>()

    lateinit var freshTabInteractor: FreshTabInteractor

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fresh_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val freshTabController = DefaultFreshTabController(
            activity as BrowserActivity,
            requireComponents.core.sessionManager,
            requireComponents.useCases.tabsUseCases,
            findNavController(),
            lifecycleScope
        )

        freshTabInteractor = FreshTabInteractor(freshTabController)

        freshTabIntegration.set(
            feature = FreshTabIntegration(
                requireContext(),
                requireComponents.core.sessionManager,
                fresh_tab_toolbar)
                .addToolbarFeature(freshTabInteractor)
                .addNewsFeature(
                    newsView,
                    lifecycleScope,
                    freshTabInteractor as NewsInteractor,
                    requireComponents.useCases.getNewsUseCase,
                    requireComponents.core.icons)
                .addTopSitesFeature(
                    topSitesView,
                    requireComponents.useCases.historyUseCases,
                    requireComponents.core.icons),
            owner = this,
            view = view
        )
    }

    override fun onBackPressed(): Boolean {
        return removeSessionIfNeeded()
    }

    private fun removeSessionIfNeeded(): Boolean {
        val sessionManager = requireComponents.core.sessionManager
        if (sessionManager.selectedSession != null &&
                !sessionManager.selectedSession!!.url.isFreshTab()) {
            sessionManager.remove()
        }
        return false
    }
}
