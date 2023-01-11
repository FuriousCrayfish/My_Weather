package com.example.myweather.presentation.view.details

import android.app.IntentService
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.myweather.BuildConfig
import com.example.myweather.model.test.WeatherDTO
import com.example.myweather.presentation.view.details.DetailsFragment.Companion.DETAILS_INTENT_FILTER
import com.example.myweather.presentation.viewModel.DetailViewModel.Companion.DETAILS_CONDITION_EXTRA
import com.example.myweather.presentation.viewModel.DetailViewModel.Companion.DETAILS_DATA_EMPTY_EXTRA
import com.example.myweather.presentation.viewModel.DetailViewModel.Companion.DETAILS_FEELS_LIKE_EXTRA
import com.example.myweather.presentation.viewModel.DetailViewModel.Companion.DETAILS_INTENT_EMPTY_EXTRA
import com.example.myweather.presentation.viewModel.DetailViewModel.Companion.DETAILS_LOAD_RESULT_EXTRA
import com.example.myweather.presentation.viewModel.DetailViewModel.Companion.DETAILS_REQUEST_ERROR_EXTRA
import com.example.myweather.presentation.viewModel.DetailViewModel.Companion.DETAILS_REQUEST_ERROR_MESSAGE_EXTRA
import com.example.myweather.presentation.viewModel.DetailViewModel.Companion.DETAILS_RESPONSE_EMPTY_EXTRA
import com.example.myweather.presentation.viewModel.DetailViewModel.Companion.DETAILS_RESPONSE_SUCCESS_EXTRA
import com.example.myweather.presentation.viewModel.DetailViewModel.Companion.DETAILS_TEMP_EXTRA
import com.example.myweather.presentation.viewModel.DetailViewModel.Companion.DETAILS_URL_MALFORMED_EXTRA
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import java.util.stream.Collectors
import javax.net.ssl.HttpsURLConnection

const val LATITUDE_EXTRA = "Latitude"
const val LONGITUDE_EXTRA = "Longitude"
private const val REQUEST_GET = "GET"
private const val REQUEST_TIMEOUT = 10000
private const val REQUEST_API_KEY = "X-Yandex-API-Key"
const val YOUR_API_KEY = "6602c458-ed8c-4142-9024-98fd92817a2f"

class DetailsService(name : String = "DetailService") : IntentService(name) {

    private val broadcastIntent = Intent(DETAILS_INTENT_FILTER)

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onHandleIntent(intent : Intent?) {
        if (intent == null){
            onEmptyIntent()
        }else{
            val lat = intent.getDoubleExtra(LATITUDE_EXTRA, 0.0)
            val lon = intent.getDoubleExtra(LONGITUDE_EXTRA, 0.0)

            if (lat ==0.0 && lon == 0.0){
                onEmptyData()
            }else{
                loadWeather(lat.toString(), lon.toString())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadWeather(lat : String, lon : String) {

        try {
            val uri = URL("https://api.weather.yandex.ru/v2/forecast?lat=${lat}&lon=${lon}")


                lateinit var urlConnection: HttpsURLConnection

                try {
                    urlConnection = uri.openConnection() as HttpsURLConnection
                    urlConnection.apply {
                        requestMethod = REQUEST_GET
                        readTimeout = REQUEST_TIMEOUT
                        addRequestProperty(REQUEST_API_KEY, YOUR_API_KEY)
                    }

                    val weatherDTO: WeatherDTO = Gson().fromJson(
                        getLines(BufferedReader(InputStreamReader(urlConnection.inputStream))),
                        WeatherDTO::class.java
                    )
                    onResponse(weatherDTO)
                }catch (e : Exception){
                    onErrorRequest(e.message?: "Empty error")
                }finally {
                    urlConnection.disconnect()
                }
        }catch (e : MalformedURLException){
            onMalformedURL()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getLines(reader: BufferedReader): String {
        return reader.lines().collect(Collectors.joining("\n"))
    }

    private fun onResponse(weatherDTO: WeatherDTO){
        val fact = weatherDTO.fact
        if (fact == null){
            onEmptyResponse()
        }else{
            onSuccessResponse(fact.temp, fact.feels_like, fact.condition)
        }
    }

    private fun onSuccessResponse(temp : Int?, feesLike : Int?, condition : String?){

        putLoadResult(DETAILS_RESPONSE_SUCCESS_EXTRA)
        broadcastIntent.putExtra(DETAILS_TEMP_EXTRA, temp)
        broadcastIntent.putExtra(DETAILS_FEELS_LIKE_EXTRA, feesLike)
        broadcastIntent.putExtra(DETAILS_CONDITION_EXTRA, condition)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)

    }

    private fun onMalformedURL() {
        putLoadResult(DETAILS_URL_MALFORMED_EXTRA)

        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
    }
    private fun onErrorRequest(error: String) {
        putLoadResult(DETAILS_REQUEST_ERROR_EXTRA)
        broadcastIntent.putExtra(DETAILS_REQUEST_ERROR_MESSAGE_EXTRA, error)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
    }
    private fun onEmptyResponse() {
        putLoadResult(DETAILS_RESPONSE_EMPTY_EXTRA)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
    }
    private fun onEmptyIntent() {
        putLoadResult(DETAILS_INTENT_EMPTY_EXTRA)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
    }
    private fun onEmptyData() {
        putLoadResult(DETAILS_DATA_EMPTY_EXTRA)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
    }
    private fun putLoadResult(result: String) {
        broadcastIntent.putExtra(DETAILS_LOAD_RESULT_EXTRA, result)
    }
}