package com.example.bikefm2.ui.map

import android.location.Location
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bikefm2.data.UserRepository
import com.example.bikefm2.data.model.Friend
import com.example.bikefm2.data.LoginResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MapViewModel @ViewModelInject constructor(
    private val _userRepository: UserRepository
): ViewModel() {
    private val _user = MutableLiveData<LoginResult>()
    val user: LiveData<LoginResult> = _user

    fun verifyUser(){
        viewModelScope.launch(Dispatchers.IO){
           val res =  _userRepository.verifyUser()
           _user.postValue(res)
        }
    }

    fun setUserLocation(loc: Location){
        viewModelScope.launch {
           val res =  _userRepository.setUserLocation(loc)
        }
    }

    fun getUser(){
        viewModelScope.launch(Dispatchers.IO){
            val res =  _userRepository.getUser()
            if(res !== null){
                _user.postValue(LoginResult(success = res))
            }
        }
    }
}