package com.example.ghost.myapplication;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//Adapter class extends with BaseAdapter and implements with OnClickListener
public class CustomAdapter extends ArrayAdapter<String>{

    private Activity activity;
    private ArrayList data;
    public Resources res;
    UserData tempValues=null;
    LayoutInflater inflater;

    public CustomAdapter(
            ShowMessage activitySpinner,
            int textViewResourceId,
            ArrayList objects,
            Resources resLocal
    )
    {
        super(activitySpinner, textViewResourceId, objects);
        

        activity = activitySpinner;
        data     = objects;
        res      = resLocal;
   

        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {


        View row = inflater.inflate(R.layout.spinner_rows, parent, false);
        

        tempValues = null;
        tempValues = (UserData) data.get(position);
        
        TextView Username        = (TextView)row.findViewById(R.id.username);
        TextView Userimei          = (TextView)row.findViewById(R.id.imei);
        ImageView UserImage = (ImageView)row.findViewById(R.id.image);
        
        

        	Username.setText(tempValues.get_name());
        	Userimei.setText(tempValues.get_imei());
        	UserImage.setImageResource(res.getIdentifier("com.example.ghost.myapplication:drawable/user_thumb",null,null));
            
           

        return row;
      }
 }
