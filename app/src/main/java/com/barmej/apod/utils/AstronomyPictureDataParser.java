package com.barmej.apod.utils;

import com.barmej.apod.entity.AstronomyObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

import okhttp3.MediaType;

public class AstronomyPictureDataParser {

    private static final String APOD_COPYRIGHT = "copyright";
    private static final String APOD_DATE = "date";
    private static final String APOD_EXPLANATION = "explanation";
    private static final String APOD_HD_URL = "hdurl";
    private static final String APOD_MEDIA_TYPE = "media_type";
    private static final String APOD_SERVICE_VERSION = "service_version";
    private static final String APOD_TITLE = "title";
    private static final String APOD_URL = "url";


    public static AstronomyObject getInfoObjectFromJson (JSONObject astronomyInfoJson) throws JSONException {

        String copyright = astronomyInfoJson.getString(APOD_COPYRIGHT);
        String date = astronomyInfoJson.getString(APOD_DATE);
        String explanation = astronomyInfoJson.getString(APOD_EXPLANATION);
        String hdurl = astronomyInfoJson.getString(APOD_HD_URL);
        String mediaType = astronomyInfoJson.getString(APOD_MEDIA_TYPE);
        String serviceVersion = astronomyInfoJson.getString(APOD_SERVICE_VERSION);
        String title = astronomyInfoJson.getString(APOD_TITLE);
        String url = astronomyInfoJson.getString(APOD_URL);

        AstronomyObject astronomyObject = new AstronomyObject();
        astronomyObject.setCopyright(copyright);
        astronomyObject.setDate(date);
        astronomyObject.setExplanation(explanation);
        astronomyObject.setHdurl(hdurl);
        astronomyObject.setMediaType(mediaType);
        astronomyObject.setService_version(serviceVersion);
        astronomyObject.setTitle(title);
        astronomyObject.setUrl(url);

        return astronomyObject;

    }

}
