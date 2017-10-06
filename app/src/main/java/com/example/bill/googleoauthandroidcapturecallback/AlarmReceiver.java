package com.example.bill.googleoauthandroidcapturecallback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Button;
//import com.example.alarmsound.MainActivity;

/**
 * Created by klausnuredini on 3/13/16.
 * This is where the alarm is set to start. This is a broadcast receiver.
 */
public class AlarmReceiver extends BroadcastReceiver{
    public AlarmReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        //final MediaPlayer mp = MediaPlayer.create(context, R.raw.music);
        Log.d("Music", "It went here.");
        //mp.start();

        MainActivity obj = MainActivity.getInstance();
        boolean result = obj.myMethod();
        MediaPlayer alarmSound = obj.sound();
        alarmSound.start();
        alarmSound.setLooping(true);
        //boolean result = intent.getBooleanExtra("some constant", false);
        Log.d("Result", Boolean.toString(result));
//        getDataAsyncTask as = new getDataAsyncTask();
    }
}

