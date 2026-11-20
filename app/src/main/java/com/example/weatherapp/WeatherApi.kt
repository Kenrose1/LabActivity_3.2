package com.example.weatherapp

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherResponse(
    val name: String,
    val weather: List<WeatherCondition>
)

data class WeatherCondition(
    val main: String,
    val description: String,
    val icon: String
)

interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "imperial"
    ): WeatherResponse
}

object WeatherApi {
    private val moshi = Moshi.Builder().build()

    val service: WeatherApiService = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(WeatherApiService::class.java)
}
