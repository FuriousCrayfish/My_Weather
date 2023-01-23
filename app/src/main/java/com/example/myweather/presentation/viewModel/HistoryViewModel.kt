package com.example.myweather.presentation.viewModel

import com.example.myweather.app.App.Companion.getHistoryDao
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myweather.app.AppState
import com.example.myweather.model.repository.LocalRepositoryImpl

class HistoryViewModel(
    val historyLiveData: MutableLiveData<AppState> = MutableLiveData(),
    private val historyRepositoryImpl: LocalRepositoryImpl = LocalRepositoryImpl(getHistoryDao())
) : ViewModel() {

    fun getAllHistory() {
        historyLiveData.value = AppState.Loading
        Thread { historyLiveData.postValue(AppState.Success(historyRepositoryImpl.getAllHistory()))}
            .start()
    }
}
