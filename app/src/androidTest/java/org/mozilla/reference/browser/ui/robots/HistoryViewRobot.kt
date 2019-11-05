package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import org.mozilla.reference.browser.helpers.click

/**
 * @author Ravjit Uppal
 */
class HistoryViewRobot {

    fun verifyHistoryExists(url: String) = assertHistoryItem(url)

    class Transition {
        fun openHistoryUrl(url: String, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            historyItemUrl(url).click()
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }
}

private fun historyItemUrl(url: String) = Espresso.onView(ViewMatchers.withText(url))

private fun assertHistoryItem(url: String) = historyItemUrl(url)
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
