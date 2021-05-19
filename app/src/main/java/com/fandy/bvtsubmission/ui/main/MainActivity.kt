package com.fandy.bvtsubmission.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.fandy.bvtsubmission.R
import com.fandy.bvtsubmission.ui.base.BaseActivity
import com.fandy.bvtsubmission.utils.CommonUtils
import com.google.android.material.snackbar.Snackbar
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.*
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style


class MainActivity : BaseActivity(), View.OnClickListener, OnMapReadyCallback,
    OnLocationClickListener, OnCameraTrackingChangedListener {

    companion object MainActivity {
        private val TAG = MainActivity::class.simpleName
        private const val KEY_LOCATION_PERMISSION: Int = 110
    }

    private var mapView: MapView? = null
    private var mMapboxMap: MapboxMap? = null
    private var locationComponent: LocationComponent? = null
    private var isInTrackingMode = false

    private lateinit var btnGetLocation: Button
    private lateinit var clParent: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_main)

        initView()

        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    private fun initView() {
        mapView = findViewById(R.id.mapView)
        clParent = findViewById(R.id.cl_parent)
        btnGetLocation = findViewById(R.id.btn_get_location)
        btnGetLocation.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view == btnGetLocation) {
            locationComponent?.addOnCameraTrackingChangedListener(this)
            if (!isInTrackingMode) {
                isInTrackingMode = true
                locationComponent?.cameraMode = CameraMode.TRACKING
                locationComponent?.zoomWhileTracking(16.0)
                Toast.makeText(this@MainActivity, resources.getString(R.string.found_location), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    resources.getString(R.string.searching_location),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mMapboxMap = mapboxMap
        mapboxMap.setStyle(
            Style.LIGHT
        ) { style -> enableLocationComponent(style) }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {

            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .elevation(5f)
                .accuracyAlpha(.6f)
                .accuracyColor(Color.RED)
                .build()

            locationComponent = mMapboxMap?.locationComponent
            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(this, loadedMapStyle)
                    .locationComponentOptions(customLocationComponentOptions)
                    .build()

            locationComponent?.activateLocationComponent(locationComponentActivationOptions)

            locationComponent?.isLocationComponentEnabled = true

            locationComponent?.cameraMode = CameraMode.TRACKING

            locationComponent?.renderMode = RenderMode.COMPASS

            locationComponent?.addOnLocationClickListener(this)

        } else {
            requestPermissionsSafely(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                KEY_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == KEY_LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMapboxMap?.getStyle {
                    enableLocationComponent(it)
                }
            } else {
                Toast.makeText(
                    this@MainActivity,
                    resources.getString(R.string.permission_alert),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLocationComponentClick() {
        if (locationComponent?.lastKnownLocation != null) {
            val lat: String = locationComponent?.lastKnownLocation?.latitude.toString()
            val long: String = locationComponent?.lastKnownLocation?.longitude.toString()
            val longlat = "Titik anda di : $lat $long"
            showLocation(longlat)
        }
    }

    override fun onCameraTrackingDismissed() {
        isInTrackingMode = false
    }

    override fun onCameraTrackingChanged(currentMode: Int) {
        Log.d(TAG, "onCameraTrackingChanged")
    }

    private fun showLocation(longlat: String) {
        Snackbar.make(clParent, longlat, Snackbar.LENGTH_SHORT).apply {
            setAction(resources.getString(R.string.action_copy_location)) {
                CommonUtils.setClipboard(applicationContext, longlat)
                Toast.makeText(
                    this@MainActivity,
                    resources.getString(R.string.location_copied),
                    Toast.LENGTH_SHORT
                ).show()
            }
            show()
        }
    }
}