package com.example.tritracker.json;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.example.tritracker.GlobalData;
import com.example.tritracker.R;
import com.example.tritracker.Util;
import com.example.tritracker.Activities.StopView;

public class JSONRequestManger extends Thread {
	private Context context = null;
	private Activity activity = null;
	private int StopID = 0;

    public void run() {
    	activity.runOnUiThread(new Runnable() {
    	     @Override
    	     public void run() {
    	 		((RelativeLayout) activity.findViewById(R.id.NoClickScreen)).setVisibility(View.VISIBLE);
    			activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
    					WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    	     	}
    	});

    	boolean shouldNotViewStop = false;
    	try {
			ForegroundJSONRequest tempF = new ForegroundJSONRequest(context, activity, 
					"http://developer.trimet.org/ws/V1/arrivals?locIDs="
							+ String.valueOf(StopID)
							+ "&json=true&appID="
							+ context.getString(R.string.appid), StopID);
			synchronized (tempF) { tempF.start(); tempF.join(); }
			shouldNotViewStop = tempF.failed;

			if (!shouldNotViewStop) {
				BackgroundJSONRequest test = new BackgroundJSONRequest(context, activity, 
						"http://developer.trimet.org/ws/V1/detours?routes="
								+ Util.getListOfLines(GlobalData.CurrentStop, false)
								+ "&json=true&appID="
								+ context.getString(R.string.appid));
				
				synchronized (test) { test.start(); test.join(); }
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	activity.runOnUiThread(new Runnable() {
	   	     @Override
	   	     public void run() {
	   			((RelativeLayout) activity.findViewById(R.id.NoClickScreen)).setVisibility(View.INVISIBLE);
	   			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	   		}
    	});

    	if (!shouldNotViewStop)
    		context.startActivity(new Intent(context, StopView.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
	

	public JSONRequestManger(Context context, Activity activity, int StopID) {
		this.context = context;
		this.activity = activity;
		this.StopID = StopID;
	}	
}