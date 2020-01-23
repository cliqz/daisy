import org.gradle.api.Project
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

object Config {
    // Synchronized build configuration for all modules
    const val compileSdkVersion = 28
    const val minSdkVersion = 21
    const val targetSdkVersion = 28

    @JvmStatic
    fun generateDebugVersionName(project: Project): String {
        return readVersionFromFile(project)
    }

    @JvmStatic
    fun releaseVersionName(project: Project): String {
        return readVersionFromFile(project)
    }

    @JvmStatic
    private fun readVersionFromFile(project: Project): String {
        return File(project.rootProject.rootDir, "version.txt")
            .useLines {
                it.firstOrNull()?.trim() ?: "0.0.0"
            }
    }
}
