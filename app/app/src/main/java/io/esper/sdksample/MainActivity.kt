package io.esper.sdksample

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.esper.devicesdk.EsperDeviceSDK
import io.esper.devicesdk.constants.AppOpsPermissions
import io.esper.devicesdk.exceptions.ActivationFailedException
import io.esper.devicesdk.models.EsperDeviceInfo
import io.esper.devicesdk.utils.EsperSDKVersions
import io.esper.sdksample.databinding.ActivityMainBinding
import io.esper.sdksample.enum.*
import io.esper.sdksample.provider.SampleJsonStringProvider
import org.json.JSONObject
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
            setAndShowSdkInfoCard(getString(R.string.esper_agent_not_found))
            return
        }

        setupSdkPlayground()
    }

    override fun onStart() {
        super.onStart()

        if (this::sdk.isInitialized && isEsperAgentInstalled()) {
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

    /*
        ||===================================================================================||
        ||------------------------- Esper Agent SDK Method(s) : START -----------------------||
        ||===================================================================================||
     */

    /**
     * Method to Activate the SDK.
     *
     * @param token sdk activation token
     */
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
                /*
                    SDK Activation was Successful!
                 */

                Log.d(TAG, "activateSdk: SDK was successfully activated")

                // Notify sdk activation success
                notifySdkActivationSuccess()
            }

            override fun onFailure(throwable: Throwable) {
                /*
                    SDK Activation Failed!
                 */

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
        val sampleConfigJsonString = SampleJsonStringProvider.getSampleApnJsonConfigString()

        val buttonClickExecutor = OnClickListener {
            val apnId = getPrimaryInputEditTextInput()

            // Add the new APN
            sdk.addNewApnConfig(
                object : EsperDeviceSDK.Callback<Int> {
                    override fun onResponse(response: Int?) {
                        showSdkMethodOutput("onResponse: APN ID: $response")
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e(TAG, "addApn: Failed to add new apn.", throwable)
                        showSdkMethodFailureOutput(throwable)
                    }
                }, apnId
            )
        }

        loadInputType(
            OneTextField(
                hint = inputHint,
                text = sampleConfigJsonString,
                buttonClickListener = buttonClickExecutor
            )
        )
    }

    /**
     * Method to Allow Power Off.
     */
    private fun allowPowerOff() {
        val switchText = getString(R.string.power_button)

        val onCheckedChangeExecutor = OnCheckedChangeListener { compoundButton, checked ->
            // Allow power off
            sdk.allowPowerOff(checked, object : EsperDeviceSDK.Callback<Boolean> {
                override fun onResponse(response: Boolean?) {
                    showSdkMethodOutput("onResponse: $response")
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, "allowPowerOff: Failure occurred.", throwable)
                    showSdkMethodFailureOutput(throwable)
                }
            })
        }

        loadInputType(
            Switch(
                switchText = switchText,
                switchCheckedChangeListener = onCheckedChangeExecutor
            )
        )
    }

    /**
     * Method to Change the App State.
     */
    private fun changeAppState() {
        val inputHint = getString(R.string.enter_package_name)
        val arrayResourceId = R.array.appStates

        val buttonClickExecutor = OnClickListener {
            val packageName = getPrimaryInputEditTextInput()
            val appState = getSelectedItemFromSpinnerInput()

            Log.d(TAG, "changeAppState: package: [$packageName] | state: [$appState]")

            // Change the app state
            sdk.changeAppState(
                packageName,
                appState,
                object : EsperDeviceSDK.Callback<Boolean> {
                    override fun onResponse(response: Boolean?) {
                        response?.let { isChanged ->
                            val msg =
                                if (isChanged) getString(R.string.app_state_change_success)
                                else getString(R.string.app_state_change_failure)

                            showSdkMethodOutput("onResponse: $msg")
                        } ?: showSdkMethodOutput("onResponse: $response")
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e(TAG, "changeAppState: Failed to change the app state.", throwable)
                        showSdkMethodFailureOutput(throwable)
                    }
                })
        }

        loadInputType(
            OneTextFieldOneSpinner(
                hint = inputHint,
                arrayResourceId = arrayResourceId,
                buttonClickListener = buttonClickExecutor
            )
        )
    }

    /**
     * Method to Clear App Data.
     */
    private fun clearAppData() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        val inputHint = getString(R.string.clear_app_data_input_prompt)

        val buttonClickExecutor = OnClickListener {
            val input = getPrimaryInputEditTextInput()
            val packageList = input.split("\n").toTypedArray()
            val packageArrayList = ArrayList(listOf(*packageList))

            // Clear app data
            sdk.clearAppData(
                packageArrayList,
                object : EsperDeviceSDK.Callback<ArrayList<String?>?> {
                    override fun onResponse(packageNameArrayList: ArrayList<String?>?) {
                        packageNameArrayList?.let {
                            val stringBuilder = StringBuilder("Failed to clear app data for:\n")
                            for (packageName in packageNameArrayList) {
                                stringBuilder.append("$packageName\n")
                            }
                            showSdkMethodOutput(stringBuilder.toString())
                        }
                            ?: showSdkMethodOutput("Successfully cleared app data for input packages.")
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e(TAG, "clearAppData: Failure occurred.", throwable)
                        showSdkMethodFailureOutput(throwable)
                    }
                })
        }

        loadInputType(
            OneTextField(
                hint = inputHint,
                buttonClickListener = buttonClickExecutor
            )
        )
    }

    /**
     * Method to Configure No Network Fallback.
     */
    private fun configNoNetworkFallback() {
        val inputHint = getString(R.string.no_network_fallback_config_json_string)
        val sampleConfigJsonString =
            SampleJsonStringProvider.getSampleNoNetworkFallbackConfigJsonString()

        val buttonClickExecutor = OnClickListener {
            val inputConfigJson = getPrimaryInputEditTextInput()

            // Apply the given configuration
            sdk.configNoNetworkFallback(inputConfigJson, object : EsperDeviceSDK.Callback<Boolean> {
                override fun onResponse(response: Boolean?) {
                    showSdkMethodOutput("onResponse: $response")
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, "configNoNetworkFallback: Failed to apply configuration.", throwable)
                    showSdkMethodFailureOutput(throwable)
                }
            })
        }

        loadInputType(
            OneTextField(
                hint = inputHint,
                text = sampleConfigJsonString,
                buttonClickListener = buttonClickExecutor
            )
        )
    }

    /**
     * Method to Enable Mobile Data.
     */
    private fun enableMobileData() {
        val switchText = getString(R.string.mobile_data)

        val onCheckedChangeExecutor = OnCheckedChangeListener { compoundButton, checked ->
            // Enable the mobile data
            sdk.enableMobileData(checked, object : EsperDeviceSDK.Callback<Boolean> {
                override fun onResponse(response: Boolean?) {
                    showSdkMethodOutput("onResponse: $response")
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, "enableMobileData: Failure occurred.", throwable)
                    showSdkMethodFailureOutput(throwable)
                }
            })
        }

        loadInputType(
            Switch(
                switchText = switchText,
                switchCheckedChangeListener = onCheckedChangeExecutor
            )
        )
    }

    /**
     * Method to Enable Wifi Tethering.
     */
    private fun enableWifiTethering() {
        val primaryInputHint = getString(R.string.hotspot_name_input_prompt)
        val secondaryInputHint = getString(R.string.password)

        val buttonClickExecutor = OnClickListener {
            val ssid = getPrimaryInputEditTextInput()
            val password = getSecondaryInputEditTextInput()

            // Enable the wifi tethering
            sdk.enableWifiTethering(
                ssid,
                password,
                true,
                object : EsperDeviceSDK.Callback<String> {
                    override fun onResponse(response: String?) {
                        showSdkMethodOutput("onResponse: $response")
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e(TAG, "enableWifiTethering: Failure occurred.", throwable)
                        showSdkMethodFailureOutput(throwable)
                    }
                })
        }

        loadInputType(
            TwoTextField(
                primaryHint = primaryInputHint,
                secondaryHint = secondaryInputHint,
                buttonClickListener = buttonClickExecutor
            )
        )
    }

    /**
     * Method to get the Device Settings.
     */
    private fun getDeviceSettings() {
        // Get the device settings
        sdk.getDeviceSettings(object : EsperDeviceSDK.Callback<JSONObject> {
            override fun onResponse(response: JSONObject?) {
                Log.d(TAG, "getDeviceSettings: Device Settings: $response")

                if (response == null) {
                    showSdkMethodOutput("Response from device is: Null")
                    return
                }

                val stringBuilder = StringBuilder()
                stringBuilder.append("\n Device Settings \n")
                for (param in response.keys()) {
                    stringBuilder.append("\n $param : ${response.opt(param)}")
                }

                showSdkMethodOutput(stringBuilder.toString())
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "getDeviceSettings: Failure occurred.", throwable)
                showSdkMethodFailureOutput(throwable)
            }
        })
    }

    /**
     * Method to get the Esper Device Info.
     */
    private fun getEsperDeviceInfo() {
        // Get the esper device info
        sdk.getEsperDeviceInfo(object : EsperDeviceSDK.Callback<EsperDeviceInfo> {
            override fun onResponse(esperDeviceInfo: EsperDeviceInfo?) {
                if (esperDeviceInfo == null) {
                    showSdkMethodOutput("onResponse: Esper Device Info is: Null")
                    return
                }

                val deviceInfoBuilder = StringBuilder("Device Id: " + esperDeviceInfo.deviceId)

                if (sdk.apiLevel >= EsperSDKVersions.TESSARION_MR2) {
                    /*
                        This check is made because this application may run on a device that has
                        old Esper Agent which has device SDK older than EsperSDKVersions.TESSARION_MR2
                     */

                    deviceInfoBuilder
                        .append("\nSerial Number: ${esperDeviceInfo.serialNo}")
                        .append("\nIMEI1: ${esperDeviceInfo.imei1}")
                        .append("\nIMEI2: ${esperDeviceInfo.imei2}")
                }

                showSdkMethodOutput(deviceInfoBuilder.toString())
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "getEsperDeviceInfo: Failure occurred.", throwable)
                showSdkMethodFailureOutput(throwable)
            }
        })
    }

    /**
     * Method to get the Esper Removable Storage Path.
     */
    private fun getEsperRemovableStoragePath() {
        // Get the esper removable storage path
        sdk.getEsperRemovableStorageCachePath(object : EsperDeviceSDK.Callback<String> {
            override fun onResponse(response: String?) {
                Log.d(TAG, "getEsperRemovableStoragePath: Successful. Path: $response")

                showSdkMethodOutput("onResponse: $response")

                response?.let { showSdkMethodOutput(getFileNameList(File(it))) }
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "getEsperRemovableStoragePath: Failure occurred.", throwable)
                showSdkMethodFailureOutput(throwable)
            }
        })
    }

    /**
     * Method to Reboot the device.
     */
    private fun reboot() {
        // Reboot the device
        sdk.reboot(object : EsperDeviceSDK.Callback<Boolean> {
            override fun onResponse(response: Boolean?) {
                showSdkMethodOutput("onResponse: $response")
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "reboot: Failed to reboot.", throwable)
                showSdkMethodFailureOutput(throwable)
            }
        })
    }

    /**
     * Method to Remove Apn.
     */
    private fun removeApn() {
        val inputHint = getString(R.string.apn_id)

        val buttonClickExecutor = OnClickListener {
            val apnId = getPrimaryInputEditTextInput()

            // Remove the apn config
            sdk.removeApnConfig(object : EsperDeviceSDK.Callback<Int> {
                override fun onResponse(response: Int?) {
                    showSdkMethodOutput("onResponse: $response")
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, "removeApn: Failed to remove apn.", throwable)
                    showSdkMethodFailureOutput(throwable)
                }
            }, apnId)
        }

        loadInputType(
            OneTextField(
                hint = inputHint,
                buttonClickListener = buttonClickExecutor
            )
        )
    }

    /**
     * Method to Set the AppOp Mode.
     */
    private fun setAppOpMode() {
        if (sdk.apiLevel >= EsperSDKVersions.TESSARION_MR6 || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val appOpMode = AppOpsPermissions.OP_WRITE_SETTINGS

            // Set the AppOp mode
            sdk.setAppOpMode(appOpMode, true, object : EsperDeviceSDK.Callback<Void> {
                override fun onResponse(response: Void?) {
                    showSdkMethodOutput("${getString(R.string.grant_settings_write_perms_text)} : Successfully set the permissions")
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, "setAppOpMode: Failure occurred.", throwable)
                    showSdkMethodFailureOutput(throwable)
                }
            })
        } else {
            showSdkMethodOutput(getAndroidVersionsMinText(this, Build.VERSION_CODES.P))
        }
    }

    /**
     * Method to set the Screen Brightness.
     */
    private fun setBrightness() {
        val inputHint = getString(R.string.brightness_scale)

        val buttonClickExecutor = OnClickListener {
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
                        showSdkMethodOutput("onResponse: $response")
                    }

                    override fun onFailure(throwable: Throwable) {
                        showSdkMethodFailureOutput(throwable)
                    }
                })
            } else {
                showSdkMethodOutput("Invalid Input")
            }
        }

        loadInputType(
            OneTextField(
                hint = inputHint,
                buttonClickListener = buttonClickExecutor
            )
        )
    }

    /**
     * Method to set the Default Apn.
     */
    private fun setDefaultApn() {
        val inputHint = getString(R.string.apn_id)

        val buttonClickExecutor = OnClickListener {
            val apnId = getPrimaryInputEditTextInput()

            // Set the default apn
            sdk.setDefaultApn(object : EsperDeviceSDK.Callback<Int> {
                override fun onResponse(response: Int?) {
                    showSdkMethodOutput("onResponse: $response")
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, "setDefaultApn: Failure.", throwable)
                    showSdkMethodFailureOutput(throwable)
                }
            }, apnId)
        }

        loadInputType(
            OneTextField(
                hint = inputHint,
                buttonClickListener = buttonClickExecutor
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
                showSdkMethodOutput("onResponse: $response")
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "setGlobalSetting: Failed to set the global setting.", throwable)
                showSdkMethodFailureOutput(throwable)
            }
        })
    }

    /**
     * Method to set the Device Orientation.
     */
    private fun setDeviceOrientation() {
        val arrayResourceId = R.array.orientations

        val buttonClickExecutor = OnClickListener {
            val orientation = getSelectedItemFromSpinnerInput()

            Log.d(TAG, "etDeviceOrientation: Selected orientation: $orientation")

            if (orientation == getString(R.string.app_state_select_message)) {
                showSdkMethodOutput(getString(R.string.enter_input))
                return@OnClickListener
            }

            // Set the device orientation
            sdk.setDeviceOrientation(orientation, object : EsperDeviceSDK.Callback<Boolean> {
                override fun onResponse(response: Boolean?) {
                    showSdkMethodOutput("onResponse: $response")
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, "setDeviceOrientation: Failure occurred.", throwable)
                    showSdkMethodFailureOutput(throwable)
                }
            })
        }

        loadInputType(
            Spinner(
                arrayResourceId = arrayResourceId,
                buttonClickListener = buttonClickExecutor
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
                showSdkMethodOutput("onResponse: $response")
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "setSystemSetting: Failed to set the system setting.", throwable)
                showSdkMethodFailureOutput(throwable)
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
                showSdkMethodOutput(getString(R.string.show_dock_success))
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "showDock: Failure occurred.", throwable)
                showSdkMethodFailureOutput(throwable)
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
                showSdkMethodOutput(getString(R.string.start_dock_success))
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "startDock: Failure occurred.", throwable)
                showSdkMethodFailureOutput(throwable)
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
                showSdkMethodOutput(getString(R.string.success))
            }

            override fun onFailure(throwable: Throwable) {
                Log.e(TAG, "startDock: Failure occurred.", throwable)
                showSdkMethodFailureOutput(throwable)
            }
        })
    }

    /**
     * Method to Update the Apn.
     */
    private fun updateApn() {
        val primaryInputHint = getString(R.string.apn_config_json_string)
        val primarySampleInputConfigJsonString =
            SampleJsonStringProvider.getSampleApnJsonConfigString()

        val secondaryInputHint = getString(R.string.apn_id)

        val buttonClickExecutor = OnClickListener {
            val apnConfigJsonString = getPrimaryInputEditTextInput()
            val apnId = getSecondaryInputEditTextInput()

            // Update the apn
            sdk.updateUpdateApnConfig(
                object : EsperDeviceSDK.Callback<Int> {
                    override fun onResponse(response: Int?) {
                        showSdkMethodOutput("onResponse: $response")
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e(TAG, "updateApn: Failure occurred.", throwable)
                        showSdkMethodFailureOutput(throwable)
                    }
                }, apnId, apnConfigJsonString
            )
        }

        loadInputType(
            TwoTextField(
                primaryHint = primaryInputHint,
                primaryText = primarySampleInputConfigJsonString,
                secondaryHint = secondaryInputHint,
                buttonClickListener = buttonClickExecutor
            )
        )
    }

    /**
     * Method to Update the App Configurations.
     */
    private fun updateAppConfigurations() {
        val inputHint = getString(R.string.app_configurations_json_string)
        val sampleConfigJsonString =
            SampleJsonStringProvider.getSampleManagedAppConfigurationsJsonString()

        val buttonClickExecutor = OnClickListener {
            val configJsonString = getPrimaryInputEditTextInput()

            // Update the app configuration
            sdk.updateAppConfigurations(
                configJsonString,
                object : EsperDeviceSDK.Callback<Boolean> {
                    override fun onResponse(response: Boolean?) {
                        showSdkMethodOutput("onResponse: $response")
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e(TAG, "updateAppConfigurations: Failure occurred.", throwable)
                        showSdkMethodFailureOutput(throwable)
                    }
                })
        }

        loadInputType(
            OneTextField(
                hint = inputHint,
                text = sampleConfigJsonString,
                buttonClickListener = buttonClickExecutor
            )
        )
    }

    /**
     * Method to Launch EEA APIs Demo.
     */
    private fun launchEeaApisDemo() {
        val intent = packageManager.getLaunchIntentForPackage(SAMPLE_EEA_APP_PACKAGE_NAME)

        if (intent == null) {
            Toast.makeText(this, "Error: Demo App Not Installed", Toast.LENGTH_LONG).show()
            return
        }

        startActivity(intent)
    }

    /*
        ||===================================================================================||
        ||------------------------- Esper Agent SDK Method(s) : END -------------------------||
        ||===================================================================================||
     */

    private fun resetOutputContainer() {
        binding.apply {
            sdkMethodOutputTextView.text = ""
            outputContainer.visibility = View.GONE
        }
    }

    private fun showSdkMethodFailureOutput(throwable: Throwable) {
        showSdkMethodOutput("Failure: $throwable}", true)
    }

    private fun showSdkMethodOutput(output: String, isFailure: Boolean = false) {
        runOnUiThread {
            binding.apply {
                sdkMethodOutputTextView.text = output

                val color = if (isFailure) {
                    ContextCompat.getColor(this@MainActivity, R.color.red_failure)
                } else {
                    ContextCompat.getColor(this@MainActivity, android.R.color.black)
                }

                sdkMethodOutputTextView.setTextColor(color)

                outputContainer.visibility = View.VISIBLE
            }
        }
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
                inputType.switchCheckedChangeListener?.let { onCheckedChangeListener ->
                    setSwitchInputOnCheckedChangeListener(onCheckedChangeListener)
                }
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

        binding.inputContainer.visibility = View.VISIBLE
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

    private fun getSecondaryInputEditTextInput() = binding.secondaryInputEditText.text.toString()

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

    private fun setSwitchInputOnCheckedChangeListener(onCheckedChangeListener: OnCheckedChangeListener) {
        binding.switchInput.setOnCheckedChangeListener(onCheckedChangeListener)
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

    private fun setAndShowSdkInfoCard(message: String? = null) {
        val esperAgentDeviceSdkApiLevel = sdk.apiLevel
        val esperAgentDeviceSdkReleaseName = sdk.apiReleaseName

        val sdkInfo = message ?: String.format(
            getString(R.string.api_level_prefix),
            esperAgentDeviceSdkApiLevel,
            esperAgentDeviceSdkReleaseName
        )

        binding.apply {
            sdkInfoTextView.text = sdkInfo
            sdkInfoCard.visibility = View.VISIBLE
        }
    }

    private fun setupSdkPlayground() {
        setAndShowSdkInfoCard()

        setShowActivateSdkCardButtonClickListener()
        setActivateSdkCardButtonClickListener()

        setSdkMethodsDropdown()

        binding.sdkPlaygroundContainer.visibility = View.VISIBLE
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private inner class SdkMethodSelectListener : AdapterView.OnItemSelectedListener {
        private val TAG = "SdkMethodSelectListener"

        private val sdkMethodList = resources.getStringArray(R.array.sdkMethods)

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            // Reset the output container
            resetOutputContainer()

            // Reset the input container
            resetInputContainer()

            if (!isSelectedPositionValid(position)) {
                Log.e(TAG, "onItemSelected: Invalid method index")
                return
            }

            try {
                when (sdkMethodList[position]) {
                    getString(R.string.add_apn) -> addApn()
                    getString(R.string.allow_power_off) -> allowPowerOff()
                    getString(R.string.change_app_state) -> changeAppState()
                    getString(R.string.clear_app_data) -> clearAppData()
                    getString(R.string.config_no_network_fallback) -> configNoNetworkFallback()
                    getString(R.string.enable_mobile_data) -> enableMobileData()
                    getString(R.string.enable_wifi_tethering) -> enableWifiTethering()
                    getString(R.string.get_device_settings) -> getDeviceSettings()
                    getString(R.string.get_esper_device_info) -> getEsperDeviceInfo()
                    getString(R.string.get_esper_removable_storage_path) -> getEsperRemovableStoragePath()
                    getString(R.string.reboot) -> reboot()
                    getString(R.string.remove_apn) -> removeApn()
                    getString(R.string.set_app_op_mode) -> setAppOpMode()
                    getString(R.string.set_brightness) -> setBrightness()
                    getString(R.string.set_default_apn) -> setDefaultApn()
                    getString(R.string.set_global_setting) -> setGlobalSetting()
                    getString(R.string.set_device_orientation) -> setDeviceOrientation()
                    getString(R.string.set_system_setting) -> setSystemSetting()
                    getString(R.string.show_dock) -> showDock()
                    getString(R.string.start_dock) -> startDock()
                    getString(R.string.stop_dock) -> stopDock()
                    getString(R.string.update_apn) -> updateApn()
                    getString(R.string.update_app_configurations) -> updateAppConfigurations()
                    getString(R.string.launch_eea_apis_demo) -> launchEeaApisDemo()
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

        private const val SAMPLE_EEA_APP_PACKAGE_NAME = "io.esper.sdkeeasample"

        private fun getAndroidVersionsMinText(context: Context, minApiLevel: Int) =
            context.getString(R.string.android_versions_min, minApiLevel, Build.VERSION.SDK_INT)

        private fun getFileNameList(parentFile: File): String {
            val filesListBuilder = StringBuilder()

            if (parentFile.exists()) {
                val allFiles = parentFile.listFiles()
                allFiles?.let {
                    for (file in allFiles) {
                        file?.apply {
                            if (isDirectory) {
                                filesListBuilder.append(getFileNameList(file))
                            } else {
                                filesListBuilder.append("${file.name}\n")
                            }
                        }
                    }
                }
            }

            return filesListBuilder.toString()
        }
    }
}