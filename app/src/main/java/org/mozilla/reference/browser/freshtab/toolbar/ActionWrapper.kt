/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.freshtab.toolbar

import android.view.View
import mozilla.components.concept.toolbar.Toolbar

/**
 * A wrapper helper to pair a Toolbar.Action with an optional View.
 */
internal class ActionWrapper(
    var actual: Toolbar.Action,
    var view: View? = null
)
