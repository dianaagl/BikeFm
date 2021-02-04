package com.example.bikefm2.ui.map

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bikefm2.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import com.mapbox.android.core.location.*
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.DirectionsCriteria.PROFILE_CYCLING
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_map_layout.*
import kotlinx.android.synthetic.main.fragment_map_layout.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 * This activity shows how to combine the Mapbox Maps SDK with
 * ONLY the Navigation SDK. There is no Navigation UI SDK code
 * of any kind in this example.
 */
@AndroidEntryPoint
class MapFragment :
    Fragment(),
    LocationEngineCallback<LocationEngineResult> {

    private val requestPermissionCode = 1
    private lateinit var mapViewModel: MapViewModel

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val permissionsToRequest = permissions.keys.filter { permissions.get(it) == false }
            if (permissionsToRequest.size > 0) {
                activity?.let {
                    ActivityCompat.requestPermissions(
                        it,
                        permissionsToRequest.toTypedArray(),
                        requestPermissionCode
                    )
                }
            } else {
                MapUtils.enableLocationComponent(
                    requireContext(),
                    this
                )
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Mapbox.getInstance(context, getString(R.string.mapbox_access_token))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mapView.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.fragment_map_layout, container, false)
        mapViewModel = ViewModelProvider(this).get<MapViewModel>(MapViewModel::class.java)

        view.mapView.getMapAsync{
            context?.let { cont ->
                MapUtils.setMapboxProperties(cont, it)
                requestPermission.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ){
                    activity?.let {
                        MapUtils.requestPermissionsIfNecessary(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ),
                            it
                        )
                    }
                }
                else MapUtils.enableLocationComponent(requireContext(), this)
            }
        }

        val cm = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(), networkCallback
        )

        return view
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
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mapView.onLowMemory()
        super.onLowMemory()
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
        val permissionsToRequest =
            java.util.ArrayList<String>()
        for (i in grantResults.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                permissionsToRequest.add(permissions[i])
        }
    }

//todo check network for old versions
    private val networkChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

        }
    }

    private val networkCallback: NetworkCallback = object : NetworkCallback() {
        override fun onAvailable(network: Network) {

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

