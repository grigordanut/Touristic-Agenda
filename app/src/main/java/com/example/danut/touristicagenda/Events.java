package com.example.danut.touristicagenda;

import com.google.firebase.database.Exclude;

public class Events {

    private String event_Date;
    private String event_Name;
    private String event_Place;
    private String event_Message;
    private String event_Image;
    private String user_Key;
    private String eventLocationKey;

    private String event_Key;

    public Events() {

    }

    public Events(String event_Date, String event_Name, String event_Place, String event_Message, String event_Image, String user_Key, String eventLocationKey) {
        this.event_Date = event_Date;
        this.event_Name = event_Name;
        this.event_Place = event_Place;
        this.event_Message = event_Message;
        this.event_Image = event_Image;
        this.user_Key = user_Key;
        this.eventLocationKey = eventLocationKey;
    }

    public String getEvent_Date() {
        return event_Date;
    }

    public void setEvent_Date(String event_Date) {
        this.event_Date = event_Date;
    }

    public String getEvent_Name() {
        return event_Name;
    }

    public void setEvent_Name(String event_Name) {
        this.event_Name = event_Name;
    }

    public String getEvent_Place() {
        return event_Place;
    }

    public void setEvent_Place(String event_Place) {
        this.event_Place = event_Place;
    }

    public String getEvent_Message() {
        return event_Message;
    }

    public void setEvent_Message(String event_Message) {
        this.event_Message = event_Message;
    }

    public String getEvent_Image() {
        return event_Image;
    }

    public void setEvent_Image(String event_Image) {
        this.event_Image = event_Image;
    }

    public String getUser_Key() {
        return user_Key;
    }

    public void setUser_Key(String user_Key) {
        this.user_Key = user_Key;
    }

    public String getEventLocationKey() {
        return eventLocationKey;
    }

    public void setEventLocationKey(String eventLocationKey) {
        this.eventLocationKey = eventLocationKey;
    }

    @Exclude
    public String getEvent_Key() {
        return event_Key;
    }

    @Exclude
    public void setEvent_Key(String event_Key) {
        this.event_Key = event_Key;
    }
}
