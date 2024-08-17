package com.example.icecream.data
import com.google.firebase.firestore.DocumentId
data class User(
    @DocumentId val id: String = "",
    val email: String,
    val fullName: String,
    val phone: String,
    val image: String,
    val points: Int=0
)
