package org.mozilla.reference.browser.topsites.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.feature.session.SessionUseCases.LoadUrlUseCase
import org.mozilla.reference.browser.database.Topsite
import org.mozilla.reference.browser.library.history.usecases.HistoryUseCases

/**
 * @author Ravjit Uppal
 */
class TopSitesPresenter(
    private val view: View,
    private val loadUrlUseCase: LoadUrlUseCase,
    private val topSitesUseCase: HistoryUseCases.GetTopSitesUseCase
) {

    interface View {
        fun updateTopSitesData(topSites: List<Topsite>)
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