package com.example.bill.googleoauthandroidcapturecallback;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.temboo.Library.CloudMine.ObjectStorage.ObjectDelete;
import com.temboo.Library.CloudMine.ObjectStorage.ObjectGet;
import com.temboo.core.TembooSession;

import java.util.concurrent.TimeUnit;

/**
 * Created by Burim on 3/14/2016. This class is an asynchronous task, which communicates with
 * cloudmine to get the hardware data.
 */
public class getDataAsyncTask extends AsyncTask<Void, Void, Void> {
    private String value = "1"; // Button value
    private String epoch; // Moment when the signal was sent.
    private boolean stop = false;
    private long wait;
    private long alarmT;

    public getDataAsyncTask(long alarmT){
        // If the alarm will start in 10 hours, there is no need for us to start querying the
        // database at this point. Set a wait time
        this.wait = alarmT - System.currentTimeMillis();
        this.value = "0";
        this.alarmT = alarmT;
        Log.d("Wait", Long.toString(this.wait));

    }

    @Override
    protected Void doInBackground(Void... arg0) {
        Log.d("Hardware", "Work");
        try {
            Log.d("Wait", "Wait");
            TimeUnit.SECONDS.sleep(this.wait/1000);
            Log.d("Wait", "Waited");
        } catch (InterruptedException e) {
            Log.d("Wait", "Error");
            //e.printStackTrace();
        }

        // Temboo is used to download and delete data from the database. After an event is received
        // it is deleted. The database is queried only every 2 minutes. The device only sends info
        // every 2 minutes as well
        while (!this.stop) {
            // Wait time, to make sure that this does not run before sensor sends info, and we do not use all choreos.
            if(System.currentTimeMillis() > this.alarmT) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // When alarm time comes
                try {
                    // Get one event at a time from hardware DB
                    TembooSession session = new TembooSession("pambuku", "finalProject", "hyJZSjG0nePuDM1iXhsRYvFn2SnGiwx2");
                    ObjectGet objectGetChoreo = new ObjectGet(session);
                    // Get an InputSet object for the choreo
                    ObjectGet.ObjectGetInputSet objectGetInputs = objectGetChoreo.newInputSet();
                    // Set inputs
                    objectGetInputs.set_APIKey("bbf381a5e0f6402c8be36338460424cd");
                    objectGetInputs.set_ApplicationIdentifier("a52a6a50c2a88b4d21598ad6bf3b6de9");
                    objectGetInputs.set_Limit("1");
                    ObjectGet.ObjectGetResultSet objectGetResults = objectGetChoreo.execute(objectGetInputs);
                    Log.d("Get Data", objectGetResults.get_Response());
                    // It is not a Json object, so I will just use string methods to parse the output
                    String[] jel = objectGetResults.get_Response().split(":");
                    if (jel.length >= 3) {
                        jel = jel[2].split("\\}");
                        String val = jel[0].replace("]", "");
                        val = val.replace("[", "");
                        jel = val.split(",");
                        this.value = jel[1];
                        String num = jel[1];
                        Log.d("Stop", this.value);
                        //Log.d("Stop", this.epoch);
                        //Log.d("Stop", Long.toString(this.alarmT));
                        this.epoch = jel[0];

                        // If we have a signal to sop, and this signal, has a proper stop time. Stop
                        // Set variable to leave while loop
                        if (num.trim().equals("0") && Long.valueOf(this.epoch) > this.alarmT) {
                            this.stop = true;
                            Log.d("Stop", "Found Stop");
                        }
                    }

                    // Delete the object from DB
                    ObjectDelete objectDeleteChoreo = new ObjectDelete(session);
                    // Get an InputSet object for the choreo
                    ObjectDelete.ObjectDeleteInputSet objectDeleteInputs = objectDeleteChoreo.newInputSet();
                    // Set inputs
                    objectDeleteInputs.set_APIKey("bbf381a5e0f6402c8be36338460424cd");
                    objectDeleteInputs.set_Keys(this.epoch);
                    objectDeleteInputs.set_ApplicationIdentifier("a52a6a50c2a88b4d21598ad6bf3b6de9");
                    // Execute Choreo
                    ObjectDelete.ObjectDeleteResultSet objectDeleteResults = objectDeleteChoreo.execute(objectDeleteInputs);
                } catch (Exception e) {
                    // if an exception occurred, log it
                    Log.e("CloudError", e.getMessage());
                }
            }
        } // while
        return null;
    } //doInBackground

    @Override

    // When this post executes stop the alarm, and get the evnt for teh next day
    protected void onPostExecute(Void param){
        MainActivity mainAc1 = MainActivity.getInstance();
        if(this.stop) {
            Log.d("Stop", "Stop");
            mainAc1.getPlayer().stop();
            mainAc1.onAccessToken(mainAc1.getToken());
        }
    }
}
