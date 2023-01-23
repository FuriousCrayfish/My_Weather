package com.example.myweather.presentation.view.details

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import coil.api.load
import com.example.myweather.R
import com.example.myweather.databinding.FragmentDetailsBinding
import com.example.myweather.model.City

import com.example.myweather.model.Weather
import com.example.myweather.app.AppState
import com.example.myweather.presentation.viewModel.DetailViewModel
import com.example.myweather.utils.showSnackBar
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_details.*


class DetailsFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    lateinit var weatherBundle: Weather

    private val viewModel: DetailViewModel by lazy {
        ViewModelProvider(this)[DetailViewModel::class.java]
    }

    companion object {
        const val BUNDLE_EXTRA = "weather"
        fun newInstance(bundle: Bundle): DetailsFragment {
            val fragment = DetailsFragment()
            fragment.arguments = bundle
            return fragment
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
        viewModel.detailsLiveData.observe(viewLifecycleOwner, Observer { renderData(it) })
        requestWeather()

        binding.buttonInfo.setOnClickListener(this)

    }

    private fun requestWeather() {
        viewModel.getWeather(weatherBundle.city.lat, weatherBundle.city.lon)
    }

    private fun renderData(appState: AppState) {

        when (appState) {
            is AppState.Success -> {
                hideProgress()
                setWeather(appState.weatherData[0])
            }

            is AppState.Loading -> {
                showProgress()
            }
            is AppState.Error -> {
                hideProgress()
                showToast()

                binding.mainView.showSnackBar(
                    getString(R.string.error),
                    getString(R.string.reload),
                    {
                        requestWeather()
                    })
            }
        }
    }

    private fun setWeather(weather: Weather) {

        val city = weatherBundle.city
        binding.cityName.text = city.city
        binding.cityCoordinates.text = String.format(
            getString(R.string.city_coordinates),
            city.lat.toString(),
            city.lon.toString()
        )
        binding.temperatureValue.text = weather.temperature.toString()
        binding.feelsLikeValue.text = weather.feelsLike.toString()
        binding.weatherCondition.text = weather.condition
        //добавление иконки погоды
        weather.icon.let {
            GlideToVectorYou.justLoadImage(activity,
                Uri.parse("https://yastatic.net/weather/i/icons/blueye/color/svg/${it}.svg"),
                weatherIcon)
        }

        binding.headerIcon.load("https://freepngimg.com/thumb/city/36421-8-city-picture.png")

    }

    private fun showToast() {
        hideProgress()
        Toast.makeText(requireContext(), "Что то пошло не так", Toast.LENGTH_SHORT).show()
    }

    private fun showProgress() {

        binding.mainView.visibility = View.GONE
        binding.loadingLayout.visibility = View.VISIBLE

    }

    private fun hideProgress() {

        binding.mainView.visibility = View.VISIBLE
        binding.loadingLayout.visibility = View.GONE

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    private fun saveCity(city: City, weather: Weather){

        viewModel.saveCityToDB(Weather(
            city,
            weather.temperature,
            weather.feelsLike,
            weather.condition
        ))
    }
}