package org.mozilla.reference.browser.topsites.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.concept.storage.VisitInfo
import mozilla.components.feature.session.SessionUseCases.LoadUrlUseCase
import org.mozilla.reference.browser.history.usecases.HistoryUseCases

/**
 * @author Ravjit Uppal
 */
class TopSitesPresenter(
    private val view: View,
    private val loadUrlUseCase: LoadUrlUseCase,
    private val topSitesUseCase: HistoryUseCases.GetTopSitesUseCase
) {

    interface View {
        fun updateTopSitesData(topSites: List<VisitInfo>)
    }

    init {
        fetchTopSites()
    }

    fun fetchTopSites() = GlobalScope.launch(Dispatchers.Main) {
        val historyInfo = withContext(Dispatchers.IO) { topSitesUseCase.invoke() }
        view.updateTopSitesData(historyInfo)
    }

    fun onTopSiteClicked(url: String?) {
        if (url != null) {
            loadUrlUseCase.invoke(url)
        }
    }
}