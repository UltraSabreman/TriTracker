package com.example.tritracker.json;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.example.tritracker.Buss;
import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.Util;
import com.example.tritracker.activities.MainService;

public class ForgroundRequestManager extends Thread {
	private int theStop = 0;
	private MainService service = null;
	private ResultCallback testBack = null;
	private Context context = null;
	private Activity activity = null;
	
	private int returnError = 0;
	private Stop returnStop = null;
	
	public interface ResultCallback {
		public void run(Stop theStop);
	}

	public void run() {		
		boolean shouldNotViewStop = false;
		try {
			Request<ResultArrival> tempF = new Request<ResultArrival>(ResultArrival.class,
					new Request.JSONcallback<ResultArrival>() {
						public void run(ResultArrival r, int error) {
								parse(r, error);
						}
					},
					"http://developer.trimet.org/ws/V1/arrivals?locIDs="
							+ String.valueOf(theStop) + "&json=true&appID="
							+ context.getString(R.string.appid));
			
			synchronized (tempF) {
				tempF.start();
				tempF.join();
			}
			shouldNotViewStop = tempF.hasFailed();

			if (!shouldNotViewStop) {
				Request<ResultDetour> test = new Request<ResultDetour>(ResultDetour.class,
						new Request.JSONcallback<ResultDetour>() {
							public void run(ResultDetour r, int error) {
								service.proccessDetours(r);
							}
						},
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
		
		grabStopOrErrors(returnStop, returnError);
	}

	public interface checkStops {
		public void doStops();
	}
	
	public void grabStopOrErrors(Stop s, int error) {		
		if (s == null) return;

			if (error > 0) {
			hanldeHTTPErrors(error, s.StopID);
		} else if (error == -1) {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);

			builder.setMessage(
					"A stop with the ID \"" + s.StopID + "\" doesn't exist.")
					.setTitle(R.string.no_stop);

			builder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int id) {
						}
					});

			builder.create().show();
		} else {
			if (testBack != null)
				testBack.run(s);
		}
		
	}
	
	private void hanldeHTTPErrors(int error, final int id) {
		checkStops newCallback = new checkStops() {
									public void doStops() {
										Stop s = service.getStop(id);
										if (s != null)
											if (testBack != null)
												testBack.run(s);
										return;
									}
								};
		
		if (error == 1)
			Util.messageDiag(activity, newCallback,
					"Connection Timed-Out",
					"The connection timed-out (Trimet's servers might be busy, or you could have a poor connection)."
							+ "\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
		else if (error == 2)
			Util.messageDiag(activity,	newCallback,
					"Malformed reponce",
					"Trimet didn't respond correctly (their servers may be under heavy load)"
							+ "\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
		else if (error == 3)
			Util.messageDiag(activity,	newCallback,
					"Error Connecting",
					"It looks like Trimet changed their API. Please contact the developer ASAP and this will be fixed."
							+ "\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
		else if (error == 4)
			Util.messageDiag(activity,	newCallback,
					"Are you connected?",
					"Can't reach the Trimet servers right now, are you connected to the internet?"
							+ "\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
		

	}
	
	
	public ForgroundRequestManager(MainService service, ResultCallback func, Activity a, Context c, int stop) {
		this.theStop = stop;
		this.service = service;
		this.testBack = func;
		this.context = c;
		this.activity = a;
		this.setName("JSON Request Manager");
	}
	
	public void parse(ResultArrival r, int error) {
		if (r == null) {
			returnError = error;
			returnStop = new Stop(theStop);
			return;
		}
		
		ResultArrival.ResultSet rs = r.resultSet;
		
		if (rs.errorMessage != null) {
			returnError = -1;
			returnStop = null;
			return;
		}

		Stop readStop = new Stop(rs.location[0]);
		readStop.LastAccesed = new Date();

		if (rs.arrival != null)
			for (ResultArrival.ResultSet.Arrival a : rs.arrival)
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