package com.example.artsyproj4
import android.content.Context
import android.content.SharedPreferences
import coil.compose.AsyncImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

//details to print the similar artist tab
@Composable
fun AuthSimilar(
    similarArtists: List<SimArtist>?,
    navController: NavController,
    favPrefs: SharedPreferences,
    favoriteIds: MutableState<Set<String>>,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    keyboardController: SoftwareKeyboardController?
)
{
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (similarArtists != null) {
            if (similarArtists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 22.dp)
                            .fillMaxWidth()
                            .height(70.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = colorResource(R.color.Header),
                        tonalElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No Similar Artists",
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
                    items(similarArtists) { artist ->
                        val isFavorite = artist.id in favoriteIds.value
                        println("Inside function")
                        println(favoriteIds.value)
                        println(isFavorite)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clickable {
                                    navController.navigate(
                                        "authartistdetails/${artist.id}/${Uri.encode(artist.name)}"
                                    )
                                },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                val painter = if (artist.image.isEmpty() ||
                                    artist.image == "/assets/shared/missing_image.png"
                                ) {
                                    painterResource(id = R.drawable.default_artist_image)
                                } else {
                                    rememberAsyncImagePainter(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(artist.image)
                                            .crossfade(true)
                                            .build()
                                    )
                                }

                                Image(
                                    painter = painter,
                                    contentDescription = artist.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
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
                                                //if the card is already favorited, then onclick we call the remove fav endpoint
                                                //if the card was not favourite then onclick we call the add favourite endpoint
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
                                                        put("artist_name", JsonPrimitive(artist.name))
                                                        put("artist_thumbnail", JsonPrimitive(artist.image))
                                                    }
                                                }.toString()

                                                val client = OkHttpClient()
                                                val mediaType = "application/json".toMediaType()
                                                val request = Request.Builder()
                                                    .url(url)
                                                    .post(requestBody.toRequestBody(mediaType))
                                                    .build()

                                                CoroutineScope(Dispatchers.IO).launch {
                                                    try {
                                                        val response = client.newCall(request).execute()
                                                        val mutableFavoriteIds = favoriteIds.value.toMutableSet()

                                                        if (response.isSuccessful) {
                                                            withContext(Dispatchers.Main) {
                                                                val message = if (isFavorite) {
                                                                    mutableFavoriteIds.remove(artist.id)
                                                                    "Removed from favorites"
                                                                } else {
                                                                    mutableFavoriteIds.add(artist.id)
                                                                    "Added to favorites"
                                                                }
                                                                // Updating the Shared preferences and the ids list maintained for the consistent update of the star icon
                                                                favPrefs.edit()
                                                                    .putStringSet(
                                                                        "favorite_artist_ids",
                                                                        mutableFavoriteIds
                                                                    )
                                                                    .apply()

                                                                favoriteIds.value = mutableFavoriteIds
                                                                // removing the keyboard if it was up and showing the appropriate message in snackbar
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
                                                            println("Favorite Failed: ${response.message}")
                                                        }
                                                    } catch (e: Exception) {
                                                        println("error ${e.localizedMessage}")
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
}


suspend fun AuthfetchSimilarArtists(artistId: String): List<SimArtist> = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://csciassign4.uw.r.appspot.com/api/similar-artists?artistId=$artistId")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val json = connection.inputStream.bufferedReader().use { it.readText() }
            println("Similar Artists Response: $json")
            val obj = JSONObject(json)
            val array = obj.getJSONArray("artists")
            return@withContext List(array.length()) { i ->
                val a = array.getJSONObject(i)
                SimArtist(
                    id = a.getString("id"),
                    name = a.getString("name"),
                    image = a.getString("image")
                )
            }
        } else {
            println("Failed to fetch similar artists. HTTP code: ${connection.responseCode}")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return@withContext emptyList()
}


@Composable
fun AuthArtistDetailsTab(artistDetails: ArtistDetail?) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = artistDetails?.name ?: "Unknown",
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        val nationality = artistDetails?.nationality ?: ""
        val birthday = artistDetails?.birthday ?: ""
        val deathday = artistDetails?.deathday ?: ""

        Text(
            text = "$nationality, $birthday - $deathday",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
            color = Color.Black ,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
        Text(
            text = cleanText(artistDetails?.biography ?: ""),
            fontSize = 15.sp,
            letterSpacing = (-0.2).sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            textAlign = TextAlign.Justify
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AuthArtworksTab(artworks: List<Artwork>) {
    var showDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }

    val scope = rememberCoroutineScope()

    var selectedArtworkTitle by remember { mutableStateOf("") }
    if (artworks.isEmpty()) {
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
                    .height(50.dp),
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
                        text = "No Artworks",
                        color = colorResource(R.color.Notext),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(
                            horizontal = 24.dp,
                            vertical = 12.dp
                        )
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(artworks) { artwork ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = artwork.image,
                            contentDescription = artwork.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            contentScale = ContentScale.FillWidth
                        )

                        Text(
                            text = "${artwork.title}, ${artwork.date}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Button(
                            onClick = {
                                isLoading = true
                                categories = emptyList()
                                showDialog = true
                                selectedArtworkTitle = artwork.title
                                scope.launch {
                                    kotlinx.coroutines.delay(500)
                                    val result = fetchCategories(artwork.id)
                                    categories = result
                                    println("Categories= $categories")
                                    isLoading = false
                                }
                            },
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Text("View categories")
                        }
                    }
                }
            }
        }
    }
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { },
                contentAlignment = Alignment.Center
            ) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Categories") },
                    text = {
                        when {
                            isLoading -> Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(strokeWidth = 4.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Loading...", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                            categories.isEmpty() -> Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(25.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No categories available.")
                            }
                            else -> {
                                val pagerState = rememberPagerState(initialPage = Int.MAX_VALUE / 2)
                                val categoryCount = categories.size
                                val scope = rememberCoroutineScope()
                                var isLeftHovered by remember { mutableStateOf(false) }
                                var isRightHovered by remember { mutableStateOf(false) }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(480.dp)
                                ) {
                                    HorizontalPager(
                                        state = pagerState,
                                        count = Int.MAX_VALUE,
                                        contentPadding = PaddingValues(horizontal = 12.dp),
                                        modifier = Modifier
                                            .fillMaxSize()
                                    ) { page ->
                                        val index = page % categoryCount
                                        val category = categories[index]

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 3.dp, vertical = 11.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = colorResource(R.color.CatCard)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                            ) {
                                                AsyncImage(
                                                    model = category.image,
                                                    contentDescription = category.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(150.dp)
                                                )

                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Spacer(modifier = Modifier.height(7.dp))

                                                    Text(
                                                        text = category.name,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    Box(
                                                        modifier = Modifier
                                                            .height(200.dp)
                                                            .verticalScroll(rememberScrollState())
                                                    ) {
                                                        Text(
                                                            text = category.description ?: "",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            textAlign = TextAlign.Start
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                            }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .offset(x = -20.dp)
                                            .size(40.dp)
                                            .background(
                                                color = if (isLeftHovered) Color.LightGray.copy(alpha = 0.4f) else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onPress = {
                                                        isLeftHovered = true
                                                        tryAwaitRelease()
                                                        isLeftHovered = false
                                                    }
                                                )
                                            }) {
                                        Icon(
                                            imageVector = Icons.Filled.ChevronLeft,
                                            contentDescription = "Previous",
                                            tint =colorResource(R.color.HeaderText)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .offset(x = 20.dp)
                                            .size(40.dp)
                                            .background(
                                                color = if (isRightHovered) Color.LightGray.copy(alpha = 0.4f) else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onPress = {
                                                        isRightHovered = true
                                                        tryAwaitRelease()
                                                        isRightHovered = false
                                                    }
                                                )
                                            }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ChevronRight,
                                            contentDescription = "Next",
                                            tint = colorResource(R.color.HeaderText)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.Close),
                                contentColor = colorResource(R.color.CloseText)
                            )
                        ) {
                            Text("Close")
                        }
                    },
                    containerColor = colorResource(R.color.CatBody)
                )
            }}
    }



}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthArtistPage(navController: NavController, artistId: String, artistName: String) {
    var isDetailsLoading by remember { mutableStateOf(true) }
    var isArtworksLoading by remember { mutableStateOf(true) }
    var isSimilar by remember { mutableStateOf(true) }
    var artistDetails by remember { mutableStateOf<ArtistDetail?>(null) }
    var artworks by remember { mutableStateOf<List<Artwork>>(emptyList()) }
    var similarArtists by remember { mutableStateOf<List<SimArtist>?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val favPrefs = context.getSharedPreferences("favs", Context.MODE_PRIVATE)
    val favoriteIds = remember { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(Unit) {
        coroutineScope.launch {

            val details = AuthfetchArtistDetails(artistId)
            //println(details)
            artistDetails = details
            isDetailsLoading=false
            val artworksData = fetchArtworks(artistId)
            artworks = artworksData

            //println(artworksData)
            isArtworksLoading=false
            val similarData = AuthfetchSimilarArtists(artistId)
            println(similarData)
            similarArtists = similarData
            isSimilar=false
            val updatedFavs = favPrefs.getStringSet("favorite_artist_ids", emptySet()) ?: emptySet()
            println("Favourites")
            println(updatedFavs)
            favoriteIds.value = updatedFavs
            println(favoriteIds.value)

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(id = R.color.Header)),
                title = { Text(artistName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.keyboard_backspace),
                            contentDescription = "Back",
                            tint = colorResource(R.color.HeaderText)
                        )
                    }
                },
                actions = {
                    val isFavorite = favoriteIds.value.contains(artistId)
                    IconButton(
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                var artistImage = ""
                                if (!isFavorite) {
                                    val results = fetchAuthSearchResults(artistName)
                                    artistImage = results.find { it.id == artistId }?.imageUrl ?: ""
                                }
                                val endpoint = if (isFavorite) "removeFavorite" else "addFavorite"
                                val url = "https://csciassign4.uw.r.appspot.com/api/$endpoint"
                                val userdet = context.getSharedPreferences("user_details", Context.MODE_PRIVATE)
                                val userEmail = userdet.getString("email", "") ?: ""

                                val requestBody = buildJsonObject {
                                    put("artist_id", JsonPrimitive(artistId))
                                    put("email", JsonPrimitive(userEmail))
                                    if (!isFavorite) {
                                        put("artist_name", JsonPrimitive(artistName))
                                        put("artist_thumbnail", JsonPrimitive(artistImage))
                                    }
                                }.toString()

                                val client = OkHttpClient()
                                val mediaType = "application/json".toMediaType()
                                val request = Request.Builder()
                                    .url(url)
                                    .post(requestBody.toRequestBody(mediaType))
                                    .build()

                                withContext(Dispatchers.IO) {
                                    try {
                                        val response = client.newCall(request).execute()
                                        val mutableFavoriteIds = favoriteIds.value.toMutableSet()
                                        if (response.isSuccessful) {
                                            withContext(Dispatchers.Main) {
                                                val message = if (isFavorite) {
                                                    mutableFavoriteIds.remove(artistId)
                                                    "Removed from favorites"
                                                } else {
                                                    mutableFavoriteIds.add(artistId)
                                                    "Added to favorites"
                                                }

                                                favPrefs.edit()
                                                    .putStringSet("favorite_artist_ids", mutableFavoriteIds)
                                                    .apply()
                                                favoriteIds.value = mutableFavoriteIds

                                                scope.launch {
                                                    snackbarHostState.showSnackbar(message)
                                                }
                                            }
                                        } else {
                                            println("Favorite failed: ${response.message}")
                                        }
                                    } catch (e: Exception) {
                                        println("Favorite exception: ${e.localizedMessage}")
                                    }
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
            )
        }
        ,
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {

                    val tabs = listOf("Details", "Artworks","Similar")
                    val tabIcons = listOf(
                        painterResource(id = R.drawable.information_outline),
                        painterResource(id = R.drawable.account_box_outline),
                        painterResource(id=R.drawable.account_search_outline)
                    )

                    TabRow(selectedTabIndex = selectedTabIndex,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        painter = tabIcons[index],
                                        contentDescription = tab,
                                        modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = tab,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }


                    when (selectedTabIndex) {
                        0  -> {
                            if (isDetailsLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 32.dp),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(strokeWidth = 4.dp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Loading...", style = MaterialTheme.typography.labelSmall)
                                    }                                }
                            } else {
                                AuthArtistDetailsTab(artistDetails)
                            }
                        }
                        1  -> {
                            if (isArtworksLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 32.dp),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(strokeWidth = 4.dp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Loading...", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            } else {
                                AuthArtworksTab(artworks)
                            }
                        }
                        2 -> {
                        if (isSimilar) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(strokeWidth = 4.dp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Loading...", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        } else {
                            AuthSimilar(
                                similarArtists = similarArtists,
                                navController = navController,
                                favPrefs = favPrefs,
                                favoriteIds = favoriteIds,
                                snackbarHostState = snackbarHostState,
                                scope = scope,
                                keyboardController = keyboardController
                            )
                        }
                    }
                    }

            }
        }
    )
}


suspend fun AuthfetchArtistDetails(id: String): ArtistDetail? = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://csciassign4.uw.r.appspot.com/api/artist/$id")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val json = connection.inputStream.bufferedReader().use { it.readText() }
            val obj = JSONObject(json)
            return@withContext ArtistDetail(
                name = obj.optString("name"),
                birthday = obj.optString("birthday"),
                deathday = obj.optString("deathday"),
                nationality = obj.optString("nationality"),
                biography = obj.optString("biography")
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return@withContext null
}

data class SimArtist(val id: String, val name: String, val image: String)

