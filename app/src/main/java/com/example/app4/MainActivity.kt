package com.example.app4

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

        binding.city.text = ""
        binding.temp.text = ""
        binding.desc.text = ""

        getWeatherByLocation(50.075539, 14.437800)

        binding.searchButton.setOnClickListener {
            val cityName = binding.cityInput.text.toString()
            if (cityName.isNotEmpty()) {
                getWeatherByCity(cityName)
            }
        }
    }

    private fun getWeatherByLocation(lat: Double, lon: Double) {
        CoroutineScope(Dispatchers.Main).launch {
            val myForecast = curentForecast(lat, lon, null)
            updateUI(myForecast)
        }
    }

    private fun getWeatherByCity(city: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val myForecast = curentForecast(null, null, city)
            updateUI(myForecast)
        }
    }

    private fun updateUI(myForecast: Forecast?) {
        val temp1 = myForecast?.main?.temp
        val city1 = myForecast?.name
        val desc1 = myForecast?.weather?.get(0)?.description
        val icon = myForecast?.weather?.get(0)?.icon

        binding.city.text = city1
        binding.temp.text = "$temp1 °C"
        binding.desc.text = desc1

        val iconUrl = "https://openweathermap.org/img/wn/$icon@2x.png"
        Glide.with(this@MainActivity).load(iconUrl).into(binding.iconIV)
    }

    private suspend fun curentForecast(lat: Double?, lon: Double?, city: String?): Forecast? {
        val apiKey = "c6a5d34220a812a204d8d55b7bfbd342"
        val url = when {
            city != null -> "https://api.openweathermap.org/data/2.5/weather?lang=pl&units=metric&q=$city&appid=$apiKey"
            lat != null && lon != null -> "https://api.openweathermap.org/data/2.5/weather?lang=pl&units=metric&lat=$lat&lon=$lon&appid=$apiKey"
            else -> return null
        }

        val httpClient = OkHttpClient()
        val httpRequest = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            val response = httpClient.newCall(httpRequest).execute()
            if (!response.isSuccessful) {
                throw IOException("Error: $response")
            }
            val body = response.body!!.string()
            val gsonParser = GsonParser()
            gsonParser.getForecast(body)
        }
    }
}

class GsonParser {
    private val gson = Gson()

    fun getForecast(json: String): Forecast {
        return gson.fromJson(json, Forecast::class.java)
    }
}
