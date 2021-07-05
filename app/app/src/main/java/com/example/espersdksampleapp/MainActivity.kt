package com.example.espersdksampleapp

import android.content.res.Resources
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.espersdksampleapp.databinding.ActivityMainNewBinding
import com.example.espersdksampleapp.enum.*
import io.esper.devicesdk.EsperDeviceSDK
import io.esper.devicesdk.exceptions.ActivationFailedException
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

            // Show token not entered error message
            setAndShowSdkActivationErrorMessage(getString(R.string.enter_token))

            return
        }

        // Activate the sdk
        sdk.activateSDK(token, object : EsperDeviceSDK.Callback<Void> {
            override fun onResponse(response: Void?) {
                Log.d(TAG, "activateSdk: SDK was successfully activated")

                // Notify sdk activation success
                notifySdkActivationSuccess()
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "activateSDK: SDK activation failed", throwable)

                // Notify sdk activation failure
                notifySdkActivationFailure(throwable)
            }
        })
    }

    private fun loadInputType(inputType: InputType) {
        resetInputContainer()

        when (inputType) {
            is OneTextField -> {
                inputType.primaryHint?.let { hint -> setPrimaryInputEditTextHint(hint) }
                setPrimaryInputEditTextVisibility(View.VISIBLE)
            }

            is TwoTextField -> {
                inputType.primaryHint?.let { hint -> setPrimaryInputEditTextHint(hint) }
                setPrimaryInputEditTextVisibility(View.VISIBLE)

                inputType.secondaryHint?.let { hint -> setSecondaryInputEditTextHint(hint) }
                setSecondaryInputEditTextVisibility(View.VISIBLE)
            }

            is Spinner -> {
                inputType.arrayResourceId?.let { resourceId -> setSpinnerInputAdapter(resourceId) }
                setSpinnerInputVisibility(View.VISIBLE)
            }

            is Switch -> {
                inputType.switchText?.let { text -> setSwitchInputText(text) }
                setSwitchInputVisibility(View.VISIBLE)
            }

            is OneTextFieldOneSpinner -> {
                inputType.primaryHint?.let { hint -> setPrimaryInputEditTextHint(hint) }
                setPrimaryInputEditTextVisibility(View.VISIBLE)

                inputType.arrayResourceId?.let { resourceId -> setSpinnerInputAdapter(resourceId) }
                setSpinnerInputVisibility(View.VISIBLE)
            }
        }

        inputType.buttonText?.let { buttonText ->
            setProcessInputButtonText(buttonText)
            setProcessInputButtonVisibility(View.VISIBLE)
        }
    }

    private fun resetInputContainer() {
        setPrimaryInputEditTextHint("")
        setPrimaryInputEditTextVisibility(View.GONE)

        setSecondaryInputEditTextHint("")
        setSecondaryInputEditTextVisibility(View.GONE)

        setSpinnerInputAdapter(null)
        setSpinnerInputVisibility(View.GONE)

        setSwitchInputCheckedState(false)
        setSwitchInputVisibility(View.GONE)

        setProcessInputButtonText("")
        setProcessInputButtonVisibility(View.GONE)
    }

    private fun setPrimaryInputEditTextHint(hint: String) {
        binding.primaryInputEditText.hint = hint
    }

    private fun setPrimaryInputEditTextVisibility(visibility: Int) {
        binding.primaryInputEditText.visibility = visibility
    }

    private fun setSecondaryInputEditTextHint(hint: String) {
        binding.secondaryInputEditText.hint = hint
    }

    private fun setSecondaryInputEditTextVisibility(visibility: Int) {
        binding.secondaryInputEditText.visibility = visibility
    }

    private fun setSpinnerInputAdapter(arrayResourceId: Int) {
        val arrayAdapter = ArrayAdapter.createFromResource(
            this,
            arrayResourceId,
            android.R.layout.simple_spinner_item
        )

        setSpinnerInputAdapter(arrayAdapter)
    }

    private fun setSpinnerInputAdapter(arrayAdapter: ArrayAdapter<CharSequence>?) {
        binding.spinnerInput.adapter = arrayAdapter
    }

    private fun setSpinnerInputVisibility(visibility: Int) {
        binding.spinnerInput.visibility = visibility
    }

    private fun setSwitchInputText(text: String) {
        binding.switchInput.text = text
    }

    private fun setSwitchInputCheckedState(isChecked: Boolean) {
        binding.switchInput.isChecked = isChecked
    }

    private fun setSwitchInputVisibility(visibility: Int) {
        binding.switchInput.visibility = visibility
    }

    private fun setProcessInputButtonText(text: String) {
        binding.processInputBtn.text = text
    }

    private fun setProcessInputButtonVisibility(visibility: Int) {
        binding.processInputBtn.visibility = visibility
    }

    private fun setupSdkPlayground() {
        setSdkMethodsDropdown()
    }

    private fun setSdkMethodsDropdown() {
        val arrayAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.sdkMethods,
            android.R.layout.simple_spinner_item
        )

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.apply {
            sdkMethodSpinner.adapter = arrayAdapter
            sdkMethodSpinner.onItemSelectedListener = SdkMethodSelectListener()
        }
    }

    private fun notifySdkActivationSuccess() {
        clearAndHideSdkActivationErrorMessage()

        setActivateSdkCardVisibility(View.GONE)

        updateSdkActivationStatusCard(true)

        setSdkActivationStatusCardVisibility(View.VISIBLE)
    }

    private fun notifySdkActivationFailure(throwable: Throwable) {
        val errorMessage = if (throwable is ActivationFailedException) {
            getString(R.string.activate_sdk_fail)
        } else {
            "Failure: $throwable"
        }

        setAndShowSdkActivationErrorMessage(errorMessage)
    }

    private fun updateSdkActivationStatusCard(isSdkActivated: Boolean) {
        when {
            isSdkActivated -> setSdkActivatedStatus()
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

    private fun setSdkActivationStatusCardVisibility(visibility: Int) {
        binding.sdkActivationStatusCard.visibility = visibility
    }

    private fun setSdkActivatedIconVisibility(visibility: Int) {
        binding.sdkActivatedIcon.visibility = visibility
    }

    private fun setShowActivateSdkCardButtonVisibility(visibility: Int) {
        binding.showActivateSdkCardBtn.visibility = visibility
    }

    private fun setShowActivateSdkCardButtonClickListener() {
        binding.showActivateSdkCardBtn.setOnClickListener {
            setSdkActivationStatusCardVisibility(View.GONE)
            setActivateSdkCardVisibility(View.VISIBLE)
        }
    }

    private fun setActivateSdkCardVisibility(visibility: Int) {
        binding.activateSdkCard.visibility = visibility
    }

    private fun setActivateSdkCardButtonClickListener() {
        binding.apply {
            activateSdkBtn.setOnClickListener {
                val token = sdkActivationTokenEditText.text.toString()
                activateSdk(token)
            }
        }
    }

    private fun clearAndHideSdkActivationErrorMessage() {
        setSdkActivationErrorMessageVisibility(View.GONE)
        setSdkActivationErrorMessage("")
    }

    private fun setAndShowSdkActivationErrorMessage(errorMessage: String) {
        setSdkActivationErrorMessage(errorMessage)
        setSdkActivationErrorMessageVisibility(View.VISIBLE)
    }

    private fun setSdkActivationErrorMessageVisibility(visibility: Int) {
        binding.sdkActivationErrorMessageTextView.visibility = visibility
    }

    private fun setSdkActivationErrorMessage(errorMessage: String) {
        binding.sdkActivationErrorMessageTextView.text = errorMessage
    }

    private fun setContentView() {
        binding = ActivityMainNewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private inner class SdkMethodSelectListener : AdapterView.OnItemSelectedListener {
        private val TAG = "SdkMethodSelectListener"

        private val sdkMethodList = resources.getStringArray(R.array.sdkMethods)

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (!isSelectedPositionValid(position)) {
                Log.e(TAG, "onItemSelected: Invalid method index")
                return
            }

            try {
                when (sdkMethodList[position]) {
                    getString(R.string.add_apn) -> TODO()
                    getString(R.string.allow_power_off) -> TODO()
                    getString(R.string.change_app_state) -> TODO()
                    getString(R.string.clear_app_data) -> TODO()
                    getString(R.string.config_no_network_fallback) -> TODO()
                    getString(R.string.enable_mobile_data) -> TODO()
                    getString(R.string.enable_wifi_tethering) -> TODO()
                    getString(R.string.get_device_settings) -> TODO()
                    getString(R.string.get_esper_device_info) -> TODO()
                    getString(R.string.get_esper_removable_storage_path) -> TODO()
                    getString(R.string.reboot) -> TODO()
                    getString(R.string.remove_apn) -> TODO()
                    getString(R.string.set_app_op_mode) -> TODO()
                    getString(R.string.set_brightness) -> TODO()
                    getString(R.string.set_default_apn) -> TODO()
                    getString(R.string.set_global_setting) -> TODO()
                    getString(R.string.set_orientation) -> TODO()
                    getString(R.string.set_system_setting) -> TODO()
                    getString(R.string.show_dock) -> TODO()
                    getString(R.string.start_dock) -> TODO()
                    getString(R.string.stop_dock) -> TODO()
                    getString(R.string.update_apn) -> TODO()
                    getString(R.string.update_app_configurations) -> TODO()
                    getString(R.string.launch_eea_apis_demo) -> TODO()
                }
            } catch (exception: Resources.NotFoundException) {
                Log.e(TAG, "onItemSelected: SDK method not found")
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}

        private fun isSelectedPositionValid(position: Int) =
            position >= 0 && position < sdkMethodList.size
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}