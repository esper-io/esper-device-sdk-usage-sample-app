package com.example.espersdksampleapp

import android.os.Bundle
import android.text.TextUtils
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

        setShowActivateSdkCardButtonClickListener()
        setActivateSdkCardButtonClickListener()

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
                    } else {
                        Log.d(TAG, "isEsperSDKActivated: SDK is not activated")
                    }

                    // Update the sdk activation status card
                    updateSdkActivationStatusCard(isActive)

                } ?: Log.e(TAG, "isEsperSDKActivated: Something went wrong. isActive is null")
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "isEsperSDKActivated: SDK is not activated", throwable)

                // Update the sdk activation status card
                updateSdkActivationStatusCard(false)
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

    private fun activateSdk(token: String) {
        if (TextUtils.isEmpty(token)) {
            Log.e(TAG, "activateSdk: Activation token is empty")
            return
        }

        // Activate the sdk
        sdk.activateSDK(token, object : EsperDeviceSDK.Callback<Void> {
            override fun onResponse(response: Void?) {
                Log.d(TAG, "activateSdk: SDK was successfully activated")

                // Update the sdk activation status card
                updateSdkActivationStatusCard(true)
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "activateSDK: SDK activation failed", throwable)
            }

        })
    }

    private fun updateSdkActivationStatusCard(isSdkActivated: Boolean) {
        when {
            isSdkActivated -> {
                setActivateSdkCardVisibility(View.GONE)
                setSdkActivatedStatus()
            }
            else -> setSdkNotActivatedStatus()
        }
    }

    private fun setSdkActivatedStatus() {
        setSdkActivationStatusMessage(getString(R.string.sdk_activated_msg))

        setShowActivateSdkCardButtonVisibility(View.GONE)

        setSdkActivatedIconVisibility(View.VISIBLE)
    }

    private fun setSdkNotActivatedStatus() {
        setSdkActivationStatusMessage(getString(R.string.sdk_not_activated_msg))

        setSdkActivatedIconVisibility(View.GONE)

        setShowActivateSdkCardButtonVisibility(View.VISIBLE)
    }

    private fun setSdkActivationStatusMessage(message: String) {
        binding.sdkActivationStatusTextView.text = message
    }

    private fun setSdkActivatedIconVisibility(visibility: Int) {
        binding.sdkActivatedIcon.visibility = visibility
    }

    private fun setShowActivateSdkCardButtonVisibility(visibility: Int) {
        binding.showActivateSdkCardBtn.visibility = visibility
    }

    private fun setShowActivateSdkCardButtonClickListener() {
        binding.showActivateSdkCardBtn.setOnClickListener {
            setActivateSdkCardVisibility(View.VISIBLE)
        }
    }

    private fun setActivateSdkCardVisibility(visibility: Int) {
        binding.activateSdkCard.visibility = visibility
    }

    private fun setActivateSdkCardButtonClickListener() {
        binding.apply {
            activateSdkBtn.setOnClickListener {
                // Get the entered sdk activation token
                val token = sdkActivationTokenEditText.text.toString()

                // Activate the sdk
                activateSdk(token)
            }
        }
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