/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.waitAndInteract
import org.mozilla.reference.browser.topsites.ui.TopSitesViewHolder

/**
 * Implementation of Robot Pattern for Fresh Tab.
 */
class FreshTabRobot {

    fun verifyTopSite(title: String) = assertTopSiteText(title)

    class Transition {

        fun openTopSite(title: String, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            topSitesView()
                .perform(RecyclerViewActions.actionOnItem<TopSitesViewHolder>(
                    hasDescendant(withText(title)), click()))

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun openTopSiteInNewTab(title: String, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            topSitesView()
                .perform(RecyclerViewActions.actionOnItem<TopSitesViewHolder>(
                    hasDescendant(withText(title)), longClick()))

            openInNewTabButton().perform(click())

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }
}

fun freshTab(interact: FreshTabRobot.() -> Unit): FreshTabRobot.Transition {
    FreshTabRobot().interact()
    return FreshTabRobot.Transition()
}

private fun topSitesView() = onView(withId(R.id.topSitesView))
private fun openInNewTabButton() = onView(withId(R.id.open_in_new_tab))

private fun assertTopSiteText(url: String) {
    mDevice.waitAndInteract(Until.findObject(By.textContains(url))) {}
}
