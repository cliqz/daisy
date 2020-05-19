/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabstray

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import mozilla.components.browser.icons.IconRequest
import mozilla.components.browser.session.Session
import mozilla.components.concept.tabstray.Tab
import mozilla.components.concept.tabstray.TabsTray
import mozilla.components.support.base.observer.Observable
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.isFreshTab
import org.mozilla.reference.browser.tabstray.thumbnail.TabThumbnailView

/**
 * A RecyclerView ViewHolder implementation for "tab" items.
 */
class TabViewHolder(
    itemView: View,
    private val tabsTray: BrowserTabsTray
) : RecyclerView.ViewHolder(itemView), Session.Observer {
    private val cardView: CardView = (itemView as CardView).apply {
        elevation = tabsTray.styling.itemElevation
    }
    private val iconView: ImageView = itemView.findViewById(R.id.mozac_browser_tabstray_icon)
    private val tabView: TextView = itemView.findViewById(R.id.mozac_browser_tabstray_url)
    private val closeView: AppCompatImageButton = itemView.findViewById(R.id.mozac_browser_tabstray_close)
    private val thumbnailView: TabThumbnailView = itemView.findViewById(R.id.mozac_browser_tabstray_thumbnail)

    internal var tab: Tab? = null

    private var thumbnailJob: Job? = null
    private var iconJob: Job? = null

    /**
     * Displays the data of the given session and notifies the given observable about events.
     */
    @Suppress("ComplexMethod")
    fun bind(tab: Tab, isSelected: Boolean, observable: Observable<TabsTray.Observer>) {
        this.tab = tab

        val title = when {
            tab.title.isNotEmpty() -> tab.title
            !tab.isFreshTab() -> tab.url
            else -> tabView.context.getString(R.string.freshtab_title)
        }

        tabView.text = title

        itemView.setOnClickListener {
            observable.notifyObservers { onTabSelected(tab) }
        }

        closeView.setOnClickListener {
            observable.notifyObservers { onTabClosed(tab) }
        }

        if (isSelected) {
            tabView.setTextColor(tabsTray.styling.selectedItemTextColor)
            cardView.setCardBackgroundColor(tabsTray.styling.selectedItemBackgroundColor)
            closeView.imageTintList = ColorStateList.valueOf(tabsTray.styling.selectedItemTextColor)
        } else {
            tabView.setTextColor(tabsTray.styling.itemTextColor)
            cardView.setCardBackgroundColor(tabsTray.styling.itemBackgroundColor)
            closeView.imageTintList = ColorStateList.valueOf(tabsTray.styling.itemTextColor)
        }

        thumbnailJob?.cancel()
        when {
            tab.isFreshTab() -> thumbnailView.setImageResource(R.drawable.placeholder_freshtab)
            tab.thumbnail != null -> thumbnailView.setImageBitmap(tab.thumbnail)
            else -> thumbnailJob = tabsTray.thumbnailsRepository.loadIntoView(
                view = thumbnailView,
                tabId = tab.id,
                placeholder = thumbnailView.context.getDrawable(R.drawable.placeholder_freshtab))
        }

        iconJob?.cancel()
        if (tab.isFreshTab()) {
            iconView.setImageResource(R.mipmap.ic_launcher)
        } else {
            iconJob = tabsTray.icons.loadIntoView(iconView, IconRequest(tab.url))
        }
    }

    /**
     * The attached view no longer needs to display any data.
     */
    fun unbind() {
        thumbnailJob?.cancel()
        iconJob?.cancel()
    }

    override fun onUrlChanged(session: Session, url: String) {
        tabView.text = url
    }
}
