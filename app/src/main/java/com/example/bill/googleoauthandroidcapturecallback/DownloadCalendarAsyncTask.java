package com.example.bill.googleoauthandroidcapturecallback;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.SimpleAdapter;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.api.rest.response.ObjectModificationResponse;

import com.cloudmine.api.CMApiCredentials;
import com.cloudmine.api.rest.CloudMineRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * Created by bill on 3/22/15, modified by burimderveni on 3/13/2016
 * This class is where the calendar events are listed. In this class the calendar events are downloaded.
 * The FIrst event of the following day is sent to DB.
 */

public class DownloadCalendarAsyncTask extends AsyncTask<Void, Void, Void> {
    private String access_token;
    private SimpleAdapter adapter;
    // EVent characteristics
    private String Epoch;
    private String Time;
    private long alarmT;
    private ArrayList<String> list = new ArrayList<String>();
    private ArrayList<HashMap<String,String>> data;
    private CalendarListerActivity parent;
    private static final String APP_ID = "337183ded56a4ffaaf526a5cbe0ca3ea";
    private static final String API_KEY = "d7218c11ec37447d9491dd8821b09214";


    public static String httpGet(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn =
                (HttpURLConnection) url.openConnection();

        if (conn.getResponseCode() != 200) {
            Log.d("HTTP", Integer.toString(conn.getResponseCode()));
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

    // Constructor
    public DownloadCalendarAsyncTask(ArrayList<HashMap<String,String>> data, String access_token, SimpleAdapter adapter, String time, CalendarListerActivity par){
        this.access_token = access_token;
        this.data = data;
        this.adapter = adapter;
        this.Time = time;
        this.parent = par;
    }

    // This is the do in background method. Calendar events are downloaded here. The earliest
    // event of teh following day is also determined here.
    @Override
    protected Void doInBackground(Void... params) {
        // Can also use Bearer HTTP header to avoid putting the access token in the URL string
        String getCalendarsUrl = "https://www.googleapis.com/calendar/v3/users/me/calendarList?access_token=" + access_token;
        Log.d("Calendar", getCalendarsUrl);
        try {
            String response = httpGet(getCalendarsUrl);
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(response);
            JsonArray cals = root.getAsJsonObject().get("items").getAsJsonArray();
            String cal_id = "";
            for (JsonElement cal : cals) {
                if (cal.getAsJsonObject().has("primary") && cal.getAsJsonObject().get("primary").getAsString().equals("true")) {
                    cal_id = cal.getAsJsonObject().get("id").getAsString();
                    Log.d("Calendars", cal_id);
                }
            }
            Log.d("Cal", cal_id);

            // Get the current date in proper google format, and ask for events only after this day
            // Get current system time
            double current = System.currentTimeMillis();
            //Log.d("current", Long.toString((long) current));
            Date eDate = new Date((long) current);
            DateFormat eFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            String formatted = eFormat.format(eDate).replaceAll("(\\d\\d)(\\d\\d)$", "$1:$2");
            //Log.d("Date", formatted);
            Calendar c = Calendar.getInstance();
            c.setTime(eFormat.parse(formatted));
            //Log.d("Date", eFormat.parse(formatted).toString());
            // Add 1 day to this time
            c.add(Calendar.DATE, 1);
            String dt = eFormat.format(c.getTime());
            //Log.d("Cal",Long.toString(c.getTime().getTime()));
            String[] a1 = dt.split("T|-");

            // Format in the proper google calendar ap format
            formatted = a1[0] + "-" + a1[1] + "-" + a1[2] + "T" +  "00:00:00" + "-" +a1[4].substring(0,2)+":"+a1[4].substring(2,4);
            SimpleDateFormat df = new SimpleDateFormat("yyyy MM dd HH:mm:ss Z");
            String dat1 =  a1[0] + " " + a1[1] + " " + a1[2] + " " + "00:00:00" + " GMT-" + a1[4];;
            current = df.parse(dat1).getTime();
            //Log.d("current", Long.toString((long) current));
            //formatted = eFormat.format(eFormat.parse(dat1));
            //Log.d("current", formatted);
            // Get the events from the first calendar listed
            String listEventsUrl = "https://www.googleapis.com/calendar/v3/calendars/" + cal_id
                    + "/events?" + "maxResults=2500&singleEvents=false" + "&access_token="
                    + access_token + "&timeMin=" + formatted;
            //Log.d("EventURL", listEventsUrl );
            // Make the api call, and parse results.
            String eventsResponse = httpGet(listEventsUrl);
            JsonParser jp2 = new JsonParser();
            JsonElement root2 = jp2.parse(eventsResponse);
            JsonArray events = root2.getAsJsonObject().get("items").getAsJsonArray();
            double min = 100000000000000000000000.0;

            // Iterate through events
            for (JsonElement event : events) {
                String start = "";
                try {
                    start = event.getAsJsonObject().get("start").getAsJsonObject().get("dateTime").getAsString();
                    //Log.d("Date", start);
                    //String name = event.getAsJsonObject().get("summary").getAsString();
                    String time = "";
                    String[] a = start.split("T|-");
                    for (int i = 0; i < a.length; i++) {
                        time = time + " " + a[i];
                        //Log.d("String", a[i]);
                    }
                    time = a[0] + " " + a[1] + " " + a[2] + " " + a[3] + " GMT-" + a[4];
                    Log.d("Time", time);
                    Date date = df.parse(time);
                    double epoch = date.getTime();

                    // Determine if this event is the smallest in the list
                    if (epoch > current && (epoch) < min) {
                        min = epoch;
                        Log.d("Min", "Done");
                        this.Epoch = Long.toString((long)min);
                        //this.Epoch = Long.toString(System.currentTimeMillis() + 20000);
                        this.Time = time;
                        //this.alarmT = System.currentTimeMillis() + 20000;
                        //Set the alarm time to 1 hour before the event.
                        this.alarmT = (long) min - 3600000;
                        Log.d("Out", Double.toString(min));
                    }
                } catch (Exception e) {
                    Log.e("Error", e.toString());

                }
                //Log.d("Min", "MIN not found");
            }
            HashMap<String, String> entry = new HashMap<String, String>();
            entry.put("start", this.Time);
            data.add(entry);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    // When this task ends
    @Override
    protected void onPostExecute(Void param) {
        adapter.notifyDataSetChanged();
        // Send the minimum time to the database
        parent.onsendDB(this.Time, Long.toString(this.alarmT));
        //parent.onReceiveDB(this.list);
        MainActivity mainAc = MainActivity.getInstance();
        // Set the alarm music to start on the alarm time
        mainAc.setTim(this.Epoch);
        Log.d("Alarm", Long.toString(this.alarmT));
        mainAc.alarmSet(this.alarmT);
    }
}