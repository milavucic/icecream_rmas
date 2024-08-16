package com.example.icecream.repositories

import android.net.Uri
import com.example.icecream.data.User
import com.google.firebase.auth.FirebaseUser
import com.google.rpc.context.AttributeContext
import java.lang.Exception

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun login(email: String, password: String): Resource<FirebaseUser>
    suspend fun register(email: String, fullName: String, phoneNumber: String, profileImage: Uri,  password: String): Resource<FirebaseUser>
    fun logout()
    suspend fun getUser(): Resource<User>
    suspend fun getAllUsers(): Resource<List<User>>
}

sealed class Resource<out R> {
    data class Success<out R>(val result: R): Resource<R>()
    data class Failure(val exception: Exception): Resource<Nothing>()
    data object loading: Resource<Nothing>()
}