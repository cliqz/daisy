/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ext

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.NavHostFragment
import org.mozilla.reference.browser.Components

// This file is from the Fenix project.

/**
 * Get the requireComponents of this application.
 */
val Fragment.requireComponents: Components
    get() = requireContext().components

fun Fragment.navController(): NavController {
    return NavHostFragment.findNavController(this)
}

fun Fragment.nav(@IdRes id: Int?, directions: NavDirections) {
    NavHostFragment.findNavController(this).nav(id, directions)
}

fun Fragment.nav(@IdRes id: Int?, directions: NavDirections, extras: Navigator.Extras) {
    NavHostFragment.findNavController(this).nav(id, directions, extras)
}

fun Fragment.nav(@IdRes id: Int?, directions: NavDirections, options: NavOptions) {
    NavHostFragment.findNavController(this).nav(id, directions, options)
}
