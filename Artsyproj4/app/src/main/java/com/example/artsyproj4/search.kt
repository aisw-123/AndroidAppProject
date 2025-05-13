package com.example.artsyproj4

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
import androidx.compose.foundation.clickable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.colorResource


data class Artist(val name: String, val imageUrl: String,val id: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, modifier: Modifier = Modifier) {
    var resultloading by remember { mutableStateOf(false) }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<Artist>() }
    var active by remember { mutableStateOf(true) }
    LaunchedEffect(searchQuery) {
        resultloading= true

        if (searchQuery.length >= 3) {
            delay(300)
            val results = fetchSearchResults(searchQuery)
            println(results)
            searchResults.clear()
            searchResults.addAll(results)
            resultloading=false
        } else {
            searchResults.clear()
        }
    }

    Scaffold(
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
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clickable {
                                        navController.navigate(
                                            "artistdetails/${artist.id}/${
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

suspend fun fetchSearchResults(query: String): List<Artist> = withContext(Dispatchers.IO) {
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
fun SearchScreenPreview() {
    val navController = rememberNavController()
    SearchScreen(navController = navController)
}
