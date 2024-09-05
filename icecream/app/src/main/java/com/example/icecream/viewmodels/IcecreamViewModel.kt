package com.example.icecream.viewmodels

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.icecream.data.Icecream
import com.example.icecream.data.Mark
import com.example.icecream.repositories.IcecreamRepositoryImpl
import com.example.icecream.repositories.MarkRepositoryImpl
import com.example.icecream.repositories.Resource
import com.example.icecream.services.FirebaseService
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IcecreamViewModel :ViewModel(){

    val icecreamRepository = IcecreamRepositoryImpl()
    val markRepository = MarkRepositoryImpl()

    private val _icecreamFlow = MutableStateFlow<Resource<String>?>(null)
    val icecreamFlow: StateFlow<Resource<String>?> = _icecreamFlow

    private val _markFlow = MutableStateFlow<Resource<String>?>(null)
    val markFlow: StateFlow<Resource<String>?> = _markFlow

    private val _icecreams = MutableStateFlow<Resource<List<Icecream>>>(Resource.Success(emptyList()))
    val icecreams: StateFlow<Resource<List<Icecream>>> get() = _icecreams

    private val _marks = MutableStateFlow<Resource<List<Mark>>>(Resource.Success(emptyList()))
    val marks: StateFlow<Resource<List<Mark>>> get() = _marks


    private val _userIcecreams = MutableStateFlow<Resource<List<Icecream>>>(Resource.Success(emptyList()))
    val userIcecreams: StateFlow<Resource<List<Icecream>>> get() = _userIcecreams

    init {
        getAllIcecreams()

    }

    val firebaseService = FirebaseService(
        FirebaseStorage.getInstance(),
        FirebaseFirestore.getInstance()
    )

    private val _galleryImageUrls = MutableLiveData<List<String>>()
    val galleryImageUrls: LiveData<List<String>> = _galleryImageUrls

    suspend fun uploadImages(images: List<Uri>) :List<String>{
        //return firebaseService.uploadIcecreamGalleryImages(images)
        //CoroutineScope(Dispatchers.IO).launch {
            val urls = firebaseService.uploadIcecreamGalleryImages(images)
            _galleryImageUrls.postValue(urls)
           // withContext(Dispatchers.Main) {
               // onComplete(urls)
            //}

        //}
        return urls
    }


    fun getAllIcecreams() = viewModelScope.launch {
        _icecreams.value = icecreamRepository.getAllIcecreams()
    }

    /*fun saveIcecream(
        name: String,
        description: String,
        galleryImages: List<Uri>,
        location: MutableState<LatLng?>
    ) = viewModelScope.launch{
        _icecreamFlow.value = Resource.Loading
        icecreamRepository.saveIcecream(
            name=name,
            description = description,
            galleryImages = galleryImages,
            location = location.value!!
        )
        _icecreamFlow.value = Resource.Success("Uspešno dodato")
    }*/

    fun List<String>.toUriList(): List<Uri> {
        return this.map { Uri.parse(it) }
    }


    fun saveIcecream(
        name: String,
        description: String,
        galleryImages: List<Uri>,
        location: MutableState<LatLng?>
    ) = viewModelScope.launch {
        _icecreamFlow.value = Resource.Loading

        // Upload images and get their URLs
        val imageUrls = uploadImages(galleryImages) // Returns List<String>

        // Convert URLs back to URIs if needed by repository
        val imageUris = imageUrls.toUriList()

        // Save the ice cream with the image URLs
        icecreamRepository.saveIcecream(
            name = name,
            description = description,
            galleryImages = imageUris, // Pass the List<Uri> if repository needs it
            location = location.value!!
        )
        _icecreamFlow.value = Resource.Success("Uspešno dodato")
    }

    fun getUserIcecreams(
        uid: String
    ) = viewModelScope.launch {
        _userIcecreams.value = icecreamRepository.getUserIcecreams(uid)
    }

    fun getIcecreamMarks(
        icid: String
    ) = viewModelScope.launch {
        _marks.value = Resource.Loading
        val result = markRepository.getIcecreamMarks(icid)
        _marks.value = result
    }

    fun addIcecreamMark(
        icid: String,
        icecream: Icecream,
        mark: Int
    ) = viewModelScope.launch {
        _markFlow.value = markRepository.addIcecreamMark(icid, icecream, mark)
    }

    fun updateIcecreamMark(
        icid: String,
        mark: Int
    ) = viewModelScope.launch{
        _markFlow.value = markRepository.updateIcecreamMark(icid, mark)
    }

}



class IcecreamViewModelFactory: ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(IcecreamViewModel::class.java)){
            return IcecreamViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}