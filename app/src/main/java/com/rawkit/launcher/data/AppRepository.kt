package com.rawkit.launcher.data

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.util.LruCache
import java.util.Locale

/**
 * Reads the set of launchable apps from [LauncherApps].
 *
 * [LauncherApps] is the API the platform intends launchers to use. It needs no
 * permission, it works across profiles, and it hands back badged icons, so we
 * never need `QUERY_ALL_PACKAGES`.
 */
class AppRepository(context: Context) {

    private val appContext = context.applicationContext
    private val launcherApps =
        appContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val densityDpi = appContext.resources.displayMetrics.densityDpi

    /**
     * Icons are expensive to create and are requested again on every launcher
     * start, so they are kept in a small in-memory cache.
     */
    private val iconCache = LruCache<String, Drawable>(256)

    fun loadApps(): List<AppEntry> {
        val entries = ArrayList<AppEntry>()

        for (user in launcherApps.profiles) {
            val activities = try {
                launcherApps.getActivityList(null, user)
            } catch (e: SecurityException) {
                // Another profile we are not allowed to read. Skip it rather
                // than failing the whole load.
                continue
            }

            for (info in activities) {
                val component = info.componentName
                val cacheKey = "${user.hashCode()}:${component.flattenToShortString()}"
                entries += AppEntry(
                    component = component,
                    label = info.label?.toString().orEmpty(),
                    user = user,
                    icon = iconCache.get(cacheKey)
                        ?: createIcon(info.getIcon(densityDpi), cacheKey),
                )
            }
        }

        entries.sortBy { it.label.lowercase(Locale.getDefault()) }
        return entries
    }

    private fun createIcon(icon: Drawable, cacheKey: String): Drawable {
        iconCache.put(cacheKey, icon)
        return icon
    }
}
