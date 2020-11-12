package com.barmej.apod.network;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.barmej.apod.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NetworkUtils {

    private static String TAG = NetworkUtils.class.getSimpleName();

    private static final String BASE_URL = "https://api.nasa.gov/planetary/apod";

    private static final String DATE_PARAM = "date";
    private static final String HD_PARAM = "hd";
    private static final String API_ID_PARAM = "api_key";

    private static Context mContext;
    private static NetworkUtils sInstance;
    private static final Object LOCK = new Object();

    private RequestQueue mRequestQueue;

    public static NetworkUtils getInstance(Context context){
        if(sInstance == null){
            synchronized (LOCK) {
                if (sInstance == null)
                    sInstance = new NetworkUtils(context);
            }
        }
        return sInstance;
    }

    private NetworkUtils (Context context){
        mContext = context.getApplicationContext();
        mRequestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue(){
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request){
        getRequestQueue().add(request);
    }

    public void cancelRequest(String tag){
        getRequestQueue().cancelAll(tag);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static URL buildUrl(Context context, String date){
        Uri.Builder uriBuilder = Uri.parse(BASE_URL).buildUpon();
        Uri uri = uriBuilder
                .appendQueryParameter(DATE_PARAM, date)
                .appendQueryParameter(HD_PARAM, "false")
                .appendQueryParameter(API_ID_PARAM, context.getString(R.string.api_key))
                .build();
        try {
            URL url = new URL(uri.toString());
            Log.d(TAG, "URL: " + url);
            return url;
        } catch (MalformedURLException e){
            e.printStackTrace();
            return null;
        }
    }

}
