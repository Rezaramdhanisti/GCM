package com.example.ghost.myapplication;

/**
 * Created by ghost on 22/03/2016.
 */
public interface Config {
    static final boolean SECOND_SIMULATOR =false;

    static final String YOUR_SERVER_URL = "http://192.168.1.218/gcmserver/";

    //Google Project id

    static final String GOOGLE_SENDER_ID = "153338835299 ";

    static final String TAG = "GCM Android Example";

    // Broadcast reciever name to show gcm registration messages on screen

    static final String DISPLAY_REGISTRATION_MESSAGE_ACTION = "com.example.ghost.myapplication.DISPLAY_REGISTRATION_MESSAGE";
    // Broadcast reciever name to show user messages on screen
    static final String DISPLAY_MESSAGE_ACTION = "com.example.ghost.myapplication.DISPLAY_MESSAGE";
    // Parse server message with this name
    static final String EXTRA_MESSAGE = "message";

}
