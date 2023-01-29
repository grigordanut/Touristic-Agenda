package com.example.danut.touristicagenda;

import com.google.firebase.database.Exclude;

public class EventLocation {

    private double event_Latitude;
    private double event_Longitude;
    private String event_Location;
    private String eventLocation_Key;

    public EventLocation() {

    }

    public EventLocation(double event_Latitude, double event_Longitude, String event_Location) {
        this.event_Latitude = event_Latitude;
        this.event_Longitude = event_Longitude;
        this.event_Location = event_Location;
    }

    public double getEvent_Latitude() {
        return event_Latitude;
    }

    public void setEvent_Latitude(double event_Latitude) {
        this.event_Latitude = event_Latitude;
    }

    public double getEvent_Longitude() {
        return event_Longitude;
    }

    public void setEvent_Longitude(double event_Longitude) {
        this.event_Longitude = event_Longitude;
    }

    public String getEvent_Location() {
        return event_Location;
    }

    public void setEvent_Location(String event_Location) {
        this.event_Location = event_Location;
    }

    @Exclude
    public String getEventLocation_Key() {
        return eventLocation_Key;
    }

    @Exclude
    public void setEventLocation_Key(String eventLocation_Key) {
        this.eventLocation_Key = eventLocation_Key;
    }
}
