package tsisyk.roman.testapp.sunapplication.services

import android.annotation.SuppressLint
import android.app.Activity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.ui.PlacePicker
import tsisyk.roman.testapp.sunapplication.R
import tsisyk.roman.testapp.sunapplication.utils.UiUtils

class LocationService(private val activity: Activity) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)

    companion object {
        internal const val PLACE_PICKER_REQUEST = 1
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(onLocationReceived: (String, String) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnCompleteListener(activity) { task ->
                activity.runOnUiThread {
                    if (task.isSuccessful && task.result != null) {
                        val location = task.result
                        onLocationReceived(
                            location!!.latitude.toString(),
                            location.longitude.toString()
                        )
                    } else {
                        UiUtils.showShortToast(
                            activity,
                            activity.getString(R.string.no_location_detected)
                        )
                    }
                }
            }
    }

    fun findNewLocation() {
        val builder = PlacePicker.IntentBuilder()
        activity.startActivityForResult(builder.build(activity), PLACE_PICKER_REQUEST)
    }
}