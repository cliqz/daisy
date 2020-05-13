/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.ui.robots

import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.waitAndInteract
import org.mozilla.reference.browser.helpers.click

class BookmarkViewRobot {

    fun verifyBookmarkViewExists() = assertBookmarkViewExists()

    fun verifyEmptyBookmarkView() = assertEmptyBookmarkView()

    fun verifyBookmarkItemExists(url: String) {
        mDevice.waitAndInteract(Until.findObject(By.text(url))) {}
        assertBookmarkItem(url)
    }

    fun clickBookmarkItemDelete() {
        mDevice.waitAndInteract(
            Until.findObject(By.res("com.cliqz.browser.daisy.debug:id/action_btn"))) {}
        bookmarkItemActionButton().click()
        bookmarkItemDeleteButton().click()
    }

    class Transition {

        fun openBookmarkUrl(url: Uri, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            // onView(withId(R.id.title_view)).click()
            bookmarkItemUrl(url.toString()).click()
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }
}

private fun bookmarkItemUrl(url: String) = onView(withText(url))

private fun bookmarkItemActionButton() = onView(withId(R.id.action_btn))

private fun bookmarkItemDeleteButton() = onView(withText(R.string.bookmark_menu_delete_button))

private fun assertBookmarkViewExists() {
    onView(withText("Bookmarks"))
        .check(matches(isDisplayed()))
}

private fun assertEmptyBookmarkView() {
    onView(withId(R.id.empty_view)).apply {
        check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        check(matches(withText(R.string.bookmarks_view_empty_text)))
    }
}

private fun assertBookmarkItem(url: String) =
    bookmarkItemUrl(url)
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
