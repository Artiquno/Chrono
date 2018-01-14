package com.artiquno.chrono;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Artiquno on 8/17/2017.
 */

public class WorkerModel {
	String name;
	String fullName;
	String role;
	String vehicle;
	String weapon;
	String ability;
	String time = "";
	int importance;
	
	WorkerModel(String name,
	            String fullName,
	            String role,
	            String vehicle,
	            String weapon,
	            String ability,
	            int importance) {
		this.name = name;
		this.fullName = fullName;
		this.role = role;
		this.vehicle = vehicle;
		this.weapon = weapon;
		this.ability = ability;
		this.importance = importance;
	}
	
	WorkerModel(JSONObject json) {
		try {
			this.name = json.getString("name");
			this.fullName = json.getString("full_name");
			this.role = json.getString("role");
			this.vehicle = json.getString("vehicle");
			this.weapon = json.getString("weapon");
			this.ability = json.getString("ability");
			this.importance = json.getInt("importance");
		} catch(JSONException e) {
			Log.d("ERROR", "JSON could not be parsed:\n" + e.getMessage());
		}
	}
}
