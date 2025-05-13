package com.example.artsyproj4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.ui.platform.LocalFocusManager
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType

import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPage(navController: NavController) {
    val context = LocalContext.current

    data class RegistrationResult(
        val success: Boolean,
        val fullName: String = "",
        val email: String = "",
        val profileImageUrl: String = "",
        val token: String = ""
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var fullName by remember { mutableStateOf("") }
    var fullNameTouched by remember { mutableStateOf(false) }
    var fullNameError by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var emailTouched by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var emailFormatError by remember { mutableStateOf(false) }
    var emailExistsError by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var passwordTouched by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val allValid = !fullNameError && !emailError && !passwordError &&
            fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank()

    val scope = rememberCoroutineScope()
    suspend fun registerUser(context: Context, fullName: String, email: String, password: String): RegistrationResult {
        return withContext(Dispatchers.IO) {
            try {
                val client = NetworkClient.getClient(context)
                val url = "https://csciassign4.uw.r.appspot.com/register"

                val jsonBody = JSONObject().apply {
                    put("fullName", fullName)
                    put("email", email)
                    put("password", password)
                }

                val body = jsonBody.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url(url).post(body).build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful && response.code == 201) {
                    val responseBody = response.body?.string() ?: return@withContext RegistrationResult(false)
                    val jsonResponse = JSONObject(responseBody)
                    val userJson = jsonResponse.getJSONObject("user")

                    return@withContext RegistrationResult(
                        success = true,
                        fullName = userJson.getString("fullName"),
                        email = userJson.getString("email"),
                        profileImageUrl = userJson.getString("profileImageUrl"),
                        token = jsonResponse.getString("token")
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext RegistrationResult(success = false)
        }
    }


    suspend fun checkEmailExists(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://csciassign4.uw.r.appspot.com/check-email")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonInput = JSONObject()
                jsonInput.put("email", email)

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(jsonInput.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                return@withContext when (responseCode) {
                    HttpURLConnection.HTTP_OK -> false
                    HttpURLConnection.HTTP_CONFLICT -> true
                    else -> {
                        println("Unexpected response code: $responseCode")
                        false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext false
            }
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
                            text = "Register",
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
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            if (fullNameTouched) {
                                fullNameError = fullName.isBlank() }
                        },
                        label = { Text("Full Name") },
                        isError = fullNameError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    fullNameTouched = true
                                    fullNameError = fullName.isBlank()
                                }
                            }
                    )
                    if (fullNameTouched && fullNameError) {
                        Text(
                            text = "Full name cannot be empty",
                            color = Color.Red,
                            fontSize = 15.sp
                        )
                    }
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailFormatError) {
                                emailFormatError = false
                            }
                            if (emailTouched) {
                                emailError = email.isBlank()
                            }
                            if (emailExistsError) {
                                emailExistsError = false
                            }
                        },
                        label = { Text("Email") },
                        isError = emailError || emailFormatError || emailExistsError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    emailTouched = true
                                    emailError = email.isBlank()
                                }
                            }
                    )
                    if (emailTouched && emailError) {
                        Text(
                            text = "Email cannot be empty",
                            color = Color.Red,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (emailTouched && emailFormatError) {
                        Text(
                            text = "Invalid email format",
                            color = Color.Red,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (emailTouched && emailExistsError) {
                        Text(
                            text = "Email already exists",
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
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    passwordTouched = true
                                    passwordError = password.isBlank()
                                }
                            }
                    )
                    if (passwordTouched && passwordError) {
                        Text(
                            text = "Password cannot be empty",
                            color = Color.Red,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            fullNameTouched = true
                            emailTouched = true
                            passwordTouched = true
                            fullNameError = fullName.isBlank()
                            emailError = email.isBlank()
                            passwordError = password.isBlank()
                            emailFormatError = !Patterns.EMAIL_ADDRESS.matcher(email).matches()

                            if (!fullNameError && !emailError && !passwordError && !emailFormatError) {
                                loading = true

                                scope.launch {
                                    val emailExists = checkEmailExists(email)
                                    if (emailExists) {
                                        emailExistsError = true
                                        loading=false
                                    } else {
                                        println("Proceed with registration!")

                                        val registrationResponse = registerUser(context,fullName, email, password)
                                        println(registrationResponse)
                                        if (registrationResponse.success) {
                                            snackbarHostState.showSnackbar("Registered successfully")
                                            val prefs = context.getSharedPreferences("user_details", Context.MODE_PRIVATE)
                                            with(prefs.edit()) {
                                                putString("fullName", registrationResponse.fullName)
                                                putString("email", registrationResponse.email)
                                                putString("profileImageUrl", registrationResponse.profileImageUrl)
                                                putString("token", registrationResponse.token)
                                                apply()
                                            }
                                            navController.navigate(
                                                "authhome"                                            )
                                        }else {
                                            loading = false
                                        }
                                    }

                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Register")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Already have an account yet? ")
                        Text(
                            text = "Login",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable {
                                navController.navigate("login")
                            }
                        )
                    }


                }
            }
        }
    )
}
