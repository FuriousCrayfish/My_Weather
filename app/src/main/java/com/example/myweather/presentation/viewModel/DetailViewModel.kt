package com.example.myweather.presentation.viewModel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myweather.model.test.FactDTO
import com.example.myweather.model.test.WeatherDTO
import com.example.myweather.presentation.view.details.*

class DetailViewModel : ViewModel() {

    companion object {
        const val DETAILS_LOAD_RESULT_EXTRA = "LOAD RESULT"
        const val DETAILS_INTENT_EMPTY_EXTRA = "INTENT IS EMPTY"
        const val DETAILS_DATA_EMPTY_EXTRA = "DATA IS EMPTY"
        const val DETAILS_RESPONSE_EMPTY_EXTRA = "RESPONSE IS EMPTY"
        const val DETAILS_REQUEST_ERROR_EXTRA = "REQUEST ERROR"
        const val DETAILS_REQUEST_ERROR_MESSAGE_EXTRA = "REQUEST ERROR MESSAGE"
        const val DETAILS_URL_MALFORMED_EXTRA = "URL MALFORMED"
        const val DETAILS_RESPONSE_SUCCESS_EXTRA = "RESPONSE SUCCESS"
        const val DETAILS_TEMP_EXTRA = "TEMPERATURE"
        const val DETAILS_FEELS_LIKE_EXTRA = "FEELS LIKE"
        const val DETAILS_CONDITION_EXTRA = "CONDITION"
        private const val TEMP_INVALID = -100
        private const val FEELS_LIKE_INVALID = -100
    }

    private val _weatherLiveData: MutableLiveData<ResultWeather> = MutableLiveData()
    val weatherLiveData: LiveData<ResultWeather> = _weatherLiveData

    init {

        _weatherLiveData.postValue(ResultWeather.Loading)

    }

    fun getWeather(): BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.getStringExtra(DETAILS_LOAD_RESULT_EXTRA)) {

                DETAILS_INTENT_EMPTY_EXTRA,
                DETAILS_DATA_EMPTY_EXTRA,
                DETAILS_RESPONSE_EMPTY_EXTRA,
                DETAILS_REQUEST_ERROR_EXTRA,
                DETAILS_REQUEST_ERROR_MESSAGE_EXTRA,
                DETAILS_URL_MALFORMED_EXTRA -> errorHandler()
                DETAILS_RESPONSE_SUCCESS_EXTRA -> _weatherLiveData.postValue(
                    ResultWeather.Success(WeatherDTO(
                        FactDTO(
                            intent.getIntExtra(
                                DETAILS_TEMP_EXTRA, TEMP_INVALID
                            ),
                            intent.getIntExtra(DETAILS_FEELS_LIKE_EXTRA, FEELS_LIKE_INVALID),
                            intent.getStringExtra(DETAILS_CONDITION_EXTRA)
                        )
                    ))

                )
                else -> errorHandler()
            }
        }
    }

    private fun errorHandler() {

        _weatherLiveData.postValue(
            ResultWeather.Error,
        )
    }
}