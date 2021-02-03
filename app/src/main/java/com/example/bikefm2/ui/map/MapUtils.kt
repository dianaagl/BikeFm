package com.example.bikefm2.ui.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.example.bikefm2.R
import com.mapbox.android.core.location.*
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.mapbox.navigation.core.MapboxNavigation

object MapUtils {
    private lateinit var locationEngine: LocationEngine
    private val REQUEST_PERMISSIONS_REQUEST_CODE: Int = 123

//    public fun checkPermissions(permissions: Array<String>, contextCompat: Activity) {
//        val checkPermission = { permissionCode: String ->
//            ActivityCompat.checkSelfPermission(
//            contextCompat, permissionCode
//        ) != PackageManager.PERMISSION_GRANTED }
//
//         {
//            requestPermissionsIfNecessary(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
//        }
//
//    }
    fun requestPermissionsIfNecessary(permissions: Array<String>, contextCompat: Activity) {
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
            requestPermissions(contextCompat,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    fun createMapInstance(context: Context, accessToken: String): MapboxNavigation {
        val mapboxNavigationOptions = MapboxNavigation
            .defaultNavigationOptionsBuilder(context, accessToken)
            .build()
        val mapboxNavigation = MapboxNavigation(mapboxNavigationOptions)
        Mapbox.getInstance(context, accessToken)
        return mapboxNavigation
    }

    fun setMapboxProperties(context: Context, mapboxMap: MapboxMap, listener: MapboxMap.OnMapLongClickListener) {
        val originColor = "#32a852" // Green
        val destinationColor = "#F84D4D" // Red
        mapboxMap.setStyle(
            Style.MAPBOX_STREETS
//            Style.Builder().fromUri("mapbox://styles/dianaagl/ckionrj5o3cwo17muljj0whc0")
//            Style.Builder().fromUri("mapbox://styles/dianaagl/ckiorfzij51xq17qvpgyxyk6t")

        ){
            // Add the click and route sources
            it.addSource(GeoJsonSource("CLICK_SOURCE"))
            it.addSource(
                GeoJsonSource(
                    "ROUTE_LINE_SOURCE_ID",
                    GeoJsonOptions().withLineMetrics(true)
                )
            )

            // Add the destination marker image
            it.addImage(
                "ICON_ID",
                BitmapUtils.getBitmapFromDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.mapbox_marker_icon_default
                    )
                )!!
            )

            // Add the LineLayer below the LocationComponent's bottom layer, which is the
            // circular accuracy layer. The LineLayer will display the directions route.
            it.addLayerBelow(
                LineLayer("ROUTE_LAYER_ID", "ROUTE_LINE_SOURCE_ID")
                    .withProperties(
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                        PropertyFactory.lineWidth(6f),
                        PropertyFactory.lineGradient(
                            Expression.interpolate(
                                Expression.linear(),
                                Expression.lineProgress(),
                                Expression.stop(
                                    0f,
                                    Expression.color(Color.parseColor(originColor))
                                ),
                                Expression.stop(
                                    1f,
                                    Expression.color(Color.parseColor(destinationColor))
                                )
                            )
                        )
                    ),
                "mapbox-location-shadow-layer"
            )

            // Add the SymbolLayer to show the destination marker
            it.addLayerAbove(
                SymbolLayer("CLICK_LAYER", "CLICK_SOURCE")
                    .withProperties(
                        PropertyFactory.iconImage("ICON_ID")
                    ),
                "ROUTE_LAYER_ID"
            )

            mapboxMap.addOnMapLongClickListener(listener)
        }
    }

    @SuppressLint("MissingPermission")
    fun enableLocationComponent(mapboxMap: MapboxMap,
                                        context: Context,
                                        locationListener: LocationEngineCallback<LocationEngineResult>) {
        locationEngine = LocationEngineProvider.getBestLocationEngine(context)

        val locationUpdateInterval = 1000L
        val locationAnimationDuration = 1000
        val locationWaitTime = 5000L
        val request = LocationEngineRequest.Builder(locationUpdateInterval)
            .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
            .setMaxWaitTime(locationWaitTime)
            .build()

        mapboxMap?.getStyle {
            mapboxMap?.locationComponent?.apply {
                activateLocationComponent(
                    LocationComponentActivationOptions.builder(
                        context,
                        it
                    )
                        .build()
                )
                isLocationComponentEnabled = true
                cameraMode = CameraMode.TRACKING
                renderMode = RenderMode.COMPASS
            }
            locationEngine?.requestLocationUpdates(
                request,
                locationListener,
                context.mainLooper
            )
            val userLocation = mapboxMap?.locationComponent?.lastKnownLocation
            if(userLocation !== null) {
                val position = CameraPosition.Builder()
                    .target(LatLng(userLocation.latitude, userLocation.longitude))
                    .zoom(10.0)
                    .tilt(20.0)
                    .build()
                mapboxMap?.animateCamera(
                    CameraUpdateFactory.newCameraPosition(position),
                    locationAnimationDuration
                )
            }
        }

    }

}