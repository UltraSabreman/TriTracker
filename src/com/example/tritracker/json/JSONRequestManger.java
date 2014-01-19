package com.example.tritracker.json;

import java.util.Date;

import android.content.Context;

import com.example.tritracker.Buss;
import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.Util;
import com.example.tritracker.Util.JSONcallback;
import com.example.tritracker.activities.MainService;

public class JSONRequestManger extends Thread {
	private int theStop = 0;
	private MainService service = null;
	private ResultCallback testBack = null;
	private Context context = null;
	
	private int returnError = 0;
	private Stop returnStop = null;
	
	public interface ResultCallback {
		public void run(Stop theStop, int error);
	}

	public void run() {

		JSONcallback fCall = new JSONcallback() {
			public void run(JSONResult r, int error) {
					parse(r, error);
			}
		};
		JSONcallback bCall = new JSONcallback() {
			public void run(JSONResult r, int error) {
					service.proccesBackground(r);
			}
		};
		
		boolean shouldNotViewStop = false;
		try {
			JSONRequest tempF = new JSONRequest(fCall,
					"http://developer.trimet.org/ws/V1/arrivals?locIDs="
							+ String.valueOf(theStop) + "&json=true&appID="
							+ context.getString(R.string.appid));
			
			synchronized (tempF) {
				tempF.start();
				tempF.join();
			}
			shouldNotViewStop = tempF.hasFailed();

			if (!shouldNotViewStop) {
				JSONRequest test = new JSONRequest(bCall,
								"http://developer.trimet.org/ws/V1/detours?routes="
								+ Util.getListOfLines(service.getStop(theStop), false) + "&json=true&appID="
								+ context.getString(R.string.appid));

				synchronized (test) {
					test.start();
					test.join();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		if (testBack != null)
			testBack.run(returnStop, returnError);
	}

	public JSONRequestManger(MainService service, ResultCallback func, Context c, int stop) {
		this.theStop = stop;
		this.service = service;
		this.testBack = func;
		this.context = c;
		this.setName("JSON Request Manager");
	}
	
	public void parse(JSONResult r, int error) {
		if (r == null) {
			returnError = error;
			returnStop = new Stop(theStop);
			return;
		}
		
		JSONResult.ResultSet rs = r.resultSet;
		
		if (rs.errorMessage != null) {
			returnError = -1;
			returnStop = null;
			return;
		}

		Stop readStop = new Stop(rs.location[0]);
		readStop.LastAccesed = new Date();

		if (rs.arrival != null)
			for (JSONResult.ResultSet.Arrival a : rs.arrival)
				readStop.Busses.add(new Buss(a));
		else
			readStop.Busses = null;

		Stop actualStop = service.getStop(readStop);
		
		if (actualStop == null) {
			readStop.inHistory = true;
			service.addStop(readStop);
		} else {
			actualStop.Update(readStop, true);
			actualStop.inHistory = true;
		}
		
		service.doUpdate(false);
		returnError = 0;
		if(actualStop != null)
			returnStop = actualStop;
		else
			returnStop = readStop;
	}
		
}