package org.mozilla.reference.browser.library.history.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.two_line_list_item_layout.view.favicon
import kotlinx.android.synthetic.main.two_line_list_item_layout.view.title_view
import kotlinx.android.synthetic.main.two_line_list_item_layout.view.url_view
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.concept.storage.SearchResult
import mozilla.components.concept.storage.VisitType
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.inflate
import org.mozilla.reference.browser.ext.loadIntoView
import org.mozilla.reference.browser.library.history.data.HistoryItem

class HistorySearchAdapter(
    private val interactor: HistoryInteractor,
    private val browserIcons: BrowserIcons
) : RecyclerView.Adapter<HistorySearchAdapter.ViewHolder>() {

    private var searchResults: List<SearchResult> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.two_line_list_item_layout))

    override fun getItemCount(): Int {
        return searchResults.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(searchResults[position])
    }

    fun setData(data: List<SearchResult>) {
        searchResults = data
        notifyDataSetChanged()
    }

    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun bind(historyItem: SearchResult) {
            with(containerView) {
                val title: String = if (!historyItem.title.isNullOrBlank()) {
                        historyItem.title!!
                    } else {
                        resources.getString(R.string.history_title_untitled)
                    }
                title_view.text = title
                url_view.text = historyItem.url
                browserIcons.loadIntoView(favicon, historyItem.url)
                setOnClickListener {
                    interactor.open(setOf(
                        HistoryItem(
                            id = 0,
                            title = title,
                            url = historyItem.url,
                            visitType = VisitType.NOT_A_VISIT,
                            visitTime = 0L
                        )
                    ))
                }
            }
        }
    }
}
