package com.morshues.lazyathome.data.network

import com.morshues.lazyathome.settings.SettingsManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides the base URL for API endpoints.
 * This is used by model classes to construct full URLs.
 * URL is fetched dynamically from settings each time to support URL changes without app restart.
 */
@Singleton
class UrlProvider @Inject constructor(
    private val settingsManager: SettingsManager
) {
    val baseUrl: String
        get() {
            var url = settingsManager.getServerPath().trim()
            if (!url.endsWith("/")) {
                url += "/"
            }
            return url
        }

    companion object {
        // Static accessor for use in model classes
        // Must be initialized at app startup
        lateinit var instance: UrlProvider
            private set

        fun init(urlProvider: UrlProvider) {
            instance = urlProvider
        }

        val baseUrl: String
            get() = instance.baseUrl
    }
}
