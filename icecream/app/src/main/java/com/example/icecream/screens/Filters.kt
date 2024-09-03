package com.example.icecream.screens

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.icecream.data.Icecream
import com.example.icecream.data.User
import com.example.icecream.repositories.Resource
import com.example.icecream.viewmodels.AuthViewModel
import com.example.icecream.viewmodels.IcecreamViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.math.RoundingMode
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun Filters(
    icecreamViewModel: IcecreamViewModel,
    viewModel: AuthViewModel,
    ic: MutableList<Icecream>,
    sheetState: ModalBottomSheetState,
    isFiltered: MutableState<Boolean>,
    isFilteredIndicator: MutableState<Boolean>,
    filteredic: MutableList<Icecream>,
    markers: MutableList<Icecream>,
    userLocation: LatLng?
){

    val context = LocalContext.current
    viewModel.getAllUsers()
    val allUsersResource = viewModel.allUsers.collectAsState()
    val allUsersNames = remember { mutableListOf<String>() }

    val sharedPreferences = context.getSharedPreferences("filters", Context.MODE_PRIVATE)
    val options = sharedPreferences.getString("options", null)
    val range = sharedPreferences.getFloat("range", 1000f)


    val initialCheckedState = remember {
        mutableStateOf(List(allUsersNames.size) { false })
    }
    val rangeValues = remember { mutableFloatStateOf(1000f) }

    val filtersSet = remember { mutableStateOf(false) }

// Use LaunchedEffect here to initialize filters based on indicator value
    LaunchedEffect(isFilteredIndicator.value) {
        if (isFilteredIndicator.value && options != null) {
            val type = object : TypeToken<List<Boolean>>() {}.type
            val savedOptions: List<Boolean> = Gson().fromJson(options, type) ?: emptyList()
            initialCheckedState.value = savedOptions
            rangeValues.floatValue = range
        }
    }



    val allUsersData = remember { mutableListOf<User>() }
    val selectedOptions = remember { mutableStateOf(initialCheckedState.value) }
    val isSet = remember { mutableStateOf(false) }

    allUsersResource.value.let {
        when(it){
            Resource.Loading -> {}
            is Resource.Failure -> {}
            is Resource.Success -> {
                allUsersNames.clear()
                allUsersData.clear()
                allUsersNames.addAll(it.result.map { user -> user.fullName})
                allUsersData.addAll(it.result)
                if(!isSet.value) {
                    initialCheckedState.value =
                        List(allUsersNames.count()) { false }.toMutableList()
                    isSet.value = true
                }
                Log.d("Names", initialCheckedState.toString())
            }

            null -> {}
        }
    }
    val coroutineScope = rememberCoroutineScope()

    val expanded = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 16.dp)
    ) {
        Text(
            text = "Autor",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))


        Column{
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { expanded.value = !expanded.value })
                    .background(Color.LightGray, RoundedCornerShape(4.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text("Izaberi autore", style = MaterialTheme.typography.body1)
                Icon(
                    if (expanded.value) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown icon"
                )
            }

            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                allUsersNames.forEachIndexed { index, option ->
                    DropdownMenuItem(onClick = {
                        val updatedCheckedState = initialCheckedState.value.toMutableList()
                        updatedCheckedState[index] = !updatedCheckedState[index]
                        initialCheckedState.value = updatedCheckedState
                        selectedOptions.value = updatedCheckedState
                    },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Log.d("Checked", initialCheckedState.value[index].toString())
                            Checkbox(
                                checked = initialCheckedState.value[index],
                                onCheckedChange = {
                                    val updatedCheckedState = initialCheckedState.value.toMutableList()
                                    updatedCheckedState[index] = it
                                    initialCheckedState.value = updatedCheckedState
                                    selectedOptions.value = updatedCheckedState
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(option)
                        }
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Udaljenost",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text =
                if(rangeValues.floatValue != 1000f)
                    rangeValues.floatValue.toBigDecimal().setScale(1, RoundingMode.UP).toString() + "m"
                else
                    "Beskonačno"
                ,style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W100
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Range(rangeValues = rangeValues)
        Spacer(modifier = Modifier.height(30.dp))


        FilterButton {
            val filteredList = ic.toMutableList()

            // Distance filtering
            if (rangeValues.floatValue != 1000f) {
                filteredList.retainAll {
                    calculateDistance(
                        userLocation!!.latitude,
                        userLocation.longitude,
                        it.location.latitude,
                        it.location.longitude
                    ) <= rangeValues.floatValue
                }
                sharedPreferences.edit().putFloat("range", rangeValues.floatValue).apply()
            }

            Log.d("Range Value", "Range: ${rangeValues.floatValue}")

            // Author filtering
            if (selectedOptions.value.contains(true)) {
                val selectedAuthors = allUsersData.filterIndexed { index, _ -> selectedOptions.value[index] }
                val selectedIndices = selectedAuthors.map { it.id }
                filteredList.retainAll { it.userId in selectedIndices }
                val selectedOptionsJson = Gson().toJson(selectedOptions.value)
                sharedPreferences.edit().putString("options", selectedOptionsJson).apply()
            }

            // Update filtered list state
            filteredic.clear()
            filteredic.addAll(filteredList)
            Log.d("Filtering", "Filtered List Size: ${filteredic.size}")
            // Trigger UI update
            isFiltered.value = !isFiltered.value

            coroutineScope.launch { sheetState.hide() }
        }


        Spacer(modifier = Modifier.height(10.dp))
        ResetFilters {
            markers.clear()
            markers.addAll(ic)

            initialCheckedState.value =
                List(allUsersNames.count()) { false }.toMutableList()
            rangeValues.floatValue = 1000f

            isFiltered.value = true
            isFiltered.value = false
            isFilteredIndicator.value = false

            with(sharedPreferences.edit()) {
                putFloat("range", 1000f)
                putString("options", null)
                apply()
            }

            coroutineScope.launch {
                sheetState.hide()
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}


private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371000.0

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c
}


@Composable
fun Range(
    rangeValues: MutableState<Float>
) {
    Slider(
        value = rangeValues.value,
        onValueChange = { rangeValues.value = it },
        valueRange = 0f..1000f,
        steps = 50,
        colors = SliderDefaults.colors(
            thumbColor = Color.Blue,
            activeTrackColor = Color.DarkGray,
            inactiveTrackColor = Color.LightGray
        )
    )
}

@Composable
fun FilterButton(
    onClick: () -> Unit
){
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.LightGray, RoundedCornerShape(30.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.LightGray,
            contentColor = Color.Black,
            disabledContainerColor = Color.DarkGray,
            disabledContentColor = Color.White
        ),

        ) {
        Text(
            "Filtriraj",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun ResetFilters(
    onClick: () -> Unit
){
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.LightGray, RoundedCornerShape(30.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Blue,
            contentColor = Color.White,
            disabledContainerColor = Color.DarkGray,
            disabledContentColor = Color.White
        ),

        ) {
        Text(
            "Resetuj Filtere",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}