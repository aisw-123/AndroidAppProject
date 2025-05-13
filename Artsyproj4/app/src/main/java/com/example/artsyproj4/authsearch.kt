package com.example.artsyproj4

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthSearchScreen(navController: NavController, modifier: Modifier = Modifier) {
    var resultloading by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val favPrefs = context.getSharedPreferences("favs", Context.MODE_PRIVATE)
    val favoriteIds = remember { mutableStateOf(emptySet<String>()) }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<Artist>() }
    var active by remember { mutableStateOf(true) }
    // only getting results when the search name length is >= 3 characters
    LaunchedEffect(searchQuery) {
        resultloading= true
        if (searchQuery.length >= 3) {
            delay(300)
            val results = fetchAuthSearchResults(searchQuery)
            //println(results)
            searchResults.clear()
            searchResults.addAll(results)
            val updatedFavs = favPrefs.getStringSet("favorite_artist_ids", emptySet()) ?: emptySet()
            //println("Favourites")
            println(updatedFavs)
            favoriteIds.value = updatedFavs
            resultloading=false
        } else {
            searchResults.clear()
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            }
        },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(R.color.Header)),
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.search),
                            contentDescription = "Search",
                            tint = colorResource(R.color.HeaderText),
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(24.dp)
                                .offset(y = 6.dp)
                        )
                        SearchBar(
                            inputField = {
                                SearchBarDefaults.InputField(
                                    query = searchQuery,
                                    onQueryChange = { searchQuery = it },
                                    onSearch = {},
                                    expanded = active,
                                    onExpandedChange = { active = it },
                                    placeholder = {
                                        Text(
                                            text = "Search artists...",
                                            fontSize = 18.sp
                                        )
                                    },
                                    leadingIcon = null,
                                    trailingIcon = {},
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = colorResource(R.color.Header),
                                        unfocusedContainerColor = colorResource(R.color.Header),
                                        disabledContainerColor = colorResource(R.color.Header),
                                        cursorColor = colorResource(R.color.HeaderText),
                                        focusedIndicatorColor = colorResource(R.color.Header),
                                        unfocusedIndicatorColor = colorResource(R.color.Header)
                                    ),
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                            },
                            expanded = active,
                            onExpandedChange = { active = it },
                            modifier = Modifier.fillMaxWidth(0.8f),
                            colors = SearchBarDefaults.colors(
                                containerColor=colorResource(R.color.Header),
                                dividerColor = Color.Transparent
                            ),
                            content = {}
                        )
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.window_close),
                                contentDescription = "Close",
                                tint = colorResource(R.color.HeaderText),
                                modifier = Modifier
                                    .padding(end = 0.dp)
                                    .size(23.dp)
                                    .offset(y = 6.dp)
                            )
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
                    .padding(16.dp)
            ) {
                if (!resultloading && searchResults.isEmpty() && searchQuery.length >= 3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 22.dp)
                                .fillMaxWidth(1f)
                                .height(70.dp),
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
                                    text = "No Results Found",
                                    color = colorResource(R.color.HeaderText),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 24.dp,
                                        vertical = 22.dp
                                    )
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(searchResults) { artist ->
                            val isFavorite = artist.id in favoriteIds.value

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clickable {
                                        navController.navigate(
                                            "authartistdetails/${artist.id}/${
                                                Uri.encode(
                                                    artist.name
                                                )
                                            }"
                                        )
                                    },
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    val painter =
                                        if (artist.imageUrl == "/assets/shared/missing_image.png" || artist.imageUrl.isEmpty()) {
                                            painterResource(id = R.drawable.default_artist_image)
                                        } else {
                                            rememberAsyncImagePainter(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(artist.imageUrl)
                                                    .crossfade(true)
                                                    .build()
                                            )
                                        }

                                    Image(
                                        painter = painter,
                                        contentDescription = artist.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = colorResource(R.color.Header),
                                            modifier = Modifier.size(32.dp),
                                            shadowElevation = 4.dp
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    val endpoint =
                                                        if (isFavorite) "removeFavorite" else "addFavorite"
                                                    val url = "https://csciassign4.uw.r.appspot.com/api/$endpoint"
                                                    val userdet = context.getSharedPreferences(
                                                        "user_details",
                                                        Context.MODE_PRIVATE
                                                    )
                                                    val userEmail =
                                                        userdet.getString("email", "") ?: ""
                                                    val requestBody = buildJsonObject {
                                                        put("artist_id", JsonPrimitive(artist.id))
                                                        put("email", JsonPrimitive(userEmail))
                                                        if (!isFavorite) {
                                                            put(
                                                                "artist_name",
                                                                JsonPrimitive(artist.name)
                                                            )
                                                            put(
                                                                "artist_thumbnail",
                                                                JsonPrimitive(artist.imageUrl)
                                                            )
                                                        }
                                                    }.toString()
                                                    println(requestBody)

                                                    val client = OkHttpClient()
                                                    val mediaType = "application/json".toMediaType()
                                                    val request = Request.Builder()
                                                        .url(url)
                                                        .post(requestBody.toRequestBody(mediaType))
                                                        .build()

                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        try {
                                                            val response =
                                                                client.newCall(request).execute()
                                                            println(response)
                                                            val mutableFavoriteIds =
                                                                favoriteIds.value.toMutableSet()

                                                            if (response.isSuccessful) {
                                                                withContext(Dispatchers.Main) {
                                                                    val message = if (isFavorite) {
                                                                        mutableFavoriteIds.remove(
                                                                            artist.id
                                                                        )
                                                                        "Removed from favorites"
                                                                    } else {
                                                                        mutableFavoriteIds.add(
                                                                            artist.id
                                                                        )
                                                                        "Added to favorites"
                                                                    }
                                                                    if (isFavorite) {
                                                                        mutableFavoriteIds.remove(
                                                                            artist.id
                                                                        )
                                                                    } else {
                                                                        mutableFavoriteIds.add(
                                                                            artist.id
                                                                        )
                                                                    }
                                                                    favPrefs.edit().putStringSet(
                                                                        "favorite_artist_ids",
                                                                        mutableFavoriteIds
                                                                    ).apply()
                                                                    favoriteIds.value =
                                                                        mutableFavoriteIds
                                                                    scope.launch {
                                                                        keyboardController?.hide()
                                                                        snackbarHostState.showSnackbar(
                                                                            message = message,
                                                                            withDismissAction = false,
                                                                            duration = SnackbarDuration.Short
                                                                        )
                                                                    }

                                                                }
                                                            } else {
                                                                Log.e(
                                                                    "Favorite",
                                                                    "Failed: ${response.message}"
                                                                )
                                                            }
                                                        } catch (e: Exception) {
                                                            Log.e(
                                                                "Favorite",
                                                                "Exception: ${e.localizedMessage}"
                                                            )
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                                    contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                                                    tint = Color.Black
                                                )
                                            }


                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(colorResource(R.color.Header).copy(alpha = 0.7f))
                                            .align(Alignment.BottomStart)
                                            .padding(8.dp)
                                            .height(20.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start
                                        ) {
                                            Text(
                                                text = artist.name,
                                                color = colorResource(R.color.HeaderText),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(
                                                painter = painterResource(id = R.drawable.chevron_right),
                                                contentDescription = "Icon",
                                                tint = colorResource(R.color.HeaderText),
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .padding(end = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


            }
        }
    )
}

suspend fun fetchAuthSearchResults(query: String): List<Artist> = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://csciassign4.uw.r.appspot.com/api/search?q=$query")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val stream = connection.inputStream.bufferedReader().use { it.readText() }
            println("Response: $stream")
            val jsonArray = JSONArray(stream)

            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                Artist(
                    name = obj.optString("title", "Untitled"),
                    imageUrl = obj.optJSONObject("_links")?.optJSONObject("thumbnail")?.optString("href", "") ?: "",
                    id = obj.optJSONObject("_links")?.optJSONObject("self")?.optString("href", "")?.substringAfterLast("/") ?: "",

                    )
            }
        } else {
            println("Error: Response code is $responseCode")
            return@withContext emptyList<Artist>()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext emptyList<Artist>()
    }
}


@Composable
@Preview(showBackground = true)
fun AuthSearchScreenPreview() {
    val navController = rememberNavController()
    AuthSearchScreen(navController = navController)
}
