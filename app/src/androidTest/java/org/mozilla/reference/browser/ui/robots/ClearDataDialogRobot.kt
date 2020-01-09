/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers

class ClearDataDialogRobot {
    fun verifyClearDataTitle() = clearDataTitleExists()

    class Transition {
        fun clearDataDialog(interact: ClearDataDialogRobot.() -> Unit): ClearDataDialogRobot.Transition {
            return ClearDataDialogRobot.Transition()
        }
    }
}

private fun clearDataTitleExists() = onView(ViewMatchers.withText("Clear Private Data"))
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
