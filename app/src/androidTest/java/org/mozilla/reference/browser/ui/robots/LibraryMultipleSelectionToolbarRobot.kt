/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matchers.allOf
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.click

/*
 * Implementation of Robot Pattern for the multiple selection toolbar of History and Bookmarks menus.
 */
class LibraryMultipleSelectionToolbarRobot {

    fun verifyMultiSelectionCheckmark() = assertMultiSelectionCheckmark()

    fun verifyMultiSelectionCounter() = assertMultiSelectionCounter()

    fun verifyCloseToolbarButton() = assertCloseToolbarButton()

    class Transition {

        fun closeToolbarReturnToBookmarks(interact: BookmarkViewRobot.() -> Unit): BookmarkViewRobot.Transition {
            closeToolbarButton().click()

            BookmarkViewRobot().interact()
            return BookmarkViewRobot.Transition()
        }

        fun closeToolbarReturnToHistory(interact: HistoryViewRobot.() -> Unit): HistoryViewRobot.Transition {
            closeToolbarButton().click()

            HistoryViewRobot().interact()
            return HistoryViewRobot.Transition()
        }

        fun clickBookmarksMultiSelectionDelete(interact: BookmarkViewRobot.() -> Unit): BookmarkViewRobot.Transition {
            multiSelectionDeleteButton().click()

            BookmarkViewRobot().interact()
            return BookmarkViewRobot.Transition()
        }

        fun clickMultiSelectionDelete(interact: HistoryViewRobot.() -> Unit): HistoryViewRobot.Transition {
            multiSelectionDeleteButton().click()

            HistoryViewRobot().interact()
            return HistoryViewRobot.Transition()
        }
    }
}

fun multipleSelectionToolbar(
    interact: LibraryMultipleSelectionToolbarRobot.() -> Unit
): LibraryMultipleSelectionToolbarRobot.Transition {
    LibraryMultipleSelectionToolbarRobot().interact()
    return LibraryMultipleSelectionToolbarRobot.Transition()
}

private fun closeToolbarButton() = onView(withContentDescription(R.string.abc_action_bar_up_description))

private fun multiSelectionDeleteButton() = onView(withId(R.id.delete))

private fun assertMultiSelectionCheckmark() =
    onView(allOf(withId(R.id.checkmark), withEffectiveVisibility(Visibility.VISIBLE)))
        .check(matches(isDisplayed()))

private fun assertMultiSelectionCounter() {
    val resources = InstrumentationRegistry.getInstrumentation().context.resources
    onView(withText(resources.getQuantityString(R.plurals.bookmark_items_selected, 1, 1)))
        .check(matches(isDisplayed()))
}

private fun assertCloseToolbarButton() =
    closeToolbarButton().check(matches(isDisplayed()))
