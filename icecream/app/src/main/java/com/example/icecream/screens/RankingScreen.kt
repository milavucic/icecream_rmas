package com.example.icecream.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.icecream.data.Icecream
import com.example.icecream.data.User
import com.example.icecream.navigation.Footer
import com.example.icecream.navigation.Screens
import com.example.icecream.viewmodels.AuthViewModel
import com.example.icecream.repositories.Resource
import com.example.icecream.viewmodels.IcecreamViewModel
import com.example.icecream.viewmodels.SharedViewModel
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun RankingScreen(
    viewModel: AuthViewModel,
    navController: NavController,
    icecreamViewModel: IcecreamViewModel,
    sharedViewModel: SharedViewModel
) {
    // Collect user data from the viewModel

    viewModel.getAllUsers()
    val userState = viewModel.allUsers.collectAsState()
    val userData = remember { mutableListOf<User?>(null) }
    userState.value.let {
        when(it){
            is Resource.Failure -> {}
            is Resource.Success -> {
                userData.clear()
                userData.addAll(it.result.sortedByDescending { x -> x.points })
            }
            Resource.Loading -> {}
            null -> {}
        }
    }
    val icecreamsState by icecreamViewModel.icecreams.collectAsState() // Collect state

    val icMarkers = remember {
        mutableStateListOf<Icecream>()
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
                    text = "Rangiranje korisnika",
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            when (val usersResource = userState.value) {
                is Resource.Success -> {
                    val users = usersResource.result
                    if (users.isEmpty()) {
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
                                Text(text = "Nema registrovanih korisnika")
                            }
                        }
                    } else {
                        Column {
                            // Header Row
                            RankingHeader()

                            // Content Rows
                            LazyColumn {
                                items(users.sortedByDescending { it.points }) { user ->
                                    RankingRow(user)
                                }
                            }
                        }
                    }
                }
                is Resource.Loading -> {
                    // Handle loading state if needed
                }
                is Resource.Failure -> {
                    Log.e("RankingScreen", "Error loading users: ${usersResource.exception}")
                }

                null -> {}
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Footer(
                addNewIcecream = {},
                active = 1,
                onHomeClick = {
                    navController.navigate(Screens.homeScreen)
                },
                onRankingClick = {
                    // Already on RankingScreen
                },
                onTableClick = {
                    when (icecreamsState) {
                        is Resource.Success -> {
                            val icecreamList = (icecreamsState as Resource.Success<List<Icecream>>).result
                            sharedViewModel.setIcecreams(icecreamList) // Use the correct method to set data
                            val icecreamsJson = Gson().toJson(icecreamList)
                            val encodedIcecreamsJson = URLEncoder.encode(icecreamsJson, StandardCharsets.UTF_8.toString())
                            Log.d("UserScreen", "encoded icecreamsJson: $encodedIcecreamsJson")
                            navController?.navigate("${Screens.tableScreen}/$encodedIcecreamsJson")
                        }
                        else -> {
                            Log.d("UserScreen", "Ice creams are not loaded or there is an error.")
                        }
                }
                               },
                onProfileClick = {
                    val userJson = Gson().toJson(userData)
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
fun RankingHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.Gray)
            .border(BorderStroke(1.dp, Color.Gray))
    ) {
        Text(
            text = "Ime",
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        )
        Text(
            text = "Poeni",
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        )
        Text(
            text = "Titula",
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        )
    }
}

@Composable
fun RankingRow(user: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White)
            .border(BorderStroke(1.dp, Color.Gray))
    ) {
        Text(
            text = user.fullName,
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = user.points.toString(),
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            style = TextStyle(fontSize = 14.sp)
        )
        Text(
            text = getRankingTitle(user.points),
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            style = TextStyle(fontSize = 14.sp)
        )
    }
}

// Determine ranking title based on points
fun getRankingTitle(points: Int): String {
    return when {
        points <= 20 -> "Sladobebac"
        points <= 80 -> "Sladokusac"
        else -> "Sladomaster"
    }
}
