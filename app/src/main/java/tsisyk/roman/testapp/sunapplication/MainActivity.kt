package tsisyk.roman.testapp.sunapplication

import android.Manifest.permission.ACCESS_COARSE_LOCATION
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
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.content_main.*
import tsisyk.roman.testapp.sunapplication.model.SunResponse
import tsisyk.roman.testapp.sunapplication.services.LocationService
import tsisyk.roman.testapp.sunapplication.services.SunApiService
import tsisyk.roman.testapp.sunapplication.utils.UiUtils
import tsisyk.roman.testapp.sunapplication.viewmodel.MainViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import tsisyk.roman.testapp.sunapplication.BuildConfig.APPLICATION_ID


class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var locationService: LocationService
    private lateinit var sunApiService: SunApiService

    private var latitude = ""
    private var longitude = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initServices()
        initViewModel()
        observeSunData()
        checkAndRequestPermissions()
    }

    private fun initServices() {
        locationService = LocationService(this)
        sunApiService = SunApiService { sunData, throwable ->
            if (throwable != null) {
                Log.e("MainActivity", "Error fetching sun data", throwable)
            } else {
                updateSunDataUI(sunData)
            }
        }
    }

    private fun initViewModel() {
        mainViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) return MainViewModel(
                    sunApiService
                ) as T
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }).get(MainViewModel::class.java)
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

    private fun updateLocationUI(
        latitude: String,
        longitude: String,
        placeName: String,
        placeAddress: String? = null
    ) {
        this.latitude = latitude
        this.longitude = longitude
        textPlaceName.text = placeName
        textPlaseAdress.text = placeAddress ?: getString(R.string.lat_lon, latitude, longitude)
        sunApiService // Update your UI or call necessary service
        mainViewModel.getSunData(latitude, longitude)
    }

    private fun observeSunData() {
        mainViewModel.sunData.observe(this, Observer { sunData ->
            sunData?.let {
                updateSunDataUI(it)
            } ?: UiUtils.showShortToast(this, "Failed to fetch sun data")
        })
    }

    private fun checkAndRequestPermissions() {
        if (checkPermissions()) {
            getLastLocation()
        } else {
            requestPermissions()
        }
    }

    private fun getLastLocation() {
        locationService.getLastLocation { latitude, longitude ->
            mainViewModel.getSunData(latitude, longitude)
        }
    }
    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSIONS_REQUEST_CODE = 34
    }
}