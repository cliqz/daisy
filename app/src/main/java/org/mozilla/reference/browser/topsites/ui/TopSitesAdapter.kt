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
import org.mozilla.reference.browser.database.Topsite
import kotlin.properties.Delegates

/**
 * @author Ravjit Uppal
 */
class TopSitesAdapter(private val browserIcons: BrowserIcons) : BaseAdapter() {

    val TOPSITE_TYPE = 0
    val PLACEHOLDER_TYPE = 1

    var topSites: List<Topsite> by Delegates.observable(emptyList()) { _, _, _ -> notifyDataSetChanged() }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row: TopsitesViewHolder
        val context = parent?.context
        val inflater = LayoutInflater.from(context)
        var tmpView = convertView

        if (getItemViewType(position) == TOPSITE_TYPE) {
            //  if (convertView == null) {
            // if it's not recycled, initialize some attributes
            tmpView = inflater.inflate(R.layout.topsites_layout, parent, false)
            row = TopsitesViewHolder(tmpView)
            tmpView.tag = row
            //} else {
            //row = convertView.tag as TopsitesViewHolder
            // }
            val topsite = topSites[position]
            row.topsite = topsite
            row.domainView.text = topsite.title
            //row.iconView.setImageResource(R.drawable.mozac_menu_indicator)
            browserIcons.loadIntoView(row.iconView, IconRequest(topsite.url))
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
        return if (position < topSites.size) TOPSITE_TYPE else PLACEHOLDER_TYPE
    }

}

internal class TopsitesViewHolder(convertView: View) {
    @Volatile
    @set:Synchronized
    var topsite: Topsite? = null
    val domainView: TextView = convertView.findViewById<View>(R.id.domain_view) as TextView
    val iconView: ImageView = convertView.findViewById(R.id.icon_view) as ImageView

    val url: String
        @Synchronized get() = topsite!!.url

}