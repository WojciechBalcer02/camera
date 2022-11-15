package com.example.camera

import android.Manifest
import android.app.DownloadManager.Request
import android.content.ContentProviderClient
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.camera.databinding.ActivityMainBinding
import com.google.android.gms.location.*


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    lateinit var locationRequest: LocationRequest

    private var PERMISSION_ID = 52


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding.LocationBtn.setOnClickListener{
            getLastLocation()
        }

        binding.button.isEnabled = false
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 123)
        }
        else
            binding.button.isEnabled = true
        binding.button.setOnClickListener {
            var i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(i,321)
        }
    }

    private fun getLastLocation(){
        if (CheckPermission()){
            if(isLocationEnabled()){
                try {
                    fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                        var location = task.result
                        if(location==null){

                            getNewLocation()
                        }else{
                            binding.Locationtxt.text = "your current coordinates are: \nLat:"+ location.latitude +"; Long: "+location.longitude
                        }
                    }
                } catch (e: SecurityException) {}
            }else{
                Toast.makeText(this,"prosze pozwolic na korzystanie lokalizacji",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getNewLocation(){
        locationRequest = com.google.android.gms.location.LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2
        try {
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest.locationCallback, Looper.myLooper()
            )
        } catch (e: SecurityException) {}

    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation = p0.lastLocation
            binding.Locationtxt.text = "your current coordinates are: \nLat:"+ lastLocation.latitude +"; Long: "+lastLocation.longitude
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==321)
        {
            val binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            var myPicture: Bitmap?
            myPicture = data?.getParcelableExtra<Bitmap>("data")
            binding.imageView.setImageBitmap(myPicture)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            val binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            binding.button.isEnabled = true
        }

        if(requestCode == PERMISSION_ID){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Log.d("Debug", "you have the permission")
            }
        }
    }

    //gps below
    private fun CheckPermission():Boolean{
        if(
            ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }
        return false
    }

    private fun RequestPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),PERMISSION_ID
        )
    }

    private fun isLocationEnabled():Boolean{
        var locationManager:LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }




}