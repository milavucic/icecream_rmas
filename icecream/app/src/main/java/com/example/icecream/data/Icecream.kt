package com.example.icecream.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class Icecream(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val galleryImages: List<String> = emptyList(),
    val location: GeoPoint = GeoPoint(0.0, 0.0)
)
