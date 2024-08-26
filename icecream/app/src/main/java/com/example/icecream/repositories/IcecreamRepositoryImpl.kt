package com.example.icecream.repositories

import android.net.Uri
import com.example.icecream.data.Icecream
import com.example.icecream.services.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.tasks.await

class IcecreamRepositoryImpl : IcecreamRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val storageInstance = FirebaseStorage.getInstance()

    private val firebaseService = FirebaseService(storageInstance,firestoreInstance)


    override suspend fun saveIcecream(
        name: String,
        description: String,
        galleryImages: List<Uri>,
        location: LatLng
    ): Resource<String> {
        return try{
            val currentUser = firebaseAuth.currentUser
            if(currentUser!=null){

                val galleryImagesUrls = firebaseService.uploadIcecreamGalleryImages(galleryImages)
                val location = GeoPoint(
                    location.latitude,
                    location.longitude
                )
                val icecream = Icecream(
                    userId = currentUser.uid,
                    name=name,
                    description = description,
                    galleryImages = galleryImagesUrls,
                    location = location
                )
                firebaseService.saveIcecreamData(icecream)
                firebaseService.addUserPoints(currentUser.uid, 5)
            }
            Resource.Success("Uspešno sačuvani podaci")
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun getAllIcecreams(): Resource<List<Icecream>> {
        return try{
            val snapshot = firestoreInstance.collection("icecreams").get().await()
            val ic = snapshot.toObjects(Icecream::class.java)
            Resource.Success(ic)
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }


    override suspend fun getUserIcecreams(uid: String): Resource<List<Icecream>> {
        return try {
            val snapshot = firestoreInstance.collection("icecreams")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            val ic = snapshot.toObjects(Icecream::class.java)
            Resource.Success(ic)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

}