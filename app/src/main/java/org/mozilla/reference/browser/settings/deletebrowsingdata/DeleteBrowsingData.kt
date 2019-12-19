/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings.deletebrowsingdata

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.layout_delete_browsing_data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.browser.session.SessionManager
import mozilla.components.feature.tabs.TabsUseCases
import org.mozilla.reference.browser.R

class DeleteBrowsingData(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val tabsUseCases: TabsUseCases,
    private val sessionManager: SessionManager
) {

    private val controller = DefaultDeleteBrowsingDataController(
        context,
        coroutineScope.coroutineContext
    )

    lateinit var alertDialog: AlertDialog

    fun askToDelete() {
        alertDialog = AlertDialog.Builder(context)
            .setTitle(R.string.delete_browsing_data_dialog_title)
            .setView(R.layout.layout_delete_browsing_data)
            .setPositiveButton(R.string.delete_browsing_data_dialog_positive_btn) { dialog, _ ->
                dialog.dismiss()
                deleteSelected(alertDialog)
            }
            .setNegativeButton(R.string.delete_browsing_data_dialog_negative_btn) { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun deleteSelected(alertDialog: AlertDialog) {
        getCheckBoxes(alertDialog).mapIndexed { index, deleteBrowsingDataItem ->
            coroutineScope.launch(Dispatchers.IO) {
                if (deleteBrowsingDataItem.isChecked) {
                    when (index) {
                        OPEN_TABS_INDEX -> controller.deleteTabs()
                        HISTORY_INDEX -> controller.deleteHistory()
                        COOKIES_INDEX -> controller.deleteCookies()
                        CACHED_INDEX -> controller.deleteCacheFiles()
                        SITE_SETTINGS_INDEX -> controller.deleteSiteSettings()
                    }
                }

                launch(Dispatchers.Main) {
                    finishDeletion()
                }
            }
        }
    }

    private fun finishDeletion() {
        if (sessionManager.sessions.isEmpty()) {
            tabsUseCases.addTab.invoke("")
        }
        Toast.makeText(context, R.string.delete_browsing_data_deleted_msg, Toast.LENGTH_LONG).show()
    }

    private fun getCheckBoxes(alertDialog: AlertDialog): List<DeleteBrowsingDataItem> {
        return listOf(
            alertDialog.open_tabs_item,
            alertDialog.history_item,
            alertDialog.cookies_item,
            alertDialog.cache_item,
            alertDialog.site_settings_item
        )
    }

    companion object {
        private const val OPEN_TABS_INDEX = 0
        private const val HISTORY_INDEX = 1
        private const val COOKIES_INDEX = 2
        private const val CACHED_INDEX = 3
        private const val SITE_SETTINGS_INDEX = 4
    }
}
