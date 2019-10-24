package org.mozilla.reference.browser.history.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_history.*
import mozilla.components.concept.storage.VisitInfo
import mozilla.components.support.base.feature.BackHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ViewModelFactory
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.ext.application
import org.mozilla.reference.browser.ext.requireComponents

/**
 * @author Ravjit Uppal
 */
class HistoryFragment : Fragment(), BackHandler {

    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyViewModel: HistoryViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        historyAdapter = HistoryAdapter(requireComponents.core.icons)
        historyViewModel = ViewModelProviders.of(this,
            ViewModelFactory.getInstance(context.application)).get(HistoryViewModel::class.java)
        historyViewModel.getHistoryItems().observe(this, Observer<List<VisitInfo>> {
            historyAdapter.items = it
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        history_list.adapter = historyAdapter
    }

    override fun onBackPressed(): Boolean {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, BrowserFragment.create())
            commit()
        }
        return true
    }
}