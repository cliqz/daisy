package org.mozilla.reference.browser.utils

import android.content.Context.MODE_PRIVATE
import android.content.Context
import android.content.SharedPreferences
import mozilla.components.support.ktx.android.content.PreferencesHolder
import mozilla.components.support.ktx.android.content.booleanPreference
import org.mozilla.reference.browser.R.string.pref_key_show_news_view
import org.mozilla.reference.browser.ext.getPreferenceKey

class PreferenceHelper private constructor(
    context: Context
) : PreferencesHolder {

    private val appContext = context.applicationContext

    override val preferences: SharedPreferences
        get() = appContext.getSharedPreferences(CLIQZ_PREFS, MODE_PRIVATE)

    var shouldShowNewsView by booleanPreference(
        appContext.getPreferenceKey(pref_key_show_news_view),
        default = true
    )

    companion object {
        const val CLIQZ_PREFS = "cliqz_preferences"

        private val instanceHolder = SingletonHolder(::PreferenceHelper)

        fun getInstance(context: Context) = instanceHolder.getInstance(context)
    }
}
