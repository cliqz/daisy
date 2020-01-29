/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.library.history.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_history.view.*
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ViewModelFactory
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.ext.application
import org.mozilla.reference.browser.library.history.data.HistoryItem

class HistoryFragment : Fragment(), UserInteractionHandler {

    private lateinit var historyViewModel: HistoryViewModel

    private lateinit var historyView: HistoryView
    private lateinit var historyInteractor: HistoryInteractor

    override fun onAttach(context: Context) {
        super.onAttach(context)

        historyViewModel = ViewModelProviders.of(this,
            ViewModelFactory.getInstance(context.application)).get(HistoryViewModel::class.java)

        historyInteractor = HistoryInteractor(
            historyViewModel,
            ::openHistoryItem,
            ::deleteAll,
            ::onBackPressed
        )

        historyViewModel.getHistoryItems().observe(this, Observer { historyList ->
            historyView.updateEmptyState(userHasHistory = historyList.isNotEmpty())
            historyView.submitList(historyList)
        })

        historyViewModel.selectedItemsLiveData.observe(this, Observer {
            if (it.isEmpty() && historyViewModel.viewMode == ViewMode.Editing) {
                historyViewModel.viewMode = ViewMode.Normal
            } else if (it.isNotEmpty() && historyViewModel.viewMode == ViewMode.Normal) {
                historyViewModel.viewMode = ViewMode.Editing
            }
            historyView.update(historyViewModel.viewMode, it)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        historyView = HistoryView(view.history_layout, historyViewModel, historyInteractor)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val layout = when (historyViewModel.viewMode) {
            ViewMode.Normal -> R.menu.history_menu
            ViewMode.Editing -> R.menu.history_multi_select_menu
        }
        inflater.inflate(layout, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home, R.id.close -> {
                onBackPressed()
                return true
            }
            R.id.delete -> {
                historyViewModel.deleteMultipleHistoryItem(historyViewModel.selectedItems)
                historyViewModel.viewMode = ViewMode.Normal
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openHistoryItem(item: HistoryItem) {
        historyViewModel.openHistoryItem(item)
        onBackPressed()
    }

    private fun showClearAllHistoryDialog() {
        context?.let {
            AlertDialog.Builder(it)
                .setMessage(R.string.history_clear_all_dialog_msg)
                .setPositiveButton(R.string.history_clear_all_dialog_positive_btn) { dialog, _ ->
                    historyViewModel.clearAllHistory()
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.history_clear_all_dialog_negative_btn) { dialog, _ ->
                    dialog.cancel()
                }
                .create()
                .show()
        }
    }

    private fun deleteAll() {
        showClearAllHistoryDialog()
    }

    override fun onBackPressed(): Boolean {
        if (historyView.onBackPressed()) {
            return true
        }
        showFreshTab()
        return true
    }

    private fun showFreshTab() {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, BrowserFragment.create())
            commit()
        }
    }
}
