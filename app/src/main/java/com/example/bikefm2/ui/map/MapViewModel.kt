package com.example.bikefm2.ui.map

import android.location.Location
import com.example.bikefm2.data.Location as myLocation
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bikefm2.data.UserRepository
import com.example.bikefm2.data.model.Friend
import com.example.bikefm2.data.LoginResult
import com.example.bikefm2.data.model.User
import com.example.bikefm2.data.Result

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MapViewModel @ViewModelInject constructor(
    private val _userRepository: UserRepository
): ViewModel() {

    fun setUserLocation(loc: Location){
        viewModelScope.launch {
           val res =  _userRepository.setUserLocation(myLocation(loc.latitude, loc.longitude))
        }
    }


}