/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.history.ui

import android.os.SystemClock
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import mozilla.components.concept.storage.VisitType
import mozilla.components.support.test.any
import mozilla.components.support.test.mock
import mozilla.components.support.test.whenever
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.startsWith
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.matchers.atPosition
import org.mozilla.reference.browser.assertions.isGone
import org.mozilla.reference.browser.assertions.isVisible
import org.mozilla.reference.browser.library.history.data.HistoryItem
import java.text.SimpleDateFormat
import java.util.Date

typealias HistoryObservable = LiveData<PagedList<HistoryItem>>
typealias HistoryObserver = Observer<PagedList<HistoryItem>>
typealias PagedHistory = PagedList<HistoryItem>

enum class TestData(val title: String, val url: String) {
    Cliqz("Cliqz", "https://cliqz.com/en"),
    Mozilla("Mozilla", "https://mozilla.org"),
    Daisy("Daisy", "https://github.com/cliqz/daisy"),
    Facebook("Facebook", "https://facebook.com")
}

private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")

enum class Dates(val dateTime: Date) {
    Now(dateFormat.parse("2020-04-11 11:15")),
    Today(dateFormat.parse("2020-04-11 10:01")),
    Yesterday(dateFormat.parse("2020-04-10 09:45")),
    TwoDaysAgo(dateFormat.parse("2020-04-09 09:00")),
    Date1(dateFormat.parse("2020-04-09 08:45"))
}

@RunWith(AndroidJUnit4::class)
class HistoryFragmentTest {

    private lateinit var historyViewModel: HistoryViewModel

    @Before
    fun setUp() {
        historyViewModel = mock()
        val selectedHistoryItem = mock<MutableLiveData<MutableSet<HistoryItem>>>()
        whenever(historyViewModel.viewMode).thenReturn(ViewMode.Normal)
        whenever(historyViewModel.selectedItemsLiveData).thenReturn(selectedHistoryItem)
    }

    @Test
    fun `the empty history message should be visible`() {
        setHistoryItems(listOf())

        launchFragmentInContainer(themeResId = R.style.Theme_AppCompat) {
            HistoryFragment(historyViewModel)
        }

        onView(withId(R.id.empty_view)).check(isVisible())
        onView(withId(R.id.history_list)).check(isGone())
        onView(withId(R.id.history_search_list)).check(isGone())
    }

    @Test
    fun `should display today, yesterday and date`() {
        SystemClock.setCurrentTimeMillis(Dates.Now.dateTime.time)

        setHistoryItems(
            TestData.values().zip(Dates.values().drop(1)) // Zip the TestData with the Dates skipping Dates.Now
                .mapIndexed { index, (data, date) ->
                    HistoryItem(1000 - index, data.title, data.url, date.dateTime.time, VisitType.TYPED)
                }
                .toList()
        )

        launchFragmentInContainer(themeResId = R.style.Theme_AppCompat) {
            HistoryFragment(historyViewModel)
        }

        onView(withId(R.id.empty_view)).check(isGone())
        onView(withId(R.id.history_list)).check(isVisible())
        onView(withId(R.id.history_search_list)).check(isGone())

        onView(withId(R.id.history_list))
            .check(matches(atPosition(0, hasDescendant(withText(R.string.history_clear_all)))))
            .check(matches(atPosition(1, allOf(
                `header title starts with`("Today"),
                `the title is`(TestData.Cliqz.title),
                `the url is`(TestData.Cliqz.url)
            ))))
            .check(matches(atPosition(2, allOf(
                `header title starts with`("Yesterday"),
                `the title is`(TestData.Mozilla.title),
                `the url is`(TestData.Mozilla.url)
            ))))
            .check(matches(atPosition(3, allOf(
                `header is visible`(),
                `the title is`(TestData.Daisy.title),
                `the url is`(TestData.Daisy.url)
            ))))
            .perform(scrollToPosition<ViewHolder>(4))
            .check(matches(atPosition(4, allOf(
                `header is gone`(),
                `the title is`(TestData.Facebook.title),
                `the url is`(TestData.Facebook.url)
            ))))
    }

    private fun setHistoryItems(items: List<HistoryItem>) {
        val data = items.asHistoryObservable() // Must be done outside a `theReturn`
        whenever(historyViewModel.historyItems).thenReturn(data)
    }
}

private fun `header title starts with`(prefix: String) = hasDescendant(
    allOf(
        withId(R.id.header_title), withText(startsWith(prefix))
    )
)

private fun `header visibility is`(visibility: ViewMatchers.Visibility) = hasDescendant(allOf(
    withId(R.id.header_title), withEffectiveVisibility(visibility)
))

private fun `header is visible`() = `header visibility is`(ViewMatchers.Visibility.VISIBLE)
private fun `header is gone`() = `header visibility is`(ViewMatchers.Visibility.GONE)

private fun `the title is`(title: String) = `the TextView has text`(R.id.title_view, title)
private fun `the url is`(url: String) = `the TextView has text`(R.id.url_view, url)
private fun `the TextView has text`(@IdRes id: Int, text: String) =
    hasDescendant(allOf(withId(id), withText(text)))

@Suppress("UNCHECKED_CAST")
private fun List<HistoryItem>.asHistoryObservable() = mock<HistoryObservable>().also { observable ->
    val pagedList = mock<PagedHistory>()
    whenever(pagedList[anyInt()]).then {
        this[it.arguments[0] as Int]
    }
    whenever(pagedList.size).thenReturn(this.size)
    whenever(pagedList.isEmpty()).thenReturn(this.isEmpty())
    whenever(observable.observe(any<Fragment>(), any<HistoryObserver>())).then { invocation ->
        val observer = invocation.arguments[1] as HistoryObserver
        observer.onChanged(pagedList)
        null
    }
}