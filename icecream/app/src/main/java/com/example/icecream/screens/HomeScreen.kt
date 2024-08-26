package com.example.icecream.screens

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.RadioButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TableRows
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.google.android.gms.maps.model.BitmapDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import com.example.icecream.navigation.Footer
import com.example.icecream.services.CameraService
import com.google.firebase.BuildConfig
import java.util.jar.Manifest

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: AuthViewModel?,
    navController: NavController?,
    icecreamViewModel: IcecreamViewModel?,

    isCameraSet: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
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

    val isFiltered = remember {
        mutableStateOf(false)
    }
    val isFilteredIndicator = remember {
        mutableStateOf(false)
    }

    if (isFilteredParam && (options != null ||  range != 1000f)) {
        isFilteredIndicator.value = true
    }

    val icecreamResource = icecreamViewModel?.icecreams?.collectAsState()
    val allIc = remember {
        mutableListOf<Icecream>()
    }
    icecreamResource?.value.let {
        when (it) {
            is Resource.Success -> {
                allIc.clear()
                allIc.addAll(it.result)
            }
            is Resource.Loading -> {

            }
            is Resource.Failure -> {
                Log.e("Podaci", it.toString())
            }
            null -> {}
        }
    }

    viewModel?.getUser()

    val userDataResource = viewModel?.currentUserFlow?.collectAsState()

    val filteredIc = remember {
        mutableListOf<Icecream>()
    }

    val searchValue = remember {
        mutableStateOf("")
    }
    val userData = remember {
        mutableStateOf<User?>(null)
    }
    val profileImage = remember {
        mutableStateOf("")
    }

    val myLocation = remember {
        mutableStateOf<LatLng?>(null)
    }

    val IcMarkerCopy = icecreamMarkers

    val showFilterDialog = remember {
        mutableStateOf(false)
    }

    val isAddNewBottomSheet = remember {
        mutableStateOf(true)
    }

    val receiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationService.ACTION_LOCATION_UPDATE) {
                    val latitude =
                        intent.getDoubleExtra(LocationService.EXTRA_LOCATION_LATITUDE, 0.0)
                    val longitude =
                        intent.getDoubleExtra(LocationService.EXTRA_LOCATION_LONGITUDE, 0.0)
                    // Update the camera position
                    myLocation.value = LatLng(latitude, longitude)
                    Log.d("Nova lokacija", myLocation.toString())
                }
            }
        }
    }

    DisposableEffect(context) {
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(receiver, IntentFilter(LocationService.ACTION_LOCATION_UPDATE))
        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
    }

    val mapUiSettings = remember { mutableStateOf(MapUiSettings()) }

    val properties = remember {
        mutableStateOf(MapProperties(mapType = MapType.TERRAIN))
    }

    val markers = remember { mutableStateListOf<LatLng>() }
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)




    LaunchedEffect(myLocation.value) {
        myLocation.value?.let {
            Log.d("Nova lokacija gore", myLocation.toString())
            if (!isCameraSet.value) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 17f)
                isCameraSet.value = true
            }
            markers.clear()
            markers.add(it)
        }
    }

    val scope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            if (isAddNewBottomSheet.value)
                AddNewIcecreamBottomSheet(icecreamViewModel!!, myLocation, sheetState)
            else
                FiltersBottomSheet(icecreamViewModel!!, viewModel!!, allIc, sheetState, isFiltered, isFilteredIndicator, filteredIc, icecreamMarkers, myLocation.value)
        },
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = properties.value,
                uiSettings = mapUiSettings.value
            ) {
                markers.forEach { marker ->
                    val icon = bitmapDescriptorFromVector(
                        context, R.drawable.location
                    )
                    Marker(
                        state = rememberMarkerState(position = marker),
                        title = "Moja Lokacija",
                        icon = icon,
                        snippet = "",
                    )
                }
                Log.d("Is Filtered", isFiltered.value.toString())
                if (!isFiltered.value) {
                    icecreamMarkers.forEach { marker ->
                        val icon = bitmapDescriptorFromUrlWithRoundedCorners(
                            context,
                            //marker.mainImage,
                            10f
                        )
                        Marker(
                            state = rememberMarkerState(
                                position = LatLng(
                                    marker.location.latitude,
                                    marker.location.longitude
                                )
                            ),
                            title = "Moja Lokacija",
                            icon = //icon.value ?:
                            BitmapDescriptorFactory.defaultMarker(),
                            snippet = marker.description,
                            onClick = {
                                val icJson = Gson().toJson(marker)
                                val encodedIcJson =
                                    URLEncoder.encode(icJson, StandardCharsets.UTF_8.toString())

                                val icsJson = Gson().toJson(icecreamMarkers)
                                val encodedicsJson = URLEncoder.encode(icsJson, StandardCharsets.UTF_8.toString())

                                navController?.navigate(Screens.aboutIcecreamScreen + "/$encodedIcJson/$encodedicsJson")
                                true
                            }
                        )
                    }
                } else {
                    Log.d("Filtered", filteredIc.count().toString())
                    filteredIc.forEach { marker ->
                        val icon = bitmapDescriptorFromUrlWithRoundedCorners(
                            context,
                            //marker.mainImage,
                            10f
                        )
                        Marker(
                            state = rememberMarkerState(
                                position = LatLng(
                                    marker.location.latitude,
                                    marker.location.longitude
                                )
                            ),
                            title = "Moja Lokacija",
                            icon = //icon.value ?:
                            BitmapDescriptorFactory.defaultMarker(),
                            snippet = marker.description,
                            onClick = {
                                val icJson = Gson().toJson(marker)
                                val encodedicJson =
                                    URLEncoder.encode(icJson, StandardCharsets.UTF_8.toString())

                                val icsJson = Gson().toJson(filteredIc)
                                val encodedicsJson = URLEncoder.encode(icsJson, StandardCharsets.UTF_8.toString())

                                navController?.navigate(Screens.aboutIcecreamScreen + "/$encodedicJson/$encodedicsJson")
                                true
                            }
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(Color.LightGray)
            ) {
                mapNavigationBar(
                    searchValue = searchValue,
                    profileImage = profileImage.value.ifEmpty { "" },
                    onImageClick = {

                        //val userJson = Gson().toJson(userData.value)
                        //val encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString())
                       // navController?.navigate(Screens.userScreen + "/$encodedUserJson")

                    },
                    icecreams = IcMarkerCopy,
                    navController = navController,
                    cameraPositionState = cameraPositionState
                )
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier
                        .clickable {
                            isAddNewBottomSheet.value = false
                            scope.launch {
                                sheetState.show()
                            }
                        }
                        .background(
                            if (isFiltered.value || isFilteredIndicator.value)
                                Color.LightGray
                            else
                                Color.White, RoundedCornerShape(30.dp)
                        )
                        .padding(horizontal = 15.dp, vertical = 7.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FilterAlt,
                            contentDescription = "",
                            tint =
                            if (isFiltered.value || isFilteredIndicator.value)
                                Color.White
                            else
                                Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Filteri",
                            style = TextStyle(
                                color = if (isFiltered.value || isFilteredIndicator.value)
                                    Color.White
                                else
                                    Color.LightGray
                            )
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
//
                    },
                    onTableClick = {
                        navController?.navigate(Screens.tableScreen)
                        val icsJson = Gson().toJson(
                            if(!isFiltered.value)
                                icecreamMarkers
                            else
                                filteredIc
                        )
                        val encodedicsJson = URLEncoder.encode(icsJson, StandardCharsets.UTF_8.toString())
                        navController?.navigate("tableScreen/$encodedicsJson")

                    },
                    onProfileClick = {

                        //navController?.navigate(Screens.userScreen)
                        val userJson = Gson().toJson(userData.value)
                        Log.d("UserScreen", "first userJson: $userJson")
                        val encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8.toString())
                        Log.d("UserScreen", "enc userJson: $encodedUserJson")
                        //navController?.navigate(Screens.userScreen + "/$encodedUserJson")
                        //navController?.navigate(Screens.userScreen.replace("{userData}", encodedUserJson))
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
            when(it){
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
            }
        }
    }
}




@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun FiltersBottomSheet(
    icecreamViewModel: IcecreamViewModel,
    viewModel: AuthViewModel,
    ic: MutableList<Icecream>,
    sheetState: ModalBottomSheetState,
    isFiltered: MutableState<Boolean>,
    isFilteredIndicator: MutableState<Boolean>,
    filteredic: MutableList<Icecream>,
    markers: MutableList<Icecream>,
    userLocation: LatLng?
){}


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



