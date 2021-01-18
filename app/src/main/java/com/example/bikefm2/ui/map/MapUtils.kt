package com.example.bikefm2.ui.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object MapUtils {
    private val REQUEST_PERMISSIONS_REQUEST_CODE: Int = 123

//    public fun checkSelfPermissions(permissions: Array<String>, contextCompat: Activity) {
//        val checkPermission = { permissionCode: String ->
//            ActivityCompat.checkSelfPermission(
//            contextCompat, permissionCode
//        ) != PackageManager.PERMISSION_GRANTED }
//
//        if (ActivityCompat.checkSelfPermission(
//                contextCompat,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
//                contextCompat,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            requestPermissionsIfNecessary(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
//        }
//
//    }
    public fun requestPermissionsIfNecessary(permissions: Array<String>, contextCompat: Activity) {
        val permissionsToRequest = java.util.ArrayList<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(contextCompat, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                contextCompat,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

}