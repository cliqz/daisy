package org.mozilla.reference.browser.history.data

import mozilla.components.concept.storage.VisitType

data class HistoryItem(
    val id: Int,
    val title: String,
    val url: String,
    val visitTime: Long,
    val visitType: VisitType
)
