package com.example.bikefm2.ui.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.bikefm2.data.UserRepository

class MainViewModel @ViewModelInject constructor(
    private val _userRepository: UserRepository
): ViewModel() {

}