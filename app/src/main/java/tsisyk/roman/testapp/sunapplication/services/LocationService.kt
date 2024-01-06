package tsisyk.roman.testapp.sunapplication.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import tsisyk.roman.testapp.sunapplication.R
import tsisyk.roman.testapp.sunapplication.ui.app.utils.UiUtils

class LocationService(private val activity: Activity) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)

    @SuppressLint("MissingPermission")
    fun getLastLocation(onLocationReceived: (String, String) -> Unit) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnCompleteListener(activity) { task ->
            if (task.isSuccessful && task.result != null) {
                task.result?.let { location ->
                    onLocationReceived(location.latitude.toString(), location.longitude.toString())
                }
            } else {
                activity.runOnUiThread {
                    UiUtils.showShortToast(
                        activity.applicationContext,
                        activity.getString(R.string.no_location_detected)
                    )
                }
            }
        }
    }
}