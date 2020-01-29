/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.hamcrest.Matchers.allOf
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.waitAndInteract
import org.mozilla.reference.browser.helpers.click

class HistoryViewRobot {

    fun verifyHistoryViewExists() = assertHistoryViewExists()

    fun verifyDeleteHistoryButtonExists() = assertDeleteHistoryButtonExists()

    fun verifyHistoryItemExists(url: String) {
        mDevice.waitAndInteract(Until.findObject(By.text(url))) {}
        assertHistoryItem(url)
    }

    fun verifyEmptyHistoryView() {
        mDevice.waitAndInteract(Until.findObject(By.text("No history to show"))) {}
        assertEmptyHistoryView()
    }

    fun verifyDeleteConfirmationMessage() = assertDeleteConfirmationMessage()

    fun verifyTimeGroupTodayExists() = assertTimeGroupTodayTest()

    fun clickDeleteHistoryButton() {
        historyDeleteButton().click()
    }

    fun clickHistoryItemDelete() {
        mDevice.waitAndInteract(
            Until.findObject(By.res("com.cliqz.browser.daisy.debug:id/meta_btn"))) {}
        historyItemDeleteButton().click()
    }

    fun clickConfirmDeleteAllHistory() {
        onView(withText("Clear"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .click()
    }

    class Transition {
        fun openHistoryUrl(url: String, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            historyItemUrl(url).click()
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }
}

private fun historyItemUrl(url: String) = onView(withText(url))

private fun historyDeleteButton() = onView(withId(R.id.clear_history))

private fun historyItemDeleteButton() = onView(withId(R.id.meta_btn))

private fun assertHistoryViewExists() {
    onView(withText("History"))
        .check(matches(isDisplayed()))
}

private fun assertDeleteHistoryButtonExists() {
    onView(withText("Clear Browsing History"))
        .check(matches(isDisplayed()))
}

private fun assertEmptyHistoryView() =
    onView(
        allOf(
            withId(R.id.empty_view),
            withEffectiveVisibility(Visibility.VISIBLE)
        )
    )
        .check(matches(withText("No history to show")))

private fun assertHistoryItem(url: String) =
    historyItemUrl(url)
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

private fun assertDeleteConfirmationMessage() =
    onView(withText("Are you sure you want to clear history?"))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))

private fun assertTimeGroupTodayTest() =
    onView(withText("Today"))
        .check(matches(isDisplayed()))
