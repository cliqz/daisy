package com.cliqz.browser.freshtab

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView

class FreshTab @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    private var view: LinearLayout = LinearLayout(context)

    init {
        view.orientation = LinearLayout.VERTICAL
        addView(view)
    }

    override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
        if (this.childCount > 0) {
            view.addView(child, params)
        } else {
            super.addView(child)
        }
    }

    override fun addView(child: View?) {
        if (this.childCount > 0) {
            view.addView(view)
        } else {
            super.addView(child)
        }
    }

    override fun addView(child: View?, width: Int, height: Int) {
        if (this.childCount > 0) {
            view.addView(view, width, height)
        } else {
            super.addView(child)
        }
    }

    override fun addView(child: View?, index: Int) {
        if (this.childCount > 0) {
            view.addView(view, index)
        } else {
            super.addView(child, index)
        }
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (this.childCount > 0) {
            view.addView(view, index, params)
        } else {
            super.addView(child, index, params)
        }
    }

}
