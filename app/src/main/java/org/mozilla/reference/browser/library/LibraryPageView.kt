/* This Source Code Form is subject to the terms of the Mozilla Public
   License, v. 2.0. If a copy of the MPL was not distributed with this
   file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.library

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MenuRes
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.two_line_list_item_with_action_layout.view.*
import org.mozilla.reference.browser.ext.asActivity

/**
 * Borrowed from the Fenix project
 */
open class LibraryPageView(
    override val containerView: ViewGroup
) : LayoutContainer {

    protected val context: Context inline get() = containerView.context
    protected val activity = context.asActivity()

    protected fun setUiForNormalMode(
        title: String,
        libraryList: RecyclerView,
        toolbar: LibraryToolbar,
        @MenuRes toolbarMenuId: Int
    ) {
        toolbar.updateToolbar(toolbarMenuId, title)
        libraryList.children.forEach {
            it.action_btn?.visibility = View.VISIBLE
        }
    }

    protected fun setUiForEditingMode(
        title: String,
        libraryList: RecyclerView,
        toolbar: LibraryToolbar,
        @MenuRes toolbarMenuId: Int
    ) {
        toolbar.updateToolbar(toolbarMenuId, title)
        libraryList.children.forEach {
            it.action_btn?.visibility = View.INVISIBLE
        }
    }
}
