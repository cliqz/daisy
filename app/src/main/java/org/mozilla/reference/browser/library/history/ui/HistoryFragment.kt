/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.library.history.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_history.view.*
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.BrowserDirection
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.openToBrowserAndLoad
import org.mozilla.reference.browser.ext.components

import org.mozilla.reference.browser.library.history.data.HistoryItem

class HistoryFragment @JvmOverloads constructor(
    private val initialHistoryViewModel: HistoryViewModel? = null,
    private val initialHistoryInteractor: HistoryInteractor? = null
) : Fragment(), UserInteractionHandler {

    private lateinit var historyViewModel: HistoryViewModel

    private lateinit var historyView: HistoryView
    private lateinit var historyInteractor: HistoryInteractor

    override fun onAttach(context: Context) {
        super.onAttach(context)

        historyViewModel = initialHistoryViewModel ?: HistoryViewModel(
                context.components.useCases.historyUseCases,
                context.components.useCases.sessionUseCases
        )

        historyInteractor = initialHistoryInteractor ?: HistoryInteractor(
                historyViewModel,
                ::openHistoryItem,
                ::openHistoryItems,
                ::deleteAll,
                ::onBackPressed
        )
    }

    override fun onResume() {
        super.onResume()
        historyViewModel.historyItems.observe(this, Observer { historyList ->
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

    private fun openHistoryItem(item: HistoryItem) {
        context?.openToBrowserAndLoad(
            searchTermOrUrl = item.url,
            newTab = false,
            from = BrowserDirection.FromHistory,
            private = false
        )
    }

    private fun openHistoryItems(items: Set<HistoryItem>, private: Boolean) {
        items.forEach { historyItem ->
            context?.openToBrowserAndLoad(
                searchTermOrUrl = historyItem.url,
                newTab = true,
                from = BrowserDirection.FromHistory,
                private = private
            )
        }
    }

    private fun showClearAllHistoryDialog() {
        context?.let {
            AlertDialog.Builder(it, R.style.HistoryAlertDialogTheme)
                .setTitle(R.string.history_clear_all_dialog_title)
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun deleteAll() {
        showClearAllHistoryDialog()
    }

    override fun onBackPressed(): Boolean {
        if (historyView.onBackPressed()) {
            return true
        }
        findNavController().navigateUp()
        return true
    }
}
