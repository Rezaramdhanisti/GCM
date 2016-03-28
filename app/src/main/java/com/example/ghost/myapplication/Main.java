package com.example.ghost.myapplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import com.example.ghost.myapplication.GridViewExample.LongOperation;
import com.google.android.gcm.GCMRegistrar;

public class Main extends AppCompatActivity {


	TextView lblMessage;
	Controller aController;
    DBAdapter DB = new DBAdapter(this);
	String regId = "";


	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		aController = (Controller) getApplicationContext();
		if (!aController.isConnectingToInternet()) {
			

			aController.showAlertDialog(Main.this,
					"Internet Connection Error",
					"Please connect to Internet connection", false);

			return;
		}
	

		int vDevice = 0;

            vDevice = DB.validateDevice();


		if(vDevice > 0)
		{	
			

			Intent i = new Intent(getApplicationContext(), GridViewExample.class);
			startActivity(i);
			finish();
		}
		else
		{
			String deviceIMEI = "";
			regId = GCMRegistrar.getRegistrationId(this);

			if(Config.SECOND_SIMULATOR){
				

				deviceIMEI = "000000000000001";
			}	
			else
			{

			 TelephonyManager tManager = (TelephonyManager) getBaseContext()
			    .getSystemService(Context.TELEPHONY_SERVICE);
			  deviceIMEI = tManager.getDeviceId();

			}
			

	        String serverURL = Config.YOUR_SERVER_URL+"validate_device.php";
	        
	       	        LongOperation serverRequest = new LongOperation();
	        
	        serverRequest.execute(serverURL,deviceIMEI,"regId","");
			
		}	

	}
	
	

	public class LongOperation  extends AsyncTask<String, Void, String> {

	       //private final HttpClient Client = new DefaultHttpClient();
	       // private Controller aController = null;
	        private String Error = null;
	        private ProgressDialog Dialog = new ProgressDialog(Main.this);
	        String data ="";
	        int sizeData = 0;


	        protected void onPreExecute() {

	            Dialog.setMessage("Validating Device..");
	            Dialog.show();

	        }


	        protected String doInBackground(String... params) {


	        	BufferedReader reader=null;
	        	String Content = "";
				//Content = Content.replaceFirst("<font>.*?</font>", "");

		            try{


			               URL url = new URL(params[0]);

						if(!params[1].equals(""))
		               	   data +="&" + URLEncoder.encode("data1", "UTF-8") + "="+params[1].toString();
			            if(!params[2].equals(""))
							data +="&" + URLEncoder.encode("data2") + "="+params[2].toString();
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

				if (null == Content || Content.length() == 0) {
					Toast.makeText(Main.this,"No data found from web!!!",Toast.LENGTH_SHORT).show();


				} else {

	            	aController.clearUserData();

	            	JSONObject jsonResponse;

	                try {


	                     jsonResponse = new JSONObject(Content);


	                     JSONArray jsonMainNode = jsonResponse.optJSONArray("Android");


	                     int lengthJsonArr = jsonMainNode.length();

						if(lengthJsonArr > 0) {

							for (int i = 0; i < lengthJsonArr; i++) {

								JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);

								String Status = jsonChildNode.optString("status").toString();

								Log.i("GCM", "---" + Status);

								if (Status.equals("update")) {

									String RegID = jsonChildNode.optString("regid").toString();
									String Name = jsonChildNode.optString("name").toString();
									String Email = jsonChildNode.optString("email").toString();
									String IMEI = jsonChildNode.optString("imei").toString();

									DB.addDeviceData(Name, Email, RegID, IMEI);

									Intent i1 = new Intent(getApplicationContext(), GridViewExample.class);
									startActivity(i1);
									finish();

									Log.i("GCM", "---" + Name);
								} else if (Status.equals("install")){

									Intent i1 = new Intent(Main.this, RegisterActivity.class);
									startActivity(i1);
									finish();

								}


							}
						}else{

						}


	                 } catch (JSONException e) {

	                     e.printStackTrace();
	                 }


	             }
	        }

	    }




	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

}
