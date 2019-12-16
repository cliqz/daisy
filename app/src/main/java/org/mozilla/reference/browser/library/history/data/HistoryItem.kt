package org.mozilla.reference.browser.library.history.data

import mozilla.components.concept.storage.VisitType

data class HistoryItem(
    val id: Int,
    val title: String,
    val url: String,
    val visitTime: Long,
    val visitType: VisitType
)
