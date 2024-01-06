package tsisyk.roman.testapp.sunapplication.services

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tsisyk.roman.testapp.sunapplication.model.SunResponse

class SunApiService {

    companion object {
        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl("https://api.sunrise-sunset.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        private val service: SunService by lazy {
            retrofit.create(SunService::class.java)
        }
    }

    fun getSunData(latitude: String, longitude: String, callback: (SunResponse?, Throwable?) -> Unit) {
        val call = service.getSunData(latitude, longitude, "date=today")
        call.enqueue(object : Callback<SunResponse> {
            override fun onResponse(call: Call<SunResponse>, response: Response<SunResponse>) {
                if (response.isSuccessful) {
                    callback(response.body(), null)
                } else {
                    callback(null, RuntimeException("Response not successful"))
                }
            }

            override fun onFailure(call: Call<SunResponse>, t: Throwable) {
                callback(null, t)
            }
        })
    }
}