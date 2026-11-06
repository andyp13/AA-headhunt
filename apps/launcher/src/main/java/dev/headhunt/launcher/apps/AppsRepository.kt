package dev.headhunt.launcher.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

/** One launchable activity as the app grid shows it. */
data class AppEntry(
    val label: String,
    val packageName: String,
    val icon: Drawable,
    val launchIntent: Intent,
)

/**
 * Lists everything with a LAUNCHER activity, except ourselves.
 *
 * The HOME role gives this app visibility of every package, so no
 * QUERY_ALL_PACKAGES is needed; the <queries> block in the manifest covers
 * the case where we are installed but not yet the home app.
 */
class AppsRepository(private val context: Context) {

    private val pm: PackageManager get() = context.packageManager

    fun launchable(): List<AppEntry> {
        val probe = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(probe, PackageManager.ResolveInfoFlags.of(0))
            .asSequence()
            .filter { it.activityInfo.packageName != context.packageName }
            .map { info ->
                val intent = Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .setClassName(info.activityInfo.packageName, info.activityInfo.name)
                AppEntry(
                    label = info.loadLabel(pm).toString(),
                    packageName = info.activityInfo.packageName,
                    icon = info.loadIcon(pm),
                    launchIntent = intent,
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }
}
