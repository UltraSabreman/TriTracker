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
import com.example.tritracker.Util.ListType;
import com.example.tritracker.json.Request;
import com.example.tritracker.json.ArrivalJSONResult;
import com.example.tritracker.json.DetourJSONResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;

public class MainService extends Service {
	private Timer refreshTime = null;
	private DataStore stopData;
	private Map<String, onUpdate> refreshList = new HashMap<String, onUpdate>();
	private final IBinder mBinder = new LocalBinder();
	
	public class LocalBinder extends Binder { MainService getService() {
            return MainService.this;
        }
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		
		return mBinder;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (refreshTime == null) {
			readData();
			
			refreshTime = new Timer(stopData.RefreshDelay);
			refreshTime.addCallBack("mainRefresh", 
				new Timer.onUpdate() {
					public void run() {
						doUpdate(true);				
					}
				}
			);
			refreshTime.restartTimer();
			doUpdate(true);
		}
		return START_STICKY;
	}

	
	/* Accesors and Setters */
	/**
	 * @param list 
	 * The list order we need, 0 is favorites, 1 is history, and 2 is stop.
	 * @return
	 * the order
	 */
	public int getSort(ListType list) {
		if (list == ListType.Favorites)
			return stopData.FavOrder;
		else if(list == ListType.History)
			return stopData.HistOrder;
		else if (list == ListType.Busses)
			return stopData.StopOrder;
		else
			return -1;
	}
	
	public void setSort(ListType list, int order) {
		if (list == ListType.Favorites)
			stopData.FavOrder = order;
		else if(list == ListType.History)
			stopData.HistOrder = order;
		else if (list == ListType.Busses)
			stopData.StopOrder = order;
	}
	
	public int getMenu() {
		return stopData.menu;
	}
	
	public void setMenu(int i) {
		stopData.menu = i;
	}
	
	public double getMapRadius(){
		return stopData.Radius;
	}
	
	public void setMapRadius(double r) {
		stopData.Radius = r;
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
	
	
	/* Update Tick */
	public void doUpdate(boolean fetch) {
		if (fetch)
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
	
	private void updateAllStops() {
		Context c = getApplicationContext();
		
		String busses = "";
		String stops = "";
		
		for (Stop s : stopData.StopList) {
			stops += String.valueOf(s.StopID) + ",";
			busses += Util.getListOfLines(s, false) + ",";
		}
		
		
		if (stops.compareTo("") != 0) {
			stops = stops.substring(0, stops.length() - 1);
			
			new Request<ArrivalJSONResult>(ArrivalJSONResult.class, 
					new Request.JSONcallback<ArrivalJSONResult>() {
						public void run(ArrivalJSONResult r, int error) {
							if (error == 0)
								proccessArrivals(r);
						}
					},
					"http://developer.trimet.org/ws/V1/arrivals?locIDs="
							+ stops + "&json=true&appID="
							+ c.getString(R.string.appid)).start();
		}
		if (busses.compareTo("") != 0) {
			busses = busses.substring(0, busses.length() - 1);

			new Request<DetourJSONResult>(DetourJSONResult.class, 
					new Request.JSONcallback<DetourJSONResult>() {
						public void run(DetourJSONResult r, int error) {
							if (error == 0)
								proccessDetours(r);
						}
					},
					"http://developer.trimet.org/ws/V1/detours?routes="
							+ busses + "&json=true&appID="
							+ c.getString(R.string.appid)).start();
		}
	}
	
	
	public void proccessDetours(DetourJSONResult r) {
		if (r == null || r.resultSet == null)
			return;

		if (r.resultSet.detour != null) {
			for (Stop s : stopData.StopList)
				s.resetAlerts();

			for (DetourJSONResult.ResultSet.Detour d : r.resultSet.detour) {

				ArrayList<String> stopIds = new ArrayList<String>();
				Matcher m = Pattern.compile("Stop ID ([0-9]+)").matcher(d.desc);
				while (m.find()) {
					stopIds.add(m.group(1));
				}

				for (String si : stopIds) {
					Stop hist = getStop(Integer.parseInt(si));
					if (hist != null) {
						hist.Alerts.add(new Alert(d.desc, d.route[0].route));
					}
				}
			}
			return;
		}
	}
	public void proccessArrivals(ArrivalJSONResult r) {
		if (r == null || r.resultSet == null || r.resultSet.errorMessage != null)
			return;

		ArrivalJSONResult.ResultSet rs = r.resultSet;
		ArrayList<Stop> stops = new ArrayList<Stop>();
		if (rs.location != null) {
			for (ArrivalJSONResult.ResultSet.Location l : rs.location) {
				stops.add(new Stop(l));
			}
	
			for (Stop s : stops) {
				if (rs.arrival != null)
					for (ArrivalJSONResult.ResultSet.Arrival a : rs.arrival) {
						if (s.StopID == a.locid)
							s.Busses.add(new Buss(a));
					}
	
				Stop tempStop = getStop(s);
				if (tempStop != null)
					tempStop.Update(s, false);
			}
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
		} finally {
			if (stopData == null)
				stopData = new DataStore();	
		}
	}	

	private class DataStore {
		@Expose public int FavOrder = 0;
		@Expose public int HistOrder = 0;
		@Expose public int StopOrder = 0;
		@Expose public int RefreshDelay = 5;
		@Expose public ArrayList<Stop> StopList = new ArrayList<Stop>();
		@Expose public double Radius = 900;// 1/2 mile in meters.
		@Expose public int menu = 0;
	}


}
