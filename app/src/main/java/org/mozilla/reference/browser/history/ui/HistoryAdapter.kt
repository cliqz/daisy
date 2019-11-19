package org.mozilla.reference.browser.history.ui

import android.view.View
import android.view.ViewGroup
import androidx.paging.AsyncPagedListDiffer
import androidx.paging.PagedList
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.clear_all_history.view.*
import kotlinx.android.synthetic.main.two_line_list_item_layout.view.*
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.icons.IconRequest
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.inflate
import org.mozilla.reference.browser.history.data.HistoryItem

/**
 * @author Ravjit Uppal
 */
class HistoryAdapter(
    private val browserIcons: BrowserIcons,
    private val historyItemClickListener: (item: HistoryItem) -> Unit,
    private val historyItemDeleteListener: (item: HistoryItem) -> Unit,
    private val historyDeleteAllListener: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            differ.getItem(position - 1)?.let {
                (holder as HistoryItemViewHolder).bind(it)
            }
        }
    }

    fun submitList(pagedList: PagedList<HistoryItem>?) {
        differ.submitList(pagedList)
    }

    fun getCurrentList(): PagedList<HistoryItem>? {
        return differ.currentList
    }

    override fun getItemCount(): Int {
        return differ.itemCount + 1
    }

    override fun getItemViewType(position: Int) = if (position == 0) TYPE_DELETE_ALL else TYPE_ITEM

    inner class DeleteAllViewHolder(
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun bind() {
            containerView.clear_history.setOnClickListener { historyDeleteAllListener() }
        }
    }

    inner class HistoryItemViewHolder(
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun bind(historyItem: HistoryItem) {
            containerView.apply {
                history_item_title.text = if (!historyItem.title.isBlank())
                    historyItem.title else resources.getString(R.string.history_title_untitled)
                url_view.text = historyItem.url
                browserIcons.loadIntoView(icon_view, IconRequest(historyItem.url))
                setOnClickListener { historyItemClickListener(historyItem) }
                delete_btn.setOnClickListener {
                    historyItemDeleteListener(historyItem)
                }
            }
        }
    }

    val adapterCallback = AdapterListUpdateCallback(this)

    private val historyUpdateCallback = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {
            adapterCallback.onInserted(position + 1, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            adapterCallback.onRemoved(position + 1, count)
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapterCallback.onChanged(position + 1, count, payload)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapterCallback.onMoved(fromPosition + 1, toPosition + 1)
        }
    }

    private val historyDiffItemCallback = object : DiffUtil.ItemCallback<HistoryItem>() {
        override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: HistoryItem, newItem: HistoryItem): Any? {
            return newItem
        }
    }

    private val differ = AsyncPagedListDiffer<HistoryItem>(historyUpdateCallback, AsyncDifferConfig.Builder<HistoryItem>(historyDiffItemCallback).build())

    companion object {
        private const val TYPE_DELETE_ALL = 0
        private const val TYPE_ITEM = 1
    }
}
