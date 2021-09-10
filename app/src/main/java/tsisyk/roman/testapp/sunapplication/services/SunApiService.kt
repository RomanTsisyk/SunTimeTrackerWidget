package tsisyk.roman.testapp.sunapplication.services

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tsisyk.roman.testapp.sunapplication.model.SunResponse

class SunApiService(private val updateUI: (SunResponse?) -> Unit) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.sunrise-sunset.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(SunService::class.java)

    fun getSunData(latitude: String, longitude: String, callback: (SunResponse?) -> Unit) {
        val call = service.getSunData(latitude, longitude, "date=today")
        call.enqueue(object : Callback<SunResponse> {
            override fun onResponse(call: Call<SunResponse>, response: Response<SunResponse>) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<SunResponse>, t: Throwable) {
                callback(null)
            }
        })
    }
}
