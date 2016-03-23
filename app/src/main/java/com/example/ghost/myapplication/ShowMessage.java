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
	
	// UI elements
	EditText txtMessage; 
	// Send Message button
	Button btnSend;
	
	// label to display gcm messages
	TextView lblMessage;
	Controller aController;
	
	// Asyntask
	AsyncTask<Void, Void, Void> mRegisterTask;
	
	String name;
	String message;
    String UserDeviceIMEI;

	DBAdapter DBAdapter = new DBAdapter(this);
	
	/**************  Intialize Variables *************/
    public  ArrayList<UserData> CustomListViewValuesArr = new ArrayList<UserData>();
    TextView output = null;
    CustomAdapter adapter;
    ShowMessage activity = null;

	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_message);
		/******************* Intialize Database *************/

		
		// Get Global Controller Class object 
		// (see application tag in AndroidManifest.xml)
		aController = (Controller) getApplicationContext();
		
		
		// Check if Internet present
		if (!aController.isConnectingToInternet()) {
			
			// Internet Connection is not present
			aController.showAlertDialog(ShowMessage.this,
					"Internet Connection Error",
					"Please connect to Internet connection", false);
			// stop executing code by return
			return;
		}
		
		lblMessage = (TextView) findViewById(R.id.lblMessage);
		
		
		
		if(lblMessage.getText().equals("")){
		
		// Register custom Broadcast receiver to show messages on activity
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				Config.DISPLAY_MESSAGE_ACTION));
		}

		List<UserData> data = null;

			data = DBAdapter.getAllUserData();



		for (UserData dt : data) {
            
            lblMessage.append(dt.get_name()+" : "+dt.get_message()+"\n");
        }
        
        
        /*************** Spinner data Start *****************/
          
        activity  = this;


		List<UserData> SpinnerUserData = null;

			SpinnerUserData = DBAdapter.getDistinctUser();


		for (UserData spinnerdt : SpinnerUserData) {
            
        	 UserData schedSpinner = new UserData();
            
            /******* Firstly take data in model object ********/
        	schedSpinner.set_name(spinnerdt.get_name());
        	schedSpinner.set_imei(spinnerdt.get_imei());
             
        	Log.i("GCMspinner", "-----"+spinnerdt.get_name());
              
          /******** Take Model Object in ArrayList **********/
          CustomListViewValuesArr.add(schedSpinner);
          
        }
        
        
        Spinner  SpinnerExample = (Spinner)findViewById(R.id.spinner);
        // Resources passed to adapter to get image
        Resources res = getResources(); 
         
        // Create custom adapter object ( see below CustomAdapter.java )
        adapter = new CustomAdapter(activity, R.layout.spinner_rows, CustomListViewValuesArr,res);
         
        // Set adapter to spinner
        SpinnerExample.setAdapter(adapter);
         
        // Listener called when spinner item selected
        SpinnerExample.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View v, int position, long id) {
                // your code here
                 
                // Get selected row data to show on screen
                String UserName       = ((TextView) v.findViewById(R.id.username)).getText().toString();
                UserDeviceIMEI        = ((TextView) v.findViewById(R.id.imei)).getText().toString();
                 
                String OutputMsg = "Selected User : \n\n"+UserName+"\n"+UserDeviceIMEI;
                 
                Toast.makeText(
                        getApplicationContext(),OutputMsg, Toast.LENGTH_LONG).show();
            }
 
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
 
        });
        
        
        txtMessage = (EditText) findViewById(R.id.txtMessage);
		btnSend    = (Button) findViewById(R.id.btnSend);
        
     // Click event on Register button
		btnSend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {  
				// Get data from EditText 
				String message = txtMessage.getText().toString(); 
				 
				// WebServer Request URL
		        String serverURL = Config.YOUR_SERVER_URL+"sendpush.php";
		        
		      if(!UserDeviceIMEI.equals(""))
		      {	  
		        
		        String deviceIMEI = "";
				if(Config.SECOND_SIMULATOR){
					
					//Make it true in CONFIG if you want to open second simutor
					// for testing actually we are using IMEI number to save a unique device
					
					deviceIMEI = "000000000000001";
				}	
				else
				{
				  // GET IMEI NUMBER      
				 TelephonyManager tManager = (TelephonyManager) getBaseContext()
				    .getSystemService(Context.TELEPHONY_SERVICE);
				  deviceIMEI = tManager.getDeviceId(); 
				}
		        
		        // Use AsyncTask execute Method To Prevent ANR Problem
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

	// Create a broadcast receiver to get message and show on screen 
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			String newMessage = intent.getExtras().getString(Config.EXTRA_MESSAGE);
			String newName = intent.getExtras().getString("name");
			String newIMEI = intent.getExtras().getString("imei");
			
			Log.i("GCMBroadcast","Broadcast called."+newIMEI);
			
			// Waking up mobile if it is sleeping
			aController.acquireWakeLock(getApplicationContext());
			
			String msg = lblMessage.getText().toString();
			msg = newName+" : "+newMessage+"\n"+msg;
			// Display message on the screen
			lblMessage.setText(msg);
			//lblMessage.append("\n"+newName+" : "+newMessage);			
			
			
			Toast.makeText(getApplicationContext(), 
					"Got Message: " + newMessage, 
					Toast.LENGTH_LONG).show();
			
			/************************************/
			 //CustomListViewValuesArr.clear();
			 int rowCount = DBAdapter.validateNewMessageUserData(newIMEI);
			 Log.i("GCMBroadcast", "rowCount:"+rowCount);
             if(rowCount <= 1 ){
		        	final UserData schedSpinner = new UserData();
		            
		            /******* Firstly take data in model object ********/
		        	schedSpinner.set_name(newName);
		        	schedSpinner.set_imei(newIMEI);
		             
		              
		          /******** Take Model Object in ArrayList **********/
		          CustomListViewValuesArr.add(schedSpinner);
		          adapter.notifyDataSetChanged();
		          
		        }
		        
		        //CustomListViewValuesArr.addAll(SpinnerUserData);
		        
		        
			
			/************************************/
			
			// Releasing wake lock
			aController.releaseWakeLock();
		}
	};
	
	
	/*********** Send message *****************/
	
	public class LongOperation  extends AsyncTask<String, Void, String> {
        
    	// Required initialization
    	
        //private final HttpClient Client = new DefaultHttpClient();
       // private Controller aController = null;
        private String Error = null;
        private ProgressDialog Dialog = new ProgressDialog(ShowMessage.this); 
        String data  = ""; 
        int sizeData = 0;  
        
        
        protected void onPreExecute() {
            // NOTE: You can call UI Element here.
             
            //Start Progress Dialog (Message)
           
            Dialog.setMessage("Please wait..");
            Dialog.show();
            
        }
 
        // Call after onPreExecute method
        protected String doInBackground(String... params) {
        	
        	/************ Make Post Call To Web Server ***********/
        	BufferedReader reader=null;
        	String Content = "";
	             // Send data 
	            try{
	            	
	            	// Defined URL  where to send data
		            URL url = new URL(params[0]);
	            	
		            // Set Request parameter
		            if(!params[1].equals(""))
	               	   data +="&" + URLEncoder.encode("data1", "UTF-8") + "="+params[1].toString();
		            if(!params[2].equals(""))
		               data +="&" + URLEncoder.encode("data2", "UTF-8") + "="+params[2].toString();	
		            if(!params[3].equals(""))
			           data +="&" + URLEncoder.encode("data3", "UTF-8") + "="+params[3].toString();	
	              
		            
		          // Send POST data request
	   
	              URLConnection conn = url.openConnection(); 
	              conn.setDoOutput(true); 
	              OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); 
	              wr.write( data ); 
	              wr.flush(); 
	          
	              // Get the server response 
	               
	              reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	              StringBuilder sb = new StringBuilder();
	              String line = null;
	            
		            // Read Server Response
		            while((line = reader.readLine()) != null)
		                {
		                       // Append server response in string
		                       sb.append(line + "\n");
		                }
	                
	                // Append Server Response To Content String 
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
        	
            /*****************************************************/
            return Content;
        }
         
        protected void onPostExecute(String Result) {
            // NOTE: You can call UI Element here.
             
            // Close progress dialog
            Dialog.dismiss();
            
            if (Error != null) {
            	Toast.makeText(getBaseContext(), "Error: "+Error, Toast.LENGTH_LONG).show();  
                 
            } else {
              
            	// Show Response Json On Screen (activity)
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
			// Unregister Broadcast Receiver
			unregisterReceiver(mHandleMessageReceiver);
			
			
		} catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
		super.onDestroy();
	}

}
