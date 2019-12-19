/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings.deletebrowsingdata

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import kotlinx.android.synthetic.main.delete_browsing_data_item.view.*
import org.mozilla.reference.browser.R

class DeleteBrowsingDataItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val titleView: TextView
        get() = title

    var isChecked: Boolean
        get() = checkbox.isChecked
        set(value) { checkbox.isChecked = value }

    var onCheckListener: ((Boolean) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.delete_browsing_data_item, this, true)

        setOnClickListener {
            checkbox.isChecked = !checkbox.isChecked
        }

        checkbox.setOnCheckedChangeListener { _, isChecked ->
            onCheckListener?.invoke(isChecked)
        }

        context.withStyledAttributes(attrs, R.styleable.DeleteBrowsingDataItem, defStyleAttr, 0) {
            val titleId = getResourceId(
                R.styleable.DeleteBrowsingDataItem_deleteBrowsingDataItemTitle,
                R.string.empty_string
            )
            title.text = resources.getString(titleId)
        }
    }
}
