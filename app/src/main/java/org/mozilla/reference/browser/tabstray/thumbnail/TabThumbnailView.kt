/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabstray.thumbnail

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatImageView

class TabThumbnailView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        if (bm != null) {
            setMatrix()
        }
    }

    @VisibleForTesting
    public override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = super.setFrame(l, t, r, b)
        if (changed) {
            setMatrix()
        }
        return changed
    }

    private fun setMatrix() = drawable?.let {
        val matrix = imageMatrix
        val scaleFactor = width / it.intrinsicWidth.toFloat()
        matrix.setScale(scaleFactor, scaleFactor, 0f, 0f)
        imageMatrix = matrix
    }
}
