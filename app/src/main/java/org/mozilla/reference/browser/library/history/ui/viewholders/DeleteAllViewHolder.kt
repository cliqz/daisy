package org.mozilla.reference.browser.library.history.ui.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.clear_all_history.view.*

class DeleteAllViewHolder(
    override val containerView: View,
    private val deleteAll: () -> Unit
) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

    fun bind(disableDeleteAll: Boolean) {
        containerView.clear_history.run {
            if (disableDeleteAll) {
                isEnabled = false
                alpha = disabledAlpha
            } else {
                isEnabled = true
                alpha = enabledAlpha
            }
        }
        containerView.clear_history.setOnClickListener { deleteAll() }
    }

    companion object {
        const val disabledAlpha = 0.4f
        const val enabledAlpha = 1f
    }
}
