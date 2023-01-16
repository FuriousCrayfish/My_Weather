package com.example.myweather.model.repository

import com.example.myweather.model.RemoteDataSource
import com.example.myweather.model.test.WeatherDTO

class DetailsRepositoryImpl(private val remoteDataSource: RemoteDataSource) : DetailsRepository {

    override fun getWeatherDetailsFromServer(
        lat: Double,
        lon: Double,
        callback: retrofit2.Callback<WeatherDTO>,
    ) {
        remoteDataSource.getWeatherDetails(lat, lon, callback)
    }
}