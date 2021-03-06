package com.example.danut.touristicagenda;

import com.google.firebase.database.Exclude;

public class User {

    private String user_firstName;
    private String user_lastName;
    private String user_emailAddress;
    private String user_Key;

    public User(){

    }

    public User(String user_firstName, String user_lastName, String user_emailAddress) {
        this.user_firstName = user_firstName;
        this.user_lastName = user_lastName;
        this.user_emailAddress = user_emailAddress;
    }

    public String getUser_firstName() {
        return user_firstName;
    }

    public void setUser_firstName(String user_firstName) {
        this.user_firstName = user_firstName;
    }

    public String getUser_lastName() {
        return user_lastName;
    }

    public void setUser_lastName(String user_lastName) {
        this.user_lastName = user_lastName;
    }

    public String getUser_emailAddress() {
        return user_emailAddress;
    }

    public void setUser_emailAddress(String user_emailAddress) {
        this.user_emailAddress = user_emailAddress;
    }

    @Exclude
    public String getUser_Key() {
        return user_Key;
    }

    @Exclude
    public void setUser_Key(String user_Key) {
        this.user_Key = user_Key;
    }
}
