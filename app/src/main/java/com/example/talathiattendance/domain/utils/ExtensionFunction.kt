package com.example.talathiattendance.domain.utils

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavHostController
import java.io.File
import java.util.regex.Pattern


fun NavHostController.superNavigate(route: String) {
    try {
        navigate(route)
    } catch (e: Exception) {
        showLog(str = e.toString())
    }
}

fun NavHostController.popNavigate(route: String) {
    try {
        popBackStack()
        navigate(route)
    } catch (e: Exception) {
        showLog(str = e.toString())
    }
}


fun String.isValidEmail():Boolean = Pattern.compile(
    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
).matcher(this.trim()).matches()
fun String.isValidPassword():Boolean = this.trim().length >= 6

fun String.isNumeric(): Boolean {
    return try {
        this.toDouble()
        true
    } catch (e: NumberFormatException) {
        false
    }
}

fun showLog(type: Int = Log.DEBUG, str: String) {
    when(type){
        Log.DEBUG -> {Log.d("LogTag", str)}
        Log.ERROR -> {Log.e("LogTag", str)}
        Log.INFO -> {Log.i("LogTag", str)}
    }
}

fun showToast(context: Context,str: String,duration:Int = Toast.LENGTH_SHORT) = Toast.makeText(context,str,duration).show()

fun Context.isAllPermissionsGranted(): Boolean {
    val targetSdkVersion = this.applicationInfo.targetSdkVersion
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                this,
                "no ACCESS_FINE_LOCATION permission, cannot scan",
                Toast.LENGTH_SHORT
            ).show()
//            Log.e(TAG, "no ACCESS_FINE_LOCATION permission, cannot scan")
            false
        } else true
    } else
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                this,
                "no ACCESS_COARSE_LOCATION permission, cannot scan",
                Toast.LENGTH_SHORT
            ).show()
//            Logger.e(TAG, "no ACCESS_COARSE_LOCATION permission, cannot scan")
            false
        } else true
}

fun LocationManager.checkGps(): Boolean {
    val isGpsEnabled = this.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val isNetworkEnabled =
        this.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    if (!isGpsEnabled && !isNetworkEnabled) return false
    return true
}

fun Double.formatDistance():String = String.format("%.2f", this / 1000) + "km"

fun Context.getFileName(uri: Uri): String? = when(uri.scheme) {
    ContentResolver.SCHEME_CONTENT -> getContentFileName(uri)
    else -> uri.path?.let(::File)?.name
}

private fun Context.getContentFileName(uri: Uri): String? = runCatching {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        cursor.moveToFirst()
        return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(cursor::getString)
    }
}.getOrNull()