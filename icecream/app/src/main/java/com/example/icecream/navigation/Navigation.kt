package com.example.icecream.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHost
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.icecream.screens.LoginScreen
import com.example.icecream.viewmodels.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.example.icecream.screens.HomeScreen
import com.example.icecream.screens.RegisterScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Nav(
    viewModel: AuthViewModel,

){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screens.loginScreen) {
        composable(Screens.loginScreen){
            LoginScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable(Screens.registerScreen) {
            RegisterScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable(Screens.homeScreen) {
            HomeScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}