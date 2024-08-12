package com.example.icecream.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.icecream.navigation.Screens
import com.example.icecream.viewmodels.AuthViewModel

@Composable
fun HomeScreen(
    viewModel: AuthViewModel?,
    navController: NavController
) {

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