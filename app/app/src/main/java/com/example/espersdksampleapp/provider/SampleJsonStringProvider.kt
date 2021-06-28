package com.example.espersdksampleapp.provider

import android.provider.Telephony
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object SampleJsonStringProvider {

    private const val TAG = "SampleJSONProvider"

    @JvmStatic
    fun getSampleApnJsonConfigString(): String {
        val apnConfigValues = JSONObject()

        try {
            apnConfigValues.put(Telephony.Carriers.NAME, "Airtel SDK")
            apnConfigValues.put(Telephony.Carriers.APN, "Airtel SDK")
            apnConfigValues.put(Telephony.Carriers.PROXY, "")
            apnConfigValues.put(Telephony.Carriers.PORT, "")
            apnConfigValues.put(Telephony.Carriers.MMSPROXY, "")
            apnConfigValues.put(Telephony.Carriers.MMSPORT, "")
            apnConfigValues.put(Telephony.Carriers.USER, "")
            apnConfigValues.put(Telephony.Carriers.SERVER, "")
            apnConfigValues.put(Telephony.Carriers.PASSWORD, "")
            apnConfigValues.put(Telephony.Carriers.MMSC, "")
            apnConfigValues.put(Telephony.Carriers.AUTH_TYPE, "-1")
            apnConfigValues.put(Telephony.Carriers.PROTOCOL, "IPV4V6")
            apnConfigValues.put(Telephony.Carriers.ROAMING_PROTOCOL, "IPV4V6")
            apnConfigValues.put(Telephony.Carriers.TYPE, "")
            apnConfigValues.put(Telephony.Carriers.MCC, "")
            apnConfigValues.put(Telephony.Carriers.MNC, "")
            apnConfigValues.put(Telephony.Carriers.NUMERIC, "40445")
            apnConfigValues.put(Telephony.Carriers.CURRENT, "1")
            apnConfigValues.put(Telephony.Carriers.BEARER, "0")
            apnConfigValues.put(Telephony.Carriers.MVNO_TYPE, "")
            apnConfigValues.put(Telephony.Carriers.MVNO_MATCH_DATA, "")
            apnConfigValues.put(Telephony.Carriers.CARRIER_ENABLED, "1")
        } catch (e: Exception) {
            Log.e(TAG, "getDummyApnJsonConfigString: ", e)
        }

        return apnConfigValues.toString()
    }

    @JvmStatic
    fun getSampleManagedAppConfigurationsJsonString(): String {
        // Preparing chrome package json object
        val packageName = "com.android.chrome"
        val urlBlacklistKey = "URLBlacklist"
        val urlBlacklist = arrayOf("instagram.com")
        val urlWhitelistKey = "URLWhitelist"
        val urlWhitelist = arrayOf("*")
        val forceGoogleSafeSearchKey = "ForceGoogleSafeSearch"
        val forceGoogleSafeSearch = "true"
        val homepageLocationKey = "HomepageLocation"
        val homepageLocation = "https://esper.io/"
        val chromePackageJsonObject = JSONObject()
        try {
            chromePackageJsonObject.put(urlBlacklistKey, JSONArray(urlBlacklist))
            chromePackageJsonObject.put(urlWhitelistKey, JSONArray(urlWhitelist))
            chromePackageJsonObject.put(forceGoogleSafeSearchKey, forceGoogleSafeSearch)
            chromePackageJsonObject.put(homepageLocationKey, homepageLocation)
        } catch (jsonException: JSONException) {
            Log.e(TAG, "getDummyCustomSettingsJsonConfigString: ", jsonException)
        }

        // Preparing managed app config values json object
        val managedAppConfigValuesJsonObject = JSONObject()
        try {
            managedAppConfigValuesJsonObject.put(packageName, chromePackageJsonObject)
        } catch (jsonException: JSONException) {
            Log.e(TAG, "getDummyCustomSettingsJsonConfigString: ", jsonException)
        }

        // Preparing custom setting config values json object
        val managedAppConfigurationsKey = "managedAppConfigurations"
        val jsonObject = JSONObject()
        try {
            jsonObject.put(managedAppConfigurationsKey, managedAppConfigValuesJsonObject)
        } catch (jsonException: JSONException) {
            Log.e(TAG, "getDummyCustomSettingsJsonConfigString: ", jsonException)
        }

        return jsonObject.toString()
    }

    @JvmStatic
    fun getSampleNoNetworkFallbackConfigJsonString(): String {
        val networkFallbackEnabledKey = "networkFallbackEnabled"
        val fallbackDurationFlightModeOnKey = "fallbackDurationFlightModeOn"
        val fallbackDurationOffKey = "fallbackDurationOff"
        val fallbackDurationRebootKey = "fallbackDurationReboot"
        val maxResetsInDayKey = "maxResetsInDay"
        val networkFallbackActionKey = "networkFallbackAction"

        val jsonObject = JSONObject()
        try {
            jsonObject.put(networkFallbackEnabledKey, true)
            jsonObject.put(fallbackDurationFlightModeOnKey, 30000)
            jsonObject.put(fallbackDurationOffKey, 30000)
            jsonObject.put(fallbackDurationRebootKey, 60000)
            jsonObject.put(maxResetsInDayKey, 3)
            jsonObject.put(networkFallbackActionKey, 3)
        } catch (jsonException: JSONException) {
            Log.e(TAG, "getDummyNoNetworkFallbackConfigJsonString(): ", jsonException)
        }

        return jsonObject.toString()
    }
}