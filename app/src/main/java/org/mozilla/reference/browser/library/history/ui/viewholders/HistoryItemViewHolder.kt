package org.mozilla.reference.browser.library.history.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.library.history.data.HistoryItem
import org.mozilla.reference.browser.library.history.ui.HistoryInteractor
import org.mozilla.reference.browser.library.history.ui.ViewMode
import org.mozilla.reference.browser.library.LibraryItemView
import org.mozilla.reference.browser.library.SelectionHolder

class HistoryItemViewHolder(
    override val containerView: LibraryItemView,
    private val selectionInteractor: HistoryInteractor,
    private val selectionHolder: SelectionHolder<HistoryItem>
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(
        historyItem: HistoryItem,
        isSelected: Boolean,
        viewMode: ViewMode
    ) {
        containerView.titleView.text = if (!historyItem.title.isBlank())
            historyItem.title else itemView.resources.getString(R.string.history_title_untitled)
        containerView.urlView.text = historyItem.url

        containerView.setSelectionInteractor(historyItem, selectionHolder, selectionInteractor)

        val deleteButton = containerView.metaButton
        deleteButton.setOnClickListener {
            selectionInteractor.delete(historyItem)
        }

        containerView.loadFavicon(historyItem.url)

        containerView.toggleMetaButton(showMetaButton = viewMode != ViewMode.Editing)
        containerView.toggleIconView(showCheckMark = isSelected)
    }
}
