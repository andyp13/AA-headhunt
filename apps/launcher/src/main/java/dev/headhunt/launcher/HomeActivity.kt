package dev.headhunt.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.headhunt.carkit.CarVehicleSource
import dev.headhunt.carkit.VehicleSource
import dev.headhunt.design.HeadhuntTheme

class HomeActivity : ComponentActivity() {

    private lateinit var vehicle: VehicleSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vehicle = CarVehicleSource(this)

        setContent {
            val state by vehicle.state.collectAsStateWithLifecycle()
            HeadhuntTheme(night = state.night) {
                HomeScreen(state)
            }
        }
    }

    override fun onDestroy() {
        vehicle.close()
        super.onDestroy()
    }
}
