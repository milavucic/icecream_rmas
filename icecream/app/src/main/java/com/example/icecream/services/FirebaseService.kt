package com.example.icecream.services

import android.net.Uri
import com.example.icecream.data.Icecream
import com.example.icecream.data.User
import com.example.icecream.repositories.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseService (
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore
){
    suspend fun saveUserData(
        uid: String,
        user: User
    ): Resource<String> {
        return try {
            firestore.collection("users").document(uid).set(user).await()
            Resource.Success("Uspešno dodati podaci o korisniku")
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    suspend fun uploadPicture(
        uid: String,
        image: Uri
    ): String{
        return try{
            val storageRef = storage.reference.child("profile_picture/$uid.jpg")
            val uploadTask = storageRef.putFile(image).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            downloadUrl.toString()
        }catch (e: Exception){
            e.printStackTrace()
            ""
        }
    }

    suspend fun saveIcecreamData(
       icecream: Icecream
    ): Resource<String>{
        return try{
            firestore.collection("icecrems").add(icecream).await()
            Resource.Success("Uspešno sačuvani podaci")
        }catch(e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    suspend fun uploadIcecreamGalleryImages(
        images: List<Uri>
    ): List<String>{
        val downloadUrls = mutableListOf<String>()
        for (image in images) {
            try {
                val fileName = "${System.currentTimeMillis()}.jpg"
                val storageRef = storage.reference.child("icecream_images/gallery_images/$fileName")
                val uploadTask = storageRef.putFile(image).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await()
                downloadUrls.add(downloadUrl.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return downloadUrls
    }



}