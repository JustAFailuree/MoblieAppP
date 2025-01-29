package com.example.app4

import android.location.Location
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.app4.databinding.ActivityMainBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.city.text=""
        binding.temp.text=""
        binding.desc.text=""


        var location = Location("me")
        location.latitude = 50.075539
        location.longitude = 14.437800

        CoroutineScope(Dispatchers.Main).launch {
            val myForecast = curentForecast(location)
            val temp1 = myForecast?.main?.temp
            val city1 = myForecast?.name
            val desc1 = myForecast?.weather?.get(0)?.description
            val icon = myForecast?.weather?.get(0)?.icon

            binding.city.text = city1
            binding.temp.text = "$temp1 C"
            binding.desc.text = desc1

            val iconUrl = "https://openweathermap.org/img/wn/$icon@2x.png"
            Glide.with(this@MainActivity).load(iconUrl).into(binding.iconIV)
        }


    }

    private suspend fun curentForecast(location: Location) :  Forecast? {
        val lat = location.latitude
        val lon = location.longitude

        val url = "https://api.openweathermap.org/data/2.5/weather?lang=pl&units=metric&lat=$lat&lon=$lon&appid=c6a5d34220a812a204d8d55b7bfbd342"
        val httpClient = OkHttpClient()
        val httpRequest = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO){
            val response = httpClient.newCall(httpRequest).execute()
            if (!response.isSuccessful){
                throw IOException("Error: $response")
            }
            val body = response.body!!.string()
            val gsonParser = GsonParser()
            val currentWeather = gsonParser.getForecast(body)
            currentWeather
        }


    }
}


class GsonParser{
    private val gson = Gson()

    fun getForecast(json : String) : Forecast{
        return gson.fromJson(json, Forecast::class.java)
    }
}