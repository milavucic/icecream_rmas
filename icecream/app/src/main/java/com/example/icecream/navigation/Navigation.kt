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
import com.example.icecream.data.Icecream
import com.example.icecream.data.User
import com.example.icecream.repositories.Resource
import com.example.icecream.screens.AboutIcecreamScreen
import com.example.icecream.screens.HomeScreen
import com.example.icecream.screens.RankingScreen
import com.example.icecream.screens.RegisterScreen
import com.example.icecream.screens.UserScreen
import com.example.icecream.viewmodels.IcecreamViewModel
import com.google.gson.Gson
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Nav(
    viewModel: AuthViewModel,
    icecreamViewModel: IcecreamViewModel

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

        composable(Screens.homeScreen){
            val icResource = icecreamViewModel.icecreams.collectAsState()
            val icMarkers = remember {
                mutableListOf<Icecream>()
            }
            icResource.value.let {
                when(it){
                    is Resource.Success -> {
                        icMarkers.clear()
                        icMarkers.addAll(it.result)
                    }
                    is Resource.Loading -> {

                    }
                    is Resource.Failure -> {
                        Log.e("Podaci", it.toString())
                    }
                    null -> {}
                }
            }
            HomeScreen(
                viewModel = viewModel,
                navController = navController,
                icecreamViewModel = icecreamViewModel,
                icecreamMarkers =icMarkers
            )
        }

        composable(
            //route = Screens.userScreen,
            route = "${Screens.userScreen}/{user}",
            arguments = listOf(navArgument("userData"){
                type = NavType.StringType
            })
        ){backStackEntry ->
            //val userDataJson = backStackEntry.arguments?.getString("user")
            //val userData = Gson().fromJson(userDataJson, User::class.java)
            //val mine = FirebaseAuth.getInstance().currentUser?.uid == userData.id
            val userJson = backStackEntry.arguments?.getString("user") ?: ""
            Log.d("UserScreen", "Encoded userJson: $userJson")
            //val decodedUserJson = URLDecoder.decode(userJson, StandardCharsets.UTF_8.toString())
            val decodedUserJson = URLDecoder.decode(userJson ?: "", StandardCharsets.UTF_8.toString())
            Log.d("UserScreen", "Decoded userJson: $decodedUserJson")
            val userData = Gson().fromJson(decodedUserJson, User::class.java)
            Log.d("UserScreen", "Parsed User: $userData")
            if (userData != null) {
            val mine = FirebaseAuth.getInstance().currentUser?.uid == userData.id
            UserScreen(
                navController = navController,
                viewModel = viewModel,
                user= userData,
                myProfile = mine
            )
            }
            else{
                Log.d("UserScreen", "Greska ")
            }
        }

        composable(Screens.rankingScreen){
            RankingScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable(
            route = "${Screens.aboutIcecreamScreen}/{icecream}/{icecreams}",
            arguments = listOf(
                navArgument("icecream"){ type = NavType.StringType },
                navArgument("icecreams"){ type = NavType.StringType },
            )
        ){backStackEntry ->
            val icJson = backStackEntry.arguments?.getString("icecream")?: ""
            Log.d("IcecreamScreen", "Encoded icJson: $icJson")
            val decodedicJson = URLDecoder.decode(icJson ?: "", StandardCharsets.UTF_8.toString())
            Log.d("IcecreamScreen", "Decoded icJson: $decodedicJson")
            val ic = Gson().fromJson(decodedicJson, Icecream::class.java)
            val icsJson = backStackEntry.arguments?.getString("icecreams")
            Log.d("IcecreamScreen", "Encoded icJson: $icJson")
            val decodedicsJson = URLDecoder.decode(icsJson ?: "", StandardCharsets.UTF_8.toString())
            Log.d("IcecreamScreen", "Decoded icsJson: $decodedicJson")
            val ics = Gson().fromJson(decodedicsJson, Array<Icecream>::class.java).toList()
            icecreamViewModel.getIcecreamMarks(ic.id)

            AboutIcecreamScreen(
                navController = navController,
                icecreamViewModel = icecreamViewModel,
                viewModel = viewModel,
                icecream=ic,
                icecreams = ics.toMutableList()
            )
        }
    }
}