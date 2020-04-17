/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.library.bookmarks.ui

import android.content.Context
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.library.LibraryItemMenu

class BookmarkItemMenu(
    private val context: Context,
    private val item: BookmarkNode,
    private val itemTapped: (BookmarkItemMenu.Item) -> Unit = {}
) : LibraryItemMenu {

    sealed class Item {
        object Edit : Item()
        object OpenInNewTab : Item()
        object OpenInPrivateTab : Item()
        object Delete : Item()
    }

    override val menuBuilder by lazy { BrowserMenuBuilder(menuItems) }

    private val menuItems by lazy {
        listOfNotNull(
            SimpleBrowserMenuItem(context.getString(R.string.bookmark_menu_edit_button)) {
                itemTapped.invoke(Item.Edit)
            },
            if (item.type == BookmarkNodeType.ITEM) {
                SimpleBrowserMenuItem(context.getString(R.string.bookmark_menu_open_in_new_tab_button)) {
                    itemTapped.invoke(Item.OpenInNewTab)
                }
            } else null,
            if (item.type == BookmarkNodeType.ITEM) {
                SimpleBrowserMenuItem(context.getString(R.string.bookmark_menu_open_in_private_tab_button)) {
                    itemTapped.invoke(Item.OpenInPrivateTab)
                }
            } else null,
            SimpleBrowserMenuItem(
                context.getString(R.string.bookmark_menu_delete_button),
                textColorResource = R.color.venetian_red
            ) {
                itemTapped.invoke(Item.Delete)
            }
        )
    }
}
