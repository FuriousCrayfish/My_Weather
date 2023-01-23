package com.example.myweather.model.repository

import com.example.myweather.model.Weather
import com.example.myweather.model.getRussianCities
import com.example.myweather.model.getWorldCities

class RepositoryImpl : Repository {

    override fun getWeatherFromServer(): Weather = Weather()

    override fun getWeatherFromLocalStorageRus(): List<Weather> = getRussianCities()

    override fun getWeatherFromLocalStorageWorld(): List<Weather> = getWorldCities()

}