/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.topsites.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.icons.IconRequest
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.database.model.TopSite
import kotlin.properties.Delegates

class TopSitesAdapter(private val browserIcons: BrowserIcons) : BaseAdapter() {

    var topSites: List<TopSite> by Delegates.observable(emptyList()) { _, _, _ -> notifyDataSetChanged() }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row: TopSitesViewHolder
        val context = parent?.context
        val inflater = LayoutInflater.from(context)
        var tmpView = convertView

        if (getItemViewType(position) == TOP_SITE_TYPE) {
            //  if (convertView == null) {
            // if it's not recycled, initialize some attributes
            tmpView = inflater.inflate(R.layout.topsites_layout, parent, false)
            row = TopSitesViewHolder(tmpView)
            tmpView.tag = row
            // } else {
            // row = convertView.tag as TopSitesViewHolder
            // }
            val topSite = topSites[position]
            row.topSite = topSite
            row.domainView.text = topSite.title
            // row.iconView.setImageResource(R.drawable.mozac_menu_indicator)
            browserIcons.loadIntoView(row.iconView, IconRequest(topSite.url))
        } else {
            tmpView = convertView
                ?: inflater.inflate(R.layout.topsites_placeholder_layout, parent, false)
        }
        return tmpView!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return topSites.get(position)
    }

    override fun getCount(): Int {
        return 5
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < topSites.size) TOP_SITE_TYPE else PLACEHOLDER_TYPE
    }

    companion object {

        const val TOP_SITE_TYPE = 0
        const val PLACEHOLDER_TYPE = 1
    }
}

internal class TopSitesViewHolder(convertView: View) {
    @Volatile
    @set:Synchronized
    var topSite: TopSite? = null
    val domainView: TextView = convertView.findViewById<View>(R.id.domain_view) as TextView
    val iconView: ImageView = convertView.findViewById(R.id.icon_view) as ImageView

    val url: String
        @Synchronized get() = topSite!!.url
}
