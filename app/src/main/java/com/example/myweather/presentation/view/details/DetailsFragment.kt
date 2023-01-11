package com.example.myweather.presentation.view.details

import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.myweather.R
import com.example.myweather.databinding.FragmentDetailsBinding

import com.example.myweather.model.Weather
import com.example.myweather.model.test.WeatherDTO
import com.example.myweather.presentation.view.details.DetailsFragment.Companion.BUNDLE_EXTRA
import com.example.myweather.presentation.viewModel.DetailViewModel
import com.example.myweather.presentation.viewModel.ResultWeather
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_details.*


class DetailsFragment : Fragment(), View.OnClickListener {

    companion object {
        const val BUNDLE_EXTRA = "weather"
        fun newInstance(bundle: Bundle): DetailsFragment {
            val fragment = DetailsFragment()
            fragment.arguments = bundle
            return fragment
        }

        const val DETAILS_INTENT_FILTER = "DETAILS INTENT FILTER"

    }

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailViewModel by lazy {
        ViewModelProvider(this)[DetailViewModel::class.java]
    }

    lateinit var weatherBundle: Weather

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            LocalBroadcastManager.getInstance(it)
                .registerReceiver(viewModel.getWeather(),
                    IntentFilter(DETAILS_INTENT_FILTER))
        }
    }

    override fun onDestroy() {

        context?.let {
            LocalBroadcastManager.getInstance(it).unregisterReceiver(viewModel.getWeather())
        }
        super.onDestroy()
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
        startService()
        observe()

        binding.buttonInfo.setOnClickListener(this)

    }

    private fun startService() {

        context?.let {
            it.startService(Intent(it, DetailsService::class.java).apply {
                putExtra(
                    LATITUDE_EXTRA,
                    weatherBundle.city.lat
                )

                putExtra(
                    LONGITUDE_EXTRA,
                    weatherBundle.city.lon
                )
            })
        }
    }

    private fun observe() {
        viewModel.weatherLiveData.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultWeather.Loading -> showProgress()
                is ResultWeather.Error -> showToast()
                is ResultWeather.Success -> renderData(result.data)
            }
        }
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

    private fun renderData(weatherDTO: WeatherDTO) {
        hideProgress()

        val fact = weatherDTO.fact
        val temp = fact!!.temp
        val feelsLike = fact.feels_like
        val condition = fact.condition

        val city = weatherBundle.city
        binding.cityName.text = city.city
        binding.cityCoordinates.text = String.format(
            getString(R.string.city_coordinates),
            city.lat.toString(),
            city.lon.toString()
        )

        binding.temperatureValue.text = temp.toString()
        binding.feelsLikeValue.text = feelsLike.toString()
        binding.weatherCondition.text = condition
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
}
