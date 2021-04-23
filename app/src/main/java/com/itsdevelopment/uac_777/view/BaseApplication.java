package com.itsdevelopment.uac_777.view;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.attribution.AppsFlyerRequestListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.itsdevelopment.uac_777.R;
import com.itsdevelopment.uac_777.view.utils.Utils;
import com.onesignal.OneSignal;

import java.util.Map;

import static com.itsdevelopment.uac_777.view.utils.Utils.SP_MAIN_LINK;


public class BaseApplication extends Application {

    // TODO: 24.03.2021 UAC-SPECIFIC
    // Updated for UAC-139
    private static final String ONE_SIGNAL_KEY = "7c9aa910-f6a9-4c27-abe9-9e938368c4cb";

    private FirebaseRemoteConfig mRemoteConfig;
    public SharedPreferences mSharedPreferences;


    @Override
    public void onCreate() {
        super.onCreate();

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONE_SIGNAL_KEY);

        // Shared Preferences initializtion
        mSharedPreferences = getSharedPreferences(Utils.SP_MAIN_LINK, MODE_PRIVATE);

        // RemoteConfig initialization
        mRemoteConfig = FirebaseRemoteConfig.getInstance();
        mRemoteConfig.setDefaultsAsync(R.xml.remote_config_default_values);
        mRemoteConfig.setConfigSettingsAsync(new FirebaseRemoteConfigSettings.Builder().build());

    }


    public FirebaseRemoteConfig getFirebaseRemoteConfig() {
        return mRemoteConfig;
    }

}
