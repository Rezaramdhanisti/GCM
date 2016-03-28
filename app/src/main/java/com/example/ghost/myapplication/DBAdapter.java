package com.example.ghost.myapplication;

/**
 * Created by ghost on 22/03/2016.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.ghost.myapplication.UserData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBAdapter extends SQLiteOpenHelper {
    public static final boolean DEBUG = true;

    public static final String LOG_TAG = "DBAdapter";

    public static final String KEY_ID = "_id";
   // public static final String KEY_ID_DEVICE = "_id_device";
    public static final String KEY_USER_IMEI = "user_imei";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_MESSAGE = "user_message";
    public static final String KEY_DEVICE_IMEI = "device_imei";
    public static final String KEY_DEVICE_NAME = "device_name";
    public static final String KEY_DEVICE_EMAIL = "device_email";
    public static final String KEY_DEVICE_REGID = "device_regid";

    //////DATABASE NAME////
    public static final String DATABASE_NAME = "DB_sqllite.db";

    ///DATABASE VERSION////
    public  static final int DATABASE_VERSION = 1;

    ///TABLES NAME///
    public static final String USER_TABLE = "tbl_user";
    public static final String DEVICE_TABLE = "tbl_device";

    /*** Set all table with comma seperated like USER_TABLE,ABC_TABLE ***/
    private static final String[] ALL_TABLES = {USER_TABLE,DEVICE_TABLE};

    ///CREATE TABLES SYNTAX///
    private static final String USER_CREATE =
            "create table  "+USER_TABLE+" ("
                            +KEY_ID+" integer primary key autoincrement,"
                            +KEY_USER_NAME+" text not null,"
                            +KEY_USER_IMEI+" text not null,"
                            +KEY_USER_MESSAGE+" text not null );";

    private static final String DEVICE_CREATE =
            "CREATE TABLE  "+DEVICE_TABLE+" ("
                            +KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                            +KEY_DEVICE_NAME+" TEXT, "
                            +KEY_DEVICE_EMAIL+" TEXT, "
                            +KEY_DEVICE_REGID+" TEXT, "
                            +KEY_DEVICE_IMEI+" TEXT );";


    ////DATABASE///

    public DBAdapter (Context context){
        super(context, DATABASE_NAME,null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if (DEBUG)
            Log.i(LOG_TAG, "new create");
        try {
            //db.execSQL(USER_MAIN_CREATE);
            db.execSQL(USER_CREATE);
            db.execSQL(DEVICE_CREATE);

        } catch (Exception exception) {
            if (DEBUG)
                Log.i(LOG_TAG, "Exception onCreate() exception");
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DEBUG)
            Log.w(LOG_TAG, "Upgrading database from version" + oldVersion
                    + "to" + newVersion + "...");

        for (String table : ALL_TABLES) {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
        onCreate(db);
    }




    // Insert installing device data
    public  void addDeviceData(String DeviceName, String DeviceEmail,
                                     String DeviceRegID, String DeviceIMEI)
    {

             SQLiteDatabase db = this.getWritableDatabase();

            String imei  = sqlEscapeString(DeviceIMEI);
            String name  = sqlEscapeString(DeviceName);
            String email = sqlEscapeString(DeviceEmail);
            String regid = sqlEscapeString(DeviceRegID);

            ContentValues cVal = new ContentValues();
            cVal.put(KEY_DEVICE_IMEI, imei);
            cVal.put(KEY_DEVICE_NAME, name);
            cVal.put(KEY_DEVICE_EMAIL, email);
            cVal.put(KEY_DEVICE_REGID, regid);

            db.insert(DEVICE_TABLE, null, cVal);
            db.close(); // Closing database connection

    }


    // Adding new user

    public  void addUserData(UserData uData) {
        try{
            SQLiteDatabase db = this.getWritableDatabase();

            String imei  = sqlEscapeString(uData.get_imei());
            String name  = sqlEscapeString(uData.get_name());
            String message  = sqlEscapeString(uData.get_message());

            ContentValues cVal = new ContentValues();
            cVal.put(KEY_USER_IMEI, imei);
            cVal.put(KEY_USER_NAME, name);
            cVal.put(KEY_USER_MESSAGE, message);
            db.insert(USER_TABLE, null, cVal);
            db.close(); // Closing database connection
        } catch (Throwable t) {
            Log.i("Database", "Exception caught: " + t.getMessage(), t);
        }
    }

    // Getting single user data
    public  UserData getUserData(int id) {
         SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(USER_TABLE, new String[] { KEY_ID,
                        KEY_USER_NAME, KEY_USER_IMEI,KEY_USER_MESSAGE}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        UserData data = new UserData(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2), cursor.getString(3));
        // return contact
        return data;
    }

    // Getting All user data
    public  List<UserData> getAllUserData() {
        List<UserData> contactList = new ArrayList<UserData>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + USER_TABLE+" ORDER BY "+KEY_ID+" desc";

         SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                UserData data = new UserData();
                data.set_id(Integer.parseInt(cursor.getString(0)));
                data.set_name(cursor.getString(1));
                data.set_imei(cursor.getString(2));
                data.set_message(cursor.getString(3));
                // Adding contact to list
                contactList.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return contact list
        return contactList;
    }

    // Getting users Count
    public  int getUserDataCount() {
        String countQuery = "SELECT  * FROM " + USER_TABLE;
         SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Getting installed device have self data or not
    public  int validateDevice() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT  * FROM " +DEVICE_TABLE, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // Getting distinct user data use in spinner
    public  List<UserData> getDistinctUser() {
        List<UserData> contactList = new ArrayList<UserData>();
        // Select All Query
        String selectQuery = "SELECT  distinct(user_imei),user_name FROM " + USER_TABLE+" ORDER BY "+KEY_ID+" desc";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                UserData data = new UserData();

                data.set_imei(cursor.getString(0));
                data.set_name(cursor.getString(1));
                // Adding contact to list
                contactList.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return contactList;
    }

    // Getting imei already in user table or not
    public  int validateNewMessageUserData(String IMEI) {
        int count = 0;
        try {
            String countQuery = "SELECT "+KEY_ID+" FROM " + USER_TABLE + " WHERE user_imei='"+IMEI+"'";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(countQuery, null);

            count = cursor.getCount();
            cursor.close();
        } catch (Throwable t) {
            count = 10;
            Log.i("Database", "Exception caught: " + t.getMessage(), t);
        }
        return count;
    }


    // Escape string for single quotes (Insert,Update)
    private static String sqlEscapeString(String aString) {
        String aReturn = "";

        if (null != aString) {
            //aReturn = aString.replace("'", "''");
            aReturn = DatabaseUtils.sqlEscapeString(aString);
            // Remove the enclosing single quotes ...
            aReturn = aReturn.substring(1, aReturn.length() - 1);
        }

        return aReturn;
    }
    // UnEscape string for single quotes (show data)
    private static String sqlUnEscapeString(String aString) {

        String aReturn = "";

        if (null != aString) {
            aReturn = aString.replace("''", "'");
        }

        return aReturn;
    }




}
