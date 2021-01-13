package com.example.bikefm2.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color.parseColor
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.SearchEvent
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bikefm2.R
import com.example.bikefm2.ui.login.LoginActivity
import com.example.bikefm2.ui.search.SearchActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
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
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND
import com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.mapbox.navigation.base.internal.extensions.applyDefaultParams
import com.mapbox.navigation.base.internal.extensions.coordinates
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottom_panel.*
import kotlinx.android.synthetic.main.layout_map.*
import kotlinx.android.synthetic.main.map_content.*
import timber.log.Timber


/**
 * This activity shows how to combine the Mapbox Maps SDK with
 * ONLY the Navigation SDK. There is no Navigation UI SDK code
 * of any kind in this example.
 */
@AndroidEntryPoint
class MapActivity :
    AppCompatActivity(),
    OnMapReadyCallback,
    MapboxMap.OnMapLongClickListener {

    private var locationObserverCallback: UserLocationObserver? = null
    private val REQUEST_PERMISSIONS_REQUEST_CODE: Int = 1
    private var mapboxNavigation: MapboxNavigation? = null
    private var mapboxMap: MapboxMap? = null
    private val ORIGIN_COLOR = "#32a852" // Green
    private val DESTINATION_COLOR = "#F84D4D" // Red

    private lateinit var mapViewModel: MapViewModel
    private var locationEngine: LocationEngine? = null

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            actionBar?.title = result.data?.getStringExtra("displayName");
            mapView.getMapAsync(this)
            locationEngine = LocationEngineProvider.getBestLocationEngine(this)
          }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mapboxNavigationOptions = MapboxNavigation
            .defaultNavigationOptionsBuilder(this, getString(R.string.mapbox_access_token))
            .build()

        mapboxNavigation = MapboxNavigation(mapboxNavigationOptions)
        Mapbox.getInstance(applicationContext, getString(R.string.mapbox_access_token))

        setContentView(R.layout.layout_map)
        setActionBar(findViewById(R.id.my_toolbar))
        mapView.onCreate(savedInstanceState)

        mapViewModel = ViewModelProvider(this).get<MapViewModel>(MapViewModel::class.java)
        mapViewModel.getUser()

        val friendsAdapter = FriendAdapter()
        val listView = findViewById<RecyclerView>(R.id.friends_list)
        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = friendsAdapter

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionsIfNecessary(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
        val bottomNavigationView =
            findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        val bottomSheet = findViewById<LinearLayout>(R.id.bottom_sheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.addBottomSheetCallback(
            object : BottomSheetCallback() {
                @SuppressLint("WrongConstant")
                override fun onStateChanged(view: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> {
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
//                        btn_bottom_sheet.setText("Close Sheet"
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                        }
                        BottomSheetBehavior.STATE_DRAGGING -> {
                        }
                        BottomSheetBehavior.STATE_SETTLING -> {
                        }
                    }
                }

                override fun onSlide(view: View, v: Float) {}
            })

        val searchView = findViewById<ImageButton>(R.id.addFriendButton)
        searchView.setOnClickListener { v ->
            val intent = Intent(this, SearchActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE, "message")
            }
            startActivity(intent)
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.friends -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//                    imgMap.setVisibility(VISIBLE)
//                    imgDial.setVisibility()
//                    imgMail.setVisibility(View.GONE)
                }
                R.id.page_2 -> {

                }
            }
            false
        }

        mapViewModel.user.observe(this@MapActivity, Observer {
            val loginResult = it ?: return@Observer
            if (loginResult.success !== null) {
                actionBar?.title = loginResult.success.displayName
                mapView.getMapAsync(this)
                locationEngine = LocationEngineProvider.getBestLocationEngine(this)
                mapViewModel.addFriends(loginResult.success.friendsList)

            } else {
                startForResult.launch(Intent(this@MapActivity, LoginActivity::class.java))
            }
        })
        mapViewModel.friendsList.observe(this@MapActivity, Observer { it ->
            friendsAdapter.updateFriendList(
                it
            )
            friendsAdapter.notifyDataSetChanged();
        })
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(), networkCallback
        )
    }

    override fun onSearchRequested(searchEvent: SearchEvent?): Boolean {
        val appData = Bundle().apply {
            putBoolean("JARGON", true)
        }
        startSearch(null, false, appData, false)
        return super.onSearchRequested(searchEvent)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(
//            Style.MAPBOX_STREETS
//            Style.Builder().fromUri("mapbox://styles/dianaagl/ckionrj5o3cwo17muljj0whc0")
            Style.Builder().fromUri("mapbox://styles/dianaagl/ckiorfzij51xq17qvpgyxyk6t")

        ){
            this.mapboxMap = mapboxMap

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
                        this,
                        R.drawable.mapbox_marker_icon_default
                    )
                )!!
            )

            // Add the LineLayer below the LocationComponent's bottom layer, which is the
            // circular accuracy layer. The LineLayer will display the directions route.
            it.addLayerBelow(
                LineLayer("ROUTE_LAYER_ID", "ROUTE_LINE_SOURCE_ID")
                    .withProperties(
                        lineCap(LINE_CAP_ROUND),
                        lineJoin(LINE_JOIN_ROUND),
                        lineWidth(6f),
                        lineGradient(
                            interpolate(
                                linear(),
                                lineProgress(),
                                stop(0f, color(parseColor(ORIGIN_COLOR))),
                                stop(1f, color(parseColor(DESTINATION_COLOR)))
                            )
                        )
                    ),
                "mapbox-location-shadow-layer"
            )

            // Add the SymbolLayer to show the destination marker
            it.addLayerAbove(
                SymbolLayer("CLICK_LAYER", "CLICK_SOURCE")
                    .withProperties(
                        iconImage("ICON_ID")
                    ),
                "ROUTE_LAYER_ID"
            )

            mapboxMap.addOnMapLongClickListener(this)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                enableLocationComponent()
            }
        }
    }

    override fun onMapLongClick(latLng: LatLng): Boolean {
        route_retrieval_progress_spinner.visibility = VISIBLE
        // Place the destination marker at the map long click location
        mapboxMap?.getStyle {
            val clickPointSource = it.getSourceAs<GeoJsonSource>("CLICK_SOURCE")
            clickPointSource?.setGeoJson(Point.fromLngLat(latLng.longitude, latLng.latitude))
        }
        mapboxMap?.locationComponent?.lastKnownLocation?.let { originLocation ->
            mapboxNavigation?.requestRoutes(
                RouteOptions.builder().applyDefaultParams()
                    .accessToken(getString(R.string.mapbox_access_token))
                    .coordinates(
                        Point.fromLngLat(originLocation.longitude, originLocation.latitude),
                        null, Point.fromLngLat(latLng.longitude, latLng.latitude)
                    )
                    .alternatives(true)
                    .profile(DirectionsCriteria.PROFILE_CYCLING)
                    .build(),
                routesReqCallback
            )
        }
        return true
    }

    private val routesReqCallback = object : RoutesRequestCallback {
        override fun onRoutesReady(routes: List<DirectionsRoute>) {
            if (routes.isNotEmpty()) {
                Snackbar.make(
                    container,
                    String.format(
                        getString(R.string.steps_in_route),
                        routes[0].legs()?.get(0)?.steps()?.size
                    ),
                    LENGTH_SHORT
                ).show()

                // Update a gradient route LineLayer's source with the Maps SDK. This will
                // visually add/update the line on the map. All of this is being done
                // directly with Maps SDK code and NOT the Navigation UI SDK.
                mapboxMap?.getStyle {
                    val clickPointSource = it.getSourceAs<GeoJsonSource>("ROUTE_LINE_SOURCE_ID")
                    val routeLineString = LineString.fromPolyline(
                        routes[0].geometry()!!,
                        6
                    )
                    clickPointSource?.setGeoJson(routeLineString)
                }
                route_retrieval_progress_spinner.visibility = INVISIBLE
            } else {
                Snackbar.make(container, R.string.no_routes, LENGTH_SHORT).show()
            }
        }

        override fun onRoutesRequestFailure(throwable: Throwable, routeOptions: RouteOptions) {
            Timber.e("route request failure %s", throwable.toString())
            Snackbar.make(container, R.string.route_request_failed, LENGTH_SHORT).show()
        }

        override fun onRoutesRequestCanceled(routeOptions: RouteOptions) {
            Timber.d("route request canceled")
        }
    }

    /**
     * Enable the Maps SDK's LocationComponent
     */
    @SuppressLint("MissingPermission")
    private fun enableLocationComponent() {
        val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
        val DEFAULT_DURATION_IN_MILLISECONDS = 1000
        val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
        var request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
            .build()

        mapboxMap?.getStyle {
            mapboxMap?.locationComponent?.apply {
                activateLocationComponent(
                    LocationComponentActivationOptions.builder(
                        this@MapActivity,
                        it
                    )
                        .build()
                )
                isLocationComponentEnabled = true
                cameraMode = CameraMode.TRACKING
                renderMode = RenderMode.COMPASS
            }
        }
        locationObserverCallback = UserLocationObserver(this, mapViewModel)
        locationEngine?.requestLocationUpdates(
            request,
            locationObserverCallback!!,
            mainLooper
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
                DEFAULT_DURATION_IN_MILLISECONDS
            );
        }

     }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
//        if(locationEngine !== null) locationEngine!!.removeLocationUpdates(locationObserverCallback!!)
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxNavigation?.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(REQUEST_PERMISSIONS_REQUEST_CODE, permissions, grantResults)
        val permissionsToRequest =
            java.util.ArrayList<String>()
        for (i in grantResults.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(permissions[i])
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
        else{
            enableLocationComponent()
        }
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest = java.util.ArrayList<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this@MapActivity,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
//
//    fun isNetworkActive(): Boolean{
//        val cm = this@OnlyMap.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
//        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
//    }

    private val networkChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

        }
    }

    private val networkCallback: NetworkCallback = object : NetworkCallback() {
        override fun onAvailable(network: Network) {
            mapViewModel.verifyUser()
            super.onAvailable(network)
        }

        override fun onLost(network: Network) {

            super.onLost(network)
        }

        override fun onUnavailable() {

            super.onUnavailable()
        }
    }
}

