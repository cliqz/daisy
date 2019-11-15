package org.mozilla.reference.browser.history.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.clear_all_history.view.*
import kotlinx.android.synthetic.main.two_line_list_item_layout.view.*
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.icons.IconRequest
import mozilla.components.concept.storage.VisitInfo
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.inflate
import kotlin.properties.Delegates

/**
 * @author Ravjit Uppal
 */
class HistoryAdapter(
    private val browserIcons: BrowserIcons,
    private val historyItemClickListener: (position: Int) -> Unit,
    private val historyItemDeleteListener: (position: Int) -> Unit,
    private val historyDeleteAllListener: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items: List<VisitInfo> by Delegates.observable(emptyList()) { _, _, _ -> notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_DELETE_ALL) {
            DeleteAllViewHolder(parent.inflate(R.layout.clear_all_history))
        } else {
            HistoryItemViewHolder(parent.inflate(R.layout.two_line_list_item_layout))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == TYPE_DELETE_ALL) {
            (holder as DeleteAllViewHolder).bind()
        } else {
            (holder as HistoryItemViewHolder).bind(items[position - 1])
        }
    }

    override fun getItemCount(): Int = items.size + 1

    override fun getItemViewType(position: Int) = if (position == 0) TYPE_DELETE_ALL else TYPE_ITEM

    inner class DeleteAllViewHolder(
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind() {
            containerView.clear_history.setOnClickListener { historyDeleteAllListener() }
        }
    }

    inner class HistoryItemViewHolder(
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun bind(historyItem: VisitInfo) {
            containerView.apply {
                history_item_title.text = if (!historyItem.title.isNullOrBlank())
                    historyItem.title else resources.getString(R.string.history_title_untitled)
                url_view.text = historyItem.url
                browserIcons.loadIntoView(icon_view, IconRequest(historyItem.url))
                setOnClickListener { historyItemClickListener(adapterPosition - 1) }
                delete_btn.setOnClickListener { historyItemDeleteListener(adapterPosition - 1) }
            }
        }
    }

    companion object {
        private const val TYPE_DELETE_ALL = 0
        private const val TYPE_ITEM = 1
    }
}