package org.mozilla.reference.browser.history.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.history_item.view.*
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
    private val historyItemClickListener: (position: Int) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    var items: List<VisitInfo> by Delegates.observable(emptyList()) { _, _, _ -> notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.history_item))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun bind(historyItem: VisitInfo) {
            containerView.history_item_title.text = if (!historyItem.title.isNullOrBlank())
                historyItem.title else containerView.resources.getString(R.string.history_title_untitled)
            containerView.history_item_url.text = historyItem.url
            browserIcons.loadIntoView(containerView.history_item_icon, IconRequest(historyItem.url))
            containerView.setOnClickListener { historyItemClickListener(adapterPosition) }
        }
    }
}