package com.example.talathiattendance.ui.screens

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.talathiattendance.MyApplication
import com.example.talathiattendance.R
import com.example.talathiattendance.domain.utils.popNavigate
import com.example.talathiattendance.ui.customComposables.VSpace
import com.example.talathiattendance.ui.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val scale = remember {
        androidx.compose.animation.core.Animatable(0.0f)
    }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 0.7f,
            animationSpec = tween(800, easing = {
                OvershootInterpolator(4f).getInterpolation(it)
            })
        )
        delay(1000)

        try {
            (context.applicationContext as MyApplication).sessionManager.getUserPref.collect{userModel->
                if (userModel.uid!!.isEmpty()) navController.popNavigate(Screens.UserSelection)
                else if (userModel.isAdmin)navController.popNavigate(Screens.AdminDashboard)
                else navController.popNavigate(Screens.Dashboard)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.onSurface)
                .padding(40.dp)
                .scale(scale.value),
        )
        VSpace(space = 30.dp)
//        Image(
//            painter = painterResource(id = R.mipmap.ic_launcher),
//            contentDescription = "",
//            alignment = Alignment.Center,
//            modifier = Modifier
//                .fillMaxSize()
//                .clip(RoundedCornerShape(12.dp))
//                .padding(40.dp)
//                .scale(scale.value)
//        )
        Text(
            text = stringResource(id = R.string.app_name),
            textAlign = TextAlign.Center,
            fontSize = 24.sp
        )

    }
}

@Preview
@Composable
fun SplashScreenPrev() {
    SplashScreen(navController = rememberNavController())

}