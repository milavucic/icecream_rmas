package com.example.icecream.screens

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.rememberModalBottomSheetState // For material3
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import com.example.icecream.R
import com.example.icecream.data.Icecream
import com.example.icecream.data.User
import com.example.icecream.navigation.Screens
import com.example.icecream.repositories.Resource
import com.example.icecream.services.LocationService
import com.example.icecream.viewmodels.AuthViewModel
import com.example.icecream.viewmodels.IcecreamViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import com.google.gson.Gson
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.TextField
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.BitmapDescriptor
import com.example.icecream.navigation.Footer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

import android.Manifest

import android.content.pm.PackageManager

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.FilterAlt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: AuthViewModel?,
    navController: NavController?,
    icecreamViewModel: IcecreamViewModel?,

    isCameraSet: MutableState<Boolean> = remember { mutableStateOf(false) },
    cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(43.321445, 21.896104), 17f)
    },
    icecreamMarkers: MutableList<Icecream>,
    isFilteredParam: Boolean = false
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("filters", Context.MODE_PRIVATE)
    val options = sharedPreferences.getString("options", null)
    val range = sharedPreferences.getFloat("range", 1000f)

    // Initialize states
    val isFiltered = remember { mutableStateOf(false) }
    val isFilteredIndicator = remember { mutableStateOf(false) }

    // Initialize icecreamMarkersState to manage the marker state
    val icecreamMarkersState = remember { mutableStateListOf<Icecream>() }
    icecreamMarkersState.clear()
    icecreamMarkersState.addAll(icecreamMarkers)

    // Check filter parameters
    if (isFilteredParam && (options != null || range != 1000f)) {
        isFilteredIndicator.value = true
    }
    val scope = rememberCoroutineScope()

    // Collect ice cream data from the view model
    val icecreamResource = icecreamViewModel?.icecreams?.collectAsState()
    val allIc = remember { mutableStateListOf<Icecream>() }
    icecreamResource?.value.let {
        when (it) {
            is Resource.Success -> {
                allIc.clear()
                allIc.addAll(it.result)
            }
            is Resource.Failure -> {
                Log.e("Podaci", it.toString())
            }
            else -> {}
        }
    }

    // Retrieve user data
    viewModel?.getUser()
    val userDataResource = viewModel?.currentUserFlow?.collectAsState()

    // Define states for search, filtered data, and user information
    val searchValue = remember { mutableStateOf("") }
    val filteredIc = remember { mutableStateListOf<Icecream>() }
    val userData = remember { mutableStateOf<User?>(null) }
    val profileImage = remember { mutableStateOf("") }
    val myLocation = remember { mutableStateOf<LatLng?>(null) }
    val showFilterDialog = remember { mutableStateOf(false) }
    val isAddNewBottomSheet = remember { mutableStateOf(true) }
    val mapUiSettings = remember { mutableStateOf(MapUiSettings()) }
    val properties = remember { mutableStateOf(MapProperties(mapType = MapType.TERRAIN)) }
    val markers = remember { mutableStateListOf<LatLng>() }
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    // Update filtered results based on search value
    LaunchedEffect(myLocation.value, searchValue.value) {
        myLocation.value?.let {
            if (!isCameraSet.value) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 17f)
                isCameraSet.value = true
            }
            markers.clear()
            markers.add(it)
        }
        val searchTerm = searchValue.value.lowercase()
        filteredIc.clear()
        if (searchTerm.isNotBlank()) {
            filteredIc.addAll(allIc.filter { icecream ->
                icecream.name.lowercase().contains(searchTerm) ||
                        icecream.description.lowercase().contains(searchTerm)
            })
        }
        isFiltered.value = searchTerm.isNotBlank()
        Log.d("Search", "Filtered Ic Size: ${filteredIc.size}")
    }

    // Broadcast receiver for location updates
    val receiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationService.ACTION_LOCATION_UPDATE) {
                    val latitude = intent.getDoubleExtra(LocationService.EXTRA_LOCATION_LATITUDE, 0.0)
                    val longitude = intent.getDoubleExtra(LocationService.EXTRA_LOCATION_LONGITUDE, 0.0)
                    myLocation.value = LatLng(latitude, longitude)
                    Log.d("Nova lokacija", "Updated location: ${myLocation.value}")
                }
            }
        }
    }

    // Registering and unregistering the receiver
    DisposableEffect(context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(
            receiver,
            IntentFilter(LocationService.ACTION_LOCATION_UPDATE)
        )
        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
    }

    // Modal bottom sheet for adding new ice cream or applying filters
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            if (isAddNewBottomSheet.value)
                AddNewIcecream(icecreamViewModel!!, myLocation, sheetState)
            else
                Filters(icecreamViewModel!!, viewModel!!, allIc, sheetState, isFiltered, isFilteredIndicator, filteredIc, icecreamMarkersState, myLocation.value)
        },
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
            // Google Map with markers
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = properties.value,
                uiSettings = mapUiSettings.value
            ) {
                markers.forEach { marker ->
                    val icon = bitmapDescriptorFromVector(context, R.drawable.location)
                    Marker(
                        state = rememberMarkerState(position = marker),
                        title = "Moja Lokacija",
                        icon = icon,
                        snippet = "",
                    )
                }
                // Decide which markers to show based on filtering
                val markersToShow = if (isFiltered.value) filteredIc else icecreamMarkersState
                markersToShow.forEach { marker ->
                    val icon = bitmapDescriptorFromUrlWithRoundedCorners(context, 10f)
                    Marker(
                        state = rememberMarkerState(
                            position = LatLng(marker.location.latitude, marker.location.longitude)
                        ),
                        title = "Ice Cream Location",
                        icon = BitmapDescriptorFactory.defaultMarker(),
                        snippet = marker.description,
                        onClick = {
                            val icJson = Gson().toJson(marker)
                            val encodedIcJson = URLEncoder.encode(icJson, StandardCharsets.UTF_8.toString())

                            val icsJson = Gson().toJson(markersToShow)
                            val encodedIcsJson = URLEncoder.encode(icsJson, StandardCharsets.UTF_8.toString())

                            navController?.navigate("${Screens.aboutIcecreamScreen}/$encodedIcJson/$encodedIcsJson")
                            true
                        }
                    )
                }
            }

            // Top Bar Layout with Search Box and Filter Icon
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    val searchJob = remember { mutableStateOf<Job?>(null) }
                    val searchDelay = 300L

                    // Search Box
                    TextField(
                        value = searchValue.value,
                        onValueChange = { newValue ->
                            searchValue.value = newValue
                            searchJob.value?.cancel()
                            searchJob.value = CoroutineScope(Dispatchers.Main).launch {
                                delay(searchDelay)
                                val searchTerm = newValue.lowercase()
                                Log.d("Search", "Search Value Changed: $searchTerm")

                                filteredIc.clear()
                                if (searchTerm.isNotBlank()) {
                                    filteredIc.addAll(allIc.filter { icecream ->
                                        icecream.name.lowercase().contains(searchTerm) ||
                                                icecream.description.lowercase().contains(searchTerm)
                                    })
                                }
                                Log.d("Search", "Filtered Ic Size: ${filteredIc.size}")
                            }
                        },
                        placeholder = { Text("Pretraži po nazivu ili opisu") },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White, RoundedCornerShape(10.dp)),
                        singleLine = true,
                        textStyle = TextStyle(color = Color.Black)
                    )

                    // Filter Icon
                    IconButton(
                        onClick = {
                            isAddNewBottomSheet.value = false
                            scope.launch {
                                sheetState.show()
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FilterAlt,
                            contentDescription = "Filter",
                            tint = if (isFiltered.value || isFilteredIndicator.value) Color.Blue else Color.Gray
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    IconButton(
                        onClick = {
                            properties.value = MapProperties(mapType = MapType.TERRAIN)
                        },
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terrain,
                            contentDescription = "Terrain",
                            tint = Color.White,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(
                        onClick = {
                            properties.value = MapProperties(mapType = MapType.SATELLITE)
                        },
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Satellite,
                            contentDescription = "Satellite",
                            tint = Color.White,
                        )
                    }
                }
                Footer(
                    addNewIcecream = {
                        isAddNewBottomSheet.value = true
                        scope.launch {
                            sheetState.show()
                        }
                    },
                    active = 0,
                    onHomeClick = {},
                    onRankingClick = {
                        navController?.navigate(Screens.rankingScreen)
                    },
                    onTableClick = {

                        val icsJson = Gson().toJson(
                            if (!isFiltered.value)
                                icecreamMarkers
                            else
                                filteredIc
                        )
                        Log.d("TableScreen", "first icsJson: $icsJson")
                        val encodedicsJson = URLEncoder.encode(icsJson, StandardCharsets.UTF_8.toString())
                        Log.d("TableScreen", "enc icsJson: $encodedicsJson")
                        navController?.navigate("${Screens.tableScreen}/$encodedicsJson")
                    },
                    onProfileClick = {
                        val userJson = Gson().toJson(userData.value)
                        Log.d("UserScreen", "first userJson: $userJson")
                        val encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString())
                        Log.d("UserScreen", "enc userJson: $encodedUserJson")
                        navController?.navigate("${Screens.userScreen}/$encodedUserJson")
                    }
                )
            }
        }
        if (showFilterDialog.value) {
            FilterDialog(
                onApply = {
                    showFilterDialog.value = false
                },
                onDismiss = {
                    showFilterDialog.value = false
                }
            )
        }


        userDataResource?.value.let {
            when (it) {
                is Resource.Success -> {
                    userData.value = it.result
                    profileImage.value = it.result.image
                }
                null -> {
                    userData.value = null
                    profileImage.value = ""
                }

                is Resource.Failure -> {}
                Resource.Loading -> {}
                else -> {}
            }
        }
    }
}






@Composable
fun bitmapDescriptorFromUrlWithRoundedCorners(
    context: Context,
   // imageUrl: String,
    cornerRadius: Float
): State<BitmapDescriptor?> {
    val bitmapDescriptorState = remember { mutableStateOf<BitmapDescriptor?>(null) }



    return rememberUpdatedState(bitmapDescriptorState.value)
}


fun bitmapDescriptorFromVector(
    context: Context,
    vectorId: Int
): BitmapDescriptor? {

    // retrieve the actual drawable
    val drawable = ContextCompat.getDrawable(context, vectorId) ?: return null
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )


    val canvas = android.graphics.Canvas(bitmap)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}



@Composable
fun mapNavigationBar(
    searchValue: MutableState<String>,
    profileImage: String,
    onImageClick: () -> Unit,
    icecreams: MutableList<Icecream>,
    navController: NavController?,
    cameraPositionState: CameraPositionState
){
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchList = remember{
        mutableListOf<Icecream>()
    }

    searchList.clear()
    searchList.addAll(searchByDescription(icecreams, searchValue.value).toMutableList())

    val focusRequester = remember{
        FocusRequester()
    }

    val isFocused = remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
    ){
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(
                    6.dp,
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    1.dp,
                    Color.Transparent,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
        ){
            OutlinedTextField(
                modifier = Modifier
                    .height(50.dp)
                    .focusRequester(focusRequester = focusRequester)
                    .onFocusChanged { focusState ->
                        isFocused.value = focusState.isFocused
                    },
                value = searchValue.value,
                onValueChange = { newValue ->
                    searchValue.value = newValue
                    isFocused.value = true
                },
                singleLine = true,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_text),
                        style = TextStyle(
                            color = Color.LightGray
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "",
                        tint = Color.LightGray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                ),
                visualTransformation = VisualTransformation.None,
                keyboardOptions = KeyboardOptions.Default
            )
            if(isFocused.value) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 60.dp)
                        .background(Color.White),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        for (icecream in searchList) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable {
                                            val icJson = Gson().toJson(icecream)
                                            val encodedicJson = URLEncoder.encode(icJson, StandardCharsets.UTF_8.toString())
                                            navController?.navigate(Screens.aboutIcecreamScreen+ "/$encodedicJson")
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(vertical = 8.dp)
                                            .weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model =R.drawable.pic ,
                                            contentDescription = "",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .width(40.dp)
                                                .height(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = if (icecream.description.length > 20) {
                                                icecream.description.substring(0, 20) + "..."
                                            } else {
                                                icecream.description
                                            }
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            isFocused.value = false
                                            keyboardController?.hide()
                                            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(icecream.location.latitude, icecream.location.longitude), 17f)
                                        },
                                        modifier = Modifier
                                            .wrapContentWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.MyLocation,
                                            contentDescription = "",
                                            tint = Color.LightGray
                                        )
                                    }
                                }
                            }
                        }
                    }

                }
            }

        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(50.dp)
                .shadow(
                    6.dp,
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    1.dp,
                    Color.Transparent,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    Color.White,
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center

        ){
            AsyncImage(
                model = profileImage,
                contentDescription = null,
                modifier = Modifier
                    .padding(2.dp)
                    .size(50.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        onImageClick()
                    },
                contentScale = ContentScale.Crop
            )
        }
    }
}

fun searchByDescription(
    icecream: MutableList<Icecream>,
    query: String
):List<Icecream>{
    val regex = query.split(" ").joinToString(".*"){
        Regex.escape(it)
    }.toRegex(RegexOption.IGNORE_CASE)
    return icecream.filter { ic ->
        regex.containsMatchIn(ic.description)
    }
}


@Composable
fun FilterDialog(
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    // Ovdje definirajte izgled dijaloga s filterima
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Filteri") },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = onApply,
            ) {
                Text(text = "Primijeni")
            }
        },
        dismissButton = {
            androidx.compose.material3.Button(
                onClick = onDismiss,
            ) {
                Text(text = "Odustani")
            }
        },
        // Dodajte elemente za filtriranje ovdje
    )
}








