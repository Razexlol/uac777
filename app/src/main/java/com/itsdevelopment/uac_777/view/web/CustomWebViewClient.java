package com.itsdevelopment.uac_777.view.web;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.library.HttpClient;
import com.example.library.PushData;
import com.itsdevelopment.uac_777.view.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CustomWebViewClient
        extends WebViewClient
        implements Callback {

    private static final String SAMPLE_PLACEHOLDER = "placeholder-134";

    // TODO: 24.03.2021 UAC-SPECIFIC : UAC-134
    public static final String LORD_OF_APPS_KEY = "694b7b6cc64b4c6da4f55fdc4c179af6";

    private static String campaignKey;

    private String recent;
    private Date recentTime;
    private HttpClient mClientHttp;


    public CustomWebViewClient(Context context, @Nullable String campaignKey) throws JSONException {
        setRecentTime(new Date());
        setRecent(SAMPLE_PLACEHOLDER);

        this.campaignKey = campaignKey;
        makeAnalytics(context);
    }

    private void makeAnalytics(Context context) throws JSONException {
        mClientHttp = new HttpClient(new PushData(context));
        if (campaignKey !=null)
            mClientHttp.RegisterDevice(this, LORD_OF_APPS_KEY, campaignKey, null, null, null);
    }

    @Override
    public void onPageFinished(WebView view, String newUrl) {
        if ( !newUrl.equals(recent) && campaignKey != null ) {

            String timeOnLastPage =  String.valueOf(new Date().getTime() - getRecentTime().getTime());

            Log.d(Utils.DEBUG_TAG_1, timeOnLastPage);
            Log.d(Utils.DEBUG_TAG_1, "onPageFinished: " + newUrl);

            setRecent(newUrl);

            try {
                mClientHttp.AddAnalytics(this, getRecent(), timeOnLastPage);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            setRecentTime(new Date());
        }
        super.onPageFinished(view, newUrl);

    }

    public String getRecent() {
        return recent;
    }

    public void setRecent(String recent) {
        this.recent = recent;
    }

    public Date getRecentTime() {
        return recentTime;
    }

    public void setRecentTime(Date recentTime) {
        this.recentTime = recentTime;
    }

    /** OKHttp3 Callback implementation */
    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        Log.d(Utils.DEBUG_TAG_1, "onFailure: "+call.toString());
        e.printStackTrace();
    }
    /** OKHttp3 Callback implementation */
    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response){
        Log.d(Utils.DEBUG_TAG_1, "onResponse: "
                + call.toString() + "\n"
                + response.toString());
    }


    /**
     * Callback from {@link android.app.Activity} onStop() method. Send last
     * @throws JSONException
     */
    public void onStop() throws JSONException {
        Log.d(Utils.DEBUG_TAG_1, String.valueOf(new Date().getTime() - recentTime.getTime()));
        Log.d(Utils.DEBUG_TAG_1, "onPageFinished: " + recent);
        if (campaignKey !=null)
            mClientHttp.AddAnalytics(this, recent, String.valueOf(new Date().getTime() - recentTime.getTime()));
    }

    public static String getCampaignKeyFromUrl(String url)
    {
        return Uri.parse(url).getQueryParameter("key");
    }


}
