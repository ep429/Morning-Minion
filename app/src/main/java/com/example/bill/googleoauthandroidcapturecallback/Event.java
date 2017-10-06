package com.example.bill.googleoauthandroidcapturecallback;

/**
 * Created by Burim on 3/11/2016. This is used to send the data to cloudmine
 */
import com.cloudmine.api.db.LocallySavableCMObject;

public class Event extends LocallySavableCMObject {
    private String Time;
    private String Epoch;

    public Event(String time, String epoch){
        this.Time = time;
        this.Epoch = epoch;
    }

    // Getter and setter methods

    public String getTime(){return this.Time;}
    public void setTime(String time){this.Time = time;}

    public String getEpoch(){return this.Epoch;}
    public void setEpoch(String epoch) {this.Epoch = epoch;}

}
