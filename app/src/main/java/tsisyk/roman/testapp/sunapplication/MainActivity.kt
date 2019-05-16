package tsisyk.roman.testapp.sunapplication

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.multidex.BuildConfig.APPLICATION_ID
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.content_main.*
import tsisyk.roman.testapp.sunapplication.model.SunResponse
import tsisyk.roman.testapp.sunapplication.services.LocationService
import tsisyk.roman.testapp.sunapplication.services.SunApiService
import tsisyk.roman.testapp.sunapplication.utils.UiUtils
import java.util.*


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude = ""
    private var longitude = ""


    private val sunApiService: SunApiService by lazy {
        SunApiService(this::updateSunDataUI)
    }

    private val locationService: LocationService by lazy {
        LocationService(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initGetDataButton()
        initLocationClient()
    }

    private fun initLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun initGetDataButton() {
        findViewById<Button>(R.id.bntGetData).setOnClickListener {
            if (checkBoxLocal.isChecked)
                locationService.getLastLocation { latitude, longitude ->
                    updateLocationUI(latitude, longitude, "Current Location")
                }
            else locationService.findNewLocation()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LocationService.PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            val place = PlacePicker.getPlace(this, data)
            val latitude = place.latLng.latitude.toString()
            val longitude = place.latLng.longitude.toString()
            updateLocationUI(latitude, longitude, place.name.toString(), place.address.toString())
        }
    }


    override fun onStart() {
        super.onStart()
        if (!checkPermissions()) requestPermissions() else getLastLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    latitude = task.result!!.latitude.toString()
                    longitude = task.result!!.longitude.toString()
                    textPlaceName.text = getString(R.string.current_location)
                    textPlaseAdress.text = getString(R.string.lat_lon, latitude, longitude)
                    sunApiService
                } else {
                    Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showSnackbar(
        snackStrId: Int,
        actionStrId: Int = 0,
        listener: View.OnClickListener? = null
    ) {
        val snackbar = Snackbar.make(
            findViewById(android.R.id.content), getString(snackStrId),
            LENGTH_INDEFINITE
        )
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }
        snackbar.show()
    }

    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this, arrayOf(ACCESS_COARSE_LOCATION),
            PERMISSIONS_REQUEST_CODE
        )
    }

    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION)) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar(R.string.permission_rationale, android.R.string.ok, View.OnClickListener {
                // Request permission
                startLocationPermissionRequest()
            })

        } else {
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")
                (grantResults[0] == PERMISSION_GRANTED) -> getLastLocation()
                else -> {
                    showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        View.OnClickListener {
                            val intent = Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", APPLICATION_ID, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                        })
                }
            }
        }
    }

    private fun updateSunDataUI(sunData: SunResponse?) {
        sunData?.let {
            textSunrise.text = it.results.sunrise
            textSunset.text = it.results.sunset
        } ?: UiUtils.showShortToast(this, "Failed to fetch sun data")
    }

    private fun updateLocationUI(latitude: String, longitude: String, placeName: String, placeAddress: String? = null) {
        this.latitude = latitude
        this.longitude = longitude
        textPlaceName.text = placeName
        textPlaseAdress.text = placeAddress ?: getString(R.string.lat_lon, latitude, longitude)
        sunApiService // Update your UI or call necessary service
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSIONS_REQUEST_CODE = 34
    }
}