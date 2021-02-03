package com.example.bikefm2.ui.main

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bikefm2.data.Result
import com.example.bikefm2.data.UserRepository
import com.example.bikefm2.data.model.User
import kotlinx.coroutines.*

class MainViewModel @ViewModelInject constructor(
    val _userRepository: UserRepository
): ViewModel() {

    fun getUser(){
        viewModelScope.launch(Dispatchers.IO){
            _userRepository.getUser()
        }
    }

    fun verifyUser(){
        viewModelScope.launch(Dispatchers.IO){
            _userRepository.verifyUser()
        }
    }

    fun logout(): Job{
        return viewModelScope.launch(Dispatchers.IO) {
            _userRepository.logout()
        }
    }

}