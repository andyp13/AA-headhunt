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
import dev.headhunt.launcher.ui.LauncherRoot

class HomeActivity : ComponentActivity() {

    private lateinit var vehicle: VehicleSource
    private lateinit var media: MediaRepository
    private lateinit var apps: AppsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vehicle = CarVehicleSource(this)
        media = MediaRepository(this)
        apps = AppsRepository(this)

        setContent {
            val vehicleState by vehicle.state.collectAsStateWithLifecycle()
            val mediaState by media.state.collectAsStateWithLifecycle()
            HeadhuntTheme(night = vehicleState.night) {
                LauncherRoot(
                    vehicle = vehicleState,
                    media = mediaState,
                    mediaControls = media,
                    loadApps = { apps.launchable() },
                    launch = ::launch,
                )
            }
        }
    }

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

    private companion object {
        const val TAG = "HomeActivity"
    }
}
