/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.history.ui

import android.os.Build
import android.os.SystemClock
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import mozilla.components.concept.storage.SearchResult
import mozilla.components.concept.storage.VisitType
import mozilla.components.support.test.any
import mozilla.components.support.test.mock
import mozilla.components.support.test.robolectric.testContext
import mozilla.components.support.test.whenever
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.startsWith
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.assertions.hasItemsCount
import org.mozilla.reference.browser.assertions.isEnabled
import org.mozilla.reference.browser.assertions.isGone
import org.mozilla.reference.browser.assertions.isNotEnabled
import org.mozilla.reference.browser.assertions.isVisible
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.library.history.data.HistoryItem
import org.mozilla.reference.browser.matchers.atPosition
import org.robolectric.shadows.ShadowAlertDialog
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
    private lateinit var historyInteractor: HistoryInteractor

    @Before
    fun setUp() {
        with(testContext.components.useCases) {
            historyViewModel = spy(HistoryViewModel(historyUseCases))
        }
        historyInteractor = mock()
    }

    @Test
    fun `the empty history message should be visible`() {
        setHistoryItems(listOf())

        launchFragment()

        onView(withId(R.id.toolbar_title)).check(matches(withText(R.string.history_screen_title)))
        onView(withId(R.id.empty_view)).check(isVisible())
        onView(withId(R.id.history_list)).check(isGone())
        onView(withId(R.id.history_search_list)).check(isGone())
    }

    @Test
    fun `should display today, yesterday and date`() {
        SystemClock.setCurrentTimeMillis(Dates.Now.dateTime.time)
        setupTestData()

        launchFragment()

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

    @Test
    fun `should switch to edit mode`() {
        setupTestData()

        launchFragment()

        onView(withText(TestData.Cliqz.title))
            .perform(longClick())

        verify(historyInteractor).select(any())
        onView(withId(R.id.toolbar_title))
            .check(matches(withText("1 selected")))
    }

    @Test
    fun `should search`() {
        setupTestData()

        launchFragment()

        onView(withContentDescription(R.string.search_widget_text_short)).perform(click())
        onView(withId(R.id.clear_search)).check(isVisible()).check(isNotEnabled())
        onView(withId(R.id.close_search)).check(isVisible())
        onView(withId(R.id.search))
            .check(isVisible())
            .perform(typeText(TestData.Cliqz.title))
        onView(withId(R.id.clear_search)).check(isEnabled())
        onView(withId(R.id.history_search_list))
            .check(isVisible())
            .check(hasItemsCount(1))
    }

    @Test
    fun `should be able to open a search result`() {
        setupTestData()

        launchFragment()

        onView(withContentDescription(R.string.search_widget_text_short)).perform(click())
        onView(withId(R.id.search)).perform(typeText("Face"))

        onView(withId(R.id.history_search_list))
            .perform(RecyclerViewActions.actionOnItemAtPosition<ViewHolder>(0, click()))

        verify(historyInteractor).open(any(), eq(false), eq(false))
    }

    @Test
    fun `should display a dialog when deleting the history`() {
        Assume.assumeTrue("This runs only in robolectric", "robolectric" == Build.FINGERPRINT)
        setupTestData()

        launchFragment()

        onView(withText(R.string.history_clear_all))
            .perform(click())

        verify(historyInteractor).onDeleteAll()
        assertTrue(ShadowAlertDialog.getLatestDialog().isShowing)
    }

    private fun setupTestData() = setHistoryItems(
        TestData.values()
            .zip(Dates.values().drop(1)) // Zip the TestData with the Dates skipping Dates.Now
            .mapIndexed { index, (data, date) ->
                HistoryItem(1000 - index, data.title, data.url, date.dateTime.time, VisitType.TYPED)
            }
            .toList()
    )

    private fun setHistoryItems(items: List<HistoryItem>) {
        val data = items.asHistoryObservable() // Must be done outside a `theReturn`
        whenever(historyViewModel.historyItems).thenReturn(data)
        whenever(historyViewModel.searchHistory(anyString())).then { invocation ->
            val query = invocation.arguments[0] as String
            return@then items
                .filter {
                    it.title.contains(query) || it.url.contains(query)
                }
                .map {
                    SearchResult(it.id.toString(), it.url, 1000, it.title)
                }
        }
    }

    private fun launchFragment() {
        val scenario = launchFragmentInContainer(themeResId = R.style.Theme_AppCompat) {
            HistoryFragment(historyViewModel, historyInteractor)
        }

        // Making the mocked interactor perform some real operation in order to check ui elements
        // and dialogs visibility
        scenario.onFragment { fragment ->
            whenever(historyInteractor.onDeleteAll()).then { fragment.deleteAll() }
            whenever(historyInteractor.select(any())).then {
                historyViewModel.addToSelectedItems(it.arguments[0] as HistoryItem)
            }
        }
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