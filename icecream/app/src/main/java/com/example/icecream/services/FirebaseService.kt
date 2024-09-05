package com.example.icecream.services

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.icecream.data.Icecream
import com.example.icecream.data.Mark
import com.example.icecream.data.User
import com.example.icecream.repositories.Resource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class FirebaseService (
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore
){
    private val _galleryImageUrls = MutableLiveData<List<String>>()
    val galleryImageUrls: LiveData<List<String>> get() = _galleryImageUrls

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
            val storageRef = storage.reference.child("registration_uploads/$uid.jpg")
            val uploadTask = storageRef.putFile(image).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            Log.d("UploadPicture", "Download URL: $downloadUrl")
            downloadUrl.toString()
        }catch (e: Exception){
            //e.printStackTrace()
            Log.e("UploadPicture", "Failed to upload image", e)
            ""
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
                Log.d("UploadPicture", "Download gallery URL: $downloadUrl")
                downloadUrls.add(downloadUrl.toString())
            } catch (e: Exception) {
                //e.printStackTrace()
                Log.e("UploadPicture", "Failed to upload gallery image", e)
            }
        }

        return downloadUrls
    }

    fun uploadImages(images: List<Uri>, onComplete: (List<String>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val urls = uploadIcecreamGalleryImages(images)
            _galleryImageUrls.postValue(urls)
            withContext(Dispatchers.Main) {
                onComplete(urls)
            }
        }
    }
    fun getIcecreamGalleryImages(onComplete: (List<String>) -> Unit) {
        val galleryImagesRef = FirebaseStorage.getInstance().reference.child("icecream_images/gallery_images/")
        galleryImagesRef.listAll().addOnSuccessListener { result ->
            val imageUrls = mutableListOf<String>()
            val tasks = result.items.map { storageRef ->
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    imageUrls.add(uri.toString())
                }
            }
            Tasks.whenAllComplete(tasks).addOnCompleteListener {
                onComplete(imageUrls)
            }
        }.addOnFailureListener {
            onComplete(emptyList())
        }
    }

    /* suspend fun movePicture(oldPath: String, newPath: String) {
         try {
             val storageRef = storage.reference
             val oldRef = storageRef.child(oldPath)
             val newRef = storageRef.child(newPath)

             // Download the file from the old path
             val fileUri = oldRef.downloadUrl.await()
             val file = File.createTempFile("tempFile", null)
             oldRef.getFile(file).await()

             // Upload the file to the new path
             val uploadTask = newRef.putFile(Uri.fromFile(file)).await()
             val downloadUrl = uploadTask.storage.downloadUrl.await()

             // Optionally delete the old file after uploading
             oldRef.delete().await()

             Log.d("MovePicture", "File moved successfully to: $downloadUrl")

         } catch (e: Exception) {
             Log.e("MovePictureError", "Failed to move picture", e)
         }
     }*/


    suspend fun movePicture(oldPath: String, newPath: String) {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.e("MovePicture", "User is not authenticated.")
                return
            }
            else {
            Log.d("MovePicture", "User authenticated with UID: ${currentUser.uid}")
            }
        } catch (e: Exception) {
            Log.e("MovePicture", "Failed auth", e)
            return
        }
        try {

            val storageRef = storage.reference
            val oldRef = storageRef.child(oldPath)
            val newRef = storageRef.child(newPath)

            // Copy the file from the old path to the new path
            val fileUri = oldRef.downloadUrl.await()
            val file = File.createTempFile("tempFile", null)
            oldRef.getFile(file).await()

            // Upload the copied file to the new path
            newRef.putFile(Uri.fromFile(file)).await()

            // Delete the old file
            oldRef.delete().await()

            Log.d("MovePicture", "Successfully moved image to: $newPath")
        } catch (e: Exception) {
            Log.e("MovePicture", "Failed to move image", e)
        }
    }


    suspend fun saveIcecreamData(
       icecream: Icecream
    ): Resource<String>{
        return try{
            firestore.collection("icecreams").add(icecream).await()
            Resource.Success("Uspešno sačuvani podaci")
        }catch(e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }



    suspend fun addUserPoints(
        userid: String,
        points: Int
    ): Resource<String>{
        return try {
            val userDoc = firestore.collection("users").document(userid)
            val userSnapshot = userDoc.get().await()

            if(userSnapshot.exists()){
                val user = userSnapshot.toObject(User::class.java)
                if(user != null){
                    val newPoints = user.points + points
                    userDoc.update("points", newPoints).await()
                    Resource.Success("Poeni su uspešno dodati")
                } else {
                    Resource.Failure(Exception("Nepostojeći korisnik"))
                }
            } else {
                Resource.Failure(Exception("Nepostojeći korisnik"))
            }
            Resource.Success("Uspešno dodato")
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }


    suspend fun getUserData(
        userid: String
    ):Resource<String>{
        return try {
            val userDoc = firestore.collection("users").document(userid)
            val userSnapshot = userDoc.get().await()

            if(userSnapshot.exists()){
                val user = userSnapshot.toObject(User::class.java)
                if(user != null){
                    Resource.Success(user)
                } else {
                    Resource.Failure(Exception("Nepostojeći korisnik"))
                }
            } else {
                Resource.Failure(Exception("Nepostojeći korisnik"))
            }
            Resource.Success("Uspešno dodato")
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    suspend fun saveIcecreamMark(
        mark: Mark
    ): Resource<String>{
        return try{
            val save = firestore.collection("marks").add(mark).await()
            Resource.Success(save.id)
        }catch(e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    suspend fun updateIcecreamMark(
        markid: String,
        mark: Int
    ): Resource<String>{
        return try{
            val doc = firestore.collection("marks").document(markid)
            doc.update("mark", mark).await()
            Resource.Success(markid)
        }catch(e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }




}