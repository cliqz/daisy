package com.cliqz.browser.news.ui

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlin.properties.Delegates

class NewsAdapter(
    private val icons: BrowserIcons?,
    private val styling: NewsViewStyling,
    private val itemClickListener: ((newsItem: NewsItem) -> Unit)
) : RecyclerView.Adapter<NewsItemViewHolder>() {

    var newsList: List<NewsItem> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(NewsItemViewHolder.LAYOUT_ID, parent, false)
        return NewsItemViewHolder(view, styling)
    }

    override fun onBindViewHolder(holder: NewsItemViewHolder, position: Int) {
        val newsItem = newsList[position]
        holder.bind(newsItem, icons)
        holder.itemView.setOnClickListener {
            itemClickListener(newsItem)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return newsList.size
    }
}

class NewsItemViewHolder(
    itemView: View,
    private val styling: NewsViewStyling
) : RecyclerView.ViewHolder(itemView) {

    private val iconView: ImageView = itemView.findViewById(R.id.icon_view)

    private val titleView: TextView = itemView.findViewById<TextView>(R.id.title_view).apply {
        setTextColor(styling.titleTextColor)
    }

    private val urlView: TextView = itemView.findViewById<TextView>(R.id.url_view).apply {
        setTextColor(styling.urlTextColor)
    }

    private val descriptionView: TextView = itemView.findViewById<TextView>(R.id.description_view).apply {
        setTextColor(styling.descriptionColor)
    }

    private val posterView: ImageView = itemView.findViewById(R.id.poster_view)

    private var newsItem: NewsItem? = null

    fun bind(
        newsItem: NewsItem,
        icons: BrowserIcons?
    ) {
        this.newsItem = newsItem
        titleView.text = buildTitleSpannable(newsItem)
        urlView.text = newsItem.domain
        descriptionView.text = newsItem.description
        Glide.with(itemView)
            .load(newsItem.media)
            .into(posterView)
        icons?.loadIntoView(iconView, IconRequest(newsItem.url))
    }

    private fun buildTitleSpannable(newsItem: NewsItem): CharSequence {
        val builder = SpannableStringBuilder()
        if (newsItem.breaking && !newsItem.breakingLabel.isNullOrBlank()) {
            appendLabel(builder, newsItem.breakingLabel.toUpperCase(Locale.getDefault()), Color.RED)
        }
        if (newsItem.isLocalNews && !newsItem.localLabel.isNullOrBlank()) {
            appendLabel(builder, newsItem.localLabel.toUpperCase(Locale.getDefault()),
                styling.titleTextColor)
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

    companion object {
        val LAYOUT_ID = R.layout.news_list_item_layout
    }
}
