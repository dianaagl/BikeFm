package com.example.bikefm2.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bikefm2.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setActionBar(findViewById(R.id.my_toolbar))
    }
}