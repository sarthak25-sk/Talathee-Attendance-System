package com.example.talathiattendance.ui.screens


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavHostController
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.talathiattendance.MyApplication
import com.example.talathiattendance.data.models.AttendanceModel
import com.example.talathiattendance.data.models.LocationDetails
import com.example.talathiattendance.data.models.UserModel
import com.example.talathiattendance.domain.utils.*
import com.example.talathiattendance.ui.customComposables.ContainerColumn
import com.example.talathiattendance.ui.customComposables.ProgressDialog
import com.example.talathiattendance.ui.customComposables.VSpace
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.*
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.time.Duration.Companion.minutes



private var locationCallback: LocationCallback? = null
var fusedLocationClient: FusedLocationProviderClient? = null

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
    navController: NavHostController
) {

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var loggedInUser by remember {
        mutableStateOf(UserModel())
    }

    LaunchedEffect(key1 = true) {

        try {
            (context.applicationContext as MyApplication).sessionManager.getUserPref.collect{userModel->
                loggedInUser = userModel
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    ProgressDialog(showDialog) {
        showDialog = false
    }

    var showLogOutDialog by remember {
        mutableStateOf(false)
    }

    var currentLocation by remember {
        mutableStateOf(LocationDetails())
    }
    var adminLocation by remember {
        mutableStateOf(LocationDetails())
    }
    var isCameraOpen by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = true){
        getGeoCoordinates(scope,context){ showProgress,locationDetails->
            showDialog = showProgress
            adminLocation = locationDetails
        }
    }




    // Safely update the current lambdas when a new one is provided
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            for (lo in p0.locations) {
                // Update UI with location data
                currentLocation = LocationDetails(lo.latitude, lo.longitude)
            }
        }
    }

    var hasImage by rememberSaveable {
        mutableStateOf(false)
    }
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        DateTimeFormatter.ofPattern("EEE dd-MMM-yyyy hh:mm a")
    } else {
        TODO("VERSION.SDK_INT < O")
    }

    var currentTime by remember {
        mutableStateOf(LocalDateTime.now())
    }

    LaunchedEffect(currentTime) {
        delay(1.minutes)
        currentTime = LocalDateTime.now()
    }

    val formattedTime = currentTime.format(formatter)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            hasImage = success
            isCameraOpen = false
            if (hasImage){
                if (imageUri == null){
                    showToast(context,"Invalid Image!")
                }else{
                    addAttendance(loggedInUser,imageUri!!,formattedTime,context){ showProgress,isSuccess->
                        showDialog = showProgress
                        if (isSuccess){
                            showToast(context,"you have successfully punched your Attendance",Toast.LENGTH_LONG)
                        }
                    }
                }

            }else showToast(context,"No Image captured!")
        }
    )

    BackHandler(enabled = isCameraOpen) {
        showToast(context,"please close camera first!")
    }


    val permissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
        )
    )


    // If lifecycleOwner changes, dispose and reset the effect
    DisposableEffect(lifecycleOwner) {
        // Create an observer that triggers our remembered callbacks
        // for lifecycle events
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (context.isAllPermissionsGranted()) {
                        startLocationUpdates()
                    } else permissionsState.launchMultiplePermissionRequest()
                }
                Lifecycle.Event.ON_STOP -> {
                    locationCallback?.let {
                        println("Removing location callback..................")
                        fusedLocationClient?.removeLocationUpdates(it)
                    }

                }
                else -> {}
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            locationCallback?.let {
                println("Removing location callback.....................")
                fusedLocationClient?.removeLocationUpdates(it)
            }
        }
    }

    val distance = SphericalUtil.computeDistanceBetween(
        LatLng(adminLocation.lat!!, adminLocation.long!!),
        LatLng(currentLocation.lat!!, currentLocation.long!!)
    )

    if (showLogOutDialog) {
        AlertDialog(
            onDismissRequest = { showLogOutDialog = false },
            title = {
                Text(text = "Logout")
            },
            text = {
                Text(text = "Are you sure?")
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {

                        (context.applicationContext as MyApplication).sessionManager.clearUserPref()
                        Firebase.auth.signOut()
                        showLogOutDialog = false
                        showToast(context,"success!")
                        navController.navigateUp()

                    }

                }) {
                    androidx.compose.material3.Text(text = "confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogOutDialog = false }) {
                    androidx.compose.material3.Text(text = "cancel")
                }
            }
        )
    }

    ContainerColumn {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Welcome",
                    style = androidx.compose.material.MaterialTheme.typography.h4,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = loggedInUser.name?:"NA",
                    style = androidx.compose.material.MaterialTheme.typography.body1,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }


            IconButton(onClick = {
                showLogOutDialog = true
            }) {
                Icon(
                    Icons.Filled.Logout,
                    contentDescription = "Logout",
                )
            }
        }

        VSpace(space = 20.dp)

        Text(
            text = formattedTime ?: "NA",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(5.dp),
            fontWeight = FontWeight(600),
            fontSize = 20.sp
        )

        VSpace(space = 20.dp)

        Text(
            text =
            if (currentLocation.lat != 0.0)
                "your location\n(${currentLocation.lat},${currentLocation.long})"
            else "click here to start tracking"
            ,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(5.dp)
                .clickable {
                    if (currentLocation.lat != 0.0) {
                        return@clickable
                    }
                    if (context.isAllPermissionsGranted()) {
                        startLocationUpdates()
                    } else permissionsState.launchMultiplePermissionRequest()
                }
            ,
            fontWeight = FontWeight(600),
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = Color.Red
        )

        VSpace(space = 20.dp)

        Button(
            onClick = {
                if (
                    adminLocation.lat == 0.0 ||
                    adminLocation.long == 0.0
                ){
                    showToast(context,"please relaunch the page to get geo location!")
                    return@Button
                }

                showToast(context, "Distance is ${distance.formatDistance()}")
                if (distance < 300.0){
                    showToast(context,"within 300 meters")
                    //launch camera
                    isCameraOpen = true
                    val uri = ComposeFileProvider.getImageUri(context)
                    imageUri = uri
                    cameraLauncher.launch(uri)
                }else showToast(context,"not within 300 meters")
            }, modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally)
                .clip(
                    CircleShape
                )
        ) {
            Text(text = "Punch Attendance")
        }


    }
}


fun addAttendance(
    loggedInUser: UserModel,
    imageUri: Uri,
    currentTime: String?,
    context: Context,
    onProgress: (showProgress: Boolean, isSuccess: Boolean) -> Unit
) {
    onProgress(true, false)
    val database = Firebase.database
    val config = mapOf(
        "cloud_name" to "dkubaxcbo",
        "api_key" to "396162372346477",
        "api_secret" to "ToNfna0j1ynq0U1aTrQ9GY-HmbY"
    )
    val cloudinary = Cloudinary(config)

    Thread {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw Exception("Cannot open input stream from URI.")

            val uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.emptyMap())
            val imageUrl = uploadResult["secure_url"] as String

            val attendanceModel = AttendanceModel(
                uid = loggedInUser.uid,
                name = loggedInUser.name?.lowercase(Locale.ENGLISH)?.trim(),
                attendanceTime = currentTime,
                imageUrl = imageUrl
            ).toMap()

            val key = database.reference.child("attendance").push().key ?: return@Thread
            val ref = database.reference.child("attendance").child(key)

            ref.setValue(attendanceModel).addOnCompleteListener { task ->
                onProgress(false, task.isSuccessful)
                if (!task.isSuccessful) {
                    showToast(context, "Failed to save attendance to Firebase.")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onProgress(false, false)
            showToast(context, "Cloudinary upload failed: ${e.message}")
        }
    }.start()
}


@SuppressLint("MissingPermission")
private fun startLocationUpdates(
//    locationCallback: LocationCallback,
//    fusedLocationClient: FusedLocationProviderClient
) {
    locationCallback?.let {
        println("Starting location callback...................")
        val locationInterval = 10000L
        val locationFastestInterval = 5000L
        val locationMaxWaitTime = 100L

        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationInterval)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(locationFastestInterval)
                .setMaxUpdateDelayMillis(locationMaxWaitTime)
                .build()

        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            it,
            Looper.getMainLooper()
        )
    }
}