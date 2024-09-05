package com.example.icecream.viewmodels

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.icecream.data.Icecream
import com.example.icecream.repositories.IcecreamRepositoryImpl
import com.example.icecream.repositories.Resource
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class MapMarker(
    val position: LatLng,
    val title: String,
    val snippet: String
)

class MapViewModel : ViewModel() {

    // LiveData for managing map markers
    private val _markers = MutableLiveData<List<Icecream>>()
    val markers: LiveData<List<Icecream>> = _markers

    // LiveData for managing search results
    private val _searchResults = MutableLiveData<List<Icecream>>()
    val searchResults: LiveData<List<Icecream>> = _searchResults

    // LiveData for managing filtered ice cream items
    private val _filteredMarkers = MutableLiveData<List<Icecream>>()
    val filteredMarkers: LiveData<List<Icecream>> = _filteredMarkers

    // State for managing search and filter options
    private var currentSearchQuery: String = ""
    private var currentFilterOptions: FilterOptions? = null

    init {
        // Initialize with all markers or data from the repository
        _markers.value = loadAllIcecreamMarkers()
    }

    // Method to handle searching
    fun searchIcecreams(query: String) {
        currentSearchQuery = query
        updateMarkers()
    }

    // Method to handle filtering
    fun filterIcecreams(filterOptions: FilterOptions) {
        currentFilterOptions = filterOptions
        updateMarkers()
    }

    // Private method to update markers based on search and filter options
    private fun updateMarkers() {
        val allMarkers = loadAllIcecreamMarkers()
        val searchResults = if (currentSearchQuery.isNotBlank()) {
            allMarkers.filter { it.name.contains(currentSearchQuery, ignoreCase = true) }
        } else {
            allMarkers
        }

        val filteredResults = currentFilterOptions?.let { options ->
            searchResults.filter { icecream ->
                // Filtering logic based on distance, userId, etc.
                (options.userId == null || icecream.userId == options.userId) &&
                        (options.distanceRange == null || calculateDistance(icecream.location.toLatLng() , options.distanceRange.toFloat()))
            }
        } ?: searchResults

        _searchResults.value = searchResults
        _filteredMarkers.value = filteredResults
        _markers.value = filteredResults
    }

    private fun GeoPoint.toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }


    // Method to load all markers (ice cream data) from the repository
    private fun loadAllIcecreamMarkers(): List<Icecream> {
        // Retrieve data from repository
        return listOf() // Replace with actual data retrieval logic
    }

    private fun calculateDistance(location: LatLng, maxDistance: Float): Boolean {
        val earthRadius = 6371 // Radius of the Earth in kilometers
        val lat1 = Math.toRadians(location.latitude)
        val lon1 = Math.toRadians(location.longitude)

        // Replace these with the actual user location coordinates
        val lat2 = Math.toRadians(0.0) // Example coordinates
        val lon2 = Math.toRadians(0.0)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) +
                cos(lat1) * cos(lat2) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val distance = earthRadius * c // Distance in kilometers

        return distance <= maxDistance
    }

}

// Factory class for MapViewModel
class MapViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


data class FilterOptions(
    val userId: String? = null,
    val distanceRange: Double? = null
)

@Composable
fun IcecreamItem(icecream: Icecream) {
    Text(text = icecream.name)
}

@Composable
fun MapViewComposable(markers: List<Icecream>) {
    // Implement your map view logic here
}
