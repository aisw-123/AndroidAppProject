package com.example.artsyproj4
import coil.compose.AsyncImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
//function to clean the biography
//removing the line break hypens
fun cleanText(raw: String): String {
    return raw.replace("\u0096", " - ")
        .replace(Regex("-\\s+"), "")
        .trim()
}

//Displayng the Details tab
@Composable
fun ArtistDetailsTab(artistDetails: ArtistDetail?) {
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
            color = colorResource(R.color.HeaderText) ,
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
//Displaying the Artworks tab
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ArtworksTab(artworks: List<Artwork>) {
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
                ) {//the no Artworks block
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
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis
                        )

                        Button(
                            onClick = {
                                isLoading = true
                                categories = emptyList()
                                showDialog = true
                                selectedArtworkTitle = artwork.title
                                println("Button clicked!")
                                //getiing the categories after the button is clicked
                                scope.launch {
                                    kotlinx.coroutines.delay(500)
                                    val result = fetchCategories(artwork.id)
                                    categories = result
                                    println("Categories fetched: $categories")
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
        //displaying the categories in infinite carousel
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
                                                    val rawDescription = category.description ?: ""
                                                    val formattedDescription = rawDescription.replace(Regex("\\[([^\\]]+)]\\([^)]*\\)"), "$1")
                                                        .replace("_", "")

                                                    Text(
                                                        text = formattedDescription,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        textAlign = TextAlign.Start
                                                    )

                                                    /*Text(
                                                        text = category.description ?: "",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        textAlign = TextAlign.Start
                                                    )*/
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
fun ArtistPage(navController: NavController, artistId: String, artistName: String) {
    var isDetailsLoading by remember { mutableStateOf(true) }
    var isArtworksLoading by remember { mutableStateOf(true) }
    var artistDetails by remember { mutableStateOf<ArtistDetail?>(null) }
    var artworks by remember { mutableStateOf<List<Artwork>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableStateOf(0) }
// on page load getting all the data for both tabs
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val details = fetchArtistDetails(artistId)
            println(details)
            artistDetails = details
            isDetailsLoading = false
            val artworksData = fetchArtworks(artistId)
            println(artworksData)

            artworks = artworksData
            isArtworksLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(R.color.Header))
            )
        },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                    val tabs = listOf("Details", "Artworks")
                    val tabIcons = listOf(
                        painterResource(id = R.drawable.information_outline),
                        painterResource(id = R.drawable.account_box_outline)
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
                    }//switching for different tabs according to the selected
                    when (selectedTabIndex) {
                        0 -> {
                            if (isDetailsLoading) {
                                Box(
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

                            } else {
                                ArtistDetailsTab(artistDetails)
                            }
                        }
                        1  -> {
                            if (isArtworksLoading) {
                                Box(
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
                            } else {
                                ArtworksTab(artworks)
                            }
                        }
                    }

            }
        }
    )
}
suspend fun fetchCategories(artworkId: String): List<Category> {
    println("Fetching categories for artwork: $artworkId")

    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://csciassign4.uw.r.appspot.com/api/categories/$artworkId")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                println("Response: $response")
                val categoriesJson = JSONObject(response).getJSONArray("categories")
                List(categoriesJson.length()) { i ->
                    val obj = categoriesJson.getJSONObject(i)
                    val name = obj.getString("name")
                    val image = obj.getString("image")
                    val description = obj.optString("description", "")

                    Category(name, image, description)
                }
            } else {
                println("EMPTY")
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

suspend fun fetchArtistDetails(id: String): ArtistDetail? = withContext(Dispatchers.IO) {
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

suspend fun fetchArtworks(artistId: String): List<Artwork> = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://csciassign4.uw.r.appspot.com/api/artworks/$artistId")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val json = connection.inputStream.bufferedReader().use { it.readText() }
            val obj = JSONObject(json)
            val artworksArray = obj.getJSONArray("artworks")
            return@withContext List(artworksArray.length()) { i ->
                val a = artworksArray.getJSONObject(i)
                Artwork(
                    id = a.getString("id"),
                    title = a.getString("title"),
                    date = a.getString("date"),
                    image = a.getString("image")
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return@withContext emptyList()
}

data class ArtistDetail(val name: String, val birthday: String,val deathday: String,val nationality: String,val biography: String)
data class Artwork(val id: String,val title: String,val date: String, val image: String)
data class Category(val name: String, val image: String,val description: String)

