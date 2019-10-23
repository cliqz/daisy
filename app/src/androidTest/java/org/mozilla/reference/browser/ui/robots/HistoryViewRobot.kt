package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers

/**
 * @author Ravjit Uppal
 */
class HistoryViewRobot {

    fun verifyHistoryExists(url: String) = assertHistoryItem(url)
}

private fun historyItemUrl(url: String) = Espresso.onView(ViewMatchers.withText(url))

private fun assertHistoryItem(url: String) = historyItemUrl(url)
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
