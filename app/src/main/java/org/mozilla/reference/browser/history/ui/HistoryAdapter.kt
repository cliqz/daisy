package org.mozilla.reference.browser.history.ui

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.paging.AsyncPagedListDiffer
import androidx.paging.PagedList
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.inflate
import org.mozilla.reference.browser.history.data.HistoryItem
import org.mozilla.reference.browser.history.ui.viewholders.DeleteAllViewHolder
import org.mozilla.reference.browser.history.ui.viewholders.HistoryItemViewHolder
import org.mozilla.reference.browser.library.LibraryItemView
import org.mozilla.reference.browser.library.SelectionHolder

/**
 * @author Ravjit Uppal
 */
class HistoryAdapter(
    private val historyInteractor: HistoryInteractor,
    private val historyViewModel: HistoryViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    SelectionHolder<HistoryItem> {

    private var viewMode = ViewMode.Normal

    override val selectedItems: MutableSet<HistoryItem>
        get() = historyViewModel.selectedItems

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_DELETE_ALL) {
            DeleteAllViewHolder(parent.inflate(R.layout.clear_all_history), historyInteractor::onDeleteAll)
        } else {
            val view = LibraryItemView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            }
            HistoryItemViewHolder(view, historyInteractor, this)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == TYPE_DELETE_ALL) {
            (holder as DeleteAllViewHolder).bind(disableDeleteAll = viewMode == ViewMode.Editing)
        } else {
            getItem(position)?.let {
                (holder as HistoryItemViewHolder).bind(it, it in selectedItems, viewMode)
            }
        }
    }

    private fun getItem(position: Int): HistoryItem? {
        return differ.getItem(position - 1)
    }

    fun submitList(pagedList: PagedList<HistoryItem>?) {
        differ.submitList(pagedList)
    }

    override fun getItemCount(): Int {
        return differ.itemCount + 1
    }

    override fun getItemViewType(position: Int) = if (position == 0) TYPE_DELETE_ALL else TYPE_ITEM

    fun updateViewMode(viewMode: ViewMode) {
        this.viewMode = viewMode
        notifyItemChanged(0)
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
