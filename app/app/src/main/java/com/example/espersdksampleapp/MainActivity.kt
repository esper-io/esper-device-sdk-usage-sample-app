package com.example.espersdksampleapp

import android.content.res.Resources
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.espersdksampleapp.databinding.ActivityMainNewBinding
import com.example.espersdksampleapp.enum.*
import com.example.espersdksampleapp.provider.SampleJsonStringProvider
import io.esper.devicesdk.EsperDeviceSDK
import io.esper.devicesdk.exceptions.ActivationFailedException
import io.esper.devicesdk.utils.EsperSDKVersions
import java.lang.NumberFormatException

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

        setupSdkPlayground()

        /*
            NOTE: For Esper SDK to be functional,
                  Esper Agent should be installed in the device.
         */
        if (!isEsperAgentInstalled()) {
            Log.e(TAG, "onCreate: Error:: Esper Agent Not Found")
            return
        }

        setupSdkPlayground()
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

    /**
     * Method to Add Apn.
     */
    private fun addApn() {
        val inputHint = getString(R.string.apn_config_json_string)
        val sampleInputText = SampleJsonStringProvider.getSampleApnJsonConfigString()

        val buttonClickListener = OnClickListener {
            // Add the new APN
            sdk.addNewApnConfig(
                object : EsperDeviceSDK.Callback<Int> {
                    override fun onResponse(response: Int?) {
                        TODO("Not yet implemented")
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e(TAG, "addApn: Failed to add new apn.", throwable)
                    }

                }, getPrimaryInputEditTextInput()
            )
        }

        loadInputType(
            OneTextField(
                inputHint,
                sampleInputText,
                buttonClickListener = buttonClickListener
            )
        )
    }

    /**
     * Method to Change the App State.
     */
    private fun changeAppState() {
        val inputHint = getString(R.string.enter_package_name)
        val arrayResourceId = R.array.appStates

        val buttonClickListener = OnClickListener {
            val packageName = getPrimaryInputEditTextInput()
            val appState = getSelectedItemFromSpinnerInput()

            Log.d(TAG, "changeAppState: package: [$packageName] | state: [$appState]")

            // Change the app state
            sdk.changeAppState(
                packageName,
                appState,
                object : EsperDeviceSDK.Callback<Boolean> {
                    override fun onResponse(response: Boolean?) {
                        TODO("Not yet implemented")
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e(TAG, "changeAppState: Failed to change the app state.", throwable)
                    }
                })
        }

        loadInputType(
            OneTextFieldOneSpinner(
                inputHint,
                arrayResourceId = arrayResourceId,
                buttonClickListener = buttonClickListener
            )
        )
    }

    /**
     * Method to Configure No Network Fallback.
     */
    private fun configNoNetworkFallback() {
        val inputHint = getString(R.string.no_network_fallback_config_json_string)
        val sampleConfigJson = SampleJsonStringProvider.getSampleNoNetworkFallbackConfigJsonString()

        val buttonClickListener = OnClickListener {
            val inputConfigJson = getPrimaryInputEditTextInput()

            // Apply the given configuration
            sdk.configNoNetworkFallback(inputConfigJson, object : EsperDeviceSDK.Callback<Boolean> {
                override fun onResponse(response: Boolean?) {
                    TODO("Not yet implemented")
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, "configNoNetworkFallback: Failed to apply configuration.", throwable)
                }
            })
        }

        loadInputType(
            OneTextField(
                inputHint,
                sampleConfigJson,
                buttonClickListener = buttonClickListener
            )
        )
    }

    /**
     * Method to Reboot the device.
     */
    private fun reboot() {
        // Reboot the device
        sdk.reboot(object : EsperDeviceSDK.Callback<Boolean> {
            override fun onResponse(response: Boolean?) {
                TODO("Not yet implemented")
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "reboot: Failed to reboot.", throwable)
            }
        })
    }

    /**
     * Method to set the Screen Brightness.
     */
    private fun setBrightness() {
        val inputHint = getString(R.string.brightness_scale)

        val buttonClickListener = OnClickListener {
            val scale = try {
                val inputScale = getPrimaryInputEditTextInput()
                Integer.parseInt(inputScale)
            } catch (exception: NumberFormatException) {
                -1
            }

            if (scale in 0..100) {
                // Set the screen brightness
                sdk.setBrightness(scale, object : EsperDeviceSDK.Callback<Boolean> {
                    override fun onResponse(response: Boolean?) {
                        TODO("Not yet implemented")
                    }

                    override fun onFailure(throwable: Throwable) {
                        TODO("Not yet implemented")
                    }
                })
            } else {
                TODO("Not yet implemented")
            }
        }

        loadInputType(
            OneTextField(
                inputHint,
                buttonClickListener = buttonClickListener
            )
        )
    }

    /**
     * Method to set the Default Apn.
     */
    private fun setDefaultApn() {
        val inputHint = getString(R.string.apn_id)

        val buttonClickListener = OnClickListener {
            val apnId = getPrimaryInputEditTextInput()

            // Set the default apn
            sdk.setDefaultApn(object : EsperDeviceSDK.Callback<Int> {
                override fun onResponse(response: Int?) {
                    TODO("Not yet implemented")
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, "setDefaultApn: Failure.", throwable)
                }
            }, apnId)
        }

        loadInputType(
            OneTextField(
                inputHint,
                buttonClickListener = buttonClickListener
            )
        )
    }

    /**
     * Method to set the Global Setting.
     */
    private fun setGlobalSetting() {
        val key = "adb_enabled"
        val value = "false"

        // Set the global setting
        sdk.setGlobalSetting(key, value, object : EsperDeviceSDK.Callback<Boolean> {
            override fun onResponse(response: Boolean?) {
                TODO("Not yet implemented")
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "setGlobalSetting: Failed to set the global setting.", throwable)
            }
        })
    }

    /**
     * Method to set the Device Orientation.
     */
    private fun setDeviceOrientation() {
        val arrayResourceId = R.array.orientations

        val buttonClickListener = OnClickListener {
            val orientation = getSelectedItemFromSpinnerInput()

            Log.d(TAG, "etDeviceOrientation: Selected orientation: $orientation")

            if (TextUtils.isEmpty(orientation)) {
                return@OnClickListener
            }

            // Set the device orientation
            sdk.setDeviceOrientation(orientation, object : EsperDeviceSDK.Callback<Boolean> {
                override fun onResponse(response: Boolean?) {
                    TODO("Not yet implemented")
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, "setDeviceOrientation: Failure occurred.", throwable)
                }
            })
        }

        loadInputType(
            Spinner(
                arrayResourceId,
                buttonClickListener = buttonClickListener
            )
        )
    }

    /**
     * Method to set the System Setting.
     */
    private fun setSystemSetting() {
        val key = "user_rotation"
        val value = "2"

        // Set the system setting
        sdk.setSystemSetting(key, value, object : EsperDeviceSDK.Callback<Boolean> {
            override fun onResponse(response: Boolean?) {
                TODO("Not yet implemented")
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "setSystemSetting: Failed to set the system setting.", throwable)
            }
        })
    }

    /**
     * Method to Show the Dock.
     */
    private fun showDock() {
        // Show the dock
        sdk.showDock(object : EsperDeviceSDK.Callback<Void> {
            override fun onResponse(response: Void?) {
                TODO("Not yet implemented")
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "showDock: Failure occurred.", throwable)
            }
        })
    }

    /**
     * Method to Start the Dock.
     */
    private fun startDock() {
        // Start the dock
        sdk.startDock(object : EsperDeviceSDK.Callback<Void> {
            override fun onResponse(response: Void?) {
                TODO("Not yet implemented")
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "startDock: Failure occurred.", throwable)
            }
        })
    }

    /**
     * Method to Stop the Dock.
     */
    private fun stopDock() {
        // Stop the dock
        sdk.stopDock(object : EsperDeviceSDK.Callback<Void> {
            override fun onResponse(response: Void?) {
                TODO("Not yet implemented")
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "startDock: Failure occurred.", throwable)
            }
        })
    }

    private fun loadInputType(inputType: InputType) {
        var buttonText = ""

        when (inputType) {
            is OneTextField -> {
                inputType.primaryHint?.let { hint -> setPrimaryInputEditTextHint(hint) }
                inputType.primaryText?.let { text -> setPrimaryInputEditText(text) }
                setPrimaryInputEditTextVisibility(View.VISIBLE)

                buttonText = inputType.buttonText ?: getString(R.string.execute)
            }

            is TwoTextField -> {
                inputType.primaryHint?.let { hint -> setPrimaryInputEditTextHint(hint) }
                inputType.primaryText?.let { text -> setPrimaryInputEditText(text) }
                setPrimaryInputEditTextVisibility(View.VISIBLE)

                inputType.secondaryHint?.let { hint -> setSecondaryInputEditTextHint(hint) }
                inputType.secondaryText?.let { text -> setSecondaryInputEditText(text) }
                setSecondaryInputEditTextVisibility(View.VISIBLE)

                buttonText = inputType.buttonText ?: getString(R.string.execute)
            }

            is Spinner -> {
                inputType.arrayResourceId?.let { resourceId -> setSpinnerInputAdapter(resourceId) }
                setSpinnerInputVisibility(View.VISIBLE)

                buttonText = inputType.buttonText ?: getString(R.string.changeButtonText)
            }

            is Switch -> {
                inputType.switchText?.let { text -> setSwitchInputText(text) }
                setSwitchInputVisibility(View.VISIBLE)
            }

            is OneTextFieldOneSpinner -> {
                inputType.primaryHint?.let { hint -> setPrimaryInputEditTextHint(hint) }
                inputType.primaryText?.let { text -> setPrimaryInputEditText(text) }
                setPrimaryInputEditTextVisibility(View.VISIBLE)

                inputType.arrayResourceId?.let { resourceId -> setSpinnerInputAdapter(resourceId) }
                setSpinnerInputVisibility(View.VISIBLE)

                buttonText = inputType.buttonText ?: getString(R.string.changeButtonText)
            }
        }

        if (!TextUtils.isEmpty(buttonText)) {
            setProcessInputButtonText(buttonText)
            inputType.buttonClickListener?.let { setProcessInputButtonClickListener(it) }
            setProcessInputButtonVisibility(View.VISIBLE)
        }
    }

    private fun resetInputContainer() {
        setPrimaryInputEditTextHint("")
        setPrimaryInputEditText("")
        setPrimaryInputEditTextVisibility(View.GONE)

        setSecondaryInputEditTextHint("")
        setSecondaryInputEditText("")
        setSecondaryInputEditTextVisibility(View.GONE)

        setSpinnerInputAdapter(null)
        setSpinnerInputVisibility(View.GONE)

        setSwitchInputCheckedState(false)
        setSwitchInputVisibility(View.GONE)

        setProcessInputButtonText("")
        setProcessInputButtonVisibility(View.GONE)
    }

    private fun getPrimaryInputEditTextInput() = binding.primaryInputEditText.text.toString()

    private fun setPrimaryInputEditTextHint(hint: String) {
        binding.primaryInputEditText.hint = hint
    }

    private fun setPrimaryInputEditText(text: String) {
        binding.primaryInputEditText.setText(text)
    }

    private fun setPrimaryInputEditTextVisibility(visibility: Int) {
        binding.primaryInputEditText.visibility = visibility
    }

    private fun setSecondaryInputEditTextHint(hint: String) {
        binding.secondaryInputEditText.hint = hint
    }

    private fun setSecondaryInputEditText(text: String) {
        binding.secondaryInputEditText.setText(text)
    }

    private fun setSecondaryInputEditTextVisibility(visibility: Int) {
        binding.secondaryInputEditText.visibility = visibility
    }

    private fun getSelectedItemFromSpinnerInput() = binding.spinnerInput.selectedItem.toString()

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

    private fun setProcessInputButtonClickListener(onClickListener: OnClickListener) {
        binding.processInputBtn.setOnClickListener(onClickListener)
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
            // Reset the input container
            resetInputContainer()

            if (!isSelectedPositionValid(position)) {
                Log.e(TAG, "onItemSelected: Invalid method index")
                return
            }

            try {
                when (sdkMethodList[position]) {
                    getString(R.string.add_apn) -> addApn()
                    getString(R.string.allow_power_off) -> TODO()
                    getString(R.string.change_app_state) -> changeAppState()
                    getString(R.string.clear_app_data) -> TODO()
                    getString(R.string.config_no_network_fallback) -> configNoNetworkFallback()
                    getString(R.string.enable_mobile_data) -> TODO()
                    getString(R.string.enable_wifi_tethering) -> TODO()
                    getString(R.string.get_device_settings) -> TODO()
                    getString(R.string.get_esper_device_info) -> TODO()
                    getString(R.string.get_esper_removable_storage_path) -> TODO()
                    getString(R.string.reboot) -> reboot()
                    getString(R.string.remove_apn) -> TODO()
                    getString(R.string.set_app_op_mode) -> TODO()
                    getString(R.string.set_brightness) -> setBrightness()
                    getString(R.string.set_default_apn) -> setDefaultApn()
                    getString(R.string.set_global_setting) -> setGlobalSetting()
                    getString(R.string.set_device_orientation) -> setDeviceOrientation()
                    getString(R.string.set_system_setting) -> setSystemSetting()
                    getString(R.string.show_dock) -> showDock()
                    getString(R.string.start_dock) -> startDock()
                    getString(R.string.stop_dock) -> stopDock()
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