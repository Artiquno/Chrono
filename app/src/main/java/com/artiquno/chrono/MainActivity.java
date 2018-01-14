package com.artiquno.chrono;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
	private Vector<WorkerModel> workers = new Vector<>();
	private List<String> nameList = new ArrayList<>();
	
	private Chronometer chrono;
	private Spinner workerSpinner;
	private TextView mTextView;
	private Button watchStarter;
	
	private WorkerModel currWorker;
	
	private boolean counting = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTextView = (TextView)findViewById(R.id.resultView);
		chrono = (Chronometer)findViewById(R.id.chrono);
		workerSpinner = (Spinner)findViewById(R.id.workerList);
		
		watchStarter = (Button)findViewById(R.id.watchStarter);
		Button resumer = (Button)findViewById(R.id.watchResumer);
		watchStarter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!counting) {
					startChronometer();
				} else {
					stopChronometer();
				}
			}
		});
		resumer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!counting) {
					chrono.start();
					counting = true;
				}
			}
		});
		
		chrono.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
			@Override
			public void onChronometerTick(Chronometer chronometer) {
				String res = getTime();
				//Log.d("RESULT", res);
			}
		});
		
		workerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				currWorker = workers.elementAt(position);
				updateText();
				chrono.setBase(SystemClock.elapsedRealtime());
				if(counting) {
					stopChronometer();
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Log.d("INFO", "Nothing selected");
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		RequestQueue queue = Volley.newRequestQueue(this);
		
		String get = "http://192.168.1.64/sample.json";
		JsonArrayRequest jsReq = new JsonArrayRequest(get,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						try {
							String res = "";
							for (int i = 0; i < response.length(); ++i) {
								workers.add(new WorkerModel(response.getJSONObject(i)));
								nameList.add(response.getJSONObject(i).getString("full_name"));
							}
						} catch(JSONException ex) {
							Log.d("ERROR", ex.getMessage());
						}

						ArrayAdapter<String> adapter =
								new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, nameList);
						adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						workerSpinner.setAdapter(adapter);
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						mTextView.setText(error.getMessage());
					}
				});
		queue.add(jsReq);
		
		String post = "http://185.206.146.180/api/values/insertnorm";

		NormPostData npd = new NormPostData();
		npd.WorkerID = 1;
		npd.Norm = new Norm();
		npd.Norm.CreationDate = new Date();
		npd.Norm.InsertionDate = new Date();
		npd.Norm.IsActive = true;
		npd.Norm.WorkerID = 1;
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		try {
			JSONObject json = new JSONObject(gson.toJson(npd));

			JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, post, json, new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject response) {
					// do something...
					Log.d("INFO", "Success!");
				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					// do something...
					Log.d("ERROR", error.toString());
				}
			}) {
				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					final Map<String, String> headers = new HashMap<>();
					headers.put("Authorization", "Basic 5OoZd9uHbC9nNmIXqJTN_thbQc54kygD3FEqViMclaj8E1FfDKv8p...");
					return headers;
				}
			};

			queue.add(postRequest);
		} catch (JSONException error) {
			Log.d("ERROR", "Error");
		}
	}
	
	protected String getTime()
	{
		long elapsedTime = SystemClock.elapsedRealtime() - chrono.getBase();
		short hours = (short)(elapsedTime / (60 * 60 * 1000));
		short minutes = (short)((elapsedTime / (60 * 1000)) % 60);
		short seconds = (short)((elapsedTime / 1000) % 60);
		long millis = elapsedTime % 1000;
		return String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, millis);
	}
	protected void startChronometer()
	{
		chrono.setBase(SystemClock.elapsedRealtime());
		chrono.start();
		counting = true;
		watchStarter.setText(R.string.stop);
	}
	protected void stopChronometer()
	{
		currWorker.time = getTime();
		chrono.stop();
		counting = false;
		watchStarter.setText(R.string.start);
		updateText();
	}
	private void updateText() {
		String res = String.format("Name: %s\n" +
						"Full Name: %s\n" +
						"Role: %s\n" +
						"Vehicle: %s\n" +
						"Weapon: %s\n" +
						"Ability: %s\n" +
						"Importance: %d\n" +
						"Time: %s\n\n",
				currWorker.name,
				currWorker.fullName,
				currWorker.role,
				currWorker.vehicle,
				currWorker.weapon,
				currWorker.ability,
				currWorker.importance,
				currWorker.time);
		
		mTextView.setText(res);
	}
}
