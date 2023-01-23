package com.example.myweather.model.repository

import com.example.myweather.model.Weather

interface Repository {

    fun getWeatherFromServer(): Weather
    fun getWeatherFromLocalStorageRus(): List<Weather>
    fun getWeatherFromLocalStorageWorld(): List<Weather>

}