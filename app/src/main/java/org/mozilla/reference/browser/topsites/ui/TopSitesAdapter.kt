/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.topsites.ui

import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.icons.IconRequest
import mozilla.components.support.ktx.android.util.dpToFloat
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.database.model.TopSite
import kotlin.properties.Delegates

class TopSitesAdapter(
    private val browserIcons: BrowserIcons,
    private val itemClickListener: ((topSite: TopSite) -> Unit)
) : RecyclerView.Adapter<TopSitesViewHolder>() {

    var topSites: List<TopSite> by Delegates.observable(emptyList()) { _, _, _ -> notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopSitesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(TopSitesViewHolder.LAYOUT_ID, parent, false)
        return TopSitesViewHolder(view, browserIcons)
    }

    override fun onBindViewHolder(holder: TopSitesViewHolder, position: Int) {
        val topSite = topSites[position]
        holder.bind(topSite)
        holder.itemView.setOnClickListener {
            itemClickListener(topSite)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return topSites.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < topSites.size) TOP_SITE_TYPE else PLACEHOLDER_TYPE
    }

    companion object {

        const val TOP_SITE_TYPE = 0
        const val PLACEHOLDER_TYPE = 1
    }
}

class TopSitesViewHolder(
    itemView: View,
    private val browserIcons: BrowserIcons
) : RecyclerView.ViewHolder(itemView) {

    private lateinit var topSite: TopSite

    private val domainView: TextView = itemView.findViewById(R.id.domain_view) as TextView
    private val iconView: ImageView = itemView.findViewById(R.id.icon_view) as ImageView

    init {
        iconView.clipToOutline = true
        iconView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                outline?.setRoundRect(
                    0,
                    0,
                    view!!.width,
                    view.height,
                    favIconBorderRadiusInPx.dpToFloat(view.context.resources.displayMetrics)
                )
            }
        }
    }

    fun bind(topSite: TopSite) {
        this.topSite = topSite
        this.domainView.text = topSite.domain
        browserIcons.loadIntoView(iconView, IconRequest(topSite.url))
    }

    companion object {
        const val LAYOUT_ID = R.layout.top_site_item_layout
        const val favIconBorderRadiusInPx = 4
    }
}
