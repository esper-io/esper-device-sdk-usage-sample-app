package com.example.espersdksampleapp.provider

import android.provider.Telephony
import android.util.Log
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
}