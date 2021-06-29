package com.example.espersdksampleapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.espersdksampleapp.databinding.ActivityMainBinding
import io.esper.devicesdk.EsperDeviceSDK
import io.esper.devicesdk.utils.EsperSDKVersions

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var sdk: EsperDeviceSDK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView()

        // Get the instance of the Esper SDK
        sdk = EsperDeviceSDK.getInstance(applicationContext)
    }

    private fun setContentView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}