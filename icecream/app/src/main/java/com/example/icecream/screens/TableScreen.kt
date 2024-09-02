package com.example.icecream.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.icecream.R
import com.example.icecream.data.Icecream
import com.example.icecream.repositories.Resource
import com.example.icecream.viewmodels.IcecreamViewModel



import android.graphics.fonts.FontStyle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPasteOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember


import androidx.compose.ui.res.painterResource

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.icecream.data.User

import com.example.icecream.navigation.Screens

import com.example.icecream.navigation.Footer
import com.example.icecream.viewmodels.AuthViewModel
import com.google.firebase.firestore.GeoPoint

import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun TableScreen(
    icecreams: List<Icecream>?,
    navController: NavController,
    icecreamViewModel: IcecreamViewModel,
    authViewModel: AuthViewModel
) {
    val userData = remember { mutableStateOf<User?>(null) }
    val profileImage = remember { mutableStateOf("") }


    //viewModel?.getUser()
    val userDataResource = authViewModel?.currentUserFlow?.collectAsState()
    val new = remember { mutableListOf<Icecream>() }

    if (icecreams.isNullOrEmpty()) {
        val icResource = icecreamViewModel.icecreams.collectAsState()
        icResource.value.let {
            when (it) {
                is Resource.Success -> {
                    Log.d("Podaci", it.toString())
                    new.clear()
                    new.addAll(it.result)
                }
                is Resource.Loading -> {
                    // Handle loading state if needed
                }
                is Resource.Failure -> {
                    Log.e("Podaci", it.toString())
                }
                null -> {}
            }
        }
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Označeni štandovi slodoleda",
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            if (icecreams.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentPasteOff, // Use the error icon from Material Icons
                            contentDescription = "No Data Found",
                            tint = Color.Gray, // Change the color if needed
                            modifier = Modifier.size(150.dp) // Adjust size as needed
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(text = "Nema štandova")
                    }
                }
            } else {
                Column {
                    // Header Row
                    TableHeader()

                    // Content Rows
                    LazyColumn {
                        items(icecreams) { ic ->
                            IcecreamRow(icecream = ic)
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Footer(
                addNewIcecream = {},
                active = 2,
                onHomeClick = {
                    val icsJson = Gson().toJson(icecreams)
                    val encodedJson = URLEncoder.encode(icsJson, StandardCharsets.UTF_8.toString())
                    navController?.navigate("${Screens.homeScreenParam}/$encodedJson")
                },
                onRankingClick = {
                    navController.navigate(Screens.rankingScreen)
                },
                onTableClick = {},

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
}

@Composable
fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.Gray)
            .border(BorderStroke(1.dp, Color.Gray))
    ) {
        Text(
            text = "Naziv",
            modifier = Modifier.weight(1f).padding(4.dp),
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        )
        Text(
            text = "Opis",
            modifier = Modifier.weight(2f).padding(4.dp),
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        )
        Text(
            text = "Lokacija",
            modifier = Modifier.weight(1f).padding(4.dp),
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        )
    }
}

@Composable
fun IcecreamRow(icecream: Icecream) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White)
            .border(BorderStroke(1.dp, Color.Gray))
    ) {
        Text(
            text = icecream.name,
            modifier = Modifier.weight(1f).padding(4.dp),
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = icecream.description,
            modifier = Modifier.weight(2f).padding(4.dp),
            style = TextStyle(fontSize = 14.sp)
        )
        Text(
            text = geoPointToString(icecream.location),
            modifier = Modifier.weight(1f).padding(4.dp),
            style = TextStyle(fontSize = 14.sp)
        )
    }
}

@Composable
fun geoPointToString(geoPoint: GeoPoint): String {
    return "Lat: ${geoPoint.latitude}, Long: ${geoPoint.longitude}"
}
