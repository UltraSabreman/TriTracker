package com.example.tritracker.activities;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.example.tritracker.Alert;
import com.example.tritracker.Buss;
import com.example.tritracker.NotificationHandler;
import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.Timer;
import com.example.tritracker.Timer.onUpdate;
import com.example.tritracker.Util.ListType;
import com.example.tritracker.json.AllRoutesJSONResult;
import com.example.tritracker.json.AllRoutesJSONResult.ResultSet.Route;
import com.example.tritracker.json.ArrivalJSONResult;
import com.example.tritracker.json.DetourJSONResult;
import com.example.tritracker.json.DetourJSONResult.ResultSet;
import com.example.tritracker.json.Request;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainService extends Service {
	public boolean isUpdating = false;
	
	private Timer refreshTime = null;
	private DataStore stopData;
	private Map<String, onUpdate> refreshList = new HashMap<String, onUpdate>();
	private final IBinder mBinder = new LocalBinder();
	private ArrayList<NotificationHandler> reminders = new ArrayList<NotificationHandler>();
	
	private static boolean started = false;
	private static MainService theService;
	public static MainService getService() {
		return theService;
	}	
	
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
			if (!started) {
				readData();
				started = true;
			}
			
			refreshTime = new Timer(stopData.RefreshDelay);
			refreshTime.addCallBack("mainRefresh", 
				new Timer.onUpdate() {
					public void run() {
                        Lock lock = new ReentrantLock();

                        try {
                            while (!lock.tryLock(100, TimeUnit.MILLISECONDS));
                            try {
                                doUpdate(true);
                            } finally {
                                lock.unlock();
                            }
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

					}
				}
			);
			refreshTime.restartTimer();
			doUpdate(true);
			
		}
		theService = this;
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
	
	public ArrayList<Route> getRoutes() {
		return stopData.AvalibleRoutes;
	}
	
	public void addReminder(NotificationHandler not) {
		if (!reminders.contains(not)) {
			reminders.add(not);
		}
	}
	
	private void updateReminders() {
		for (Iterator<NotificationHandler> it = reminders.iterator(); it.hasNext();)
			if (!it.next().IsSet)
				it.remove();
	}
	
	public NotificationHandler getReminder(Buss b) {
		for (NotificationHandler n : reminders)
			if (n.getBuss() == b)
				return n;
		
		return null;
	}
	
	public boolean stopHasReminders(Stop s) {
		for (NotificationHandler n : reminders)
			if (n.getStop() == s)
				return true;
		
		return false; 
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
		
		updateReminders();
	}
	
	public ArrayList<Alert> getStopAlerts(Stop s) {
		if (s == null) return null;
		ArrayList<Alert> retList = new ArrayList<Alert>();
		for (Alert a : stopData.Alerts)
			if (a.affectsStop(s))
				if (!retList.contains(a))
					retList.add(a);
		return retList;	
	}
	
	public boolean routeHasAlert(int route) {
		for (Alert a :stopData.Alerts)
			for (Integer i : a.AffectedLines)
				if (i.intValue() == route)
					return true;
		return false;
	}
	
	private void updateAvalibleRoutes() {
		isUpdating = true;
		new Request<AllRoutesJSONResult>(AllRoutesJSONResult.class, 
				new Request.JSONcallback<AllRoutesJSONResult>() {
					public void run(AllRoutesJSONResult res, int error) {
						Lock lock = new ReentrantLock();
						
						try {
							while (!lock.tryLock(100, TimeUnit.MILLISECONDS));
							try {
								stopData.AvalibleRoutes.clear();
								for (AllRoutesJSONResult.ResultSet.Route r : res.resultSet.route) {
									stopData.AvalibleRoutes.add(r);
								}
								isUpdating = false;
							} finally {
								lock.unlock();
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				},
				"http://developer.trimet.org/ws/V1/routeConfig?json=true&dir=true&stops=true&appID="
						+ getApplicationContext().getString(R.string.appid)).start();
	}
	
	
	private void updateDetours() {
		new Request<DetourJSONResult>(DetourJSONResult.class, 
				new Request.JSONcallback<DetourJSONResult>() {
					public void run(DetourJSONResult r, int error) {
						if (error != 0) return;
						MainService.this.stopData.Alerts.clear();
						
						for (ResultSet.Detour d : r.resultSet.detour)
							stopData.Alerts.add(new Alert(d));
						
						
					}
				},
				"http://developer.trimet.org/ws/V1/detours?json=true&appID="
						+ getApplicationContext().getString(R.string.appid)).start();
	}
	
	private void updateAllStops() {
		Context c = getApplicationContext();
		
		String stops = "";
		
		for (Stop s : stopData.StopList)
			stops += String.valueOf(s.StopID) + ",";
		
		
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
		updateDetours();
		
		long last = stopData.lastRouteUpdate;
		long now = new Date().getTime();
		
		long days = (now - last) / 1000 / 60 / 60 / 24;
		
		if (days > 7) {
			updateAvalibleRoutes();
			stopData.lastRouteUpdate = now;
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
			
			Lock lock = new ReentrantLock();
			
			try {
				while (!lock.tryLock(100, TimeUnit.MILLISECONDS));
				try {
					String data = new Gson().toJson(stopData);//new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
			
					outputStream.write(data.getBytes());
					outputStream.close();
				} finally {
					lock.unlock();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

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

			//File t = new File(c.getString(R.string.data_path));
			//t.delete();
			
		    BufferedReader r = new BufferedReader(new InputStreamReader(c.openFileInput(c.getString(R.string.data_path))));
		    			
			stopData = new Gson().fromJson(r, DataStore.class);
			
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
		public int FavOrder = 0;
		public int HistOrder = 0;
		public int StopOrder = 0;
		public int RefreshDelay = 5;
		public double Radius = 900;// 1/2 mile in meters.
		public int menu = 0;
		
		public ArrayList<Stop> StopList = new ArrayList<Stop>();
		public ArrayList<Route> AvalibleRoutes = new ArrayList<Route>();
		public ArrayList<Alert> Alerts = new ArrayList<Alert>();
		public long lastRouteUpdate = 0;
	}


}
