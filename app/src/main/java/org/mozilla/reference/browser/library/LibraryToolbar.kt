/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.transition.Fade
import androidx.transition.Scene
import androidx.transition.TransitionManager
import com.google.android.material.appbar.AppBarLayout
import mozilla.components.support.base.observer.Observable
import mozilla.components.support.base.observer.ObserverRegistry
import mozilla.components.support.ktx.android.view.hideKeyboard
import mozilla.components.support.ktx.android.view.showKeyboard
import org.mozilla.reference.browser.R

@SuppressLint("PrivateResource")
class LibraryToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    delegate: Observable<Observer> = ObserverRegistry()
) : AppBarLayout(context, attrs, defStyleAttr),
    Observable<LibraryToolbar.Observer> by delegate/*, HistoryBar*/ {

    interface Observer {

        fun close()

        fun delete()

        fun searchOpened()

        fun searchClosed()

        fun searchQueryChanged(query: String)
    }

    var title: CharSequence
        get() = toolbarTitle.text
        set(value) {
            toolbarTitle.text = value
        }

    private val sceneA: Scene

    private val sceneB: Scene

    private val toolbarTitle: TextView
    private val toolbar: Toolbar
    private val search: EditText
    private val closeSearch: ImageButton
    private val clearSearch: ImageButton
    private val searchContainer: ViewGroup

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.library_toolbar, this, true)
        toolbarTitle = view.findViewById(R.id.toolbar_title)
        toolbar = view.findViewById(R.id.inner_toolbar)
        search = view.findViewById(R.id.search)
        closeSearch = view.findViewById(R.id.close_search)
        clearSearch = view.findViewById(R.id.clear_search)
        searchContainer = view.findViewById(R.id.search_container)

        sceneA = Scene(this, toolbar)
        sceneB = Scene(this, searchContainer)

        setNormalMode()

        // Setting up the toolbar
        toolbar.setNavigationIcon(R.drawable.mozac_ic_back)
        toolbar.setNavigationContentDescription(R.string.abc_action_bar_up_description)
        toolbar.setNavigationOnClickListener { notifyObservers { close() } }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.close -> {
                    notifyObservers { close() }
                    true
                }
                R.id.delete -> {
                    notifyObservers { delete() }
                    true
                }
                R.id.history_search -> {
                    notifyObservers { searchOpened() }
                    search.showKeyboard()
                    setSearchMode()
                    true
                }
                else -> throw IllegalArgumentException("Invalid menu item")
            }
        }

        closeSearch.setOnClickListener {
            notifyObservers { searchClosed() }
            search.text.clear()
            search.hideKeyboard()
            hideSearch()
        }

        clearSearch.setOnClickListener {
            search.text.clear()
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                notifyObservers { searchQueryChanged(s.toString()) }
                val length = s?.length ?: 0
                clearSearch.isEnabled = length > 0
                clearSearch.isClickable = length > 0
            }
        })
    }

    fun setNormalMode() {
        hideSearch()
        toolbarTitle.text = context.getString(R.string.history_screen_title)
        toolbar.invalidate()
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.history_menu)
    }

    fun setEditingMode() {
        hideSearch()
        toolbar.invalidate()
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.history_multi_select_menu)
    }

    private fun setSearchMode() = TransitionManager.go(sceneB, Fade())

    private fun hideSearch() = TransitionManager.go(sceneA, Fade())
}
