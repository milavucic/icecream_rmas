package com.example.icecream.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.icecream.data.User
import com.example.icecream.repositories.AuthRepositoryImpl
import com.example.icecream.repositories.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class AuthViewModel(): ViewModel(){
    val repository = AuthRepositoryImpl()
    private val _loginFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val loginFlow: StateFlow<Resource<FirebaseUser>?> = _loginFlow

    private val _registerFlow = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val registerFlow: StateFlow<Resource<FirebaseUser>?> = _registerFlow

    private val _currentUserFlow = MutableStateFlow<Resource<User>?>(null)
    val currentUserFlow: StateFlow<Resource<User>?> = _currentUserFlow

    private val _allUsers = MutableStateFlow<Resource<List<User>>?>(null)
    val allUsers: StateFlow<Resource<List<User>>?> = _allUsers

    val currentUser: FirebaseUser?
        get() = repository.currentUser





    init {
        if(repository.currentUser != null){
            _loginFlow.value = Resource.Success(repository.currentUser!!)
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch{
        _loginFlow.value = Resource.loading
        val result = repository.login(email, password)
        _loginFlow.value = result
    }
    fun register(fullName: String, phoneNumber: String, profileImage: Uri, email: String, password: String) = viewModelScope.launch{
        _registerFlow.value = Resource.loading
        val result = repository.register(email, fullName, phoneNumber, profileImage, password)
        _registerFlow.value = result
    }

    fun logout(){
        repository.logout()
        _loginFlow.value = null
        _registerFlow.value = null
        _currentUserFlow.value = null
    }
}

class AuthViewModelFactory: ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AuthViewModel::class.java)){
            return AuthViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}