package com.example.talathiattendance.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.talathiattendance.MyApplication
import com.example.talathiattendance.data.models.AttendanceModel
import com.example.talathiattendance.data.models.LocationDetails
import com.example.talathiattendance.domain.utils.*
import com.example.talathiattendance.ui.customComposables.ContainerColumn
import com.example.talathiattendance.ui.customComposables.ProgressDialog
import com.example.talathiattendance.ui.customComposables.VSpace
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun AdminDashboardScreen(
    navController: NavHostController
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    ProgressDialog(showDialog) {
        showDialog = false
    }

    var showLogOutDialog by remember {
        mutableStateOf(false)
    }

    var latitudeText by remember {
        mutableStateOf(TextFieldValue(""))
    }

    var longitudeText by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var searchText by remember {
        mutableStateOf(TextFieldValue(""))
    }

    var adminLocation by remember {
        mutableStateOf(LocationDetails())
    }
    LaunchedEffect(key1 = true){
        getGeoCoordinates(scope,context){ showProgress,locationDetails->
            showDialog = showProgress
            latitudeText = TextFieldValue(locationDetails.lat.toString())
            longitudeText = TextFieldValue(locationDetails.long.toString())
        }
    }
    val attendanceList = remember { mutableStateListOf<AttendanceModel>()}

    if (
        latitudeText.text.isNotEmpty() &&
        longitudeText.text.isNotEmpty() &&
        latitudeText.text.isNumeric() &&
        longitudeText.text.isNumeric()
    ){
        adminLocation = LocationDetails(latitudeText.text.toDouble(),longitudeText.text.toDouble())
    }

    if (showLogOutDialog) {
        AlertDialog(
            onDismissRequest = { showLogOutDialog = false },
            title = {
                androidx.compose.material.Text(text = "Logout")
            },
            text = {
                androidx.compose.material.Text(text = "Are you sure?")
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
                    Text(text = "confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogOutDialog = false }) {
                    Text(text = "cancel")
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
            androidx.compose.material.Text(
                text = "Welcome",
                style = MaterialTheme.typography.h4,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.GpsFixed, contentDescription = "")
            androidx.compose.material.Text(text = "Enter Geo-location coordinates")
        }

        VSpace(space = 5.dp)
        TextField(
            value = latitudeText,
            onValueChange = { newText ->
                latitudeText = newText
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { androidx.compose.material.Text(text = "Latitude") },
            placeholder = { androidx.compose.material.Text(text = "Enter Latitude value (With Decimal)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        VSpace(space = 5.dp)
        TextField(
            value = longitudeText,
            onValueChange = { newText ->
                longitudeText = newText
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { androidx.compose.material.Text(text = "Longitude") },
            placeholder = { androidx.compose.material.Text(text = "Enter Longitude value (With Decimal)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        VSpace(space = 20.dp)

        androidx.compose.material3.Button(
            onClick = {
                if (
                    latitudeText.text.isEmpty() &&
                    longitudeText.text.isEmpty() &&
                    !latitudeText.text.isNumeric() &&
                    !longitudeText.text.isNumeric()
                ){
                    showToast(context,"please provide valid coordinates!")
                    return@Button
                }

                updateAdminGeoCoordinates(context,adminLocation,scope){ showDialog = it}
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .height(50.dp)
                .align(Alignment.End)
                .padding(horizontal = 16.dp)
        ) {
            Text(text = "Update")
        }
        VSpace(20.dp)
        TextField(
            value = searchText,
            onValueChange = { newText ->
                searchText = newText
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (searchText.text.isEmpty()){
                    showToast(context,"please enter talathi name first!")
                    return@KeyboardActions
                }
                scope.launch {
                    getAttendance(context,searchText.text.lowercase(Locale.ENGLISH).trim()){ showProgress, list ->
                        showDialog = showProgress
                        attendanceList.clear()
                        if (list.isNotEmpty()){
                            attendanceList.addAll(list)
                        }
                    }
                }
            }),
            label = { androidx.compose.material.Text(text = "Talathi Name") },
            placeholder = { androidx.compose.material.Text(text = "Enter talathi name to get attendance records") },
            trailingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search",
                    modifier = Modifier.clickable {
                        if (searchText.text.isEmpty()){
                            showToast(context,"please enter talathi name first!")
                            return@clickable
                        }
                        scope.launch {
                            getAttendance(context,searchText.text.lowercase(Locale.ENGLISH).trim()){ showProgress, list ->
                                showDialog = showProgress
                                attendanceList.clear()
                                if (list.isNotEmpty()){
                                    attendanceList.addAll(list)
                                }
                            }
                        }
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        VSpace(space = 20.dp)


        if (attendanceList.isNotEmpty()){
            Text(
                text = "Attendance Records",
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            VSpace(20.dp)
            LazyColumn{
                items(attendanceList){
                    AttendanceListItem(it,context)
                }
            }
        }


    }
}

@Composable
fun AttendanceListItem(item: AttendanceModel, context: Context) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier
                .fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = item.name?:"NA", fontSize = 18.sp)
                Text(text = "Punch time: ${item.attendanceTime?:"NA"}", fontSize = 14.sp)
            }

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.imageUrl)
                    .crossfade(true)
                    .build(),
//                placeholder = painterResource(R.drawable.placeholder),
                contentDescription = "Selfie image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.clip(CircleShape).size(50.dp)
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(item.imageUrl)
                        context.startActivity(intent)
                    }
            )
        }

    }
}

fun getGeoCoordinates(
    scope: CoroutineScope,
    context: Context,
    onProgress:(show:Boolean,locationDetails:LocationDetails)->Unit
) {
    onProgress(true, LocationDetails())
    val database = Firebase.database

    val myRef = database.reference
    myRef.child("geoCoordinates").addValueEventListener(object :ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            try {
                val locationDetails = snapshot.getValue<LocationDetails>()
                if (locationDetails == null){
                    onProgress(false,LocationDetails())
                }else onProgress(false,locationDetails)
            }catch (e:Exception) {
                e.printStackTrace()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            onProgress(false,LocationDetails())
            showToast(context, error.message)
        }

    })
}

fun getAttendance(
    context: Context,
    searchText: String,
    onProgress: (showProgress: Boolean, list: List<AttendanceModel>) -> Unit
) {
    onProgress(true, emptyList())
    val database = Firebase.database

    val myRef = database.reference
    val query:Query = myRef.child("attendance")
        .orderByChild("name")
        .startAt(searchText)
        .endAt(searchText+"\uf8ff")

    query.addListenerForSingleValueEvent(object:ValueEventListener{

        override fun onDataChange(snapshot: DataSnapshot) {
            try {
                if (!snapshot.exists()){
                    onProgress(false, emptyList())
                    showToast(context, "no records found!")
                }else{
                    showLog(str = "${snapshot.getValue<AttendanceModel>()}")
                    val tempList = mutableListOf<AttendanceModel>()
                    snapshot.children.forEach {dataSnapshot ->
                        val attendanceModel = dataSnapshot.getValue<AttendanceModel>()
                        attendanceModel?.let {
                            tempList.add(it)
                        }
                    }
                    onProgress(false, tempList)
                }

            }catch (e:Exception) {
                e.printStackTrace()
                onProgress(false, emptyList())
                showToast(context, e.message.toString())
            }
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }

    })

//    myRef
//        .orderByChild("name")
//        .equalTo(searchText.trim(),"name")
//        .get().addOnCompleteListener { taskSnaps->
//        try {
//            if (!taskSnaps.isSuccessful){
//                onProgress(false, emptyList())
//                showToast(context, "no records found!")
//            }else{
//                showLog(str = "${taskSnaps.result}")
//                if (taskSnaps.result.exists()){
//                    val tempList = mutableListOf<AttendanceModel>()
//                    taskSnaps.result.children.forEach {dataSnapshot ->
//                        val attendanceModel = dataSnapshot.getValue<AttendanceModel>()
//                        attendanceModel?.let {
//                            tempList.add(it)
//                        }
//                    }
//                    onProgress(false, tempList)
//                }else{
//                    onProgress(false, emptyList())
//                    showToast(context, "no attendance data found!")
//                }
//            }
//
//        }catch (e:Exception) {
//            e.printStackTrace()
//            onProgress(false, emptyList())
//            showToast(context, e.message.toString())
//        }
//    }


}

fun updateAdminGeoCoordinates(
    context: Context,
    adminLocation: LocationDetails,
    scope: CoroutineScope,
    onProgress:(show:Boolean)->Unit
) {
    onProgress(true)
    val database = Firebase.database

    val myRef = database.reference
    myRef.child("geoCoordinates")
        .setValue(adminLocation)
        .addOnSuccessListener {
            showToast(context,"Success!")
            onProgress(false)
        }.addOnFailureListener {
            showToast(context,it.message.toString())
            onProgress(false)
        }
}
