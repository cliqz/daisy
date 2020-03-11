package com.cliqz.browser.freshtab

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView

class FreshTab @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    private var content: LinearLayout = LinearLayout(context)

    init {
        content.orientation = LinearLayout.VERTICAL
        super.addView(content, -1, super.generateDefaultLayoutParams())
    }

    override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
        addView(child, -1, params)
    }

    override fun addView(child: View?) {
        val params = child?.layoutParams ?: generateDefaultLayoutParams()
        addView(child, -1, params)
    }

    override fun addView(child: View?, width: Int, height: Int) {
        throw NotImplementedError("Do not use this variant")
    }

    override fun addView(child: View?, index: Int) {
        val params = child?.layoutParams ?: generateDefaultLayoutParams()
        addView(child, index, params)
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        content.addView(child, index, params)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
