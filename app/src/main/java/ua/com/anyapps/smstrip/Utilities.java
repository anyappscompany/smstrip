package ua.com.anyapps.smstrip;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Utilities {
    private static final String TAG = "debapp";

    public static int GETQuery(String requestURL, HashMap<String, String> getDataParams, HashMap<String, String> getHeaders, String authStr){
        String url = "";
        int responseCode = -1;

        try {
            url = requestURL + "?" + getUrlDataString(getDataParams);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "The Character Encoding is not supported. " + e.getMessage());
        }

        try {
            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestMethod("GET");
            if(authStr.length()>0){
                conn.setRequestProperty  ("Authorization", "Basic " + android.util.Base64.encodeToString(authStr.getBytes("UTF-8"), android.util.Base64.DEFAULT));
            }
            for(Map.Entry<String, String> entry : getHeaders.entrySet()){
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
            responseCode = conn.getResponseCode();
            return responseCode;

        } catch (Exception e) {
            Log.e(TAG, "GET query error. " + e.getMessage());
        }
        return responseCode;
    }

    public static int POSTQuery(String requestURL, HashMap<String, String> postDataParams, HashMap<String, String> postHeaders, String authStr){
        int responseCode = -1;

        //Log.d(TAG, "RU "+requestURL);
        //Log.d(TAG, "AS "+authStr);

        try {
            URL url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            if(authStr.length()>0){
                conn.setRequestProperty  ("Authorization", "Basic " + android.util.Base64.encodeToString(authStr.getBytes("UTF-8"), android.util.Base64.DEFAULT));
            }

            for(Map.Entry<String, String> entry : postHeaders.entrySet()){
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getUrlDataString(postDataParams));
            writer.flush();
            writer.close();
            os.close();
            responseCode = conn.getResponseCode();
            return responseCode;
        } catch (Exception e) {
            Log.e(TAG, "POST query error. " + e.getMessage());
        }
        return responseCode;
    }

    // параметры в строку
    public static String getUrlDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }
}
