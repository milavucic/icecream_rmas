package com.example.icecream

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
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
import com.example.icecream.viewmodels.AuthViewModelFactory
import com.example.icecream.viewmodels.IcecreamViewModel
import com.example.icecream.viewmodels.IcecreamViewModelFactory

class MainActivity : ComponentActivity() {
    //private lateinit var navController: NavHostController
    //private lateinit var authViewModel: AuthViewModel
    //private lateinit var icecreamViewModel: IcecreamViewModel
    private val userViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory()
    }
    private val icecreamViewModel: IcecreamViewModel by viewModels{
        IcecreamViewModelFactory()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Application(userViewModel, icecreamViewModel)

        }
    }
}
