package org.mozilla.reference.browser.topsites.data

/**
 * @author Ravjit Uppal
 */

/**
 *
 * @param id the urls database id for this topsite
 * @param url the topsite url
 * @param domain the pre-extracted domain (from url)
 * @param title the title of the page
 */
data class TopSite(val id: Long,
                   val url: String,
                   val domain: String,
                   val title: String?)