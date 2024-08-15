package com.example.icecream.repositories

import android.net.Uri
import com.example.icecream.data.Icecream
import com.google.type.LatLng

interface IcecreamRepository {
    suspend fun saveIcecream(
        name: String,
        description: String,
        galleryImages: List<Uri>,
        location: LatLng
    ): Resource<String>

    suspend fun getAllIcecreams(): Resource<List<Icecream>>

    suspend fun getUserIcecreams(
        uid: String
    ): Resource<List<Icecream>>


}