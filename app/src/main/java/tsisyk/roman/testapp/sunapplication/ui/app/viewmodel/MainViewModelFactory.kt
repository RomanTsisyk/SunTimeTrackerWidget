package tsisyk.roman.testapp.sunapplication.ui.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tsisyk.roman.testapp.sunapplication.services.SunApiService

class MainViewModelFactory(private val sunApiService: SunApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(sunApiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}