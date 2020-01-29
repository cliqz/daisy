/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import android.content.Intent
import android.net.Uri
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import mozilla.components.support.ktx.android.content.appName
import org.mozilla.reference.browser.ext.waitAndInteract
import java.util.regex.Pattern

private val appName = instrumentation.targetContext.appName

class ExternalIntentsRobot {

    class Transition {

        fun openUrlByImplicitIntent(uri: Uri, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = uri
            instrumentation.targetContext.startActivity(intent)

            mDevice.waitForIdle()

            val justOnce = mDevice.findObject(By.text(Pattern.compile("just once", Pattern.CASE_INSENSITIVE)))
            val openWith = mDevice.findObject(By.textStartsWith("Open with"))
            if (!justOnce.isEnabled) {
                mDevice.waitAndInteract(Until.findObject(By.text(appName))) {
                    click()
                }
            } else if (!openWith.text.contains(appName)) {
                mDevice.findObject(By.text(appName)).click()
            }
            justOnce.click()

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
