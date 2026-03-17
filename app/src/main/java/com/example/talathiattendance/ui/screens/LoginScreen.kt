package com.example.talathiattendance.ui.screens

import android.content.Context
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.talathiattendance.MyApplication
import com.example.talathiattendance.data.models.UserModel
import com.example.talathiattendance.ui.customComposables.ProgressDialog
import com.example.talathiattendance.ui.customComposables.VSpace
import com.example.talathiattendance.ui.navigation.Screens
import com.example.talathiattendance.domain.utils.*
import com.example.talathiattendance.ui.theme.md_theme_dark_onSurface
import com.example.talathiattendance.ui.theme.md_theme_dark_surface
import com.example.talathiattendance.ui.theme.md_theme_light_onSurface
import com.example.talathiattendance.ui.theme.md_theme_light_surface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController, userType: String
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
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        if (userType != "admin") {
            Box(modifier = Modifier.fillMaxSize()) {
                ClickableText(
                    text = AnnotatedString("Sign up here"),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(20.dp),
                    onClick = {
                        navController.superNavigate(Screens.Registration)
                    },
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Default,
                        textDecoration = TextDecoration.Underline,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }


        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val email = remember { mutableStateOf("") }
            val password = remember { mutableStateOf("") }

            Text(
                text = "Login",
                style = TextStyle(
                    fontSize = 40.sp, fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            VSpace(space = 40.dp)

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
                        if (email.value.isValidEmail() && password.value.isValidPassword()) {
                            loginUser(
                                email.value,
                                password.value,
                                context,
                                navController,
                                scope,
                                userType
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
                    androidx.compose.material3.Text(text = "Login")
                }
            }

        }
    }
}

fun loginUser(
    email: String,
    password: String,
    context: Context,
    navController: NavHostController,
    scope: CoroutineScope,
    userType: String,
    onProgress: (show: Boolean) -> Unit
) {
    onProgress(true)
    val auth: FirebaseAuth = Firebase.auth
    scope.launch {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    try {
                        if (!task.isSuccessful) {
                            onProgress(false)
                            showLog(Log.ERROR, "signInWithEmailAndPass failure : ${task.exception}")
                            showToast(context, "Authentication failed.")
                            return@addOnCompleteListener
                        }

                        showLog(str = "createUserWithEmail:success")
                        auth.currentUser?.let { firebaseUser ->
                            val database = Firebase.database
                            val myRef = database.getReference("users").child(firebaseUser.uid)

                            myRef.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    try {
                                        val userModel = snapshot.getValue<UserModel>()
                                        userModel?.isAdmin =
                                            snapshot.child("isAdmin").value.toString().toBoolean()

                                        onProgress(false)
                                        userModel?.let {
                                            if (it.isAdmin && userType != "admin") {
                                                showToast(context, "Unauthorised access!")
                                                Firebase.auth.signOut()
                                                return
                                            } else if (!it.isAdmin && userType == "admin") {
                                                showToast(context, "Unauthorised access!")
                                                Firebase.auth.signOut()
                                                return
                                            }

                                            scope.launch {
                                                (context.applicationContext as MyApplication).sessionManager.setUserPref(
                                                    it
                                                )
                                            }
                                            showToast(context, "success!")
                                            navController.popNavigate(
                                                if (it.isAdmin)
                                                Screens.AdminDashboard
                                            else Screens.Dashboard
                                            )
                                        }
                                    } catch (e: Exception) {
                                        onProgress(false)
                                        e.printStackTrace()
                                    }

                                }

                                override fun onCancelled(error: DatabaseError) {
                                    onProgress(false)
                                    showLog(Log.ERROR, "Failed to read value ${error.message}")
                                }

                            })

                        }


                    } catch (e: Exception) {
                        onProgress(false)
                        e.printStackTrace()
                    }

                }
        } catch (e: Exception) {
            onProgress(false)
            e.printStackTrace()
        }

    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun LoginPreview() {
    LoginScreen(rememberNavController(), "talathi")
}