/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.library.history.ui.viewholders

import android.content.Context
import android.text.format.DateFormat
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.history_list_item.view.*
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.library.SelectionHolder
import org.mozilla.reference.browser.library.history.data.HistoryItem
import org.mozilla.reference.browser.library.history.ui.HistoryInteractor
import org.mozilla.reference.browser.library.history.ui.ViewMode
import java.util.Date

enum class HistoryItemTimeGroup {
    Today, Yesterday, Older;

    fun humanReadable(context: Context, visitTime: Long): String {
        val dateFormat = DateFormat.getMediumDateFormat(context)
        val date = dateFormat.format(Date(visitTime))
        return when (this) {
            Today -> context.resources.getString(R.string.history_today, date)
            Yesterday -> context.resources.getString(R.string.history_yesterday, date)
            Older -> date
        }
    }
}

class HistoryItemViewHolder(
    override val containerView: View,
    private val selectionInteractor: HistoryInteractor,
    private val selectionHolder: SelectionHolder<HistoryItem>
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(
        historyItem: HistoryItem,
        timeGroup: HistoryItemTimeGroup?,
        isSelected: Boolean,
        viewMode: ViewMode
    ) {
        with(itemView.history_layout) {
            titleView.text = if (!historyItem.title.isBlank())
                historyItem.title else itemView.resources.getString(R.string.history_title_untitled)
            urlView.text = historyItem.url

            setSelectionInteractor(historyItem, selectionHolder, selectionInteractor)

            val deleteButton = actionButton
            deleteButton.setOnClickListener {
                selectionInteractor.delete(historyItem)
            }

            loadFavicon(historyItem.url)

            toggleActionButton(showActionButton = viewMode != ViewMode.Editing)
            changeSelected(isSelected = isSelected)
        }

        val headerText = timeGroup?.humanReadable(itemView.context, historyItem.visitTime)
        toggleHeader(headerText)
    }

    private fun toggleHeader(headerText: String?) {
        if (headerText != null) {
            itemView.header_title.visibility = View.VISIBLE
            itemView.header_title.text = headerText
        } else {
            itemView.header_title.visibility = View.GONE
        }
    }
}
