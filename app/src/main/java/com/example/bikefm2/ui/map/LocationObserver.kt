package com.example.bikefm2.ui.map

import android.location.Location
import com.example.bikefm2.BikeFmApp
import com.example.bikefm2.data.UserRepository
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.ref.WeakReference

class UserLocationObserver(activity: OnlyMap, private val mapViewModel: MapViewModel):
    LocationEngineCallback<LocationEngineResult> {

    private val activityWeakReference: WeakReference<OnlyMap> = WeakReference(activity)

    override fun onSuccess(result: LocationEngineResult?) {
            mapViewModel.setUserLocation(result?.getLastLocation()!!)
    }

    override fun onFailure(exception: Exception) {
        TODO("Not yet implemented")
    }

}