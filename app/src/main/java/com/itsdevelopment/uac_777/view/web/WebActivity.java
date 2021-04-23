package com.itsdevelopment.uac_777.view.web;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.appsflyer.AppsFlyerLib;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.itsdevelopment.uac_777.R;
import com.itsdevelopment.uac_777.view.BaseApplication;
import com.itsdevelopment.uac_777.view.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebActivity extends AppCompatActivity {


    private static final String TAG = "getSimpleName";
    private WebView mWebView;

    /**
     *  Variables and constants for WebChromeClient mechanics
     */
    public static final int INPUT_FILE_REQUEST_CODE = 1;

    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;


    /**
     * Static constants for conversions interception mechanics
     */
    public static final String FIRST_PART = "https://mytraffictracker.com/?page=Conversions&date=9&Offer_fltr=All&GEO=All&camp_id=All&search_name=*****";
    public static final String SECOND_PART = "***&api_key=10000010c505d4636e1a0a03cda7b50767b5160";

    public static final HashSet<String> REGISTRATION_VALUES_HASH_SET = new HashSet<>();
    public static final HashSet<String> SALE_VALUES_HASH_SET = new HashSet<>();


    /**
     * Custom for every UAC String constants
     */
    private String mCampaignKey;


    private CustomWebViewClient customWebViewClient;
    private Handler mHandler;
    private OkHttpClient mOkHttpClient;
    private BaseApplication mApplication;
    private FirebaseAnalytics mFirebaseAnalytics;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        initialization();
        webSettingsSetup();

        String cookies = CookieManager.getInstance().getCookie(Utils.HOST_URL);

        if (needToCatchConversion() && cookies!=null)
            goRequestsEveryTwoOrThreeMinutesIdk(retrieveCookie());
    }


    private void initialization()
    {
        mWebView = findViewById(R.id.web);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mApplication = (BaseApplication) getApplication();
        mCampaignKey = CustomWebViewClient.getCampaignKeyFromUrl(getIntent().getStringExtra(Utils.INTENT_EXTRA_LINK_KEY));

        try {
            customWebViewClient = new CustomWebViewClient(this, mCampaignKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Setup web based settings for WebView
     */
    private void webSettingsSetup() {
        mWebView.setWebViewClient(customWebViewClient);
        mWebView.setWebChromeClient(new WebChromeClient(){

            @Override
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {
                if(mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.e(TAG, "Unable to create Image File", ex);
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("image/*");

                Intent[] intentArray;
                if(takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

               startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

                return true;
            }
        });

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDisplayZoomControls(false);


        fillContent();
    }


    private void goRequestsEveryTwoOrThreeMinutesIdk(String uClickCookie) {
        fillStatusHashSet();

        mHandler = new Handler();
        mHandler.post(new Runnable() {
                         @Override
                         public void run() {

                             if (needToCatchConversion()) {
                                 mHandler.postDelayed(this, 3 * 60 * 1000); // every 3 minutes
                             }

                             URL url = null;

                             try {
                                 url = new URL(FIRST_PART + uClickCookie + SECOND_PART);
                             } catch (MalformedURLException e) { e.printStackTrace(); }

                             Request request = new Request.Builder()
                                     .url(url)
                                     .get()
                                     .build();

                             mOkHttpClient = new OkHttpClient();
                             mOkHttpClient.newCall(request).enqueue(new Callback() {

                                 @Override
                                 public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                     Log.d(Utils.DEBUG_TAG_1, "onResponse: FINISH " + call.toString());
                                 }

                                 @Override
                                 public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                     String stringResponse = response.body().string();
                                     Log.d(Utils.DEBUG_TAG_1, "onResponse: FINISH " + stringResponse);

                                     try {
                                         JSONArray jsonArray = new JSONArray(stringResponse);
                                         String status = jsonArray.getJSONObject(0).getString("status");

                                         if (REGISTRATION_VALUES_HASH_SET.contains(status)) {
                                             Log.d(Utils.DEBUG_TAG_1, "onResponse: "+status+" exist in"+ REGISTRATION_VALUES_HASH_SET);

                                             if (mApplication.mSharedPreferences.getString(Utils.SP_REGISTRATION, Utils.SP_DEFAULT).equals(Utils.SP_DEFAULT)) {
                                                 mApplication.mSharedPreferences.edit().putString(Utils.SP_REGISTRATION, status).commit();

                                                 Bundle bundle = new Bundle();
                                                 bundle.putString("Stub", "Stub");
                                                 mFirebaseAnalytics.logEvent("fb_mobile_complete_registration", bundle);

                                                 HashMap<String, Object> hashApps = new HashMap<>();
                                                 hashApps.put("fb_mobile_complete_registration", "fb_mobile_complete_registration");

                                                 AppsFlyerLib.getInstance().logEvent(
                                                         WebActivity.this,
                                                         "fb_mobile_complete_registration",
                                                         hashApps);

                                             }
                                         }

                                         if (SALE_VALUES_HASH_SET.contains(status)) {
                                             Log.d(Utils.DEBUG_TAG_1, "onResponse: "+status+" exist in"+ SALE_VALUES_HASH_SET);

                                             if (mApplication.mSharedPreferences.getString("sale", "NaN").equals("NaN")) {
                                                 mApplication.mSharedPreferences.edit().putString("sale", status).commit();

                                                 Bundle bundle = new Bundle();
                                                 bundle.putString("Stub", "Stub");
                                                 mFirebaseAnalytics.logEvent("Donate", bundle);

                                                 HashMap<String, Object> hashApps = new HashMap<>();
                                                 hashApps.put("Donate", "Donate");

                                                 AppsFlyerLib.getInstance().logEvent(
                                                         WebActivity.this,
                                                         "Donate",
                                                         hashApps);
                                             }
                                         }

                                     } catch (JSONException e) {
                                         e.printStackTrace();
                                     }
                                 }
                             });
                         }
                     });
    }


    /**
     * @return true if conversion already was cached
     */
    private boolean needToCatchConversion() {
        String statusSale = mApplication.mSharedPreferences.getString(Utils.SP_SALE, Utils.SP_DEFAULT);
        String statusRegistration = mApplication.mSharedPreferences.getString(Utils.SP_REGISTRATION, Utils.SP_DEFAULT);

        if (!statusSale.equals("NaN") && !statusRegistration.equals("NaN"))
            return false;
        else
            return true;
    }

    private static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    private void fillStatusHashSet() {
        REGISTRATION_VALUES_HASH_SET.add("reg");
        REGISTRATION_VALUES_HASH_SET.add("registration");
        REGISTRATION_VALUES_HASH_SET.add("new");
        REGISTRATION_VALUES_HASH_SET.add("lead");

        SALE_VALUES_HASH_SET.add("approved");
        SALE_VALUES_HASH_SET.add("sale");
    }


    /**
     * Load content into WebView
     */
    private void fillContent() {
        Intent intent = getIntent();
        String url = intent.getStringExtra(Utils.INTENT_EXTRA_LINK_KEY);
        mWebView.loadUrl(url);
    }

    /**
     * Implement correct work for "Back" button in WebView
     */
    @Override
    public void onBackPressed() {
        if(mWebView != null && mWebView.canGoBack())
            mWebView.goBack();
        else
            super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            customWebViewClient.onStop();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieve cookie for event handling in Firebase Analytics
     * @return cookie (uClick)
     */
    private String retrieveCookie() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
        } else {
            CookieManager.getInstance().setAcceptCookie(true);

        }

        String cookies = CookieManager.getInstance().getCookie(Utils.HOST_URL);
        Log.d(Utils.DEBUG_TAG_1, "All the cookies in a string:" + cookies);

        String result = null;

        if (cookies.contains("uclick=")) {
            result = cookies.substring(cookies.indexOf("uclick=") + 7, cookies.indexOf("uclick=") + 15);
            Log.d(Utils.DEBUG_TAG_1, "extractUclickCookie: " + result);
        }
        return result;
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if(requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        Uri[] results = null;

        // Check that the response is a good one
        if(resultCode == Activity.RESULT_OK) {
            if(data == null) {
                // If there is not data, then we may have taken a photo
                if(mCameraPhotoPath != null) {
                    results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                }
            } else {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }

        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;
        return;
    }


}