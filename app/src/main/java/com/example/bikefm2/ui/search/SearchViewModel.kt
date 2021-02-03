package com.example.bikefm2.ui.search

import android.widget.Toast
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.bikefm2.data.Result
import com.example.bikefm2.data.UserRepository
import com.example.bikefm2.data.model.Friend
import com.example.bikefm2.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class SearchViewModel @ViewModelInject constructor(
    val _userRepository: UserRepository
): ViewModel() {
    private val _friends = MutableLiveData<Result<List<Friend>>>()
    val friends: LiveData<Result<List<Friend>>> = _friends

    fun findUsers(query: String){
        viewModelScope.launch(Dispatchers.IO) {
            try{
                val users = _userRepository.findUser(query)
                _friends.postValue(users)
            }
            catch (e: Exception){
                _friends.postValue(Result.Error(e))
            }
        }
    }

    fun addFriend(friend: Friend){
        viewModelScope.launch(Dispatchers.IO)  {
            _userRepository.addFriend(friend.userId)
        }
    }
}