package org.mozilla.reference.browser.history.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_history.*
import mozilla.components.concept.storage.VisitInfo
import mozilla.components.support.base.feature.BackHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.ext.requireComponents

/**
 * @author Ravjit Uppal
 */
class HistoryFragment : Fragment(), BackHandler, HistoryPresenter.View {

    private lateinit var historyPresenter: HistoryPresenter
    private lateinit var historyAdapter: HistoryAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        historyAdapter = HistoryAdapter(requireComponents.core.icons)
        historyPresenter = HistoryPresenter(this, requireComponents.useCases.historyUseCases
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        history_list.adapter = historyAdapter
        historyPresenter.onCreate()
    }

    override fun onDestroy() {
        historyPresenter.onDestroy()
        super.onDestroy()
    }

    override fun renderHistory(historyItems: List<VisitInfo>) {
        historyAdapter.items = historyItems
    }

    override fun onBackPressed(): Boolean {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, BrowserFragment.create())
            commit()
        }
        return true
    }
}