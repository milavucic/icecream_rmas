package com.example.icecream.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TableRows
import androidx.compose.material.icons.outlined.TableView
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.icecream.R

@Composable
fun Footer(
    addNewIcecream: () -> Unit,
    active: Int,
    onHomeClick: () -> Unit,
    onRankingClick: () -> Unit,
    onTableClick: ()-> Unit,
    onProfileClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                    spotColor = Color.Transparent
                )
                .border(
                    1.dp,
                    Color.Transparent,
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                )
                .background(
                    Color.White,
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = onHomeClick) {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "",
                        tint = if(active == 0) Color.LightGray else Color.Blue,
                        modifier = Modifier.size(35.dp)
                    )
                }
                IconButton(onClick = onRankingClick) {
                    Icon(
                        imageVector = Icons.Outlined.List,
                        contentDescription = "",
                        tint = if(active == 1) Color.LightGray else Color.Blue,
                        modifier = Modifier.size(35.dp)
                    )
                }
                Spacer(modifier = Modifier.size(70.dp))
                IconButton(onClick = onTableClick) {
                    Icon(
                        imageVector = Icons.Outlined.TableView,
                        contentDescription = "",
                        tint = if(active == 2) Color.LightGray else Color.Blue,
                        modifier = Modifier.size(35.dp)
                    )
                }
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "",
                        tint = if(active == 3) Color.LightGray else Color.Blue,
                        modifier = Modifier.size(35.dp)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-30).dp)
                .size(90.dp)
        ) {
            IconButton(onClick = addNewIcecream,
                modifier = Modifier.fillMaxSize()) {

                Icon(
                    imageVector = Icons.Outlined.AddLocationAlt,
                    contentDescription = "",
                    tint = if(active == 0) Color.Blue else Color.LightGray,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
