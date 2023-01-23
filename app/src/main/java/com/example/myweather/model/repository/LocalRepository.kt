package com.example.myweather.model.repository

import com.example.myweather.model.Weather

interface LocalRepository {

    fun getAllHistory(): List<Weather>
    fun saveEntity(weather: Weather)

}