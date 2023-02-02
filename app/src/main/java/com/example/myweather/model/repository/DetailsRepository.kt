package com.example.myweather.model.repository

import com.example.myweather.model.test.WeatherDTO
import retrofit2.Callback


interface DetailsRepository {
    fun getWeatherDetailsFromServer(lat: Double, lon: Double, callback: Callback<WeatherDTO>)
}