package com.example.myweather.presentation.view.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myweather.R
import com.example.myweather.databinding.ActivityMainBinding
import com.geekbrains.androidkotlinapp.view.MainFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.getRoot()
        setContentView(view)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }
}