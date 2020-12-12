package com.example.bikefm2.ui.map

import android.location.Location
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bikefm2.data.UserRepository
import com.example.bikefm2.data.db.UserDao
import com.example.bikefm2.data.model.Friend
import com.example.bikefm2.ui.login.LoginResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MapViewModel @ViewModelInject constructor(
    private val _userRepository: UserRepository
): ViewModel() {
    private val _user = MutableLiveData<LoginResult>()
    val user: LiveData<LoginResult> = _user

    private val _friendsList = MutableLiveData<List<Friend>>()
    var friendsList: LiveData<List<Friend>> = _friendsList



    fun verifyUser(){
        viewModelScope.launch(Dispatchers.IO){

           val res =  _userRepository.verifyUser()
           _user.postValue(res)
        }
    }

    fun addFriends(users: List<Friend>){
        viewModelScope.launch (Dispatchers.IO){
            _userRepository.addFriendsList(users)
            _friendsList.postValue(users)
        }
    }

    fun setUserLocation(loc: Location){
        viewModelScope.launch {

           val res =  _userRepository.setUserLocation(loc)
        }
    }
}