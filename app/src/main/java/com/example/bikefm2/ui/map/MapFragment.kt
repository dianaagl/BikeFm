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
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bikefm2.R
import com.example.bikefm2.data.Result
import com.example.bikefm2.ui.login.LoginActivity
import com.example.bikefm2.ui.map.MapUtils.enableLocationComponent
import com.example.bikefm2.ui.map.MapUtils.requestPermissionsIfNecessary
import com.example.bikefm2.ui.search.SearchActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import com.mapbox.android.core.location.*
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
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.bottom_panel.*
import kotlinx.android.synthetic.main.layout_map.*
import kotlinx.android.synthetic.main.layout_map.container
import kotlinx.android.synthetic.main.map_content.*
import kotlinx.android.synthetic.main.map_content.view.*
import timber.log.Timber
import java.lang.Exception

/**
 * This activity shows how to combine the Mapbox Maps SDK with
 * ONLY the Navigation SDK. There is no Navigation UI SDK code
 * of any kind in this example.
 */
@AndroidEntryPoint
class MapFragment :
    Fragment(),
    MapboxMap.OnMapLongClickListener,
    LocationEngineCallback<LocationEngineResult> {

    private val requestPermissionCode = 1
    private var mapboxNavigation: MapboxNavigation? = null
    private var mapboxMap: MapboxMap? = null

    private var friendsAdapter = FriendAdapter()
    private lateinit var mapViewModel: MapViewModel

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            //result.data?.let { actionBar?.title = it.getStringExtra("displayName") }
          }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        MapUtils.createMapInstance(context, getString(R.string.mapbox_access_token))
        activity?.let {
            MapUtils.requestPermissionsIfNecessary(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                it
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.layout_map, container, false)
        view.mapView.onCreate(savedInstanceState)
        view.mapView.getMapAsync { mapbox ->
            context?.let { fragmentContext ->
                MapUtils.setMapboxProperties(fragmentContext, mapbox, this)
                activity?.let { act ->
                    if (ActivityCompat.checkSelfPermission(
                            act,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                            act,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissionsIfNecessary(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), act)
                    } else enableLocationComponent(mapbox, fragmentContext, this)
                }
                this.mapboxMap = mapbox
            }
        }

        mapViewModel = ViewModelProvider(this).get<MapViewModel>(MapViewModel::class.java)
        mapViewModel.getUser()

        val listView = view.findViewById<RecyclerView>(R.id.friends_list)
        listView.layoutManager = LinearLayoutManager(activity)
        listView.adapter = friendsAdapter

        val bottomNavigationView =
            view.findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        val bottomSheet = view.findViewById<LinearLayout>(R.id.bottom_sheet)
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
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        }
                    }
                }
                override fun onSlide(view: View, v: Float) {}
            })

        val searchView = view.findViewById<MaterialButton>(R.id.addFriendButton)
        searchView.setOnClickListener {
            val intent = Intent(activity, SearchActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE, "message")
            }
            startActivity(intent)
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.friends -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                R.id.page_2 -> {

                }
            }
            false
        }

        mapViewModel.user.observe(viewLifecycleOwner, Observer {
            val loginResult = it ?: return@Observer
            when (loginResult){
                is Result.Success -> {
                    //actionBar?.title = loginResult.data.displayName

                    loginResult.data.friends?.let { friends -> friendsAdapter.updateFriendList(friends) }
                    friendsAdapter.notifyDataSetChanged()
                }
                is Result.Error -> {
                    startForResult.launch(Intent(activity, LoginActivity::class.java))
                }
            }
        })

        val cm = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(), networkCallback
        )
        return view
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

    override fun onStart() {
        super.onStart()
        view?.mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        view?.mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        view?.mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
//        if(locationEngine !== null) locationEngine!!.removeLocationUpdates(locationObserverCallback!!)
        view?.mapView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxNavigation?.onDestroy()
        view?.mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        view?.mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        view?.mapView?.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestPermissionCode, permissions, grantResults)
        val permissionsToRequest = mutableListOf<String>()
        for (i in grantResults.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(permissions[i])
        }
        activity?.let {
            if (permissionsToRequest.size > 0) {
                ActivityCompat.requestPermissions(
                    it,
                    permissionsToRequest.toTypedArray(),
                    requestPermissionCode
                )
            }
            else{
                mapboxMap?.let { map -> MapUtils.enableLocationComponent(map, it, this) }
            }
        }
    }

//todo check network for old versions
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

    override fun onSuccess(result: LocationEngineResult?) {
        result?.lastLocation?.let { mapViewModel.setUserLocation(it) }
    }

    override fun onFailure(exception: Exception) {
        TODO("Not yet implemented")
    }
}

