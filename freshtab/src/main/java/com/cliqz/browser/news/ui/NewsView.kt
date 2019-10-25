package com.cliqz.browser.news.ui

import android.animation.LayoutTransition
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.cliqz.browser.freshtab.R
import com.cliqz.browser.news.data.NewsItem
import mozilla.components.browser.icons.BrowserIcons
import kotlin.math.pow

class NewsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), NewsPresenter.View {

    private val expandNewsIcon = AppCompatResources.getDrawable(context, R.drawable.ic_action_expand)
    private val collapseNewsIcon = AppCompatResources.getDrawable(context, R.drawable.ic_action_collapse)
    private val newsItemHeight = resources.getDimensionPixelSize(R.dimen.three_line_list_item_height)

    private val view: View = LayoutInflater.from(context).inflate(R.layout.news_layout, this, true)

    private val newsLabelView: TextView

    private val topNewsListView: LinearLayout

    internal val styling: NewsViewStyling

    var presenter: NewsPresenter? = null

    init {
        // region style view
        context.obtainStyledAttributes(attrs, R.styleable.NewsView, defStyleAttr, 0). apply {
            styling = NewsViewStyling(
                getColor(R.styleable.NewsView_newsViewTitleTextColor,
                    ContextCompat.getColor(context, R.color.newsview_default_title_text_color)),
                getColor(R.styleable.NewsView_newsViewUrlTextColor,
                    ContextCompat.getColor(context, R.color.newsview_default_url_text_color)),
                getColor(R.styleable.NewsView_newsViewBackgroundColor,
                    ContextCompat.getColor(context, R.color.newsview_default_background_color))
            )
            recycle()
        }

        newsLabelView = view.findViewById<TextView>(R.id.news_label).apply {
            setTextColor(styling.titleTextColor)
            setBackgroundColor(styling.backgroundColor)
        }
        topNewsListView = view.findViewById<LinearLayout>(R.id.topnews_list).apply {
            setBackgroundColor(styling.backgroundColor)
        }
        // endregion

        // For the animation when news item views are added to the container
        topNewsListView.layoutTransition.enableTransitionType(LayoutTransition.APPEARING)

        newsLabelView.setOnClickListener {
            // Disable this so that the toggle animation works fine
            topNewsListView.layoutTransition.disableTransitionType(LayoutTransition.APPEARING)
            presenter?.toggleNewsViewClicked()
        }
    }

    override fun displayNews(newsList: List<NewsItem>, isNewsViewExpanded: Boolean, icons: BrowserIcons?) {
        view.visibility = View.GONE
        if (newsList.isNullOrEmpty()) {
            return
        }
        setInitialViewHeight(isNewsViewExpanded, newsList.count())
        topNewsListView.removeAllViews()
        val inflater = LayoutInflater.from(context)
        for (newsItem in newsList) {
            val itemView = inflater.inflate(R.layout.three_line_list_item_layout, topNewsListView, false)
            NewsItemViewHolder(itemView, this).bind(newsItem, icons) {
                presenter?.onOpenInNormalTab(it)
            }
            topNewsListView.addView(itemView)
        }
        toggleNewsLabelIcon(isNewsViewExpanded)
        view.visibility = View.VISIBLE
    }

    override fun hideNews() {
        view.visibility = View.GONE
    }

    override fun toggleNewsView(isNewsViewExpanded: Boolean) {
        val count = topNewsListView.childCount
        val collapsedHeight = newsItemHeight * COLLAPSED_NEWS_NO
        val expandedHeight = newsItemHeight * count
        if (isNewsViewExpanded) {
            getToggleAnimation(topNewsListView, collapsedHeight, expandedHeight, count).start()
        } else {
            getToggleAnimation(topNewsListView, expandedHeight, collapsedHeight, count).start()
        }
        toggleNewsLabelIcon(isNewsViewExpanded)
    }

    private fun setInitialViewHeight(isNewsViewExpanded: Boolean, count: Int) {
        val collapsedHeight = newsItemHeight * COLLAPSED_NEWS_NO
        val expandedHeight = newsItemHeight * count
        val viewHeight = if (isNewsViewExpanded) expandedHeight else collapsedHeight
        topNewsListView.setViewHeight(viewHeight)
    }

    private fun getToggleAnimation(
        view: View,
        startHeight: Int,
        endHeight: Int,
        childCount: Int
    ): ValueAnimator {
        val durationFactor = 45F
        val animationDuration = (durationFactor * childCount).toLong()
        val animator = ValueAnimator.ofInt(startHeight, endHeight)

        if (startHeight < endHeight) {
            // Expanding animation motion is the easing function 'easeInQuint'
            animator.interpolator = TimeInterpolator { (it * it * it * it * it) }
        } else {
            // Collapsing animation motion is the easing function 'easeOutQuint'
            animator.interpolator = TimeInterpolator { (1 - (1 - it).toDouble().pow(5.0)).toFloat() }
        }

        animator.addUpdateListener { view.setViewHeight(it.animatedValue as Int) }
        animator.duration = animationDuration
        return animator
    }

    private fun toggleNewsLabelIcon(isNewsViewExpanded: Boolean) {
        val newsLabelIcon = if (isNewsViewExpanded) collapseNewsIcon else expandNewsIcon
        newsLabelView.setCompoundDrawablesWithIntrinsicBounds(null, null, newsLabelIcon, null)
    }

    private fun View.setViewHeight(height: Int) {
        val params = layoutParams as LayoutParams
        params.height = height
        this.layoutParams = params
    }

    companion object {
        private const val COLLAPSED_NEWS_NO = 2
    }
}

internal data class NewsViewStyling(
    val titleTextColor: Int,
    val urlTextColor: Int,
    val backgroundColor: Int
)
