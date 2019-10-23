/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_browser.*
import kotlinx.android.synthetic.main.fragment_browser.view.*
import mozilla.components.feature.downloads.DownloadsFeature
import mozilla.components.feature.downloads.manager.FetchDownloadManager
import mozilla.components.feature.findinpage.view.FindInPageView
import mozilla.components.feature.prompts.PromptFeature
import mozilla.components.feature.session.FullScreenFeature
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.session.SwipeRefreshFeature
import mozilla.components.feature.session.WindowFeature
import mozilla.components.feature.sitepermissions.SitePermissionsFeature
import mozilla.components.support.base.feature.BackHandler
import mozilla.components.support.base.feature.PermissionsFeature
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.ktx.android.view.enterToImmersiveMode
import mozilla.components.support.ktx.android.view.exitImmersiveModeIfNeeded
import org.mozilla.reference.browser.AppPermissionCodes.REQUEST_CODE_APP_PERMISSIONS
import org.mozilla.reference.browser.AppPermissionCodes.REQUEST_CODE_DOWNLOAD_PERMISSIONS
import org.mozilla.reference.browser.AppPermissionCodes.REQUEST_CODE_PROMPT_PERMISSIONS
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.UserInteractionHandler
import org.mozilla.reference.browser.downloads.DownloadService
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.pip.PictureInPictureIntegration

/**
 * Base fragment extended by [BrowserFragment] and [ExternalAppBrowserFragment].
 * This class only contains shared code focused on the main browsing content.
 * UI code specific to the app or to custom tabs can be found in the subclasses.
 */
@Suppress("TooManyFunctions")
abstract class BaseBrowserFragment : Fragment(), BackHandler, UserInteractionHandler {
    private val sessionFeature = ViewBoundFeatureWrapper<SessionFeature>()
    private val toolbarIntegration = ViewBoundFeatureWrapper<ToolbarIntegration>()
    private val contextMenuIntegration = ViewBoundFeatureWrapper<ContextMenuIntegration>()
    private val downloadsFeature = ViewBoundFeatureWrapper<DownloadsFeature>()
    private val promptsFeature = ViewBoundFeatureWrapper<PromptFeature>()
    private val fullScreenFeature = ViewBoundFeatureWrapper<FullScreenFeature>()
    private val findInPageIntegration = ViewBoundFeatureWrapper<FindInPageIntegration>()
    private val sitePermissionFeature = ViewBoundFeatureWrapper<SitePermissionsFeature>()
    private val pictureInPictureIntegration = ViewBoundFeatureWrapper<PictureInPictureIntegration>()
    private val swipeRefreshFeature = ViewBoundFeatureWrapper<SwipeRefreshFeature>()
    private val windowFeature = ViewBoundFeatureWrapper<WindowFeature>()

    private val backButtonHandler: List<ViewBoundFeatureWrapper<*>> = listOf(
        fullScreenFeature,
        findInPageIntegration,
        toolbarIntegration,
        sessionFeature
    )

    protected val sessionId: String?
        get() = arguments?.getString(SESSION_ID)

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_browser, container, false)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionFeature.set(
            feature = SessionFeature(
                requireComponents.core.sessionManager,
                requireComponents.useCases.sessionUseCases,
                engineView,
                sessionId),
            owner = this,
            view = view)

        toolbarIntegration.set(
            feature = ToolbarIntegration(
                requireContext(),
                toolbar,
                requireComponents.core.historyStorage,
                requireComponents.core.sessionManager,
                requireComponents.useCases.sessionUseCases,
                requireComponents.useCases.tabsUseCases,
                requireComponents.useCases.webAppUseCases,
                sessionId,
                activity?.supportFragmentManager),
            owner = this,
            view = view)

        contextMenuIntegration.set(
            feature = ContextMenuIntegration(
                requireContext(),
                requireFragmentManager(),
                requireComponents.core.store,
                requireComponents.useCases.tabsUseCases,
                requireComponents.useCases.contextMenuUseCases,
                view,
                sessionId),
            owner = this,
            view = view)

        downloadsFeature.set(
            feature = DownloadsFeature(
                requireContext(),
                store = requireComponents.core.store,
                useCases = requireComponents.core.downloadsUseCases,
                fragmentManager = childFragmentManager,
                downloadManager = FetchDownloadManager(
                    requireContext().applicationContext,
                    DownloadService::class
                ),
                onNeedToRequestPermissions = { permissions ->
                    requestPermissions(permissions, REQUEST_CODE_DOWNLOAD_PERMISSIONS)
                }),
            owner = this,
            view = view)

        promptsFeature.set(
            feature = PromptFeature(
                fragment = this,
                store = requireComponents.core.store,
                customTabId = sessionId,
                fragmentManager = requireFragmentManager(),
                onNeedToRequestPermissions = { permissions ->
                    requestPermissions(permissions, REQUEST_CODE_PROMPT_PERMISSIONS)
                }),
            owner = this,
            view = view)

        windowFeature.set(
            feature = WindowFeature(requireComponents.core.sessionManager),
            owner = this,
            view = view
        )

        fullScreenFeature.set(
            feature = FullScreenFeature(
                requireComponents.core.sessionManager,
                requireComponents.useCases.sessionUseCases,
                sessionId, ::fullScreenChanged),
            owner = this,
            view = view)

        findInPageIntegration.set(
            feature = FindInPageIntegration(
                requireComponents.core.store,
                sessionId,
                findInPageBar as FindInPageView,
                engineView),
            owner = this,
            view = view)

        sitePermissionFeature.set(
            feature = SitePermissionsFeature(
                context = requireContext(),
                fragmentManager = requireFragmentManager(),
                sessionManager = requireComponents.core.sessionManager,
                sessionId = sessionId
            ) { permissions ->
                requestPermissions(permissions, REQUEST_CODE_APP_PERMISSIONS)
            },
            owner = this,
            view = view
        )

        pictureInPictureIntegration.set(
            feature = PictureInPictureIntegration(
                requireComponents.core.sessionManager,
                requireActivity()
            ),
            owner = this,
            view = view
        )

        swipeRefreshFeature.set(
            feature = SwipeRefreshFeature(
                requireComponents.core.sessionManager,
                requireComponents.useCases.sessionUseCases.reload,
                view.swipeRefresh
            ),
            owner = this,
            view = view
        )
    }

    private fun fullScreenChanged(enabled: Boolean) {
        if (enabled) {
            activity?.enterToImmersiveMode()
            toolbar.visibility = View.GONE
        } else {
            activity?.exitImmersiveModeIfNeeded()
            toolbar.visibility = View.VISIBLE
        }
    }

    @CallSuper
    override fun onBackPressed(): Boolean {
        return backButtonHandler.any { it.onBackPressed() }
    }

    final override fun onHomePressed(): Boolean {
        return pictureInPictureIntegration.get()?.onHomePressed() ?: false
    }

    final override fun onPictureInPictureModeChanged(enabled: Boolean) {
        val session = requireComponents.core.sessionManager.selectedSession
        val fullScreenMode = session?.fullScreenMode ?: false
        // If we're exiting PIP mode and we're in fullscreen mode, then we should exit fullscreen mode as well.
        if (!enabled && fullScreenMode) {
            onBackPressed()
            fullScreenChanged(false)
        }
    }

    final override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        val feature: PermissionsFeature? = when (requestCode) {
            REQUEST_CODE_DOWNLOAD_PERMISSIONS -> downloadsFeature.get()
            REQUEST_CODE_PROMPT_PERMISSIONS -> promptsFeature.get()
            REQUEST_CODE_APP_PERMISSIONS -> sitePermissionFeature.get()
            else -> null
        }
        feature?.onPermissionsResult(permissions, grantResults)
    }

    companion object {
        private const val SESSION_ID = "session_id"

        @JvmStatic
        protected fun Bundle.putSessionId(sessionId: String?) {
            putString(SESSION_ID, sessionId)
        }
    }

    final override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        promptsFeature.withFeature { it.onActivityResult(requestCode, resultCode, data) }
    }
}
