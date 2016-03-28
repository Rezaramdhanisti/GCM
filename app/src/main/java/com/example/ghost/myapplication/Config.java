package com.example.ghost.myapplication;

/**
 * Created by ghost on 22/03/2016.
 */
public interface Config {
    static final boolean SECOND_SIMULATOR =false;

    static final String YOUR_SERVER_URL = "http://192.168.3.1/gcmserver/";

    //Google Project id

    static final String GOOGLE_SENDER_ID = "153338835299 ";

    static final String TAG = "GCM Android Example";

    static final String DISPLAY_REGISTRATION_MESSAGE_ACTION = "com.example.ghost.myapplication.DISPLAY_REGISTRATION_MESSAGE";
    static final String DISPLAY_MESSAGE_ACTION = "com.example.ghost.myapplication.DISPLAY_MESSAGE";
    static final String EXTRA_MESSAGE = "message";

}
