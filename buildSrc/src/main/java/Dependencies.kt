/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

// Synchronized version numbers for dependencies used by (some) modules
private object Versions {
    const val kotlin = "1.3.30"
    const val coroutines = "1.3.2"

    const val junit = "4.12"
    const val mockk = "1.9.3"

    const val workmanager = "2.0.0"
    const val google_material = "1.2.0-alpha03"

    const val android_gradle_plugin = "3.6.0"
    const val mozilla_android_components = "29.0.0"

    const val glide = "4.11.0"

    const val thirdparty_sentry = "1.7.10"

    const val mockwebserver = "3.10.0"
    const val orchestrator = "1.1.1"
    const val tools_test_rules = "1.1.0"
    const val tools_test_runner = "1.1.0"
    const val uiautomator = "2.2.0"
    const val robolectric = "4.3.1"
    const val mockito = "2.24.5"

    const val leakcanary = "2.0-beta-3"

    object AndroidX {
        const val core = "1.1.0"
        const val test_ext = "1.1.1"
        const val appcompat = "1.0.2"
        const val recyclerview = "1.1.0-rc01"
        const val paging = "2.1.0"
        const val constraintlayout = "2.0.0-beta3"
        const val preference = "1.0.0"
        const val lifecycle = "2.2.0-rc02"
        const val espresso = "3.1.0"
        const val fragment = "1.1.0"
    }
}

// Synchronized dependencies used by (some) modules
object Deps {
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val kotlin_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    const val kotlin_coroutines_core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val kotlin_coroutines_test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"

    const val testing_junit = "junit:junit:${Versions.junit}"
    const val testing_mockk = "io.mockk:mockk:${Versions.mockk}"
    const val androidx_test_junit = "androidx.test.ext:junit:${Versions.AndroidX.test_ext}"

    const val mozilla_concept_engine = "org.mozilla.components:concept-engine:${Versions.mozilla_android_components}"
    const val mozilla_concept_tabstray = "org.mozilla.components:concept-tabstray:${Versions.mozilla_android_components}"
    const val mozilla_concept_toolbar = "org.mozilla.components:concept-toolbar:${Versions.mozilla_android_components}"
    const val mozilla_concept_storage = "org.mozilla.components:concept-storage:${Versions.mozilla_android_components}"
    const val mozilla_concept_sync = "org.mozilla.components:concept-sync:${Versions.mozilla_android_components}"
    const val mozilla_concept_fetch = "org.mozilla.components:concept-fetch:${Versions.mozilla_android_components}"
    const val mozilla_concept_push = "org.mozilla.components:concept-push:${Versions.mozilla_android_components}"

    const val mozilla_browser_awesomebar = "org.mozilla.components:browser-awesomebar:${Versions.mozilla_android_components}"
    const val mozilla_browser_engine_gecko = "org.mozilla.components:browser-engine-gecko:${Versions.mozilla_android_components}"
    const val mozilla_browser_engine_gecko_beta = "org.mozilla.components:browser-engine-gecko-beta:${Versions.mozilla_android_components}"
    const val mozilla_browser_engine_gecko_nightly = "org.mozilla.components:browser-engine-gecko-nightly:${Versions.mozilla_android_components}"
    const val mozilla_browser_domains = "org.mozilla.components:browser-domains:${Versions.mozilla_android_components}"
    const val mozilla_browser_search = "org.mozilla.components:browser-search:${Versions.mozilla_android_components}"
    const val mozilla_browser_session = "org.mozilla.components:browser-session:${Versions.mozilla_android_components}"
    const val mozilla_browser_state = "org.mozilla.components:browser-state:${Versions.mozilla_android_components}"
    const val mozilla_browser_toolbar = "org.mozilla.components:browser-toolbar:${Versions.mozilla_android_components}"
    const val mozilla_browser_menu = "org.mozilla.components:browser-menu:${Versions.mozilla_android_components}"
    const val mozilla_browser_errorpages = "org.mozilla.components:browser-errorpages:${Versions.mozilla_android_components}"
    const val mozilla_browser_storage_sync = "org.mozilla.components:browser-storage-sync:${Versions.mozilla_android_components}"
    const val mozilla_browser_icons = "org.mozilla.components:browser-icons:${Versions.mozilla_android_components}"

    const val mozilla_feature_accounts = "org.mozilla.components:feature-accounts:${Versions.mozilla_android_components}"
    const val mozilla_feature_accounts_push = "org.mozilla.components:feature-accounts-push:${Versions.mozilla_android_components}"
    const val mozilla_feature_addons = "org.mozilla.components:feature-addons:${Versions.mozilla_android_components}"
    const val mozilla_feature_app_links = "org.mozilla.components:feature-app-links:${Versions.mozilla_android_components}"
    const val mozilla_feature_awesomebar = "org.mozilla.components:feature-awesomebar:${Versions.mozilla_android_components}"
    const val mozilla_feature_contextmenu = "org.mozilla.components:feature-contextmenu:${Versions.mozilla_android_components}"
    const val mozilla_feature_customtabs = "org.mozilla.components:feature-customtabs:${Versions.mozilla_android_components}"
    const val mozilla_feature_findinpage = "org.mozilla.components:feature-findinpage:${Versions.mozilla_android_components}"
    const val mozilla_feature_media = "org.mozilla.components:feature-media:${Versions.mozilla_android_components}"
    const val mozilla_feature_sitepermissions = "org.mozilla.components:feature-sitepermissions:${Versions.mozilla_android_components}"
    const val mozilla_feature_intent = "org.mozilla.components:feature-intent:${Versions.mozilla_android_components}"
    const val mozilla_feature_search = "org.mozilla.components:feature-search:${Versions.mozilla_android_components}"
    const val mozilla_feature_session = "org.mozilla.components:feature-session:${Versions.mozilla_android_components}"
    const val mozilla_feature_toolbar = "org.mozilla.components:feature-toolbar:${Versions.mozilla_android_components}"
    const val mozilla_feature_tabs = "org.mozilla.components:feature-tabs:${Versions.mozilla_android_components}"
    const val mozilla_feature_downloads = "org.mozilla.components:feature-downloads:${Versions.mozilla_android_components}"
    const val mozilla_feature_storage = "org.mozilla.components:feature-storage:${Versions.mozilla_android_components}"
    const val mozilla_feature_prompts = "org.mozilla.components:feature-prompts:${Versions.mozilla_android_components}"
    const val mozilla_feature_push = "org.mozilla.components:feature-push:${Versions.mozilla_android_components}"
    const val mozilla_feature_pwa = "org.mozilla.components:feature-pwa:${Versions.mozilla_android_components}"
    const val mozilla_feature_qr = "org.mozilla.components:feature-qr:${Versions.mozilla_android_components}"
    const val mozilla_feature_readerview = "org.mozilla.components:feature-readerview:${Versions.mozilla_android_components}"
    const val mozilla_feature_syncedtabs = "org.mozilla.components:feature-syncedtabs:${Versions.mozilla_android_components}"
    const val mozilla_feature_webcompat= "org.mozilla.components:feature-webcompat:${Versions.mozilla_android_components}"
    const val mozilla_feature_webnotifications = "org.mozilla.components:feature-webnotifications:${Versions.mozilla_android_components}"

    const val mozilla_ui_autocomplete = "org.mozilla.components:ui-autocomplete:${Versions.mozilla_android_components}"
    const val mozilla_ui_colors = "org.mozilla.components:ui-colors:${Versions.mozilla_android_components}"
    const val mozilla_ui_icons = "org.mozilla.components:ui-icons:${Versions.mozilla_android_components}"

    const val mozilla_service_firefox_accounts = "org.mozilla.components:service-firefox-accounts:${Versions.mozilla_android_components}"
    const val mozilla_service_experiments = "org.mozilla.components:service-experiments:${Versions.mozilla_android_components}"

    const val mozilla_support_utils = "org.mozilla.components:support-utils:${Versions.mozilla_android_components}"
    const val mozilla_support_ktx= "org.mozilla.components:support-ktx:${Versions.mozilla_android_components}"
    const val mozilla_support_rustlog = "org.mozilla.components:support-rustlog:${Versions.mozilla_android_components}"
    const val mozilla_support_rusthttp = "org.mozilla.components:support-rusthttp:${Versions.mozilla_android_components}"
    const val mozilla_support_webextensions = "org.mozilla.components:support-webextensions:${Versions.mozilla_android_components}"
    const val mozilla_support_test = "org.mozilla.components:support-test:${Versions.mozilla_android_components}"

    const val mozilla_lib_crash = "org.mozilla.components:lib-crash:${Versions.mozilla_android_components}"
    const val mozilla_lib_push_firebase = "org.mozilla.components:lib-push-firebase:${Versions.mozilla_android_components}"

    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val glide_compiler = "com.github.bumptech.glide:compiler:${Versions.glide}"

    const val thirdparty_sentry = "io.sentry:sentry-android:${Versions.thirdparty_sentry}"

    const val androidx_appcompat = "androidx.appcompat:appcompat:${Versions.AndroidX.appcompat}"
    const val androidx_recyclerview = "androidx.recyclerview:recyclerview:${Versions.AndroidX.recyclerview}"
    const val androidx_paging = "androidx.paging:paging-runtime-ktx:${Versions.AndroidX.paging}"
    const val androidx_core_ktx = "androidx.core:core-ktx:${Versions.AndroidX.core}"
    const val androidx_constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.constraintlayout}"
    const val androidx_preference_ktx = "androidx.preference:preference-ktx:${Versions.AndroidX.preference}"
    const val androidx_work_runtime_ktx = "androidx.work:work-runtime-ktx:${Versions.workmanager}"
    const val androidx_lifecycle_runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.AndroidX.lifecycle}"
    const val androidx_lifecycle_viewmodel_ktx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.AndroidX.lifecycle}"
    const val androidx_fragment_testing = "androidx.fragment:fragment-testing:${Versions.AndroidX.fragment}"

    const val google_material = "com.google.android.material:material:${Versions.google_material}"

    const val tools_androidgradle = "com.android.tools.build:gradle:${Versions.android_gradle_plugin}"
    const val tools_kotlingradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"

    const val espresso_contrib = "androidx.test.espresso:espresso-contrib:${Versions.AndroidX.espresso}"
    const val espresso_core = "androidx.test.espresso:espresso-core:${Versions.AndroidX.espresso}"
    const val espresso_idling_resources = "androidx.test.espresso:espresso-idling-resource:${Versions.AndroidX.espresso}"
    const val espresso_web = "androidx.test.espresso:espresso-web:${Versions.AndroidX.espresso}"
    const val mockwebserver = "com.squareup.okhttp3:mockwebserver:${Versions.mockwebserver}"
    const val orchestrator =  "androidx.test:orchestrator:${Versions.orchestrator}"
    const val tools_test_ext_junit = "androidx.test.ext:junit:${Versions.AndroidX.core}"
    const val tools_test_rules = "androidx.test:rules:${Versions.tools_test_rules}"
    const val tools_test_runner = "androidx.test:runner:${Versions.tools_test_runner}"
    const val uiautomator = "androidx.test.uiautomator:uiautomator:${Versions.uiautomator}"
    const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
    const val robolectric_shadows = "org.robolectric:shadows-framework:${Versions.robolectric}"
    const val mozilla_support_test_appservices  = "org.mozilla.components:support-test-appservices:${Versions.mozilla_android_components}"
    const val testing_mockito = "org.mockito:mockito-core:${Versions.mockito}"

    const val leakcanary = "com.squareup.leakcanary:leakcanary-android:${Versions.leakcanary}"
}
