package com.example.artsyproj4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject
import android.content.Context
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.text.input.PasswordVisualTransformation
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var isLoading by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }


    suspend fun loginUser(context: Context, email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = NetworkClient.getClient(context)

                val json = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = json.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url("https://csciassign4.uw.r.appspot.com/login")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: return@withContext false
                        val jsonResponse = JSONObject(responseBody)
                        val userJson = jsonResponse.getJSONObject("user")

                        val prefs = context.getSharedPreferences("user_details", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putString("fullName", userJson.getString("fullName"))
                            putString("email", userJson.getString("email"))
                            putString("profileImageUrl", userJson.getString("profileImageUrl"))
                            putString("token", jsonResponse.getString("token"))
                            apply()
                        }

                        return@withContext true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext false
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorResource(id = R.color.Header)),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_left),
                                contentDescription = "Back",
                                tint = colorResource(id = R.color.HeaderText)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Login",
                            fontSize = 22.sp,
                            color = colorResource(id = R.color.HeaderText)
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color.Gray,
                    contentColor = Color.White
                )
            }
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .imePadding(),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailTouched) {
                                emailError = email.isBlank()
                            }

                        },
                        label = { Text("Email") },
                        isError = emailError,
                        singleLine = true,
                        shape = RoundedCornerShape(6.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    emailTouched = true
                                    emailError = email.isBlank()
                                } else {
                                    if (emailTouched) {
                                        emailError = email.isBlank()
                                    }
                                }
                            }


                    )
                    if (emailError) {
                        val errorMessage = when {
                            email.isBlank() -> "Email cannot be empty"
                            else -> "Invalid email format"
                        }

                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordTouched) {
                                passwordError = password.isBlank()
                            }
                        },
                        label = { Text("Password") },
                        isError = passwordError,
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(6.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    passwordTouched = true
                                    passwordError = password.isBlank()
                                } else {
                                    if (passwordTouched) {
                                        passwordError = password.isBlank()
                                    }
                                }
                            }
                    )

                    if (passwordError) {
                        Text(
                            text = "Password cannot be empty",
                            color = Color.Red,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            emailTouched = true
                            passwordTouched = true
                            //onclick, checking for email pattern correctness
                            emailError = email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                            passwordError = password.isBlank()
                            //on both fields being valid, proceeding with login
                            if (!emailError && !passwordError) {
                                isLoading = true
                                errorMessage = ""

                                scope.launch {
                                    val success = loginUser(context, email, password)

                                    if (success) {
                                        snackbarHostState.showSnackbar("Logged in successfully")
                                        navController.navigate("authhome")
                                    } else {
                                        isLoading = false
                                        errorMessage = "Username or password is incorrect"
                                    }
                                }

                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Login")
                        }
                    }
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Don't have an account yet? ")
                        Text(
                            text = "Register",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable {
                                navController.navigate("register")
                            }
                        )
                    }


                }
            }
        }
    )}