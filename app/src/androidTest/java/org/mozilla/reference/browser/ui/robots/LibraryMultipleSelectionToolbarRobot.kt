/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.mozilla.reference.browser.R
import org.hamcrest.Matchers.allOf
import org.mozilla.reference.browser.helpers.click

/*
 * Implementation of Robot Pattern for the multiple selection toolbar of History and Bookmarks menus.
 */
class LibraryMultipleSelectionToolbarRobot {

    fun verifyMultiSelectionCheckmark() = assertMultiSelectionCheckmark()

    fun verifyMultiSelectionCounter() = assertMultiSelectionCounter()

    fun verifyCloseToolbarButton() = assertCloseToolbarButton()

    class Transition {
        fun closeToolbarReturnToHistory(interact: HistoryViewRobot.() -> Unit): HistoryViewRobot.Transition {
            closeToolbarButton().click()

            HistoryViewRobot().interact()
            return HistoryViewRobot.Transition()
        }
        fun clickMultiSelectionDelete(interact: HistoryViewRobot.() -> Unit) : HistoryViewRobot.Transition {
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

private fun assertMultiSelectionCounter() =
    onView(withText("1 selected"))
        .check(matches(isDisplayed()))

private fun assertCloseToolbarButton() =
    closeToolbarButton().check(matches(isDisplayed()))
