package com.example.myweather.presentation.viewModel

import com.example.myweather.model.test.WeatherDTO

sealed class ResultWeather{

    data class Success(val data: WeatherDTO) : ResultWeather()
    object Error : ResultWeather()
    object Loading : ResultWeather()
}
