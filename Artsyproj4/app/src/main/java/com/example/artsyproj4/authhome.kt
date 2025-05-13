package com.example.artsyproj4
import android.content.Context
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.delay
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

suspend fun fetchAndStoreFavorites(email: String, context: Context): Set<String> {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val url = "https://csciassign4.uw.r.appspot.com/api/getFavorites?email=$email"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonArray = JSONArray(responseBody)
                    val idSet = mutableSetOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        idSet.add(jsonArray.getString(i))
                    }

                    val favPrefs = context.getSharedPreferences("favs", Context.MODE_PRIVATE)
                    favPrefs.edit().putStringSet("favorite_artist_ids", idSet).apply()

                    return@withContext idSet
                }
            }
            emptySet()
        } catch (e: Exception) {
            e.printStackTrace()
            emptySet()
        }
    }
}
data class FavoriteArtist(
    val id: String,
    val name: String,
    val birthday: String,
    val deathday: String,
    val nationality: String,
    val dateAdded: String = ""
)
suspend fun fetchFavoriteDate(email: String, artistId: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val json = """{ "email": "$email", "artist_id": "$artistId" }"""
            val mediaType = "application/json".toMediaType()
            val requestBody = json.toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://csciassign4.uw.r.appspot.com/api/favorite-date")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonObject = org.json.JSONObject(responseBody ?: "")
                return@withContext jsonObject.getString("relativeTime")
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}




suspend fun fetchArtistDetailsById(id: String): FavoriteArtist? {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val url = "https://csciassign4.uw.r.appspot.com/api/artist/$id"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val json = org.json.JSONObject(response.body?.string() ?: "")
                FavoriteArtist(
                    id = id,
                    name = json.getString("name"),
                    birthday = json.optString("birthday", ""),
                    deathday = json.optString("deathday", ""),
                    nationality = json.optString("nationality", "")
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthHomePage(navController: NavController) {
    var favloading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("user_details", Context.MODE_PRIVATE)
    val email = prefs.getString("email", "") ?: ""
    println("Fetching favorites for: $email")

    val profileImageUrl = prefs.getString("profileImageUrl", "") ?: ""
    val date = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    var expanded by remember { mutableStateOf(false) }
    val favoriteArtists = remember { mutableStateListOf<FavoriteArtist>() }
    // on page load we getting and storing both favorites and the times at which it was stored
    LaunchedEffect(Unit) {
        favloading=true
        val ids = fetchAndStoreFavorites(email, context)
        val artistsWithDate = ids.mapNotNull { id ->
            val artist = fetchArtistDetailsById(id)
            val date = fetchFavoriteDate(email, id)
            artist?.copy(dateAdded = date ?: "")
        }
        favoriteArtists.addAll(artistsWithDate)
        favloading=false

    }
    Scaffold(
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                navController.navigate("authsearch")
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.search),
                                    contentDescription = "Search",
                                    tint = colorResource(R.color.HeaderText),
                                    modifier = Modifier
                                        .size(35.dp)
                                        .padding(end = 16.dp)
                                )
                            }

                            Box(modifier = Modifier.padding(end = 8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(25.dp)
                                        .clip(CircleShape)
                                        .clickable { expanded = true }
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(profileImageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile Image",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {//Clearing all cookies and data on Shared Preferences when logging out and deleting account
                                    //After both logout ad delete, navigating to original home page
                                    DropdownMenuItem(
                                        text = { Text("Log Out") },
                                        onClick = {
                                            expanded = false
                                            val cookiePrefs = context.getSharedPreferences("cookies", Context.MODE_PRIVATE)
                                            val cookieJar = PersistentCookieJar(cookiePrefs)
                                            cookieJar.clearCookies()
                                            val userPrefs = context.getSharedPreferences("user_details", Context.MODE_PRIVATE)
                                            userPrefs.edit().clear().apply()
                                            val favPrefs = context.getSharedPreferences("favs", Context.MODE_PRIVATE)
                                            favPrefs.edit().clear().apply()
                                            navController.navigate("homepage?snackbarMessage=Logged out successfully")  {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    inclusive = true
                                                }
                                                launchSingleTop = true
                                            }
                                        }
                                    )


                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Delete Account",
                                                color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                                        },
                                        onClick = {
                                            expanded = false
                                            val cookiePrefs = context.getSharedPreferences("cookies", Context.MODE_PRIVATE)
                                            val cookieJar = PersistentCookieJar(cookiePrefs)
                                            cookieJar.clearCookies()
                                            val userPrefs = context.getSharedPreferences("user_details", Context.MODE_PRIVATE)
                                            val userEmail = userPrefs.getString("email", "") ?: ""
                                            println(userEmail)
                                            userPrefs.edit().clear().apply()
                                            val favPrefs = context.getSharedPreferences("favs", Context.MODE_PRIVATE)
                                            favPrefs.edit().clear().apply()
                                            val client = OkHttpClient()
                                            val url = "https://csciassign4.uw.r.appspot.com/delete"
                                            val requestBody = JSONObject().apply {
                                                put("email", userEmail)
                                            }.toString()
                                            println(requestBody)

                                            val request = Request.Builder()
                                                .url(url)
                                                .delete(requestBody.toRequestBody("application/json".toMediaType()))
                                                .build()

                                            CoroutineScope(Dispatchers.IO).launch {
                                                try {
                                                    val response = client.newCall(request).execute()
                                                    if (response.isSuccessful) {
                                                        withContext(Dispatchers.Main) {
                                                            navController.navigate("homepage?snackbarMessage=Deleted User successfully") {
                                                                popUpTo(navController.graph.startDestinationId) {
                                                                    inclusive = true
                                                                }
                                                                launchSingleTop = true
                                                            }
                                                        }
                                                    } else {
                                                        Log.e("Delete Account", "Failed: ${response.message}")
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("Delete Account", "Exception: ${e.localizedMessage}")
                                                }
                                            }
                                        }
                                    )
                                }
                            }
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
                Text(text = date, color = Color(0xFF7A7A7A), fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .background(Color(0xFFF0F0F5)),
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
                if (favloading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(strokeWidth = 4.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Loading...", style = MaterialTheme.typography.labelSmall)
                        }                    }
                }
                else if (!favloading && favoriteArtists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 32.dp)
                                .fillMaxWidth(1f)
                                .height(60.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = colorResource(R.color.Header),
                            tonalElevation = 2.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No favorites",
                                    color = colorResource(R.color.HeaderText),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 24.dp,
                                        vertical = 12.dp
                                    )
                                )
                            }
                        }
                    }

                }// if there is favorites, displaying them in cards
                 //clicking cards navigate to the artist details
                 else {
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        items(favoriteArtists) { artist ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate(
                                            "authartistdetails/${artist.id}/${Uri.encode(artist.name)}"
                                        )                                    },
                                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.Fav))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            artist.name,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colorResource(R.color.HeaderText),
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            "${artist.nationality}, ${artist.birthday}",
                                            fontSize = 14.sp,
                                            color = colorResource(R.color.HeaderText)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 16.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (artist.dateAdded.contains("second")) {
                                            var secondsElapsed by remember { mutableStateOf(artist.dateAdded.filter { it.isDigit() }.toIntOrNull() ?: 0) }
                                            val coroutineScope = rememberCoroutineScope()

                                            LaunchedEffect(artist.id) {
                                                while (secondsElapsed < 60) {
                                                    delay(1000)
                                                    secondsElapsed++
                                                }
                                                // When 60 seconds are reached, fetch a fresh date
                                                val updatedDate = fetchFavoriteDate(email, artist.id)
                                                val index = favoriteArtists.indexOfFirst { it.id == artist.id }
                                                if (index != -1 && updatedDate != null) {
                                                    val updatedArtist = favoriteArtists[index].copy(dateAdded = updatedDate)
                                                    favoriteArtists[index] = updatedArtist
                                                }
                                            }

                                            Text(
                                                text = "$secondsElapsed seconds ago",
                                                fontStyle = FontStyle.Italic,
                                                fontSize = 13.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                        } else {
                                            Text(
                                                text = artist.dateAdded,
                                                fontStyle = FontStyle.Italic,
                                                fontSize = 13.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                        }


                                        Icon(
                                            painter = painterResource(id = R.drawable.chevron_right),
                                            contentDescription = "Details",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                }
                            }

                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))



                Text(
                    text = "Powered by Artsy",
                    color = Color(0xFF7A7A7A),
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.artsy.net"))
                            context.startActivity(intent)
                        }
                )
            }
        }
    )
}
