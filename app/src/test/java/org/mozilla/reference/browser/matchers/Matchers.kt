/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.matchers

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

fun atPosition(position: Int, matcher: Matcher<View>) = object : BaseMatcher<View>() {
    override fun describeTo(description: Description?) {
        description?.apply {
            appendText("the item at position $position should match: ")
            matcher.describeTo(this)
        }
    }

    override fun matches(item: Any?): Boolean {
        requireNotNull(item)
        val recyclerView = item as RecyclerView
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
        return matcher.matches(viewHolder?.itemView)
    }
}