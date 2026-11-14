package com.rawkit.launcher

import android.app.Activity
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rawkit.launcher.data.AppEntry
import com.rawkit.launcher.data.AppRepository
import com.rawkit.launcher.ui.AppGridAdapter
import java.util.Locale

/**
 * The home screen: a searchable grid of launchable apps.
 *
 * The app list is loaded on a background thread so that the first frame is never
 * blocked by the package manager.
 */
class MainActivity : Activity() {

    private lateinit var repository: AppRepository
    private lateinit var adapter: AppGridAdapter
    private lateinit var emptyLabel: TextView
    private lateinit var launcherApps: LauncherApps

    private val mainHandler = Handler(Looper.getMainLooper())
    private var allApps: List<AppEntry> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = AppRepository(this)
        launcherApps = getSystemService(LAUNCHER_APPS_SERVICE) as LauncherApps
        emptyLabel = findViewById(R.id.empty_label)

        val grid = findViewById<RecyclerView>(R.id.app_grid)
        grid.layoutManager = GridLayoutManager(this, columnCount())
        grid.setHasFixedSize(true)
        grid.itemAnimator = null

        adapter = AppGridAdapter(
            onClick = ::launch,
            onLongClick = ::showAppInfo,
        )
        grid.adapter = adapter

        findViewById<EditText>(R.id.search).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) = applyFilter(s?.toString().orEmpty())
        })

        loadApps()
    }

    private fun loadApps() {
        Thread({
            val apps = repository.loadApps()
            mainHandler.post {
                allApps = apps
                applyFilter(currentQuery())
            }
        }, "rawkit-app-load").start()
    }

    private fun currentQuery(): String = findViewById<EditText>(R.id.search).text.toString()

    private fun applyFilter(query: String) {
        val trimmed = query.trim()
        val filtered = if (trimmed.isEmpty()) {
            allApps
        } else {
            val needle = trimmed.lowercase(Locale.getDefault())
            allApps.filter { it.label.lowercase(Locale.getDefault()).contains(needle) }
        }

        adapter.submitList(filtered)
        emptyLabel.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun launch(entry: AppEntry) {
        try {
            // LauncherApps.startMainActivity is the sanctioned path for a
            // launcher and keeps work-profile apps in their own profile.
            // It reports failure by throwing, not by return value.
            launcherApps.startMainActivity(entry.component, entry.user, null, null)
            return
        } catch (e: SecurityException) {
            // Fall through to the plain intent below.
        }

        // Fallback for the case where Rawkit is not the default launcher yet.
        val intent = packageManager.getLaunchIntentForPackage(entry.component.packageName)
        if (intent != null) {
            startActivity(intent)
        }
    }

    private fun showAppInfo(entry: AppEntry) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", entry.component.packageName, null),
        )
        startActivity(intent)
    }

    private fun columnCount(): Int {
        val cellWidthDp =
            resources.getDimension(R.dimen.grid_cell_width) / resources.displayMetrics.density
        val widthDp = resources.configuration.screenWidthDp
        return (widthDp / cellWidthDp).toInt().coerceAtLeast(4)
    }
}
