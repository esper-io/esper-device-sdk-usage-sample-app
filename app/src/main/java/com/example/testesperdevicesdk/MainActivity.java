package com.example.testesperdevicesdk;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.esper.devicesdk.EsperDeviceSDK;
import io.esper.devicesdk.constants.AppOpsPermissions;
import io.esper.devicesdk.models.EsperDeviceInfo;
import io.esper.devicesdk.utils.EsperSDKVersions;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView activateSDKtext = findViewById(R.id.activateSDKtext);
        TextView isSDKactivated = findViewById(R.id.isSDKactivated);
        TextView getEsperDeviceInfo = findViewById(R.id.getEsperDeviceInfo);
        TextView clearAppData = findViewById(R.id.clearAppData);
        TextView setAppOpMode = findViewById(R.id.setAppOpMode);
        TextView showDock = findViewById(R.id.showDock);
        TextView stopDock = findViewById(R.id.stopDock);
        TextView enableWifiTethering = findViewById(R.id.enableWifiTethering);

        //Once you add external library file into app/libs/ and sync your build.gradle with it, you can access below APIs
        EsperDeviceSDK sdk = EsperDeviceSDK.getInstance(getApplicationContext());

        //Enter "token" which is generated at your endpoint under API Management section
        String token = "<API-Token>";

        ArrayList<String> appsToBeCleared = new ArrayList<String>();
        //appsToBeCleared.add("io.esper.experiment.espertestlauncher.b");
        appsToBeCleared.add("com.google.android.wearable.app");

        //API to activate SDK before use
        sdk.activateSDK(token, new EsperDeviceSDK.Callback<Void>() {
            @Override
            public void onResponse(@Nullable Void aVoid) {
                activateSDKtext.append("Activation was successful");

                //API to check activation still persists
                sdk.isActivated(new EsperDeviceSDK.Callback<Boolean>() {
                    @Override
                    public void onResponse(Boolean active) {
                        if (active) {
                            //SDK is activated
                            isSDKactivated.append("SDK is activated");
                        } else {
                            //SDK is not activated
                            isSDKactivated.append("SDK is not activated");
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        //There was an issue retrieving activation status
                        isSDKactivated.append("SDK activation check failed");
                        t.printStackTrace();
                    }
                });

                // API to get device metadata
                sdk.getEsperDeviceInfo(new EsperDeviceSDK.Callback<EsperDeviceInfo>() {
                    @Override
                    public void onResponse(@Nullable EsperDeviceInfo esperDeviceInfo) {
                        String deviceId = esperDeviceInfo.getDeviceId();
                        if (sdk.getAPILevel() >= EsperSDKVersions.TESSARION_MR2) {
                            String serialNo = esperDeviceInfo.getSerialNo();
                            String imei1 = esperDeviceInfo.getImei1();
                            String imei2 = esperDeviceInfo.getImei2();
                            getEsperDeviceInfo.append("\nserialNo: " + serialNo + "\nimei1: " + imei1 + "\nimei2: " + imei2);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                        getEsperDeviceInfo.append("Failure");
                    }
                });

                /**
                 * @param packageNames - list of package names whose data is to be cleared
                 * @param callback     - callback implementation to be invoked upon completion
                 *                       of the operation.
                 */
                // API to clear specified apps' data. Will take a moment to clear the data and hence to show the status.
                sdk.clearAppData(appsToBeCleared, new EsperDeviceSDK.Callback<ArrayList<String>>() {
                    @Override
                    public void onResponse(@Nullable ArrayList<String> response) {
                        clearAppData.append("cleared successfully");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        clearAppData.append("Failure / no specified package found");
                    }
                });

                /**
                 * @param appOpMode - integer value of the AppOp permission for which grant status is to be set
                 * @param granted   - true or false
                 * @param callback  - callback implementation to be invoked upon completion
                 *                    of the operation.
                 */
                // API to set specified permission, can check available permissions into io.esper.devicesdk.constants.AppOpsPermissions
                sdk.setAppOpMode(AppOpsPermissions.OP_WRITE_SETTINGS, true, new EsperDeviceSDK.Callback<Void>() {
                    @Override
                    public void onResponse(@Nullable Void response) {
                        setAppOpMode.append("Successfully set the permissions");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        setAppOpMode.append("Failed to set the permissions");
                        t.printStackTrace();
                    }
                });

                sdk.showDock(new EsperDeviceSDK.Callback<Void>() {
                    @Override
                    public void onResponse(Void response) {
                        showDock.append("success");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                        showDock.append("failure");
                    }
                });

                sdk.stopDock(new EsperDeviceSDK.Callback<Void>() {
                    @Override
                    public void onResponse(Void response) {
                        stopDock.append("success");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                        stopDock.append("failure");
                    }
                });

                sdk.enableWifiTethering("abcd", "", false, new EsperDeviceSDK.Callback<String>() {
                    @Override
                    public void onResponse(@Nullable String response) {
                        enableWifiTethering.append("onResponse: " + response);
                    }
                    @Override
                    public void onFailure(Throwable t) {
                        enableWifiTethering.append("onFailure: "+ t);
                    }
                });

            }
            @Override
            public void onFailure(Throwable throwable) {
                activateSDKtext.append("Activation failure");
            }
        });
    }
}
