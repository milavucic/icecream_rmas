package com.example.icecream.repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.TopAppBarState
import com.example.icecream.data.User
import com.example.icecream.services.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await



class AuthRepositoryImpl : AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val storageInstance = FirebaseStorage.getInstance()
    private val databaseService = FirebaseService(storageInstance,firestoreInstance)

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun login(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result.user!!)
        }catch(e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun register(
        email: String,
        fullName: String,
        phoneNumber: String,
        profileImage: Uri,
        password: String
    ): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

            if (result.user != null) {
                // Upload the profile picture
                val temporaryImageUrl = databaseService.uploadPicture(result.user!!.uid, profileImage)

                // Define the final image path
                val finalImagePath = "profile_picture/${result.user!!.uid}.jpg"

                // Move the picture to the final location
                databaseService.movePicture("profile_picture/${result.user!!.uid}_temp.jpg", finalImagePath)

                // Create user data with the final image URL
                val user = User(
                    email = email,
                    fullName = fullName,
                    phone = phoneNumber,
                    image = finalImagePath  // Save the final image path
                )

                // Save user data to Firestore
                databaseService.saveUserData(result.user!!.uid, user)
            } else {
                Log.e("UploadError", "Failed to upload image")
            }
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }






    override fun logout() {
        firebaseAuth.signOut()
    }


    override suspend fun getUser(): Resource<User> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val uid = currentUser.uid
                val firestore = FirebaseFirestore.getInstance()

                val userDoc = firestore.collection("users").document(uid)
                val userSnapshot = userDoc.get().await()

                if (userSnapshot.exists()) {
                    val user = userSnapshot.toObject(User::class.java)
                    if (user != null) {
                        Log.d("getUser", "User successfully retrieved: $user")
                        Resource.Success(user)
                    } else {
                        Log.e("getUser", "User mapping failed")
                        Resource.Failure(Exception("Mapping failed"))
                    }
                } else {
                    Log.e("getUser", "Document does not exist")
                    Resource.Failure(Exception("Document does not exist"))
                }
            } else {
                Log.e("getUser", "No authenticated user")
                Resource.Failure(Exception("No authenticated user"))
            }
        } catch (e: Exception) {
            Log.e("getUser", "Exception occurred: ${e.message}")
            e.printStackTrace()
            Resource.Failure(e)
        }
    }


    override suspend fun getAllUsers(): Resource<List<User>> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val usersDoc = firestore.collection("users")
            val usersSnapshot = usersDoc.get().await()

            if (!usersSnapshot.isEmpty) {
                val usersList = usersSnapshot.documents.mapNotNull { document ->
                    document.toObject(User::class.java)
                }
                Resource.Success(usersList)
            } else {
                Resource.Failure(Exception("Ne postoje korisnici u bazi"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

}