package viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import model.Repository
import model.RepositoryImpl
import java.lang.Thread.sleep

class MainViewModel(

    private val liveDataToObserve : MutableLiveData<AppState> = MutableLiveData(),
    private val repositoryImpl : Repository = RepositoryImpl()

) : ViewModel() {

    fun getLiveData() = liveDataToObserve

    fun getWeather() = getDataFromLocalSource()

    private fun getDataFromLocalSource() {

        liveDataToObserve.value = AppState.Loading

        Thread {
            sleep(1000)

            liveDataToObserve.postValue(
                AppState.Success(repositoryImpl.getWeatherFromLocalStorage()))
        }.start()
    }
}