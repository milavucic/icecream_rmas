package com.example.icecream.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.icecream.navigation.Screens
import com.example.icecream.viewmodels.AuthViewModel
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun RankingScreen(
    viewModel: AuthViewModel,
    navController: NavController
){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.White
            )
    ){


        Button(onClick = {
            viewModel?.logout()
            navController.navigate(Screens.loginScreen) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }

        }) {
            Text(text = "Log out")

        }

    }
}