/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.library.history.ui.viewholders

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.history_list_item.view.*
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.library.history.data.HistoryItem
import org.mozilla.reference.browser.library.history.ui.HistoryInteractor
import org.mozilla.reference.browser.library.history.ui.ViewMode
import org.mozilla.reference.browser.library.SelectionHolder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class HistoryItemTimeGroup {
    Today, Yesterday, Older;

    private val datePattern = "dd MMM yyyy"
    private val dateFormat = SimpleDateFormat(datePattern, Locale.getDefault())

    fun humanReadable(context: Context, visitTime: Long): String = when (this) {
        Today -> context.getString(R.string.history_today)
        Yesterday -> context.getString(R.string.history_yesterday)
        Older -> dateFormat.format(Date(visitTime))
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

            val deleteButton = metaButton
            deleteButton.setOnClickListener {
                selectionInteractor.delete(historyItem)
            }

            loadFavicon(historyItem.url)

            toggleMetaButton(showMetaButton = viewMode != ViewMode.Editing)
            toggleIconView(showCheckMark = isSelected)
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
