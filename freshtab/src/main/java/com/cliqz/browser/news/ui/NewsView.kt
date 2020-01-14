/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.cliqz.browser.news.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.cliqz.browser.freshtab.R
import com.cliqz.browser.news.data.NewsItem
import mozilla.components.browser.icons.BrowserIcons

class NewsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), NewsPresenter.View {

    internal val styling: NewsViewStyling

    var presenter: NewsPresenter? = null

    init {
        orientation = VERTICAL
        // region style view
        context.obtainStyledAttributes(attrs, R.styleable.NewsView, defStyleAttr, 0). apply {
            styling = NewsViewStyling(
                getColor(R.styleable.NewsView_newsViewTitleTextColor,
                    ContextCompat.getColor(context, R.color.newsview_default_title_text_color)),
                getColor(R.styleable.NewsView_newsViewUrlTextColor,
                    ContextCompat.getColor(context, R.color.newsview_default_url_text_color)),
                getColor(R.styleable.NewsView_newsViewDescriptionTextColor,
                    ContextCompat.getColor(context, R.color.newsview_default_description_color)),
                getColor(R.styleable.NewsView_newsViewBackgroundColor,
                    ContextCompat.getColor(context, R.color.newsview_default_background_color))
            )
            recycle()
        }
        setBackgroundColor(styling.backgroundColor)
        // endregion
    }

    override fun displayNews(newsList: List<NewsItem>, icons: BrowserIcons?) {
        visibility = View.GONE
        if (newsList.isNullOrEmpty()) {
            return
        }
        removeAllViews()
        val inflater = LayoutInflater.from(context)
        for (newsItem in newsList) {
            val itemView = inflater.inflate(R.layout.news_list_item_layout, this, false)
            NewsItemViewHolder(itemView, this).bind(newsItem, icons) {
                presenter?.onOpenInNormalTab(it)
            }
            addView(itemView)
        }
        visibility = View.VISIBLE
    }

    override fun hideNews() {
        visibility = View.GONE
    }
}

internal data class NewsViewStyling(
    val titleTextColor: Int,
    val urlTextColor: Int,
    val descriptionColor: Int,
    val backgroundColor: Int
)
