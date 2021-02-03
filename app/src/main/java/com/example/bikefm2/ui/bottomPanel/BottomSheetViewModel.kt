package com.example.bikefm2.ui.bottomPanel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikefm2.data.UserRepository
import com.example.bikefm2.data.model.User
import com.example.bikefm2.data.Result
import com.example.bikefm2.data.model.Friend

class BottomSheetViewModel @ViewModelInject constructor(
    val _userRepository: UserRepository
) : ViewModel() {

    val user: LiveData<Result<User>> = _userRepository.user

    var friends: List<Friend> = listOf()
    var sentFriendReqs: List<Friend> = listOf()
    var incomingFriendReqs: List<Friend> = listOf()

    fun confirmFriendship(friendId: String){

    }

    fun removeFriend(friendId: String){

    }

    fun denyFriendRequest(friendId: String){

    }

    fun cancelFriendRequest(friendId: String){

    }
}