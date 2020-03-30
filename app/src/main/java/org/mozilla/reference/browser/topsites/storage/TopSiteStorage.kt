/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.topsites.storage

import org.mozilla.reference.browser.database.model.TopSite

/**
 * An interface which defines the storage implementation of top sites.
 */
interface TopSiteStorage {

    /**
     * Retrieves a list of top most visited websites.
     * @param limit the number of items to return
     * @return a list of [TopSite]. The time stamp of these elements is always -1.
     */
    fun getTopSites(limit: Int): List<TopSite>

    /**
     * Records the [domains] to be maintained in a 'block list'.
     * @param domains one more more domain names
     */
    fun blockDomainsForTopSites(vararg domains: String)

    /**
     * Remove domains from the 'block list' which are blocked from top sites.
     */
    fun restoreTopSites()
}
