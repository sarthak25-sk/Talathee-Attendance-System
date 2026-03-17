package com.example.talathiattendance.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.CoroutineScope
 import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestAllPermissions(context: Context, onAllGranted: () -> Unit) {

    // Camera permission state
    val permissionsState = rememberMultiplePermissionsState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { //Permissions for android 12 and above
            listOf(
//                Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else { //Permissions for below android 12
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    )

    LaunchedEffect(true){
        try {
            permissionsState.launchMultiplePermissionRequest()
            if (permissionsGranted(context)) {
                onAllGranted()
            } else if (!permissionsState.shouldShowRationale) {
                openSettingScreen(context)
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }catch (e: SecurityException) {
            e.printStackTrace()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

}


private fun permissionsGranted(context: Context): Boolean {
    val targetSdkVersion = context.applicationInfo.targetSdkVersion
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                context,
                "no ACCESS_FINE_LOCATION permission",
                Toast.LENGTH_SHORT
            ).show()
//            Log.e(TAG, "no ACCESS_FINE_LOCATION permission, cannot scan")
            false
        } else if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                context,
                "no CAMERA permission",
                Toast.LENGTH_SHORT
            ).show()
//            Log.e(TAG, "no ACCESS_FINE_LOCATION permission, cannot scan")
            false
        } else true
    } else
        if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                context,
                "no ACCESS_COARSE_LOCATION permission",
                Toast.LENGTH_SHORT
            ).show()
//            Logger.e(TAG, "no ACCESS_COARSE_LOCATION permission, cannot scan")
            false
        } else if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                context,
                "no CAMERA permission",
                Toast.LENGTH_SHORT
            ).show()
//            Logger.e(TAG, "no ACCESS_COARSE_LOCATION permission, cannot scan")
            false
        } else true
}

 fun openSettingScreen(context: Context) {
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    val uri = Uri.fromParts("package", context.packageName, null)
    intent.data = uri
    context.startActivity(intent)
}