package com.example.artsyproj4

import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Homepage(navController: NavController, modifier: Modifier = Modifier) {
    val date = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    //snackbar for after being logged out and account deletion
    val snackbarMessage = navController.currentBackStackEntry
        ?.arguments?.getString("snackbarMessage").orEmpty()
    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage.isNotBlank()) {
            snackbarHostState.showSnackbar(
                message = snackbarMessage,
                duration = SnackbarDuration.Short
            )
        }
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = Color.Gray,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(12.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(id = R.color.Header)),
                title = {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Artist Search",
                            color = colorResource(id = R.color.HeaderText),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row {
                            IconButton(onClick = {
                                navController.navigate("search")
                            }) {
                            Icon(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = "Search",
                                tint = colorResource(R.color.HeaderText),
                                modifier = Modifier
                                    .size(35.dp)
                                    .padding(end = 16.dp)
                            )}
                            IconButton(onClick = {
                                navController.navigate("login")
                            }) {
                            Icon(
                                painter = painterResource(id = R.drawable.user),
                                contentDescription = "User",
                                tint = colorResource(R.color.HeaderText),
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(23.dp)
                                    .offset(y = 1.dp))}


                        }
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(5.dp)
            ) {
                Text(
                    text = date,
                    color = Color(0xFF7A7A7A),
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .background(Color(0xFFEEEEF5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Favourites",
                        fontSize = 17.sp,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(35.dp))

                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp)
                        .background(Color(0xFF385794), shape = RoundedCornerShape(27.dp))
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            navController.navigate("login")
                        }
                ) {
                    Text(
                        text = "Log in to see favorites",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.height(35.dp))

                Text(
                    text = "Powered by Artsy",
                    color = Color(0xFF7A7A7A),
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.artsy.net"))
                            context.startActivity(intent)
                        }
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun HomepagePreview() {
    val navController = rememberNavController()
    Homepage(navController = navController)
}
