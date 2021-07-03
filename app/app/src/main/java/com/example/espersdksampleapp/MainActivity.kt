package com.example.espersdksampleapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.espersdksampleapp.databinding.ActivityMainNewBinding
import io.esper.devicesdk.EsperDeviceSDK
import io.esper.devicesdk.utils.EsperSDKVersions

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainNewBinding

    private lateinit var sdk: EsperDeviceSDK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView()

        // Get the instance of the Esper SDK
        sdk = EsperDeviceSDK.getInstance(applicationContext)

        /*
            NOTE: For Esper SDK to be functional,
                  Esper Agent should be installed in the device.
         */
        if (!isEsperAgentInstalled()) {
            Log.e(TAG, "onCreate: Error:: Esper Agent Not Found")
            return
        }
    }

    override fun onStart() {
        super.onStart()

        if (this::sdk.isInitialized) {
            // Initiate the check whether Esper SDK activated or not
            initEsperSDKActivationCheck()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (this::sdk.isInitialized) {
            // Dispose off the SDK instance
            sdk.dispose()
        }
    }

    /**
     * Method to initiate check to know whether Esper SDK is activated or not.
     */
    private fun initEsperSDKActivationCheck() {
        // Check whether sdk is activated or not
        sdk.isActivated(object : EsperDeviceSDK.Callback<Boolean> {
            override fun onResponse(isActive: Boolean?) {
                isActive?.let {
                    if (isActive) {
                        Log.d(TAG, "isEsperSDKActivated: SDK is activated")

                        // Hide the sdk not activated info card
                        setSdkNotActivatedInfoCardVisibility(View.GONE)
                    } else {
                        Log.d(TAG, "isEsperSDKActivated: SDK is not activated")

                        // Show the sdk not activated info card
                        setSdkNotActivatedInfoCardVisibility(View.VISIBLE)
                    }
                } ?: Log.e(TAG, "isEsperSDKActivated: Something went wrong. isActive is null")
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "isEsperSDKActivated: SDK is not activated", throwable)

                // Show the sdk not activated info card
                setSdkNotActivatedInfoCardVisibility(View.VISIBLE)
            }
        })
    }

    /**
     * Method to check if Esper Agent is installed in the device or not.
     *
     * @return true if Esper Agent is installed else false
     */
    private fun isEsperAgentInstalled(): Boolean {
        if (!this::sdk.isInitialized) {
            Log.e(TAG, "isEsperAgentInstalled: sdk is not instantiated yet")
            return false
        }

        // Get the sdk api level
        val esperDeviceSDKApiLevel = sdk.apiLevel

        /*
            If Esper Agent is not installed,
            then getApiLevel() method returns EsperSDKVersions.INVALID_VERSION
         */
        if (esperDeviceSDKApiLevel == EsperSDKVersions.INVALID_VERSION) {
            return false
        }

        return true
    }

    private fun setSdkNotActivatedInfoCardVisibility(visibility: Int) {
        binding.sdkNotActivatedCard.visibility = visibility
    }

    private fun setContentView() {
        binding = ActivityMainNewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}