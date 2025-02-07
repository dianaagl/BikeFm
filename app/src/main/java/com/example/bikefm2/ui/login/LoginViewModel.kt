package com.example.bikefm2.ui.login

import android.util.Patterns
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.bikefm2.data.UserRepository
import com.example.bikefm2.data.Result
import com.example.bikefm2.R
import com.example.bikefm2.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel @ViewModelInject constructor(
    val _userRepository: UserRepository
) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    fun login(username: String, password: String){
        // can be launched in a separate asynchronous job
        viewModelScope.launch(Dispatchers.IO) {
           val result = _userRepository.loginUser(username, password)
        }
    }

    fun register(username: String, password: String){
        // can be launched in a separate asynchronous job
        viewModelScope.launch(Dispatchers.IO) {
            val result = _userRepository.registerUser(username, password)
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}
