package com.example.bill.googleoauthandroidcapturecallback;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.cloudmine.api.CMApiCredentials;
import com.cloudmine.api.rest.CloudMineRequest;
import com.cloudmine.api.rest.response.ObjectModificationResponse;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by burimderveni on 3/13/16.
 * This class is where the calendar events are listed. A Simple adaper is used to perform teh task.
 */

public class CalendarListerActivity extends ActionBarActivity {
    // Cloudmine credentials. We are assuming that no one will read these.
    private static final String APP_ID = "337183ded56a4ffaaf526a5cbe0ca3ea";
    private static final String API_KEY = "d7218c11ec37447d9491dd8821b09214";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_lister);

        // Initialize the cloudmine library.
        CMApiCredentials.initialize(APP_ID, API_KEY, getApplicationContext());
        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

        String[] from = {"start"};
        int[] to = {R.id.start};

        // Connect the adapter to the data array
        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.calendar_list_entry, from, to);

        // Connect the adapter to the list view
        ListView listView = (ListView) (findViewById(R.id.listView));
        listView.setAdapter(adapter);

        String time = "0";
        // Connect the adapter to the network call. Start downloading al the calendar events.
        String access_token = getIntent().getStringExtra("access_token");
        DownloadCalendarAsyncTask task = new DownloadCalendarAsyncTask(data, access_token, adapter, time, this);
        task.execute();
        if(task.getStatus() == AsyncTask.Status.FINISHED){
            Log.d("Out", time);
        }
    } // onCreate

    // This method sends the first event of teh following day to the DB
    public void onsendDB(String time, String epoch){
        Event ev =  new Event(time, epoch);
        CloudMineRequest req = ev.save(this, new Response.Listener<ObjectModificationResponse>() {
            @Override
            public void onResponse(ObjectModificationResponse modificationResponse) {
                Log.d("SAVE", "I was saved: " + modificationResponse.getCreatedObjectIds());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("SAVE", "Failed saving me", volleyError);
            }
        });
        Log.d("Cloud", req.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calendar_lister, menu);
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
