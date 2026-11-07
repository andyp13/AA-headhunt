package dev.headhunt.launcher

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.headhunt.carkit.CarVehicleSource
import dev.headhunt.carkit.VehicleSource
import dev.headhunt.design.HeadhuntTheme
import dev.headhunt.launcher.apps.AppsRepository
import dev.headhunt.launcher.media.MediaRepository
import dev.headhunt.launcher.ui.Destination
import dev.headhunt.launcher.ui.LauncherRoot
import kotlinx.coroutines.flow.MutableStateFlow

class HomeActivity : ComponentActivity() {

    private lateinit var vehicle: VehicleSource
    private lateinit var media: MediaRepository
    private lateinit var apps: AppsRepository

    /** Destination asked for by the launching intent, e.g. the system bar's Apps button. */
    private val requested = MutableStateFlow<Destination?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vehicle = CarVehicleSource(this)
        media = MediaRepository(this)
        apps = AppsRepository(this)
        requested.value = intent.destination()

        setContent {
            val vehicleState by vehicle.state.collectAsStateWithLifecycle()
            val mediaState by media.state.collectAsStateWithLifecycle()
            HeadhuntTheme(night = vehicleState.night) {
                val requestedDestination by requested.collectAsStateWithLifecycle()
                LauncherRoot(
                    vehicle = vehicleState,
                    media = mediaState,
                    mediaControls = media,
                    loadApps = { apps.launchable() },
                    launch = ::launch,
                    requestedDestination = requestedDestination,
                    onDestinationConsumed = { requested.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.destination()?.let { requested.value = it }
    }

    private fun Intent.destination(): Destination? =
        getStringExtra(EXTRA_DESTINATION)?.let { runCatching { Destination.valueOf(it) }.getOrNull() }

    private fun launch(intent: Intent) {
        try {
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "nothing handles $intent")
            Toast.makeText(this, R.string.no_app_for_action, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        media.close()
        vehicle.close()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "HomeActivity"

        /** String extra naming a [Destination]; the CarSystemUI overlay's Apps button sends APPS. */
        const val EXTRA_DESTINATION = "dev.headhunt.launcher.DESTINATION"
    }
}
