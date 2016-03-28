package com.example.ghost.myapplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.ghost.myapplication.CustomGridAdapter;
import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.GridView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;

public class GridViewExample extends Activity {

	GridView gridView;
    Controller aController = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.grid_view_android_example);
		gridView = (GridView) findViewById(R.id.gridView1);
	    aController = (Controller) getApplicationContext();
        String serverURL = Config.YOUR_SERVER_URL+"userdata.php";
        LongOperation serverRequest = new LongOperation(); 
        
        serverRequest.execute(serverURL,"rrr","","");
		 
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				
				String deviceIMEI = "";

				if(Config.SECOND_SIMULATOR){
					

					deviceIMEI = "000000000000001";
				}	
				else
				{
				  // GET IMEI NUMBER      
				 TelephonyManager tManager = (TelephonyManager) getBaseContext()
				    .getSystemService(Context.TELEPHONY_SERVICE);
				  deviceIMEI = tManager.getDeviceId(); 
				}
				
				String uIMEI = aController.getUserData(position).get_imei();
				String uName = aController.getUserData(position).get_name();
				String regId = GCMRegistrar.getRegistrationId(GridViewExample.this);
				
				
				
				// Launch Main Activity
				Intent i = new Intent(getApplicationContext(), SendPushNotification.class);
				
				// Registering user on our server					
				// Sending registraiton details to MainActivity
				i.putExtra("name", uName);
				i.putExtra("imei", uIMEI);  // Send to
				i.putExtra("sendfrom", deviceIMEI);
				i.putExtra("regId",regId);
				startActivity(i);
				//finish();
				
				/*
				Toast.makeText(
						getApplicationContext(),
						((TextView) v.findViewById(R.id.grid_item_label))
								.getText(), Toast.LENGTH_SHORT).show();
                */
			}
		});

	}
	
	

	public class LongOperation  extends AsyncTask<String, Void, String> {
	         

	    	
	        //private final HttpClient Client = new DefaultHttpClient();
	       // private Controller aController = null;
	        private String Error = null;
	        private ProgressDialog Dialog = new ProgressDialog(GridViewExample.this); 
	        String data =""; 
	        int sizeData = 0;  
	        
	        
	        protected void onPreExecute() {

	            Dialog.setMessage("Getting registered users ..");
	            Dialog.show();
	            
	        }
	 

	        protected String doInBackground(String... params) {
	        	

	        	BufferedReader reader=null;
	        	String Content = "";

		            try{
		            	

			               URL url = new URL(params[0]);
		            	

			            if(!params[1].equals(""))
		               	   data +="&" + URLEncoder.encode("data", "UTF-8") + "="+params[1].toString();
			            if(!params[2].equals(""))
			               	   data +="&" + URLEncoder.encode("data2", "UTF-8") + "="+params[2].toString();	
			            if(!params[3].equals(""))
			               	   data +="&" + URLEncoder.encode("data3", "UTF-8") + "="+params[3].toString();
		              Log.i("GCM",data);
			            

		   
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
	         
	        protected void onPostExecute(String Content) {

	            Dialog.dismiss();
	            
	            if (Error != null) {
	                 
	                 
	            } else {
	              

	            	aController.clearUserData();
	            	
	            	JSONObject jsonResponse;
	                      
	                try {
	                      

	                     jsonResponse = new JSONObject(Content);

	                     JSONArray jsonMainNode = jsonResponse.optJSONArray("Android");
	                      

	                     int lengthJsonArr = jsonMainNode.length();  
	  
	                     for(int i=0; i < lengthJsonArr; i++) 
	                     {

	                         JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
	                          
	                         /******* Fetch node values **********/
	                         String IMEI       = jsonChildNode.optString("imei").toString();
	                         String Name       = jsonChildNode.optString("name").toString();
	                          
	                         Log.i("GCM","---"+Name);
	                         
	                         UserData userdata = new UserData();
	                         userdata.set_imei(IMEI);
	                         userdata.set_name(Name);

	                         aController.setUserData(userdata);
	                         
	                    }

	                  gridView.setAdapter(new CustomGridAdapter(getBaseContext(), aController));
	                    
	                      
	                 } catch (JSONException e) {
	          
	                     e.printStackTrace();
	                 }
	  
	                 
	             }
	        }
	         
	    }


}