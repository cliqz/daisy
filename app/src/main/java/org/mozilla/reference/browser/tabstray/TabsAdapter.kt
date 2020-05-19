/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabstray

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.concept.tabstray.Tab
import mozilla.components.concept.tabstray.Tabs
import mozilla.components.concept.tabstray.TabsTray
import mozilla.components.support.base.observer.Observable
import mozilla.components.support.base.observer.ObserverRegistry
import org.mozilla.reference.browser.R

/**
 * RecyclerView adapter implementation to display a list/grid of tabs.
 */
@Suppress("TooManyFunctions")
class TabsAdapter(
    delegate: Observable<TabsTray.Observer> = ObserverRegistry()
) : RecyclerView.Adapter<TabViewHolder>(),
    TabsTray,
    Observable<TabsTray.Observer> by delegate {

    internal lateinit var tabsTray: BrowserTabsTray

    private val holders = mutableListOf<TabViewHolder>()
    private var tabs: List<Tab> = listOf()
    private var selectedIndex: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        return TabViewHolder(
            LayoutInflater.from(parent.context).inflate(
                    R.layout.mozac_browser_tabstray_item,
                parent,
                false),
            tabsTray
        ).also {
            holders.add(it)
        }
    }

    override fun getItemCount() = tabs.size

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(tabs[position], position == selectedIndex, this)
    }

    override fun onViewRecycled(holder: TabViewHolder) {
        holder.unbind()
    }

    fun unsubscribeHolders() {
        holders.forEach { it.unbind() }
        holders.clear()
    }

//  TODO Do we need this?
//    override fun displaySessions(sessions: List<Session>, selectedIndex: Int) {
//        this.sessions = sessions
//        this.selectedIndex = selectedIndex
//        notifyDataSetChanged()
//    }

    override fun onTabsChanged(position: Int, count: Int) =
        notifyItemRangeChanged(position, count, null)

    override fun onTabsInserted(position: Int, count: Int) =
        notifyItemRangeInserted(position, count)

    override fun onTabsMoved(fromPosition: Int, toPosition: Int) =
        notifyItemMoved(fromPosition, toPosition)

    override fun onTabsRemoved(position: Int, count: Int) =
        notifyItemRangeRemoved(position, count)

    override fun updateTabs(tabs: Tabs) {
        this.tabs = tabs.list
        this.selectedIndex = tabs.selectedIndex
        notifyDataSetChanged()
    }
}
