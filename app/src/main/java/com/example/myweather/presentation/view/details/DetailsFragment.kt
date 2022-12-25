package com.example.myweather.presentation.view.details

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.example.myweather.R
import com.example.myweather.databinding.FragmentDetailsBinding
import com.example.myweather.model.City

import com.example.myweather.model.Weather
import com.example.myweather.model.test.WeatherDTO
import com.example.myweather.presentation.viewModel.AppState
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_details.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL
import java.util.stream.Collectors
import javax.net.ssl.HttpsURLConnection

const val YOUR_API_KEY = "6602c458-ed8c-4142-9024-98fd92817a2f"

class DetailsFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    lateinit var weatherBundle: Weather
    private val onLoadListener: WeatherLoader.WeatherLoaderListener =
        object : WeatherLoader.WeatherLoaderListener {
            override fun onLoaded(weatherDTO: WeatherDTO) {
                displayWeather(weatherDTO)
            }

            override fun onFailed(throwable: Throwable) {
                //Обработка ошибки
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        weatherBundle = arguments?.getParcelable(BUNDLE_EXTRA) ?: Weather()
        binding.mainView.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE
        val loader = WeatherLoader(onLoadListener, weatherBundle.city.lat,
            weatherBundle.city.lon)

        loader.loadWeather()

        binding.buttonInfo.setOnClickListener(this)

    }

    companion object {
        const val BUNDLE_EXTRA = "weather"
        fun newInstance(bundle: Bundle): DetailsFragment {
            val fragment = DetailsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onClick(view: View) {

        binding.tvInfo.text = arguments?.getParcelable<Weather>(BUNDLE_EXTRA)?.let {
            getString(R.string.founded, it.city.year)
        }

        button_info.createAndShow("Внимание!", R.string.snackBar_text, this)

    }

    private fun View.createAndShow(
        text: String,
        actionText: Int,
        action: DetailsFragment,
        length: Int = Snackbar.LENGTH_SHORT,
    ) {
        Snackbar.make(this, text, length).setAction(actionText, action).show()
    }

    private fun displayWeather(weatherDTO: WeatherDTO) {

        with(binding) {
            mainView.visibility = View.VISIBLE
            loadingLayout.visibility = View.GONE
            val city = weatherBundle.city
            cityCoordinates.text = String.format(
                getString(R.string.city_coordinates),
                city.lat.toString(),
                city.lon.toString()
            )

            cityName.text = city.city
            weatherCondition.text = weatherDTO.fact?.condition
            temperatureValue.text = weatherDTO.fact?.temp.toString()
            feelsLikeValue.text = weatherDTO.fact?.feels_like.toString()
        }
    }
}
