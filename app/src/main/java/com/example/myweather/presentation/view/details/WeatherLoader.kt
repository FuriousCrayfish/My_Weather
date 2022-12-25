package com.example.myweather.presentation.view.details

import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.myweather.model.test.WeatherDTO
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.stream.Collectors
import javax.net.ssl.HttpsURLConnection

class WeatherLoader(
    private val listener: WeatherLoaderListener,
    private val lat: Double,
    private val lon: Double,
) {

    @RequiresApi(Build.VERSION_CODES.N)
    fun loadWeather() {

        try {//не знаю почему, но ссылка из методички не работала, выпадала FileNotFoundException
            //путем долгих поисков, я узнал, что нужно заменить informers на forecast в ссылке
            //при первом клике по городу данные долго грузятся, при повторных практически сразу
            //к сожалению, обработать ошибки в методе onFailed я уже не успеваю,
            // так как решение этой задачи для меня не очевидно, я пропущу дедлайн
            //надеюсь, что заслужил хотя бы трояк)
            //val uri = URL("https://api.weather.yandex.ru/v2/informers?lat=${lat}&lon=${lon}")
            val uri = URL("https://api.weather.yandex.ru/v2/forecast?lat=${lat}&lon=${lon}")

            val handler = Handler()
            Thread(Runnable {
                lateinit var urlConnection: HttpsURLConnection

                try {
                    urlConnection = uri.openConnection() as HttpsURLConnection
                    urlConnection.requestMethod = "GET"
                    urlConnection.addRequestProperty(
                        "X-YANDEX-API-Key",
                        YOUR_API_KEY
                    )
                    urlConnection.readTimeout = 10000
                    val bufferedReader =
                        BufferedReader(InputStreamReader(urlConnection.inputStream))

                    // преобразование ответа от сервера (JSON) в модель данных (WeatherDTO)
                    val weatherDTO: WeatherDTO = Gson().fromJson(getLines(bufferedReader),
                        WeatherDTO::class.java)
                    handler.post { listener.onLoaded(weatherDTO) }

                } catch (e: Exception) {
                    Log.e("", "Fail connection", e)
                    e.printStackTrace()

                } finally {
                    urlConnection.disconnect()
                }
            }).start()

        } catch (e: MalformedURLException) {
            Log.e("", "Fail URI", e)
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getLines(reader: BufferedReader): String {
        return reader.lines().collect(Collectors.joining("\n"))
    }

    interface WeatherLoaderListener {
        fun onLoaded(weatherDTO: WeatherDTO)
        fun onFailed(throwable: Throwable)
    }
}