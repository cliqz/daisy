/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.assertions

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers

import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals

// Visibility
fun isVisible() = matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
fun isInvisible() = matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE))
fun isGone() = matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE))

// Enabled/Disabled
fun isEnabled() = matches(ViewMatchers.isEnabled())
fun isNotEnabled() = matches(not(ViewMatchers.isEnabled()))

// RecyclerView
fun hasItemsCount(count: Int) = ViewAssertion { view, noViewFoundException ->
    if (view is RecyclerView) {
        assertEquals("Item count is different", count, view.adapter?.itemCount ?: -1)
    } else {
        throw noViewFoundException
    }
}

fun hasNoItem() = hasItemsCount(0)
