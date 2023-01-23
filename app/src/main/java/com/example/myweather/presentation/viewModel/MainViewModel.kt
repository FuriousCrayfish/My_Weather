package com.example.myweather.presentation.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myweather.app.AppState
import com.example.myweather.model.repository.Repository
import com.example.myweather.model.repository.RepositoryImpl
import java.lang.Thread.sleep

class MainViewModel(

    private val liveDataToObserve: MutableLiveData<AppState> = MutableLiveData(),
    private val repositoryImpl: Repository = RepositoryImpl(),

    ) : ViewModel() {

    fun getLiveData() = liveDataToObserve

    fun getWeatherFromLocalSourceRus() = getDataFromLocalSource(true)
    fun getWeatherFromLocalSourceWorld() = getDataFromLocalSource(false)

    private fun getDataFromLocalSource(isRussian: Boolean) {

        liveDataToObserve.value = AppState.Loading

        Thread {
            sleep(1000)

            liveDataToObserve.postValue(AppState.Success(if (isRussian) {
                repositoryImpl.getWeatherFromLocalStorageRus()
            } else {
                repositoryImpl.getWeatherFromLocalStorageWorld()
            }))
        }.start()
    }
}
