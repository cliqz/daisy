/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.cliqz.browser.news.ui

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cliqz.browser.freshtab.R
import com.cliqz.browser.news.data.NewsItem
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.icons.IconRequest
import java.util.Locale

class NewsItemViewHolder(
    itemView: View,
    private val newsView: NewsView
) : RecyclerView.ViewHolder(itemView) {

    private val iconView: ImageView = itemView.findViewById(R.id.icon_view)

    private val titleView: TextView = itemView.findViewById<TextView>(R.id.title_view).apply {
        setTextColor(newsView.styling.titleTextColor)
    }

    private val urlView: TextView = itemView.findViewById<TextView>(R.id.url_view).apply {
        setTextColor(newsView.styling.urlTextColor)
    }

    private val descriptionView: TextView = itemView.findViewById<TextView>(R.id.description_view).apply {
        setTextColor(newsView.styling.descriptionColor)
    }

    private val posterView: ImageView = itemView.findViewById(R.id.poster_view)

    private var newsItem: NewsItem? = null

    fun bind(
        newsItem: NewsItem,
        icons: BrowserIcons?,
        clickListener: ((newsItem: NewsItem) -> Unit)
    ) {
        this.newsItem = newsItem
        titleView.text = buildTitleSpannable(newsItem)
        urlView.text = newsItem.domain
        descriptionView.text = newsItem.description
        Glide.with(itemView)
            .load(newsItem.media)
            .into(posterView)
        icons?.loadIntoView(iconView, IconRequest(newsItem.url))
        itemView.setOnClickListener { clickListener(newsItem) }
    }

    private fun buildTitleSpannable(newsItem: NewsItem): CharSequence {
        val builder = SpannableStringBuilder()
        if (newsItem.breaking && !newsItem.breakingLabel.isNullOrBlank()) {
            appendLabel(builder, newsItem.breakingLabel.toUpperCase(Locale.getDefault()), Color.RED)
        }
        if (newsItem.isLocalNews && !newsItem.localLabel.isNullOrBlank()) {
            appendLabel(builder, newsItem.localLabel.toUpperCase(Locale.getDefault()),
                    newsView.styling.titleTextColor)
        }
        builder.append(newsItem.title)
        return builder
    }

    private fun appendLabel(builder: SpannableStringBuilder, str: String, @ColorInt color: Int) {
        val oldLen = builder.length
        builder.append(str).append(": ")
        builder.setSpan(ForegroundColorSpan(color), oldLen, builder.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}
