package com.example.artsyproj4

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.artsyproj4.ui.theme.Artsyproj4Theme
import okhttp3.HttpUrl.Companion.toHttpUrl

class MainActivity : ComponentActivity() {
    private fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences("cookies", Context.MODE_PRIVATE)
        val cookieJar = PersistentCookieJar(prefs)
        val cookies = cookieJar.loadForRequest("https://csciassign4.uw.r.appspot.com/".toHttpUrl())
        Log.d("CookiesOnStartup", cookies.joinToString { "${it.name}=${it.value}" })
        println(cookies)
        println("Im here, getting cookies")
        return cookies.isNotEmpty()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Artsyproj4)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //Handler(Looper.getMainLooper()).postDelayed({
            setContent {
                Artsyproj4Theme {
                    val navController = rememberNavController()
                    val startDestination = if (isLoggedIn(applicationContext)) "authhome" else "homepage"

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        NavHost(navController = navController, startDestination = startDestination) {
                            composable(
                                "homepage?snackbarMessage={snackbarMessage}",
                                arguments = listOf(navArgument("snackbarMessage") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                    nullable = true
                                })
                            ) {

                            Homepage(navController = navController, modifier = Modifier.padding(innerPadding))
                            }
                            composable("search") {
                                SearchScreen(navController = navController)
                            }
                            composable("authsearch") {
                                AuthSearchScreen(navController = navController)
                            }
                            composable("login") {
                                LoginPage(navController)
                            }
                            composable("register") {
                                RegisterPage(navController)
                            }
                            composable("authhome") {
                                AuthHomePage(navController = navController)
                            }
                            composable(
                                "artistdetails/{artistId}/{artistName}",
                                arguments = listOf(
                                    navArgument("artistId") { type = NavType.StringType },
                                    navArgument("artistName") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                                val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
                                ArtistPage(navController = navController, artistId = artistId, artistName = artistName)
                            }
                            composable(
                                "authartistdetails/{artistId}/{artistName}",
                                arguments = listOf(
                                    navArgument("artistId") { type = NavType.StringType },
                                    navArgument("artistName") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                                val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
                                AuthArtistPage(navController = navController, artistId = artistId, artistName = artistName)
                            }
                        }
                    }
                }
            }
        //}, 1500)
    }
}