package org.mozilla.reference.browser.topsites.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.GridView
import android.widget.LinearLayout
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.feature.session.SessionUseCases
import org.mozilla.reference.browser.database.Topsite
import org.mozilla.reference.browser.library.history.usecases.HistoryUseCases

/**
 * @author Ravjit Uppal
 */
class TopSitesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0) : GridView(context, attrs, defStyleAttr), TopSitesPresenter.View {

    var presenter: TopSitesPresenter? = null
    private var topSitesadapter: TopSitesAdapter? = null

    fun init(loadUrlUseCase: SessionUseCases.LoadUrlUseCase, getTopSitesUseCase: HistoryUseCases.GetTopSitesUseCase, browserIcons: BrowserIcons) {
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        gravity = Gravity.CENTER
        topSitesadapter = TopSitesAdapter(browserIcons)
        adapter = topSitesadapter
        numColumns = 5
        presenter = TopSitesPresenter(this, loadUrlUseCase, getTopSitesUseCase)
        setOnItemClickListener {parent, view, position, id ->
            presenter?.onTopSiteClicked(topSitesadapter?.topSites?.get(position)?.url)
        }
    }

    fun updateTopSites() {
        presenter?.fetchTopSites()
    }

    override fun updateTopSitesData(topSites: List<Topsite>) {
        topSitesadapter?.topSites = topSites
    }

}