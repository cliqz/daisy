/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.ContentViewCallback
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.daisy_snackbar.view.snackbar_btn
import kotlinx.android.synthetic.main.daisy_snackbar.view.snackbar_text
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.findSuitableParent

class DaisySnackbar private constructor(
    val parent: ViewGroup,
    content: View,
    contentViewCallback: DaisySnackbarCallback,
    isError: Boolean
) : BaseTransientBottomBar<DaisySnackbar>(parent, content, contentViewCallback) {

    init {
        view.setBackgroundColor(Color.WHITE)

        setAppropriateTextColor(isError)
    }

    fun setText(text: String) = this.apply {
        this.view.snackbar_text.text = text
    }

    fun setAction(text: String, action: () -> Unit) = this.apply {
        this.view.snackbar_btn.apply {
            setText(text)
            visibility = View.VISIBLE
            setOnClickListener {
                action.invoke()
                dismiss()
            }
        }
    }

    private fun setAppropriateTextColor(isError: Boolean) {
        if (isError) {
            view.snackbar_text.setBackgroundColor(ContextCompat.getColor(parent.context, R.color.venetian_red))
        }
    }

    companion object {
        const val LENGTH_LONG = Snackbar.LENGTH_LONG
        const val LENGTH_SHORT = Snackbar.LENGTH_SHORT

        fun make(
            view: View,
            duration: Int,
            isError: Boolean = false
        ): DaisySnackbar {
            val parent = view.findSuitableParent() ?: run {
                throw IllegalArgumentException("No suitable parent found for the given view")
            }
            val inflater = LayoutInflater.from(parent.context)
            val content = inflater.inflate(R.layout.daisy_snackbar, parent, false)

            val callback = DaisySnackbarCallback(content)
            return DaisySnackbar(parent, content, callback, isError).also {
                it.duration = duration
            }
        }
    }
}

private class DaisySnackbarCallback(
    private val content: View
) : ContentViewCallback {

    override fun animateContentIn(delay: Int, duration: Int) {
        content.translationY = (content.height).toFloat()
        content.animate().apply {
            translationY(defaultYTranslation)
            setDuration(animateInDuration)
            startDelay = delay.toLong()
        }
    }

    override fun animateContentOut(delay: Int, duration: Int) {
        content.translationY = defaultYTranslation
        content.animate().apply {
            translationY((content.height).toFloat())
            setDuration(animateOutDuration)
            startDelay = delay.toLong()
        }
    }

    companion object {
        private const val defaultYTranslation = 0f
        private const val animateInDuration = 200L
        private const val animateOutDuration = 150L
    }
}
