package com.example.bill.googleoauthandroidcapturecallback;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bill on 3/22/15.
 */
public class OauthGoogleAsyncTask extends AsyncTask<Void, Void, Void> {
    private String code;
    private String key;
    private String secret;
    private String redirect;

    private MainActivity parent;

    private String access_token;

    public static String httpPost(String urlStr, String[] paramName,
                                  String[] paramVal) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn =
                (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
        conn.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");

        // Create the form content
        OutputStream out = conn.getOutputStream();
        Writer writer = new OutputStreamWriter(out, "UTF-8");
        for (int i = 0; i < paramName.length; i++) {
            writer.write(paramName[i]);
            writer.write("=");
            writer.write(URLEncoder.encode(paramVal[i], "UTF-8"));
            writer.write("&");
        }

        writer.close();
        out.close();

        if (conn.getResponseCode() != 200) {
            throw new IOException(conn.getResponseMessage());
        }

        // Buffer the result into a string
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();

        conn.disconnect();
        return sb.toString();
    }

    public OauthGoogleAsyncTask(MainActivity parent, String code, String key, String secret, String redirect) {
        this.code = code;
        this.key = key;
        this.secret = secret;
        this.redirect = redirect;
        this.parent = parent;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String url = "https://www.googleapis.com/oauth2/v3/token";
        String[] post_params = { "code", "client_id", "client_secret", "redirect_uri", "grant_type" };
        String[] post_values = { code, key, secret, redirect, "authorization_code" };

        try {
            String response = OauthGoogleAsyncTask.httpPost(url, post_params, post_values);

            // http://stackoverflow.com/questions/4818567/how-do-i-use-google-json-parsing-api-gson-to-parse-some-dynamic-fields-in-my-j
            Map response_json = new Gson().fromJson(response, new TypeToken<HashMap<String, String>>() {}.getType());

            access_token = (String) (response_json.get("access_token"));

            Log.d("MONGAN", "ACCESS TOKEN = " + access_token);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        parent.onAccessToken(access_token);
    }
}
