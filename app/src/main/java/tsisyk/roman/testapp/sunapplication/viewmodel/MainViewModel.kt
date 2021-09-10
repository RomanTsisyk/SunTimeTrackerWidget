package tsisyk.roman.testapp.sunapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tsisyk.roman.testapp.sunapplication.model.SunResponse
import tsisyk.roman.testapp.sunapplication.services.SunApiService

class MainViewModel(private val sunApiService: SunApiService) : ViewModel() {

    private val _sunData = MutableLiveData<SunResponse?>()
    val sunData: LiveData<SunResponse?> = _sunData

    fun getSunData(latitude: String, longitude: String) {
        sunApiService.getSunData(latitude, longitude) { sunResponse ->
            _sunData.postValue(sunResponse)
        }
    }
}
