package tsisyk.roman.testapp.sunapplication.ui.app

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
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import tsisyk.roman.testapp.sunapplication.model.SunResponse
import tsisyk.roman.testapp.sunapplication.services.LocationService
import tsisyk.roman.testapp.sunapplication.services.SunApiService
import tsisyk.roman.testapp.sunapplication.ui.app.utils.UiUtils
import tsisyk.roman.testapp.sunapplication.ui.app.viewmodel.MainViewModel
import tsisyk.roman.testapp.sunapplication.BuildConfig.APPLICATION_ID
import tsisyk.roman.testapp.sunapplication.R
import tsisyk.roman.testapp.sunapplication.ui.app.viewmodel.MainViewModelFactory


class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var locationService: LocationService
    private lateinit var sunApiService: SunApiService

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
        sunApiService = SunApiService()
    }

    private fun initViewModel() {
        val factory = MainViewModelFactory(sunApiService)
        mainViewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
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
            showSnackbar(
                R.string.permission_rationale,
                android.R.string.ok
            ) { startLocationPermissionRequest() }
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
                    showSnackbar(
                        R.string.permission_denied_explanation, R.string.settings
                    ) {
                        val intent = Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun updateSunDataUI(sunData: SunResponse?) {
        sunData?.let {
            findViewById<TextView>(R.id.textSunrise).text = it.results.sunrise
            findViewById<TextView>(R.id.textSunset).text = it.results.sunset
        } ?: UiUtils.showShortToast(this, "Failed to fetch sun data")

    }

    private fun observeSunData() {
        mainViewModel.sunData.observe(this) { sunData ->
            sunData?.let {
                updateSunDataUI(it)
            } ?: UiUtils.showShortToast(this, "Failed to fetch sun data")
        }
    }

    private fun checkAndRequestPermissions() {
        if (checkPermissions()) getLastLocation() else requestPermissions()
    }

    private fun getLastLocation() {
        locationService.getLastLocation { latitude, longitude ->
            mainViewModel.getSunData(latitude, longitude)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        const val PERMISSIONS_REQUEST_CODE = 34
    }
}