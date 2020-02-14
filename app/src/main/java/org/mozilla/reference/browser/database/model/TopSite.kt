/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.database.model

/**
 * This class holds basic information about a "topSite"
 *
 * @param id the urls database id for this topSite
 * @param url the topSite url
 * @param domain the pre-extracted domain (from url)
 * @param title the title of the page
 */
class TopSite(
    val id: Long,
    val url: String,
    val domain: String,
    val title: String?
)
