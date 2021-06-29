package com.example.espersdksampleapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.espersdksampleapp.databinding.ActivityMainBinding
import io.esper.devicesdk.EsperDeviceSDK
import io.esper.devicesdk.utils.EsperSDKVersions
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var sdk: EsperDeviceSDK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView()

        /*
            NOTE: For Esper SDK to be functional,
                  Esper Agent should be installed in the device.
         */
        if (!isEsperAgentInstalled()) {
            Log.e(TAG, "onCreate: Error:: Esper Agent Not Found")
            return
        }

        // Get the instance of the Esper SDK
        sdk = EsperDeviceSDK.getInstance(applicationContext)
    }

    override fun onStart() {
        super.onStart()
        // Initiate the Esper SDK activated or not check
        initEsperSDKActivationCheck()
    }

    /**
     * Method to initiate check to know whether Esper SDK is activated or not.
     */
    private fun initEsperSDKActivationCheck() {
        sdk.isActivated(object : EsperDeviceSDK.Callback<Boolean> {
            override fun onResponse(isActive: Boolean?) {
                isActive?.let {
                    if (isActive) {
                        Log.d(TAG, "isEsperSDKActivated: SDK is activated")
                    } else {
                        Log.d(TAG, "isEsperSDKActivated: SDK is not activated")
                    }
                } ?: Log.e(TAG, "isEsperSDKActivated: Something went wrong. isActive is null")
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "isEsperSDKActivated: SDK is not activated", throwable)
            }
        })
    }

    /**
     * Method to check if Esper Agent is installed in the device or not.
     *
     * @return true if Esper Agent is installed else false
     */
    private fun isEsperAgentInstalled(): Boolean {
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

    private fun setContentView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}