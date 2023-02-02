package com.example.myweather.presentation.view.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.myweather.R
import com.example.myweather.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar
import com.example.myweather.model.Weather
import com.example.myweather.presentation.view.details.DetailsFragment
import com.example.myweather.app.AppState
import com.example.myweather.model.City
import com.example.myweather.presentation.view.contentProvider.REQUEST_CODE
import com.example.myweather.presentation.viewModel.MainViewModel
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.IOException

private const val IS_WORLD_KEY = "LIST_OF_TOWNS_KEY"
private const val REQUEST_CODE = 12345
private const val REFRESH_PERIOD = 60000L
private const val MINIMAL_DISTANCE = 100f

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel : MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel :: class.java)
    }

    private var isDataSetRus: Boolean = true

    private val adapter = MainFragmentAdapter(object : OnItemViewClickListener {

        override fun onItemViewClick(weather: Weather) {
            activity?.supportFragmentManager?.apply {
                beginTransaction()
                    .add(R.id.container, DetailsFragment.newInstance(Bundle().apply {
                        putParcelable(DetailsFragment.BUNDLE_EXTRA, weather)
                    }))
                    .addToBackStack("")
                    .commitAllowingStateLoss()
            }
        }
    })

    private val onLocationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            context?.let {
                getAddressAsync(it, location)
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainFragmentRecyclerView.adapter = adapter
        mainFragmentFAB.setOnClickListener { changeWeatherDataSet() }
        mainFragmentFABLocation.setOnClickListener{checkPermission()}
        viewModel.getLiveData().observe(viewLifecycleOwner, Observer {
            renderData(it)
        })
        showListOfTowns()
    }

    private fun checkPermission(){
        activity?.let {
            when{
                ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION)==
                        PackageManager.PERMISSION_GRANTED ->{
                            getLocation()
                        }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ->{
                    showRationaleDialog()
                }
                else -> {
                    requestPermission()
                }
            }
        }
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE
        )
    }

    private fun showRationaleDialog() {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.dialog_rationale_title))
                .setMessage(getString(R.string.dialog_rationale_meaasge))
                .setPositiveButton(getString(R.string.dialog_rationale_give_access)) { _, _ ->
                    requestPermission()
                }
                .setNegativeButton(getString(R.string.dialog_rationale_decline)) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    private fun showDialog(title: String, message: String) {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(getString(R.string.dialog_button_close)) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    private fun getAddressAsync(
        context: Context,
        location: Location
    ) {
        val geoCoder = Geocoder(context)
        Thread {
            try {
                val addresses = geoCoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                )
                binding.mainFragmentFAB.post {
                    showAddressDialog(addresses[0].getAddressLine(0), location)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun showAddressDialog(address: String, location: Location) {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.dialog_address_title))
                .setMessage(address)
                .setPositiveButton(getString(R.string.dialog_address_get_weather)) { _, _ ->
                    openDetailsFragment(
                        Weather(
                            //!!!
                            City(address, location.latitude, location.longitude, String())
                        )
                    )
                }
                .setNegativeButton(getString(R.string.dialog_button_close)) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    private fun openDetailsFragment(
        weather: Weather
    ) {
        activity?.supportFragmentManager?.apply {
            beginTransaction()
                .add(
                    R.id.container,
                    DetailsFragment.newInstance(Bundle().apply {
                        putParcelable(DetailsFragment.BUNDLE_EXTRA, weather)
                    })
                )
                .addToBackStack("")
                .commitAllowingStateLoss()
        }
    }

    private fun getLocation() {
        activity?.let { context ->
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                val locationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    val provider = locationManager.getProvider(LocationManager.GPS_PROVIDER)
                    provider?.let {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            REFRESH_PERIOD,
                            MINIMAL_DISTANCE,
                            onLocationListener
                        )
                    }
                } else {
                    val location =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (location == null) {
                        showDialog(
                            getString(R.string.dialog_title_gps_turned_off),
                            getString(R.string.dialog_message_last_location_unknown)
                        )
                    } else {
                        getAddressAsync(context, location)
                        showDialog(
                            getString(R.string.dialog_title_gps_turned_off),
                            getString(R.string.dialog_message_last_known_location)
                        )
                    }
                }
            } else {
                showRationaleDialog()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun changeWeatherDataSet() {
        if (isDataSetRus) {
            viewModel.getWeatherFromLocalSourceWorld()
            binding.mainFragmentFAB.setImageResource(R.drawable.ic_earth)
        } else {
            viewModel.getWeatherFromLocalSourceRus()
            binding.mainFragmentFAB.setImageResource(R.drawable.ic_russia)
        }
        isDataSetRus = !isDataSetRus

        saveListOfTowns()
    }

    private fun renderData(appState: AppState) {

        when (appState) {

            is AppState.Success -> {
                binding.mainFragmentLoadingLayout.visibility = View.GONE
                adapter.setWeather(appState.weatherData)
            }

            is AppState.Loading -> {
                binding.mainFragmentLoadingLayout.visibility = View.VISIBLE
            }

            is AppState.Error -> {
                binding.mainFragmentLoadingLayout.visibility = View.GONE
                mainFragmentRootView.showSnackBar(
                    getString(R.string.error),
                    getString(R.string.reload),
                    {viewModel.getWeatherFromLocalSourceRus()}
                )
            }
        }
    }

    private fun saveListOfTowns() {
        activity?.let {
            with(it.getPreferences(Context.MODE_PRIVATE).edit()) {
                putBoolean(IS_WORLD_KEY, !isDataSetRus)
                apply()
            }
        }
    }

    private fun showListOfTowns() {
        activity?.let {
            if (it.getPreferences(Context.MODE_PRIVATE)
                    .getBoolean(IS_WORLD_KEY, false)
            ) changeWeatherDataSet() else viewModel.getWeatherFromLocalSourceRus()
        }
    }

    private fun View.showSnackBar(
        text : String,
        actionText : String,
        action : (View) -> Unit,
        length : Int = Snackbar.LENGTH_INDEFINITE
    ){
        Snackbar.make(this,text, length).setAction(actionText, action).show()
    }


    interface OnItemViewClickListener{
        fun onItemViewClick(weather: Weather)
    }

    companion object {
        fun newInstance() =
            MainFragment()
    }
}