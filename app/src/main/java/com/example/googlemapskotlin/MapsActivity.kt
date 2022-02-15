package com.example.googlemapskotlin

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Camera
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.googlemapskotlin.databinding.ActivityMapsBinding
import androidx.annotation.NonNull
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        var myLat = 0.0
        var myLong = 0.0
        mMap.setOnMapClickListener(listener)

        mMap.uiSettings.isZoomControlsEnabled = true
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener{
            override fun onLocationChanged(p0: Location) {
                mMap.clear()
                val loc = LatLng(p0.latitude,p0.longitude)
                myLat = p0.latitude.toDouble()
                myLong = p0.longitude.toDouble()
                mMap.addMarker(MarkerOptions().position(loc).title("Your Location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,15f))
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,1f,locationListener)
            val lastloc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if(lastloc != null){
                val lastlocLatLng = LatLng(lastloc.latitude,lastloc.longitude)
                mMap.addMarker(MarkerOptions().position(lastlocLatLng).title("Last Known Location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastlocLatLng,15f))

                val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
                try {
                    val adressList = geocoder.getFromLocation(myLat,myLong, 1)
                    if(adressList.size > 0){
                        println(adressList.get(0).toString())
                    }
                } catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1){
            if(grantResults.size > 0){
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,1f,locationListener)

                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    val listener = object : GoogleMap.OnMapClickListener {
        override fun onMapClick(p0: LatLng?) {
            mMap.clear()
            val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
            if(p0 != null){
                var adress = ""
                try {
                    val addressList = geocoder.getFromLocation(p0.latitude,p0.longitude,1)
                    if(addressList.size > 1){
                        if(addressList.get(0).thoroughfare != null){
                       adress += addressList.get(0).thoroughfare
                        }
                        if(addressList.get(0).subThoroughfare != null){
                            adress += addressList.get(0).subThoroughfare
                        }
                    }

                }catch (e: Exception){
                    e.printStackTrace()
                }
                mMap.addMarker((MarkerOptions().position(p0).title(adress)))
            }
        }
    }

}