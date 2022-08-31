package com.example.myfirebaseapp;

public class ReadWriteUserDetails {
    public String fullName, doB, gender;

    //Constructor
    public ReadWriteUserDetails(){};

    public ReadWriteUserDetails(String textDoB, String textGender){
        this.doB = textDoB;
        this.gender = textGender;

    }
}
