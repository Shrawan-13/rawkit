package com.rawkit.launcher.data

import android.content.ComponentName
import android.graphics.drawable.Drawable
import android.os.UserHandle

/**
 * A single launchable app.
 *
 * [user] is kept because a launcher may show apps from more than one profile
 * (personal + work); an app is only identified by the pair (user, component).
 */
data class AppEntry(
    val component: ComponentName,
    val label: String,
    val user: UserHandle,
    val icon: Drawable,
) {
    /** Stable identity within a session, used for diffing the grid. */
    val key: String = "${user.hashCode()}:${component.flattenToShortString()}"
}
