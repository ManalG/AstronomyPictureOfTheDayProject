package com.barmej.apod;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.barmej.apod.entity.AstronomyObject;
import com.barmej.apod.fragment.AboutFragment;
import com.barmej.apod.network.NetworkUtils;
import com.barmej.apod.utils.AstronomyPictureDataParser;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_DATE = 0;
    private static final int WRITE_PERMISSION = 130;

    private TextView mTextViewTitle;
    private TextView mTextViewExplanation;
    private TouchImageView mTouchImageView;
    private WebView mWebView;
    private NetworkUtils mNetworkUtils;

    String currentDay = String.valueOf(java.time.LocalDate.now());
    AstronomyObject astronomyObject = null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewTitle = findViewById(R.id.text_view_title);
        mTextViewExplanation = findViewById(R.id.text_view_explanation);
        mTouchImageView = findViewById(R.id.img_picture_view);
        mWebView = findViewById(R.id.wv_video_player);

        mNetworkUtils = NetworkUtils.getInstance(MainActivity.this);

        requestMediaInfo(currentDay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_pick_day) {
            startActivityForResult(new Intent(MainActivity.this, DatePickerActivity.class), REQUEST_DATE);
        } else if (id == R.id.action_download_hd) {
            checkWritePermission();
        } else if (id == R.id.action_share) {
            shareImage(astronomyObject.getUrl(), this);
        } else if (id == R.id.action_about) {
            AboutFragment aboutFragment = new AboutFragment();
            aboutFragment.show(getSupportFragmentManager(), "fragment_about");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DATE && resultCode == RESULT_OK) {
            requestMediaInfo(loadDate());
        }
    }

    private String loadDate() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.APP_PREF, MODE_PRIVATE);
        return sharedPreferences.getString(Constants.DATE, String.valueOf(java.time.LocalDate.now()));
    }

    private void requestMediaInfo(String date){
        final String astronomyRequestUrl = NetworkUtils.buildUrl(this, date).toString();
        JsonObjectRequest astronomyRequest = new JsonObjectRequest(
                Request.Method.GET,
                astronomyRequestUrl,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Weather Request Received!");
                        try {
                            astronomyObject = AstronomyPictureDataParser.getInfoObjectFromJson(response);
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                        if (astronomyObject != null){
                            if(astronomyObject.getMediaType().equals("image")){
                                mWebView.setVisibility(View.GONE);
                                mTouchImageView.setVisibility(View.VISIBLE);
                                Picasso.get().load(astronomyObject.getUrl()).into(mTouchImageView);
                            } else {
                                mTouchImageView.setVisibility(View.GONE);
                                mWebView.setVisibility(View.VISIBLE);
                                mWebView.loadUrl(astronomyObject.getUrl());
                            }
                            mTextViewTitle.setText(astronomyObject.getTitle());
                            mTextViewExplanation.setText(astronomyObject.getExplanation());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
        astronomyRequest.setTag(TAG);
        mNetworkUtils.addToRequestQueue(astronomyRequest);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                downloadFile();
            } else {
                Toast.makeText(this, R.string.error_permissions_download_failed, Toast.LENGTH_LONG).show();
            }
        }
    }


    private void checkWritePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION);
        } else {
            downloadFile();
        }
    }

    private void downloadFile(){
        if(astronomyObject.getHdurl() == null)
            return;

        Uri uri = Uri.parse(astronomyObject.getHdurl());
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(astronomyObject.getTitle());
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getResources().getString(R.string.app_name));
        request.setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(astronomyObject.getHdurl())));
        downloadManager.enqueue(request);
    }

    public void shareImage(String url, final Context context) {
        Picasso.get().load(url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap, context));
                context.startActivity(Intent.createChooser(i, "Share Image"));
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Toast.makeText(MainActivity.this, R.string.error_share_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }
    static public Uri getLocalBitmapUri(Bitmap bmp, Context context) {
        Uri bmpUri = null;
        try {
            File file =  new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            findViewById(R.id.bottom_sheet).setVisibility(View.GONE);
            getSupportActionBar().hide();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            findViewById(R.id.bottom_sheet).setVisibility(View.VISIBLE);
            getSupportActionBar().show();
        }
    }
}
