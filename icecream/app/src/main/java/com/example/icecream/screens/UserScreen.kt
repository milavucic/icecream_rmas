
package com.example.icecream.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TableRows
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.icecream.R
import com.example.icecream.data.Icecream
import com.example.icecream.data.User
import com.example.icecream.navigation.Screens
import com.example.icecream.navigation.Footer
import com.example.icecream.repositories.Resource
import com.example.icecream.services.LocationService
import com.example.icecream.viewmodels.AuthViewModel
import com.example.icecream.viewmodels.IcecreamViewModel
import com.google.gson.Gson
import androidx.compose.ui.Modifier
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.draw.clip

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun UserScreen(
    navController: NavController?,
    viewModel: AuthViewModel?,
    user: User?,
    myProfile: Boolean
) {
    Log.d("UserScreen", "User: $user")

    // State management for settings and user ice creams
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val trackingEnabled = sharedPreferences.getBoolean("tracking_location", true)

    val checked = remember { mutableStateOf(trackingEnabled) }
    val icecreams = remember { mutableStateListOf<Icecream>() }

    val storageBaseUrl = "https://firebasestorage.googleapis.com/v0/b/icecream.appspot.com/o/"
    //val imagePath = user?.image?.replace("/", "%2F") // Replace slashes with encoded versions
    //val encodedImagePath = URLEncoder.encode(user?.image, "UTF-8")
    val imagePath = "registration_uploads/${user?.id}.jpg".replace("/", "%2F")
    val fullImageUrl = "$storageBaseUrl$imagePath?alt=media"

    Log.d("GeneratedImageUrl", "URL: $fullImageUrl")
    /*icResource?.value.let {
        when(it){
            is Resource.Success -> {
                Log.d("Podaci", it.toString())
                icecreams.clear()
                icecreams.addAll(it.result)
            }
            is Resource.Loading -> {}
            is Resource.Failure -> {
                Log.e("Podaci", it.toString())
            }
            null -> {}
        }
    }*/

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(bottom = 80.dp) // Padding to avoid overlap with Footer
        ) {
            item {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 140.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = fullImageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .border(5.dp, Color.White, CircleShape)
                                .background(Color.White, RoundedCornerShape(70.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = user?.fullName!!.replace('+', ' '),
                            style = MaterialTheme.typography.displaySmall,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = when {
                                user.points <= 20 -> "Sladobebac"
                                user.points <= 80 -> "Sladokusac"
                                else -> "Sladomaster"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // User Statistics Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = "Broj bodova: ${user?.points ?: 0}", style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Basic Information Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ){
                    Text(text = "Informacije", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(13.dp))
                    if (myProfile) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Filled.Email, contentDescription = "")
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(text = viewModel?.currentUser?.email ?: "Nema email-a")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.Phone, contentDescription = "")
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = user?.phone ?: "Nema broja telefona")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Location Tracking Switch
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 20.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Lokacija",
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(5.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Štand u blizini",
                                style = TextStyle(
                                    fontSize = 16.sp
                                )
                            )
                            Switch(
                                checked = trackingEnabled,
                                onCheckedChange = {
                                    checked.value = it
                                    if (it){
                                        Intent(context, LocationService::class.java).apply {
                                            action = LocationService.ACTION_FIND_NEARBY
                                            context.startForegroundService(this)
                                        }
                                        with(sharedPreferences.edit()) {
                                            putBoolean("tracking_location", true)
                                            apply()
                                        }
                                    } else {
                                        Intent(context, LocationService::class.java).apply {
                                            action = LocationService.ACTION_STOP
                                            context.stopService(this)
                                        }
                                        Intent(context, LocationService::class.java).apply {
                                            action = LocationService.ACTION_START
                                            context.startForegroundService(this)
                                        }
                                        with(sharedPreferences.edit()) {
                                            putBoolean("tracking_location", false)
                                            apply()
                                        }
                                    }
                                },
                                thumbContent = if (checked.value) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                } else {
                                    null
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.LightGray,
                                    checkedTrackColor = Color.Blue,
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color.White,
                                )
                            )
                        }
                    }
                }
            }



            // Logout Button
            if (myProfile) {
                item {
                    Button(
                        onClick = {
                            viewModel?.logout()
                            navController?.navigate(Screens.loginScreen) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier

                            .padding(16.dp)
                            .fillMaxWidth(),

                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
                    ) {
                        Text(text = "Odjavi se", color = Color.White)
                    }
                }
            }
        }



        // Footer for Navigation
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Footer(
                addNewIcecream = {},
                active = 3,
                onHomeClick = {
                    navController?.navigate(Screens.homeScreen)
                },
                onRankingClick = {
                    navController?.navigate(Screens.rankingScreen)

                },
                onTableClick = {
                    val icsJson = Gson().toJson(icecreams)
                    val encodedicsJson = URLEncoder.encode(icsJson, StandardCharsets.UTF_8.toString())
                    navController?.navigate("tableScreen/$encodedicsJson")

                },
                onProfileClick = {}
            )
        }
    }
}



