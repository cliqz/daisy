package org.mozilla.reference.browser.history.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_history.view.*
import mozilla.components.support.base.feature.BackHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ViewModelFactory
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.ext.application
import org.mozilla.reference.browser.history.data.HistoryItem

/**
 * @author Ravjit Uppal
 */
class HistoryFragment : Fragment(), BackHandler {

    private lateinit var historyViewModel: HistoryViewModel

    private lateinit var historyView: HistoryView
    private lateinit var historyInteractor: HistoryInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        historyViewModel = ViewModelProviders.of(this,
            ViewModelFactory.getInstance(context.application)).get(HistoryViewModel::class.java)

        historyInteractor = HistoryInteractor(
            historyViewModel,
            ::openHistoryItem,
            ::deleteAll,
            ::invalidateOptionsMenu
        )

        historyViewModel.getHistoryItems().observe(this, Observer {
            historyView.updateEmptyState(userHasHistory = it.isNotEmpty())
            historyView.historyAdapter.submitList(it)
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
        (activity as AppCompatActivity).apply {
            setSupportActionBar(view.findViewById(R.id.toolbar))
            title = activity?.getString(R.string.history_screen_title)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.show()
        }
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
            AlertDialog.Builder(it).apply {
                setMessage(R.string.history_clear_all_dialog_msg)
                setPositiveButton(R.string.history_clear_all_dialog_postive_btn) { dialog, _ ->
                    historyViewModel.clearAllHistory()
                    dialog.dismiss()
                }
                setNegativeButton(R.string.history_clear_all_dialog_negative_btn) { dialog, _ ->
                    dialog.cancel()
                }
                create()
                show()
            }
        }
    }

    private fun deleteAll() {
        showClearAllHistoryDialog()
    }

    private fun invalidateOptionsMenu() {
        activity?.invalidateOptionsMenu()
    }

    override fun onBackPressed(): Boolean {
        if (historyView.onBackPressed()) {
            return true
        }
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, BrowserFragment.create())
            commit()
        }
        return true
    }
}
