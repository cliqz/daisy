/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings.deletebrowsingdata

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.content.withStyledAttributes
import com.google.android.material.checkbox.MaterialCheckBox
import org.mozilla.reference.browser.R

class DeleteBrowsingDataItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.checkboxStyle
) : MaterialCheckBox(context, attrs, defStyleAttr) {

    init {
        isChecked = true

        TypedValue().apply {
            context.theme.resolveAttribute(R.attr.selectableItemBackgroundBorderless, this, true)
            setBackgroundResource(resourceId)
        }

        setPadding(
            paddingLeft + resources.getDimensionPixelSize(R.dimen.margin_padding_size_medium),
            paddingTop,
            paddingRight,
            paddingBottom
        )

        context.withStyledAttributes(attrs, R.styleable.DeleteBrowsingDataItem, defStyleAttr, 0) {
            val titleId = getResourceId(
                R.styleable.DeleteBrowsingDataItem_deleteBrowsingDataItemTitle,
                R.string.empty_string
            )
            text = resources.getString(titleId)
        }
    }
}
