package com.example.myweather.utils

import com.example.myweather.model.Weather
import com.example.myweather.model.getDefaultCity
import com.example.myweather.model.test.FactDTO
import com.example.myweather.model.test.WeatherDTO

fun convertDtoToModel(weatherDTO: WeatherDTO): List<Weather> {

    val fact: FactDTO = weatherDTO.fact!! // Рисковано, переделать

    return listOf(Weather
        (getDefaultCity(), fact.temp!!, fact.feels_like!!, fact.condition!!, fact.icon!!))
}