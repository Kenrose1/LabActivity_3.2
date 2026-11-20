package com.example.weatherapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class WeatherViewModel : ViewModel() {
    var weather by mutableStateOf<WeatherResponse?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    suspend fun search(city: String) {
        val trimmedCity = city.trim()
        if (trimmedCity.isEmpty()) return

        isLoading = true
        errorMessage = null
        try {
            if (BuildConfig.OPEN_WEATHER_API_KEY.isBlank()) {
                errorMessage = "Add OPEN_WEATHER_API_KEY to local.properties first."
                return
            }
            weather = WeatherApi.service.getCurrentWeather(
                city = trimmedCity,
                apiKey = BuildConfig.OPEN_WEATHER_API_KEY
            )
        } catch (exception: Exception) {
            errorMessage = "Unable to load weather. Check the city name and your connection."
        } finally {
            isLoading = false
        }
    }
}
