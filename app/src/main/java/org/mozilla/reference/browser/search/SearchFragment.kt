/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_search.*
import mozilla.components.browser.session.Session
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.search.awesomebar.AwesomeBarFeature
import org.mozilla.reference.browser.search.toolbar.ToolbarFeature

class SearchFragment : Fragment() {

    private lateinit var searchInteractor: SearchInteractor

    private var session: Session? = null

    private val sessionId: String?
        get() = arguments?.getString(SESSION_ID)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args = arguments?.let { navArgs<SearchFragmentArgs>().value }
        session = args?.sessionId?.let(requireComponents.core.sessionManager::findSessionById)
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchController = DefaultSearchController(
            requireContext(),
            awesomeBar,
            findNavController(),
            session,
            ::showAwesomeBar,
            ::hideAwesomeBar
        )

        searchInteractor = SearchInteractor(searchController)

        AwesomeBarFeature(awesomeBar, searchInteractor)
            .addSearchProvider(
                requireContext(),
                requireComponents.search.searchEngineManager,
                requireComponents.core.client)
            .addSessionProvider(requireComponents.core.store)
            .addHistoryProvider(requireComponents.core.historyStorage)
            .addClipboardProvider(requireContext())

        val toolbarFeature = ToolbarFeature(
            toolbar, searchInteractor, requireComponents.core.historyStorage, false)
        val url = session?.url.orEmpty()
        toolbarFeature.updateText(url)
    }

    private fun showAwesomeBar() {
        awesomeBar.visibility = View.VISIBLE
    }

    private fun hideAwesomeBar() {
        awesomeBar.visibility = View.GONE
    }

    companion object {
        private const val SESSION_ID = "session_id"
    }
}
