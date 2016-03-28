package com.example.ghost.myapplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.ghost.myapplication.DBAdapter;



public class ShowMessage extends Activity {
	
	EditText txtMessage;
	Button btnSend;

	TextView lblMessage;
	Controller aController;

	AsyncTask<Void, Void, Void> mRegisterTask;
	
	String name;
	String message;
    String UserDeviceIMEI;

	DBAdapter DBAdapter = new DBAdapter(this);

    public  ArrayList<UserData> CustomListViewValuesArr = new ArrayList<UserData>();
    TextView output = null;
    CustomAdapter adapter;
    ShowMessage activity = null;

	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_message);
		aController = (Controller) getApplicationContext();

		if (!aController.isConnectingToInternet()) {

			aController.showAlertDialog(ShowMessage.this,
					"Internet Connection Error",
					"Please connect to Internet connection", false);
			return;
		}
		
		lblMessage = (TextView) findViewById(R.id.lblMessage);
		
		
		
		if(lblMessage.getText().equals("")){

		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				Config.DISPLAY_MESSAGE_ACTION));
		}

		List<UserData> data = null;

			data = DBAdapter.getAllUserData();



		for (UserData dt : data) {
            
            lblMessage.append(dt.get_name()+" : "+dt.get_message()+"\n");
        }
        

          
        activity  = this;


		List<UserData> SpinnerUserData = null;

			SpinnerUserData = DBAdapter.getDistinctUser();


		for (UserData spinnerdt : SpinnerUserData) {
            
        	 UserData schedSpinner = new UserData();

        	schedSpinner.set_name(spinnerdt.get_name());
        	schedSpinner.set_imei(spinnerdt.get_imei());
             
        	Log.i("GCMspinner", "-----"+spinnerdt.get_name());

          CustomListViewValuesArr.add(schedSpinner);
          
        }
        
        
        Spinner  SpinnerExample = (Spinner)findViewById(R.id.spinner);
        Resources res = getResources();
        adapter = new CustomAdapter(activity, R.layout.spinner_rows, CustomListViewValuesArr,res);

        SpinnerExample.setAdapter(adapter);

        SpinnerExample.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View v, int position, long id) {

                String UserName       = ((TextView) v.findViewById(R.id.username)).getText().toString();
                UserDeviceIMEI        = ((TextView) v.findViewById(R.id.imei)).getText().toString();
                 
                String OutputMsg = "Selected User : \n\n"+UserName+"\n"+UserDeviceIMEI;
                 
                Toast.makeText(
                        getApplicationContext(),OutputMsg, Toast.LENGTH_LONG).show();
            }
 
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
 
        });
        
        
        txtMessage = (EditText) findViewById(R.id.txtMessage);
		btnSend    = (Button) findViewById(R.id.btnSend);

		btnSend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String message = txtMessage.getText().toString(); 

		        String serverURL = Config.YOUR_SERVER_URL+"sendpush.php";
		        
		      if(!UserDeviceIMEI.equals(""))
		      {	  
		        
		        String deviceIMEI = "";
				if(Config.SECOND_SIMULATOR){

					deviceIMEI = "000000000000001";
				}	
				else
				{

				 TelephonyManager tManager = (TelephonyManager) getBaseContext()
				    .getSystemService(Context.TELEPHONY_SERVICE);
				  deviceIMEI = tManager.getDeviceId(); 
				}

		        new LongOperation().execute(serverURL,UserDeviceIMEI,message,deviceIMEI); 
		        
		        txtMessage.setText("");
		      }
		      else
		      {
		    	  Toast.makeText(
	                        getApplicationContext(),"Please select send to user.", Toast.LENGTH_LONG).show();
		    	  
		      }
			}
		});
		
	}		

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			String newMessage = intent.getExtras().getString(Config.EXTRA_MESSAGE);
			String newName = intent.getExtras().getString("name");
			String newIMEI = intent.getExtras().getString("imei");
			
			Log.i("GCMBroadcast","Broadcast called."+newIMEI);

			aController.acquireWakeLock(getApplicationContext());
			
			String msg = lblMessage.getText().toString();
			msg = newName+" : "+newMessage+"\n"+msg;
			lblMessage.setText(msg);
			
			
			Toast.makeText(getApplicationContext(), 
					"Got Message: " + newMessage, 
					Toast.LENGTH_LONG).show();

			 int rowCount = DBAdapter.validateNewMessageUserData(newIMEI);
			 Log.i("GCMBroadcast", "rowCount:"+rowCount);
             if(rowCount <= 1 ){
		        	final UserData schedSpinner = new UserData();

		        	schedSpinner.set_name(newName);
		        	schedSpinner.set_imei(newIMEI);

		          CustomListViewValuesArr.add(schedSpinner);
		          adapter.notifyDataSetChanged();
		          
		        }
		        

			aController.releaseWakeLock();
		}
	};
	

	
	public class LongOperation  extends AsyncTask<String, Void, String> {
        

        private String Error = null;
        private ProgressDialog Dialog = new ProgressDialog(ShowMessage.this); 
        String data  = ""; 
        int sizeData = 0;  
        
        
        protected void onPreExecute() {

           
            Dialog.setMessage("Please wait..");
            Dialog.show();
            
        }
 

        protected String doInBackground(String... params) {
        	

        	BufferedReader reader=null;
        	String Content = "";

	            try{
	            	

		            URL url = new URL(params[0]);
	            	

		            if(!params[1].equals(""))
	               	   data +="&" + URLEncoder.encode("data1", "UTF-8") + "="+params[1].toString();
		            if(!params[2].equals(""))
		               data +="&" + URLEncoder.encode("data2", "UTF-8") + "="+params[2].toString();	
		            if(!params[3].equals(""))
			           data +="&" + URLEncoder.encode("data3", "UTF-8") + "="+params[3].toString();	
	              

	   
	              URLConnection conn = url.openConnection(); 
	              conn.setDoOutput(true); 
	              OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); 
	              wr.write( data ); 
	              wr.flush(); 

	              reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	              StringBuilder sb = new StringBuilder();
	              String line = null;
	            

		            while((line = reader.readLine()) != null)
		                {

		                       sb.append(line + "\n");
		                }

	               Content = sb.toString();
	            }
	            catch(Exception ex)
	            {
	            	Error = ex.getMessage();
	            }
	            finally
	            {
	                try
	                {
	     
	                    reader.close();
	                }
	   
	                catch(Exception ex) {}
	            }
        	

            return Content;
        }
         
        protected void onPostExecute(String Result) {

            Dialog.dismiss();
            
            if (Error != null) {
            	Toast.makeText(getBaseContext(), "Error: "+Error, Toast.LENGTH_LONG).show();  
                 
            } else {

            	 Toast.makeText(getBaseContext(), "Message sent."+Result, Toast.LENGTH_LONG).show();  
                 
             }
        }
         
    }
	
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	
	@Override
	protected void onDestroy() {
		try {

			unregisterReceiver(mHandleMessageReceiver);
			
			
		} catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
		super.onDestroy();
	}

}
