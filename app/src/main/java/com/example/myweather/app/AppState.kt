package com.example.myweather.app

import com.example.myweather.model.Weather

sealed class AppState {

    data class Success(val weatherData: List<Weather>) : AppState()
    data class Error(val error: Throwable) : AppState()
    object Loading : AppState()

}
