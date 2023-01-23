package com.example.myweather.model.repository

import com.example.myweather.model.Weather
import com.example.myweather.room.HistoryDao
import com.example.myweather.utils.convertHistoryEntityToWeather
import com.example.myweather.utils.convertWeatherToEntity

class LocalRepositoryImpl(private val localDataSource: HistoryDao) :
    LocalRepository {

    override fun getAllHistory(): List<Weather> {
        return convertHistoryEntityToWeather(localDataSource.all())
    }

    override fun saveEntity(weather: Weather) {
        localDataSource.insert(convertWeatherToEntity(weather))
    }
}
