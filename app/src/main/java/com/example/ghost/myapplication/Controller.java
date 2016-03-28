package com.example.ghost.myapplication;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import com.google.android.gcm.GCMRegistrar;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.util.Log;

public class Controller extends Application{

    private  final int MAX_ATTEMPTS = 5;
    private  final int BACKOFF_MILLI_SECONDS = 2000;
    private  final Random random = new Random();

    private  ArrayList<UserData> UserDataArr = new ArrayList<UserData>();

    DBAdapter DB = new DBAdapter(this);



    void register(final Context context, String name, String email, final String regId,final String IMEI) {

        Log.i(Config.TAG, "registering device (regId = " + regId + ")");



        String serverUrl = Config.YOUR_SERVER_URL+"register.php";

        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        params.put("name", name);
        params.put("email", email);
        params.put("imei", IMEI);

        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);


        for (int i = 1; i <= MAX_ATTEMPTS; i++) {

            Log.d(Config.TAG, "Attempt #" + i + " to register");

            try {

                displayRegistrationMessageOnScreen(context, context.getString(
                        R.string.server_registering, i, MAX_ATTEMPTS));


                post(serverUrl, params);

                GCMRegistrar.setRegisteredOnServer(context, true);


                String message = context.getString(R.string.server_registered);
                displayRegistrationMessageOnScreen(context, message);

                DB.addDeviceData(name, email, regId, IMEI);


                Intent i1 = new Intent(getApplicationContext(), GridViewExample.class);
                i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i1);

                return;
            } catch (IOException e) {



                Log.e(Config.TAG, "Failed to register on attempt " + i + ":" + e);

                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {

                    Log.d(Config.TAG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);

                } catch (InterruptedException e1) {

                    Log.d(Config.TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return;
                }


                backoff *= 2;
            }
        }

        String message = context.getString(R.string.server_register_error,
                MAX_ATTEMPTS);


        displayRegistrationMessageOnScreen(context, message);
    }


    void unregister(final Context context, final String regId,final String IMEI) {

        Log.i(Config.TAG, "unregistering device (regId = " + regId + ")");

        String serverUrl = Config.YOUR_SERVER_URL+"unregister.php";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        params.put("imei", IMEI);

        try {
            post(serverUrl, params);
            GCMRegistrar.setRegisteredOnServer(context, false);

            String message = context.getString(R.string.server_unregistered);
            displayRegistrationMessageOnScreen(context, message);
        } catch (IOException e) {



            String message = context.getString(R.string.server_unregister_error,
                    e.getMessage());
            Log.i("GCM K", message);

            displayRegistrationMessageOnScreen(context, message);
        }
    }


    private static void post(String endpoint, Map<String, String> params)
            throws IOException {

        URL url;
        try {

            url = new URL(endpoint);

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }

        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();


        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }

        String body = bodyBuilder.toString();

        Log.v(Config.TAG, "Posting '" + body + "' to " + url);

        byte[] bytes = body.getBytes();

        HttpURLConnection conn = null;
        try {

            Log.e("URL", "> " + url);

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");

            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();


            int status = conn.getResponseCode();


            if (status != 200) {

                throw new IOException("Post failed with error code " + status);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }




    public boolean isConnectingToInternet(){

        ConnectivityManager connectivity =
                (ConnectivityManager) getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }


    void displayRegistrationMessageOnScreen(Context context, String message) {

        Intent intent = new Intent(Config.DISPLAY_REGISTRATION_MESSAGE_ACTION);
        intent.putExtra(Config.EXTRA_MESSAGE, message);


        context.sendBroadcast(intent);

    }


    void displayMessageOnScreen(Context context, String title,String message,String imei) {

        Intent intent = new Intent(Config.DISPLAY_MESSAGE_ACTION);
        intent.putExtra(Config.EXTRA_MESSAGE, message);
        intent.putExtra("name", title);
        intent.putExtra("imei", imei);
        context.sendBroadcast(intent);

    }



    public void showAlertDialog(Context context, String title, String message,
                                Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();


        alertDialog.setTitle(title);
        alertDialog.setMessage(message);

        if(status != null)
            alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);

        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();
    }

    private PowerManager.WakeLock wakeLock;

    public  void acquireWakeLock(Context context) {
        if (wakeLock != null) wakeLock.release();

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "WakeLock");

        wakeLock.acquire();
    }

    public  void releaseWakeLock() {
        if (wakeLock != null) wakeLock.release(); wakeLock = null;
    }

    public UserData getUserData(int pPosition) {

        return UserDataArr.get(pPosition);
    }

    public void setUserData(UserData Products) {

        UserDataArr.add(Products);

    }

    public int getUserDataSize() {

        return UserDataArr.size();
    }

    public void clearUserData() {

        UserDataArr.clear();
    }
}
