package com.itsdevelopment.uac_777.view.utils;

import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;

public final class Utils {

    /**
     * Shared Preferences
     */
    public static final String SP_REGISTRATION = "registration";;
    public static final String SP_SALE = "sale";
    public static final String SP_MAIN_LINK = "theSP";
    public static final String SP_DEFAULT = "NaN";


    public static final String DEBUG_TAG_1 = "sampleDebug"+"TD";

    public static final String INTENT_EXTRA_LINK_KEY = "theINTENT"+"intent";

    // TODO: 25.03.2021 UAC-SPECIFIC
    //Updated for UAC-139
    public static final String REMOTE_CONFIG_KEY_134 = "remoteConfig139";
    //Updated for UAC-139
    public static final String REMOTE_CONFIG_DEFAULT_134 = "remoteConfigDefault139";

    //Static
    public static final String HOST_URL = "https://mytraffictracker.com/";


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String decodeRemoteConfigLink(String rootUrl) {
        if (rootUrl == null && rootUrl.isEmpty()) {
            Log.d(DEBUG_TAG_1, "decodeFromBase64: Root String is null or empty");
            return null;
        } else {
            byte[] result = Base64.decode(rootUrl, Base64.NO_WRAP);
            String s = new String(result, StandardCharsets.UTF_8);
            return s;
        }
    }

}
