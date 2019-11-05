package org.mozilla.reference.browser.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH
import org.mozilla.reference.browser.widget.SearchWidgetProviderSize.LARGE
import org.mozilla.reference.browser.widget.SearchWidgetProviderSize.MEDIUM
import org.mozilla.reference.browser.widget.SearchWidgetProviderSize.SMALL
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import androidx.annotation.Dimension.DP
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.Dimension
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.utils.toDp

/**
 * Based on SearchWidgetProvider in Firefox preview.
 */
class SearchWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val searchIntent = buildSearchPendingIntent(context)
        for (appWidgetId in appWidgetIds) {
            val currentWidth = appWidgetManager
                    .getAppWidgetOptions(appWidgetId).getInt(OPTION_APPWIDGET_MIN_WIDTH)
            val widgetView = createRemoteViews(context, searchIntent, currentWidth)
            appWidgetManager.updateAppWidget(appWidgetId, widgetView)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        val searchIntent = buildSearchPendingIntent(context)
        val currentWidth = appWidgetManager.getAppWidgetOptions(appWidgetId)
                .getInt(OPTION_APPWIDGET_MIN_WIDTH)
        val widgetView = createRemoteViews(context, searchIntent, currentWidth)
        appWidgetManager.updateAppWidget(appWidgetId, widgetView)
    }

    private fun buildSearchPendingIntent(context: Context): PendingIntent {
        val searchIntent = Intent(context, BrowserActivity::class.java)
        searchIntent.putExtra(BrowserActivity.EXTRA_OPEN_TO_SEARCH, true)
        searchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_TO_SEARCH,
            searchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createRemoteViews(
        context: Context,
        searchIntent: PendingIntent,
        currentSize: Int
    ): RemoteViews {
        val layoutSize = getLayoutSize(currentSize, context)
        val text = getText(layoutSize, context)
        val showSearchIcon = shouldShowSearchIcon(layoutSize)
        return RemoteViews(context.packageName, R.layout.search_widget).apply {
            setOnClickPendingIntent(R.id.search_widget, searchIntent)
            setTextViewText(R.id.search_widget_text, text)
            setViewVisibility(R.id.search_widget_icon, if (showSearchIcon) View.VISIBLE else View.GONE)
        }
    }

    private fun getLayoutSize(@Dimension(unit = DP) dp: Int, context: Context) = when {
        dp >= context.resources.getDimension(R.dimen.search_widget_four_rows_width).toDp(context.resources.displayMetrics) -> LARGE
        dp >= context.resources.getDimension(R.dimen.search_widget_three_rows_width).toDp(context.resources.displayMetrics) -> MEDIUM
        else -> SMALL
    }

    private fun getText(size: SearchWidgetProviderSize, context: Context) = when (size) {
        LARGE -> context.getString(R.string.search_widget_text_long)
        else -> context.getString(R.string.search_widget_text_short)
    }

    private fun shouldShowSearchIcon(size: SearchWidgetProviderSize) = when (size) {
        LARGE, MEDIUM -> true
        else -> false
    }

    companion object {
        private const val REQUEST_CODE_OPEN_TO_SEARCH = 0
    }
}

enum class SearchWidgetProviderSize {
    SMALL,
    MEDIUM,
    LARGE
}
