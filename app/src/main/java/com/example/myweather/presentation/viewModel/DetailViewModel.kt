package com.example.myweather.presentation.viewModel

import com.example.myweather.app.App.Companion.getHistoryDao
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myweather.app.AppState
import com.example.myweather.model.repository.RemoteDataSource
import com.example.myweather.model.Weather
import com.example.myweather.model.repository.DetailsRepository
import com.example.myweather.model.repository.DetailsRepositoryImpl
import com.example.myweather.model.test.WeatherDTO
import com.example.myweather.model.repository.LocalRepository
import com.example.myweather.model.repository.LocalRepositoryImpl
import com.example.myweather.utils.convertDtoToModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val SERVER_ERROR = "Ошибка сервера"
private const val REQUEST_ERROR = "Ошибка запрса на сервер"
private const val CORRUPTED_DATA = "Неполные данные"

class DetailViewModel(
    val detailsLiveData: MutableLiveData<AppState> = MutableLiveData(),
    private val detailsRepositoryImpl: DetailsRepository = DetailsRepositoryImpl(RemoteDataSource()),
    private val historyRepository: LocalRepository =
        LocalRepositoryImpl(getHistoryDao())
) : ViewModel() {

    fun saveCityToDB(weather: Weather){
        Thread { historyRepository.saveEntity(weather) }.start()
    }

    private val callBack = object : Callback<WeatherDTO> {
        override fun onResponse(call: Call<WeatherDTO>, response: Response<WeatherDTO>) {
            val serverResponse: WeatherDTO? = response.body()

            detailsLiveData.postValue(
                if (response.isSuccessful && serverResponse != null) {
                    checkResponse(serverResponse)
                } else {
                    AppState.Error(Throwable(SERVER_ERROR))
                }
            )
        }

        override fun onFailure(call: Call<WeatherDTO>, t: Throwable) {
            detailsLiveData.postValue(AppState.Error(Throwable(t?.message ?: REQUEST_ERROR)))
        }

        private fun checkResponse(serverResponse: WeatherDTO): AppState {


            val fact = serverResponse.fact

            return if (fact == null || fact.temp == null || fact.feels_like == null ||
                fact.condition.isNullOrEmpty()
            ) {
                AppState.Error(Throwable(CORRUPTED_DATA))
            } else {
                AppState.Success(convertDtoToModel(serverResponse))
            }

        }
    }

    fun getWeather(lat: Double, lon: Double) {
        detailsLiveData.value = AppState.Loading
        detailsRepositoryImpl.getWeatherDetailsFromServer(lat, lon, callBack)
    }
}