/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mozilla.reference.browser.ext

import org.mockito.stubbing.Stubber

fun <T> Stubber.whenever(mock: T): T = this.`when`(mock)