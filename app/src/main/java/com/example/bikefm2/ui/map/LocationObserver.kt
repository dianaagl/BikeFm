package com.example.bikefm2.ui.map

import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import java.lang.Exception
import java.lang.ref.WeakReference

class UserLocationObserver(activity: MapActivity, private val mapViewModel: MapViewModel):
    LocationEngineCallback<LocationEngineResult> {

    private val activityWeakReference: WeakReference<MapActivity> = WeakReference(activity)

    override fun onSuccess(result: LocationEngineResult?) {
            mapViewModel.setUserLocation(result?.getLastLocation()!!)
    }

    override fun onFailure(exception: Exception) {
        TODO("Not yet implemented")
    }

}