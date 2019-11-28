package org.mozilla.reference.browser.library

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.two_line_list_item_layout.view.*
import org.mozilla.reference.browser.ext.asActivity

/**
 * Borrowed from the Fenix project
 */
open class LibraryPageView(
    override val containerView: ViewGroup
) : LayoutContainer {

    protected val context: Context inline get() = containerView.context
    protected val activity = context.asActivity()

    protected fun setUiForNormalMode(title: String, libraryList: RecyclerView) {
        activity?.title = title
        libraryList.children.forEach {
            it.meta_btn?.visibility = View.VISIBLE
        }
    }

    protected fun setUiForEditingMode(title: String, libraryList: RecyclerView) {
        activity?.title = title
        libraryList.children.forEach {
            it.meta_btn?.visibility = View.INVISIBLE
        }
    }
}
