package com.example.talathiattendance.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.talathiattendance.data.models.UserModel
import com.example.talathiattendance.domain.utils.isValidEmail
import com.example.talathiattendance.domain.utils.isValidPassword
import com.example.talathiattendance.domain.utils.showLog
import com.example.talathiattendance.domain.utils.showToast
import com.example.talathiattendance.ui.customComposables.ProgressDialog
import com.example.talathiattendance.ui.customComposables.VSpace
import com.example.talathiattendance.ui.theme.md_theme_dark_onSurface
import com.example.talathiattendance.ui.theme.md_theme_dark_surface
import com.example.talathiattendance.ui.theme.md_theme_light_onSurface
import com.example.talathiattendance.ui.theme.md_theme_light_surface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavHostController
) {
    var context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    ProgressDialog(showDialog) {
        showDialog = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) md_theme_dark_surface else md_theme_light_surface),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val name = remember { mutableStateOf("") }
            val email = remember { mutableStateOf("") }
            val password = remember { mutableStateOf("") }

            Text(
                text = "Register",
                style = TextStyle(
                    fontSize = 40.sp, fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold
                ),
                color = if (isSystemInDarkTheme()) md_theme_dark_onSurface else md_theme_light_onSurface
            )

            VSpace(space = 40.dp)

            TextField(
                modifier = Modifier.fillMaxWidth(),
                label = { androidx.compose.material3.Text(text = "Name") },
                value = name.value,
                singleLine = true,
                onValueChange = { name.value = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                )
            )

            VSpace(20.dp)
            TextField(
                modifier = Modifier.fillMaxWidth(),
                label = { androidx.compose.material3.Text(text = "Email") },
                value = email.value,
                singleLine = true,
                onValueChange = { email.value = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
                )
            )

            VSpace(20.dp)
            TextField(modifier = Modifier.fillMaxWidth(),
                label = { androidx.compose.material3.Text(text = "Password") },
                value = password.value,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                ),
                onValueChange = { password.value = it })

            VSpace(40.dp)
            Box(
                modifier = Modifier
                    .padding(0.dp, 0.dp)
                    .fillMaxWidth()
            ) {
                androidx.compose.material3.Button(
                    onClick = {
                        if (name.value.isNotEmpty() && email.value.isValidEmail() && password.value.isValidPassword()) {
                            registerUser(
                                name.value,
                                email.value,
                                password.value,
                                context,
                                navController,
                                scope
                            ) {
                                showDialog = it
                            }
                        } else {
                            showToast(context, "please enter valid credentials!")
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    androidx.compose.material3.Text(text = "Register")
                }
            }

        }
    }
}

fun registerUser(
    name: String,
    email: String,
    pass: String,
    context: Context,
    navController: NavHostController,
    scope: CoroutineScope, onProgress:(show:Boolean)->Unit
) {
    onProgress(true)
    val auth: FirebaseAuth = Firebase.auth
    scope.launch {
        try {
            auth.createUserWithEmailAndPassword(email,pass)
                .addOnCompleteListener { task->
                    try {
                        if (!task.isSuccessful) {
                            onProgress(false)
                            showLog(Log.ERROR, "signInWithEmailAndPass failure : ${task.exception}")
                            showToast(context, "Authentication failed.")
                            return@addOnCompleteListener
                        }

                        val user = auth.currentUser
                        user?.let {
                            val database = Firebase.database
                            val myRef = database.getReference("users").child(it.uid)
                            val userModel = UserModel(it.uid, name, email,false)
                            val userValues = userModel.toMap()
                            myRef.setValue(userValues)

                            onProgress(false)
                            showToast(context,"success!")
                            navController.navigateUp()
                        }

                    } catch (e: Exception) {
                        onProgress(false)
                        e.printStackTrace()
                    }
                }

        }catch (e:Exception){
            onProgress(false)
            e.printStackTrace()
        }

    }
}

@Preview
@Composable
fun RegisterPreview() {
    RegisterScreen(rememberNavController())
}