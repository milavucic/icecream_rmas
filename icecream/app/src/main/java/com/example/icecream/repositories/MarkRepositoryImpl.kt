package com.example.icecream.repositories

import com.example.icecream.data.Icecream
import com.example.icecream.data.Mark
import com.example.icecream.services.FirebaseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class MarkRepositoryImpl:MarkRepository {

    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val storageInstance = FirebaseStorage.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val firestoreService = FirebaseService(storageInstance, firestoreInstance)
    override suspend fun addIcecreamMark(
        icecreamid: String,
        icecream: Icecream,
        mark: Int
    ): Resource<String> {
        return try{
            val currentUserId = firebaseAuth.currentUser?.uid ?: return Resource.Failure(Exception("User not authenticated"))
            val marknew = Mark(
                icecreamId=icecreamid,
                userId = firebaseAuth.currentUser!!.uid,
                mark = mark
            )
            firestoreService.addUserPoints(icecream.userId, mark*2)
            firestoreService.addUserPoints(currentUserId, mark)
            val save = firestoreService.saveIcecreamMark(marknew)
            save
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun updateIcecreamMark(markid: String, mark: Int):
            Resource<String> {
        return try{
            val update= firestoreService.updateIcecreamMark(markid, mark)
            update
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }

    override suspend fun getIcecreamMarks(
        icecreamid: String
    ): Resource<List<Mark>> {
        return try {
            val doc = firestoreInstance.collection("marks")
            val querySnapshot = doc.get().await()
            val marks = mutableListOf<Mark>()
            for (document in querySnapshot.documents) {
                val icId = document.getString("icecreamId") ?: ""
                if (icId == icecreamid) {
                    marks.add(Mark(
                            id = document.id,
                            icecreamId = icecreamid,
                            userId = document.getString("userId") ?: "",
                            mark = document.getLong("mark")?.toInt() ?: 0
                        )
                    )
                }
            }
            Resource.Success(marks)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e)
        }
    }


    override suspend fun getUserMarks(): Resource<List<Mark>> {
        return try{
            val doc = firestoreInstance.collection("marks")
            val querySnapshot = doc.get().await()
            val marks = mutableListOf<Mark>()
            for(document in querySnapshot.documents){
                val userId = document.getString("userId") ?: ""
                if(userId == firebaseAuth.currentUser?.uid){
                    marks.add(Mark(
                        id = document.id,
                        userId = userId,
                        icecreamId = document.getString("icecreamId") ?: "",
                        mark = document.getLong("mark")?.toInt() ?: 0
                    ))
                }
            }
            Resource.Success(marks)
        }catch (e: Exception){
            e.printStackTrace()
            Resource.Failure(e)
        }
    }


}