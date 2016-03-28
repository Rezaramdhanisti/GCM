package com.example.ghost.myapplication;


import  com.example.ghost.myapplication.DBAdapter;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends Activity {

	EditText txtName;
	EditText txtEmail;

	Button btnRegister;
	private GoogleApiClient client;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register);

		final Controller aController = (Controller) getApplicationContext();

		if (!aController.isConnectingToInternet()) {

			aController.showAlertDialog(RegisterActivity.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);

			return;
		}

		if (Config.YOUR_SERVER_URL == null ||
				Config.GOOGLE_SENDER_ID == null ||
				Config.YOUR_SERVER_URL.length() == 0 ||
				Config.GOOGLE_SENDER_ID.length() == 0) {

			aController.showAlertDialog(RegisterActivity.this,
					"Configuration Error!",
					"Please set your Server URL and GCM Sender ID",
					false);

			return;
		}

		txtName = (EditText) findViewById(R.id.txtName);
		txtEmail = (EditText) findViewById(R.id.txtEmail);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		btnRegister.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String name = txtName.getText().toString();
				String email = txtEmail.getText().toString();
				if (name.trim().length() > 0 && email.trim().length() > 0) {

					Intent i = new Intent(getApplicationContext(), MainActivity.class);

					i.putExtra("name", name);
					i.putExtra("email", email);
					startActivity(i);
					finish();

				} else {


					aController.showAlertDialog(RegisterActivity.this,
							"Registration Error!",
							"Please enter your details",
							false);
				}
			}
		});

		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}

	@Override
	public void onStart() {
		super.onStart();

		client.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW,
				"Register Page",
				Uri.parse("android-app://com.example.ghost.myapplication/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);
	}

	@Override
	public void onStop() {
		super.onStop();

		Action viewAction = Action.newAction(
				Action.TYPE_VIEW,
				"Register Page",
				Uri.parse("http://host/path"),
				Uri.parse("android-app://com.example.ghost.myapplication/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);
		client.disconnect();
	}
}
