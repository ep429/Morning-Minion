package com.example.bill.googleoauthandroidcapturecallback;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.Calendar;

public class MainActivity extends ActionBarActivity {

    public static final String CONSUMER_SECRET = "5e5CFv-6fJC45kqcN8eUyTnP";
    public static final String CONSUMER_KEY = "323449297599-vppeirrc1nb19m8li43t8r7j4k6764i1.apps.googleusercontent.com";
    public static final String SCOPE = "https://www.googleapis.com/auth/calendar.readonly";
    public static final String OAUTH_CALLBACK_URL = "http://localhost";
    private String time;
    public MediaPlayer mp;
    private String accessToken;

    public boolean result = true;
    private static MainActivity instance;

    @Override
    protected void onStart() {
        super.onStart();
        instance = this;
    }

    public boolean myMethod(){
        return result;
    }

    public MediaPlayer sound() {
        return mp;
    }

    public static MainActivity getInstance(){
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mp = MediaPlayer.create(this, R.raw.music);

        //setContentView(R.layout.activity_main);
        String authorizationUrl = "https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=" +
                MainActivity.CONSUMER_KEY + "&redirect_uri=" + MainActivity.OAUTH_CALLBACK_URL +
                "&scope=" + MainActivity.SCOPE;
        Log.d("URL", authorizationUrl);

        Uri uri = Uri.parse(authorizationUrl);

        WebView webView = new WebView(this);

        // http://stackoverflow.com/questions/25664146/android-4-4-giving-err-cache-miss-error-in-onreceivederror-for-webview-back
        if (Build.VERSION.SDK_INT >= 19) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        // http://stackoverflow.com/questions/8846077/how-to-enable-java-script-into-webview
        webView.getSettings().setJavaScriptEnabled(true);

        // http://stackoverflow.com/questions/8273991/webview-shouldinterceptrequest-example
        webView.setWebViewClient(new OAuthWebViewClient(this));

        webView.loadUrl(authorizationUrl);
        setContentView(webView);

    } // On Create

    // Method to set the alarm at the specified time. In this method we also start the call
    // to the hardware database to get teh results from the database.
    public void alarmSet(long num){
        Calendar t = Calendar.getInstance();
        // Set start time after the given time
        t.setTimeInMillis(num);
        result = true;
        Context context = this;
        AlarmManager alarmMgr;
        alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("some_constant", result);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, t.getTimeInMillis(), pendingIntent);

        // Start getting hardware data to determine what is the right time to stop.
        getDataAsyncTask tsk = new getDataAsyncTask(num+1000);
        tsk.execute();
    }

    public MediaPlayer getPlayer(){
        return this.mp;
    }

    // This is called by the Web View Client after the user has authorized with a code
    public void onOAuthAuthorization(String code) {
        OauthGoogleAsyncTask task = new OauthGoogleAsyncTask(this, code, MainActivity.CONSUMER_KEY,
                MainActivity.CONSUMER_SECRET, MainActivity.OAUTH_CALLBACK_URL);
        task.execute();
    }

    // Method called to set the alarm time
    public String setTim(String tim){
        this.time = tim;
        return tim;
    }

    public String getToken(){
        return this.accessToken;
    }
    // This is called by the Google Oauth AsyncTask after completion
    public void onAccessToken(String access_token) {
        Log.d("MONGAN", "RECEIVED Access Token " + access_token);
        this.accessToken = access_token;
      //  ((TextView) findViewById(R.id.textView)).setText("Token Value: " + access_token);
        Intent i = new Intent(MainActivity.this, CalendarListerActivity.class);
        // Could store this in a preference file or database, and have onCreate check for it and try
        // using before performing OAuth, reverting to the OAuth procedure fired by onCreate
        i.putExtra("access_token", access_token);

        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
