package com.example.icecream.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TableRows
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.icecream.R
import com.example.icecream.data.Icecream
import com.example.icecream.data.Mark
import com.example.icecream.navigation.Screens
import com.example.icecream.repositories.Resource
import com.example.icecream.viewmodels.AuthViewModel
import com.example.icecream.viewmodels.IcecreamViewModel
import com.google.gson.Gson
import java.math.RoundingMode
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AboutIcecreamScreen(
    navController: NavController,
    icecreamViewModel: IcecreamViewModel,
    viewModel: AuthViewModel,
    icecream: Icecream,
    icecreams: MutableList<Icecream>?
) {
    val marksResource = icecreamViewModel.marks.collectAsState()
    val newmarkResource = icecreamViewModel.markFlow.collectAsState()

    val marks = remember { mutableListOf<Mark>() }
    val avgMark = remember { mutableStateOf(0.0) }
    val myMark = remember { mutableStateOf(0) }
    val showRateDialog = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }


    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            // Back Button
            item {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        Color.Blue,
                        contentColor = Color.White
                    ),

                    onClick = {
                        if (icecreams == null) {
                            navController.popBackStack()
                        } else {
                            val isCameraSet = true
                            val latitude = icecream.location.latitude
                            val longitude = icecream.location.longitude

                            val icsJson = Gson().toJson(icecreams)
                            val encodedicsJson = URLEncoder.encode(icsJson, StandardCharsets.UTF_8.toString())
                            navController.navigate(
                                Screens.homeScreenParam +
                                        "/$isCameraSet/$latitude/$longitude/$encodedicsJson"
                            )
                        }
                    }

                ) {
                    Text(text = "Nazad")
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Ic Location
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = icecream.name.replace('+', ' '),
                        style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Latitude: ${icecream.location.latitude}, Longitude: ${icecream.location.longitude}")
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }

            // Ic Average Rate
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Prosečna ocena: ${avgMark.value}",
                        style = TextStyle(fontSize = 18.sp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }

            // Ic Description
            item{
                Text(
                    text = "Pročitaj o štandu",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                )
            }
            item { Spacer(modifier = Modifier.height(5.dp)) }
            item {
                Text(
                    text = icecream.description.replace('+', ' '),
                    style = TextStyle(fontSize = 18.sp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Gallery
            item {
                Text(
                    text = "Galerija",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),

                )
                Spacer(modifier = Modifier.height(10.dp))
                // Here, replace with images using a standard composable if needed
                icecream.galleryImages.forEach { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Gallery Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(8.dp)
                    )
                }
            }
        }

        // Rate Button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 15.dp, vertical = 20.dp)
        ) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    Color.Blue,
                    contentColor = Color.White
                ),

                onClick = {
                    val marked = marks.firstOrNull {
                        it.icecreamId == icecream.id && it.userId == viewModel.currentUser!!.uid
                    }
                    if (marked != null) myMark.value = marked.mark
                    showRateDialog.value = true
                },
                enabled = icecream.userId != viewModel.currentUser?.uid
            ) {
                Text(text = "Oceni sladoled")
            }
        }

        // Rate
        if (showRateDialog.value) {
            RateDialog(
                showRateDialog = showRateDialog,
                mark = myMark,
                markIc = {
                    val marked = marks.firstOrNull {
                        it.icecreamId == icecream.id && it.userId == viewModel.currentUser!!.uid
                    }
                    if (marked != null) {
                        isLoading.value = true
                        icecreamViewModel.updateIcecreamMark(
                            icid = marked.id,
                            mark = myMark.value
                        )
                    } else {
                        isLoading.value = true
                        icecreamViewModel.addIcecreamMark(
                            icid = icecream.id,
                            icecream=icecream,
                            mark = myMark.value
                        )
                    }
                },
                isLoading = isLoading
            )
        }
    }

    // Data Handling for Rates
    marksResource.value.let {
        when (it) {
            is Resource.Success -> {
                marks.addAll(it.result)
                val sum = it.result.sumOf { mark -> mark.mark.toDouble() }
                if (sum != 0.0) {
                    val avg = (sum / it.result.size).toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
                    avgMark.value = avg
                } else {

                }
            }
            is Resource.Loading -> {
                // Handle loading state if necessary
            }
            is Resource.Failure -> {
                Log.e("Error", it.toString())
            }
        }
    }

    // Handling New Rate Resource
    newmarkResource.value.let {
        when (it) {
            is Resource.Success -> {
                isLoading.value = false
                marks.firstOrNull { mark -> mark.id == it.result }?.mark = myMark.value
            }
            is Resource.Loading -> {
                // Handle loading state if necessary
            }
            is Resource.Failure -> {
                val context = LocalContext.current
                Toast.makeText(context, "Greška pri ocenjivanju", Toast.LENGTH_LONG).show()
                isLoading.value = false
            }
            null -> {
                isLoading.value = false
            }
        }
    }
}

@Composable
fun RateDialog(showRateDialog: MutableState<Boolean>,
             mark: MutableState<Int>, markIc: () -> Unit, isLoading: MutableState<Boolean>) {
    val interactionSrc =
        remember{ MutableInteractionSource() }

    AlertDialog(
        modifier = Modifier
            .clip(
                RoundedCornerShape(20.dp)
            ),
        onDismissRequest = {},

        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .background(Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ){
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
//                        Image(painter = painterResource(id = R.drawable.cutestar), contentDescription = "")
                        Text(
                            text = "Oceni sladoled na ovom štandu",
                            style = TextStyle(
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(40.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround

                        ) {
                            for (i in 1..5){
                                Icon(
                                    imageVector =
                                    if(mark.value>= i) Icons.Filled.Star
                                    else Icons.Filled.StarBorder,
                                    contentDescription = "",
                                    tint =
                                    if(mark.value >= i) Color.LightGray
                                    else Color.DarkGray,
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clickable(
                                            interactionSource = interactionSrc,
                                            indication = null
                                        ) {
                                            mark.value = i
                                        }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                        Button(
                            onClick = markIc,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Blue,
                                contentColor = Color.Black,
                                disabledContainerColor = Color.Gray,
                                disabledContentColor = Color.White,
                            ),
                        ) {
                            if (isLoading.value) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Potvrdi",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(15.dp))
                        Text(
                            text = "Zatvori",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = interactionSrc,
                                    indication = null
                                ) {
                                    showRateDialog.value = false
                                },
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
        },
        dismissButton = {
        }
    )

}
