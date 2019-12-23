package org.mozilla.reference.browser.ui.robots

import android.content.Intent
import android.net.Uri
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime

class ExternalIntentsRobot {

    class Transition {

        fun openUrlByImplicitIntent(uri: Uri, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = uri
            instrumentation.targetContext.startActivity(intent)

            mDevice.waitForIdle()

            intentChooser().swipe(Direction.UP, 0.5f)

            val mostRecentBrowser = intentChooserMostRecentBrowser()
            if (mostRecentBrowser != null) {
                intentChooserJustOnceButton().click()
            } else {
                val intentChooserDaisy = intentChooserDaisy()
                intentChooserDaisy.click()
                intentChooserJustOnceButton().click()
            }

            mDevice.waitForIdle()

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }
}

fun externalIntents(interact: ExternalIntentsRobot.() -> Unit): ExternalIntentsRobot.Transition {
    ExternalIntentsRobot().interact()
    return ExternalIntentsRobot.Transition()
}

private fun intentChooserDaisy() = mDevice
    .wait(Until.findObject(By.textContains("Daisy")), waitingTime)

private fun intentChooserMostRecentBrowser() = mDevice
    .wait(Until.findObject(By.textContains("Open with Daisy")), waitingTime)

private fun intentChooser() = mDevice
    .wait(Until.findObject(By.textContains("Open with")), waitingTime).parent.parent.parent

private fun intentChooserJustOnceButton() = mDevice
    .findObject(By.text("JUST ONCE"))
