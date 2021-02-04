package com.example.bikefm2.ui.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.example.bikefm2.R
import com.mapbox.android.core.location.*
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
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
import kotlinx.android.synthetic.main.fragment_map_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object MapUtils :
    MapboxMap.OnMapLongClickListener{
    private lateinit var accesToken: String
    const val CLICK_SOURCE_NAME = "CLICK_SOURCE_ID"
    const val ROUTE_SOURCE_NAME = "ROUTE_LINE_SOURCE_ID"
    const val CLICK_LAYER_NAME = "CLICK_LAYER"
    const val ROUTE_LAYER_NAME = "ROUTE_LAYER_ID"
    const val DEST_ICON_NAME = "ICON_ID"


    private lateinit var locationEngine: LocationEngine
    private const val REQUEST_PERMISSIONS_REQUEST_CODE: Int = 123
    private lateinit var mapboxMap: MapboxMap

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

    fun setMapboxProperties(context: Context, mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        this.accesToken = context.getString(R.string.mapbox_access_token)
        mapboxMap.setStyle(
            Style.MAPBOX_STREETS
//            Style.Builder().fromUri("mapbox://styles/dianaagl/ckionrj5o3cwo17muljj0whc0")
//            Style.Builder().fromUri("mapbox://styles/dianaagl/ckiorfzij51xq17qvpgyxyk6t")

        ){
            val resId = BitmapUtils.getBitmapFromDrawable(
                ContextCompat.getDrawable(
                context,
                R.drawable.mapbox_marker_icon_default
            ))
            addRouteSourceANdLayer(it)
            addClickSourceAndLayer(it, resId)

            mapboxMap.addOnMapLongClickListener(this)
        }
    }

    fun addClickSourceAndLayer(style: Style, bitMap: Bitmap?) {
        style.addSource(GeoJsonSource(CLICK_SOURCE_NAME))
        style.addLayerAbove(
            SymbolLayer(CLICK_LAYER_NAME, CLICK_SOURCE_NAME)
                .withProperties(
                    PropertyFactory.iconImage(DEST_ICON_NAME)
                ),
            ROUTE_LAYER_NAME
        )
        style.addImage(DEST_ICON_NAME, bitMap!! )
    }

    fun addRouteSourceANdLayer(style: Style) {
        style.addSource(
            GeoJsonSource(
                ROUTE_SOURCE_NAME,
                GeoJsonOptions().withLineMetrics(true)
            )
        )
        style.addLayerBelow(
            LineLayer(ROUTE_LAYER_NAME, ROUTE_SOURCE_NAME)
                .withProperties(
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                    PropertyFactory.lineWidth(6f),
                    PropertyFactory.lineColor("#fff")
                ),
            "mapbox-location-shadow-layer"
        )
    }

    @SuppressLint("MissingPermission")
    fun enableLocationComponent(context: Context,
                                locationListener: LocationEngineCallback<LocationEngineResult>) {
        locationEngine = LocationEngineProvider.getBestLocationEngine(context)

        val locationUpdateInterval = 1000L
        val locationAnimationDuration = 1000
        val locationWaitTime = 5000L
        val request = LocationEngineRequest.Builder(locationUpdateInterval)
            .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
            .setMaxWaitTime(locationWaitTime)
            .build()

        mapboxMap.getStyle {
            mapboxMap.locationComponent.apply {
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
            locationEngine.requestLocationUpdates(
                request,
                locationListener,
                context.mainLooper
            )
            val userLocation = mapboxMap.locationComponent.lastKnownLocation
            if(userLocation !== null) {
                val position = CameraPosition.Builder()
                    .target(LatLng(userLocation.latitude, userLocation.longitude))
                    .zoom(10.0)
                    .tilt(20.0)
                    .build()
                mapboxMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(position),
                    locationAnimationDuration
                )
            }
        }

    }

    private fun showDestination(latLng: LatLng ){
        mapboxMap.getStyle {
            val clickPointSource = it.getSourceAs<GeoJsonSource>(CLICK_SOURCE_NAME)
            clickPointSource?.setGeoJson(Point.fromLngLat(latLng.longitude, latLng.latitude))
        }
    }

    private fun drawRoute(latLng: LatLng ){
        mapboxMap.locationComponent.lastKnownLocation?.let { originLocation ->
            val client = MapboxDirections.builder()
                .origin(Point.fromLngLat(originLocation.longitude, originLocation.latitude))
                .destination(Point.fromLngLat(latLng.longitude, latLng.latitude))
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_CYCLING)
                .accessToken(accesToken)
                .build();

            client.enqueueCall( object: Callback<DirectionsResponse> {
                override fun onResponse(
                    call: Call<DirectionsResponse>,
                    response: Response<DirectionsResponse>
                ) {
                    if(response.body() == null || response.body()?.routes()?.isNotEmpty() == true) {
                        mapboxMap.getStyle {

                            val routeLineSource = it.getSourceAs<GeoJsonSource>(ROUTE_SOURCE_NAME)
                            val currentRoute = response.body()?.routes()?.get(0);
                            if (routeLineSource != null) {
                                currentRoute?.geometry()?.let{ geo ->
                                    routeLineSource.setGeoJson( LineString.fromPolyline( geo,
                                        Constants.PRECISION_6
                                    ))
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    Log.e("direct", t.message)
                }
            })
        }
    }

    override fun onMapLongClick(latLng: LatLng): Boolean {
        showDestination(latLng)
        drawRoute(latLng)
        return true
    }
}