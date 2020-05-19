/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.tabstray

import android.graphics.Bitmap
import mozilla.components.concept.tabstray.Tab
import java.util.UUID

internal fun tabWithUrl(url: String, title: String = "", thumbnail: Bitmap? = null) =
    Tab(
        id = UUID.randomUUID().toString(),
        url = url,
        title = title,
        thumbnail = thumbnail
    )