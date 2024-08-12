package com.example.icecream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.icecream.navigation.Screens
import com.example.icecream.screens.HomeScreen
import com.example.icecream.screens.LoginScreen
import com.example.icecream.screens.RegisterScreen
import com.example.icecream.ui.theme.IcecreamTheme
import com.example.icecream.viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = AuthViewModel() // Initialize your ViewModel
        setContent {
            IcecreamTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screens.loginScreen
                    ) {
                        composable(Screens.loginScreen) {
                            LoginScreen(viewModel = authViewModel, navController = navController)
                        }

                        composable(Screens.registerScreen) {
                            RegisterScreen(viewModel = authViewModel, navController = navController)
                        }

                        composable(Screens.homeScreen) {
                            HomeScreen(viewModel = authViewModel, navController = navController)
                        }
                        // Define other composable screens here...
                    }
                }
            }
        }
    }
}
