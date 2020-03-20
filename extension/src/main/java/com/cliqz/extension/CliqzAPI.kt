package com.cliqz.extension

import kotlinx.coroutines.CoroutineScope
import org.json.JSONObject

const val ANTITRACKING_MODULE = "antitracking"
const val ADBLOCKER_MODULE = "adblocker"
const val ANOLYSIS_MODULE = "anolysis"
const val AUTOCONSENT_MODULE = "autoconsent"
const val COOKIEMONSTER_MODULE = "cookie-monster"
const val HUMANWEB_MODULE = "human-web-lite"
const val INSIGHTS_MODULE = "insights"

class CliqzAPI(val extension: CliqzExtensionFeature, private val scope: CoroutineScope) {

    /**
     * Gets the current enabled status of Cliqz modules running in the extension.
     */
    suspend fun getModuleStatus(): ModuleStatus? {
        val moduleStatus = extension.callActionAsync(scope, "core", "status").await()
                .let { (it as JSONObject).getJSONObject("modules") }

        fun isModuleEnabled(module: String) = moduleStatus.has(module) &&
                moduleStatus.getJSONObject(module).optBoolean("isEnabled", false)

        return ModuleStatus(
                isModuleEnabled(ADBLOCKER_MODULE),
                isModuleEnabled(ANOLYSIS_MODULE),
                isModuleEnabled(ANTITRACKING_MODULE),
                isModuleEnabled(AUTOCONSENT_MODULE),
                isModuleEnabled(COOKIEMONSTER_MODULE),
                isModuleEnabled(HUMANWEB_MODULE),
                isModuleEnabled(INSIGHTS_MODULE)
        )
    }

    /**
     * Set the enabled status of an extension module.
     */
    suspend fun setModuleEnabled(module: String, enabled: Boolean) {
        extension.callActionAsync(scope, "core",
                if (enabled) "enableModule" else "disableModule", module).await()
    }

    /**
     * Get aggregated privacy stats over the given {StatsPeriod}.
     */
    suspend fun getBlockingStats(period: StatsPeriod): BlockingStats? {
        return extension.callActionAsync(scope, "insights", "getDashboardStats",
                period.period).await()
                .let { (it as JSONObject) }
                .let { res ->
                    BlockingStats(
                            res.optInt("adsBlocked", 0),
                            res.optInt("cookiesBlocked", 0),
                            res.optInt("dataSaved", 0),
                            res.optString("day", ""),
                            res.optInt("fingerprintsRemoved", 0),
                            res.optInt("loadTime", 0),
                            res.optInt("pages", 0),
                            res.optInt("timeSaved", 0),
                            res.optJSONArray("trackers").let {
                                if (it != null) List(it.length()) { i -> it.getString(i) } else emptyList()
                            },
                            res.optJSONArray("trackersDetailed").let {
                                if (it != null) List(it.length()) { i ->
                                    val tracker = it.getJSONObject(i)
                                    TrackerInfo(tracker.getString("name"),
                                            tracker.getString("cat"),
                                            tracker.getString("wtm"))
                                } else emptyList()
                            },
                            res.optInt("trackersDetected", 0)
                    )
                }
    }
}

data class ModuleStatus(
    val adblockerEnabled: Boolean,
    val anolysisEnabled: Boolean,
    val antitrackingEnabled: Boolean,
    val autoconsentEnabled: Boolean,
    val cookieMonsterEnabled: Boolean,
    val humanwebEnabled: Boolean,
    val insightsEnabled: Boolean
)

enum class StatsPeriod(val period: String) {
    ALL_TIME(""),
    DAY("day"),
    WEEK("week"),
    MONTH("month")
}

data class TrackerInfo(
    val name: String,
    val category: String,
    val wtm: String
)

data class BlockingStats(
    val adsBlocked: Int,
    val cookiesBlocked: Int,
    val dataSaved: Int,
    val day: String,
    val fingerprintsRemoved: Int,
    val loadTime: Int,
    val pages: Int,
    val timeSaved: Int,
    val trackers: List<String>,
    val trackersDetailed: List<TrackerInfo>,
    val trackersDetected: Int
)
