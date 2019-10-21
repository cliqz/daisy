package com.cliqz.browser.news.ui

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cliqz.browser.freshtab.R
import com.cliqz.browser.news.data.NewsItem

class NewsItemViewHolder(itemView: View, private val presenter: Presenter)
    : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

    override fun onLongClick(v: View?): Boolean {
        return true
    }

    override fun onClick(v: View?) {
        newsItem?.let { presenter.onOpenInNormalTab(it) }
    }

    private val iconView: ImageView = itemView.findViewById(R.id.icon_view)

    val urlView: TextView = itemView.findViewById(R.id.url_view)
    val titleView: TextView = itemView.findViewById(R.id.title_view)

    private var newsItem: NewsItem? = null

    init {
        itemView.setOnClickListener(this)
        itemView.setOnLongClickListener(this)
    }

    fun bind(newsItem: NewsItem) {
        this.newsItem = newsItem
        presenter.loadNewsItemIcon(iconView, newsItem.url)
    }
}
