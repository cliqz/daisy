/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.freshtab.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatImageView
import mozilla.components.browser.menu.BrowserMenu
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.toolbar.R
import mozilla.components.support.ktx.android.content.res.resolveAttribute

/**
 * Borrowed from  Mozilla Android Component's browser-menu
 */
@Suppress("ViewConstructor") // This view is only instantiated in code
class MenuButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    @VisibleForTesting internal var menu: BrowserMenu? = null

    private val menuIcon = AppCompatImageView(context).apply {
        setImageResource(R.drawable.mozac_ic_menu)
        scaleType = ImageView.ScaleType.CENTER
        contentDescription = context.getString(R.string.mozac_browser_toolbar_menu_button)
    }

    init {
        setBackgroundResource(context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless))

        visibility = View.GONE
        isClickable = true
        isFocusable = true

        setOnClickListener {
            if (menu == null) {
                menu = menuBuilder?.build(context)
                val endAlwaysVisible = menuBuilder?.endOfMenuAlwaysVisible ?: false
                menu?.show(
                        anchor = this,
                        orientation = BrowserMenu.determineMenuOrientation(parent as View?),
                        endOfMenuAlwaysVisible = endAlwaysVisible
                ) { menu = null }
            } else {
                menu?.dismiss()
            }
        }

        addView(menuIcon)
    }

    var menuBuilder: BrowserMenuBuilder? = null
        set(value) {
            field = value
            menu?.dismiss()
            if (value != null) {
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
                menu = null
            }
        }

    /**
     * Declare that the menu items should be updated if needed.
     */
    fun invalidateMenu() {
        menu?.invalidate()
    }

    fun dismissMenu() {
        menu?.dismiss()
    }

    fun setColorFilter(@ColorInt color: Int) {
        menuIcon.setColorFilter(color)
    }
}
