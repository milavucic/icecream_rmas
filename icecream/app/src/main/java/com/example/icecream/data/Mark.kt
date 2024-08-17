package com.example.icecream.data

import com.google.firebase.firestore.DocumentId

data class Mark(
    @DocumentId val id: String = "",
    val icecreamId: String = "",
    val userId: String = "",
    var mark: Int = 0
)
