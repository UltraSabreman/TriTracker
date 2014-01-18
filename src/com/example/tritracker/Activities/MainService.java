package com.example.tritracker.activities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.example.tritracker.Buss;
import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.Stop.Alert;
import com.example.tritracker.Timer;
import com.example.tritracker.Timer.onUpdate;
import com.example.tritracker.Util;
import com.example.tritracker.Util.JSONcallback;
import com.example.tritracker.json.JSONRequest;
import com.example.tritracker.json.JSONResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;

public class MainService extends Service {
	private Timer refreshTime;
	private DataStore stopData;
	private Map<String, onUpdate> refreshList = new HashMap<String, onUpdate>();
	private final IBinder mBinder = new LocalBinder();
	
	public class LocalBinder extends Binder { MainService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MainService.this;
        }
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		readData();
		
		refreshTime = new Timer(stopData.RefreshDelay);
		refreshTime.addCallBack("mainRefresh", 
			new Timer.onUpdate() {
				public void run() {
					doUpdate();				
				}
			}
		);
		refreshTime.restartTimer();
		return mBinder;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*readData();
		
		refreshTime = new Timer(stopData.RefreshDelay);
		refreshTime.addCallBack("mainRefresh", 
			new Timer.onUpdate() {
				public void run() {
					doUpdate();				
				}
			}
		);
		refreshTime.restartTimer();*/
		System.out.println("Command start");
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	
	public void setDelay(int i) {
		stopData.RefreshDelay = i;
		refreshTime.updateDelay(stopData.RefreshDelay);
	}
	
	public int getDelay() {
		return stopData.RefreshDelay;
	}
	
	public Stop getStop(Stop stop) {
		return getStop(stop.StopID);
	}
	
	public Stop getStop(int StopID) {
		if (stopData.StopList != null && stopData.StopList.size() != 0)
			for (Stop s : stopData.StopList)
				if (s.StopID == StopID)
					return s;
		
		return null;
	}
	
	public void addStop(Stop s) {
		stopData.StopList.add(s);
	}
	
	public void sub(String key, onUpdate func) {
		if (!refreshList.containsKey(key))
			refreshList.put(key, func);
	}
	
	public void unsub(String key) {
		if (refreshList.containsKey(key))
			refreshList.remove(key);
	}
	
	public ArrayList<Stop> getFavorties() {
		ArrayList<Stop> temp = new ArrayList<Stop>();
		for (Stop s : stopData.StopList) {
			if (s.inFavorites)
				temp.add(s);
		}
		
		return temp;
	}
	
	public ArrayList<Stop> getHistory() {
		ArrayList<Stop> temp = new ArrayList<Stop>();
		for (Stop s : stopData.StopList) {
			if (s.inHistory)
				temp.add(s);
		}
		
		return temp;
	}
	
	public void removeStop(Stop s) {
		if (stopData.StopList.contains(s))
			stopData.StopList.remove(s);
	}
	
	public void doUpdate() {
		updateAllStops();
		dumpData();
		
		if (refreshList != null && refreshList.size() != 0) {
			Iterator<Entry<String, onUpdate>> it = refreshList.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<String, onUpdate> pairs = (Map.Entry<String, onUpdate>)it.next();
		        if(pairs != null && pairs.getValue() != null)
		        	((onUpdate)pairs.getValue()).run();
		    }
		}
	}
	
	
	public void proccesBackground(JSONResult r) {
		if (r == null || r.resultSet == null)
			return;

		if (r.resultSet.detour != null) {
			for (Stop s : stopData.StopList)
				s.resetAlerts();

			for (JSONResult.ResultSet.Detour d : r.resultSet.detour) {

				ArrayList<String> stopIds = new ArrayList<String>();
				Matcher m = Pattern.compile("Stop ID ([0-9]+)").matcher(d.desc);
				while (m.find()) {
					stopIds.add(m.group(1));
				}

				for (String si : stopIds) {
					Stop hist = getStop(Integer.parseInt(si));
					if (hist != null) {
						hist.Alerts.add(new Alert(d.desc, Integer.parseInt(d.route[0].route))); // Is this safe?
					}
				}
			}
			return;
		}

		if (r == null || r.resultSet == null || r.resultSet.errorMessage != null)
			return;

		JSONResult.ResultSet rs = r.resultSet;
		ArrayList<Stop> stops = new ArrayList<Stop>();
		if (rs.location != null) {
			for (JSONResult.ResultSet.Location l : rs.location) {
				stops.add(new Stop(l));
			}
	
			for (Stop s : stops) {
				if (rs.arrival != null)
					for (JSONResult.ResultSet.Arrival a : rs.arrival) {
						if (s.StopID == a.locid)
							s.Busses.add(new Buss(a));
					}
	
				Stop tempStop = getStop(s);
				if (tempStop != null)
					tempStop.Update(s, false);
			}
		}
	}

	private void updateAllStops() {
		Context c = getApplicationContext();
		
		String busses = "";
		String stops = "";
		
		for (Stop s : stopData.StopList) {
			stops += String.valueOf(s.StopID) + ",";
			busses += Util.getListOfLines(s, false) + ",";
		}

		JSONcallback tempCall = new JSONcallback() {
			public void run(JSONResult r, int error) {
				if (error == 0)
					proccesBackground(r);
			}
		};
		
		if (stops.compareTo("") != 0) {
			stops = stops.substring(0, stops.length() - 1);
			
			new JSONRequest(tempCall,
					"http://developer.trimet.org/ws/V1/arrivals?locIDs="
							+ stops + "&json=true&appID="
							+ c.getString(R.string.appid)).start();
		}
		if (busses.compareTo("") != 0) {
			busses = busses.substring(0, busses.length() - 1);

			new JSONRequest(tempCall,
					"http://developer.trimet.org/ws/V1/detours?routes="
							+ busses + "&json=true&appID="
							+ c.getString(R.string.appid)).start();
		}
	}
	
	private void dumpData() {
		try {
			Context c = getApplicationContext();
			String path = c.getString(R.string.data_path);
			FileOutputStream outputStream = c.openFileOutput(path, Context.MODE_PRIVATE);
			
			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
			String data = gson.toJson(stopData);//new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
			
			outputStream.write(data.getBytes());
			outputStream.close();
		} catch (JsonSyntaxException e) {
			System.out.println("---Error: WRITE, json syntax");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("---Error: WRITE, file not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("---Error: WRITE, IOexception");
			e.printStackTrace();
		}
	}

	private void readData() {
		try {
			Context c = getApplicationContext();
		    BufferedReader r = new BufferedReader(new InputStreamReader(c.openFileInput(c.getString(R.string.data_path))));
		    			
			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
			stopData = gson.fromJson(r, DataStore.class);
			//stopData = null;
			if (stopData == null)
				stopData = new DataStore();
			
			for(Stop s : stopData.StopList) {
				s.inFavorites = true;
				s.inHistory = true;
				
			}
				
			
			r.close();
		} catch (JsonSyntaxException e) {
			System.out.println("---Error: READ, json syntax");
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("---Error: READ, file not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("---Error: READ, IO eceptions");
			e.printStackTrace();
		} catch (Exception  e) {
			System.out.println("---Error: READ, WTF");
			e.printStackTrace();
		}
	}

	

	private class DataStore {
		@Expose public int FavOrder = 0;
		@Expose public int HistOrder = 0;
		@Expose public int StopOrder = 0;
		@Expose public int RefreshDelay = 5;
		@Expose public ArrayList<Stop> StopList = new ArrayList<Stop>();
	}


}
