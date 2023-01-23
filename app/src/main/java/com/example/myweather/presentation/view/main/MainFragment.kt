package com.example.myweather.presentation.view.main

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.example.myweather.R
import com.example.myweather.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar
import com.example.myweather.model.Weather
import com.example.myweather.presentation.view.details.DetailsFragment
import com.example.myweather.app.AppState
import com.example.myweather.presentation.viewModel.MainViewModel
import kotlinx.android.synthetic.main.fragment_main.*

private const val IS_WORLD_KEY = "LIST_OF_TOWNS_KEY"

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mainFragmentRecyclerView.adapter = adapter
        binding.mainFragmentFAB.setOnClickListener { changeWeatherDataSet() }
        viewModel.getLiveData().observe(viewLifecycleOwner, Observer {
            renderData(it)
        })
        showListOfTowns()
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