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
import com.example.tritracker.json.XmlRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.thoughtworks.xstream.XStream;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainService extends Service {
	private Timer refreshTime = null;
	private DataStore stopData;

	private XmlRequest mapRoutes;
	private ArrayList<Route> searchRoutes = new ArrayList<Route>();

	private Map<String, onUpdate> refreshList = new HashMap<String, onUpdate>();
	private final IBinder mBinder = new LocalBinder();
	private ArrayList<NotificationHandler> reminders = new ArrayList<NotificationHandler>();

	private static boolean started = false;
	private static MainService theService;

    private boolean changedMapRoutes = false;
    public boolean updatingMapRoutes = false;
    private boolean changedSearchRoutes = false;
    public boolean updatingSearchRoutes = false;

	public static MainService getService() {
		return theService;
	}

	public class LocalBinder extends Binder {
		MainService getService() {
			return MainService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		theService = this;
		if (refreshTime == null) {
			if (!started) {
				readData();
				started = true;
			}

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
			//updateMapRoutes();
			//updateSearchRoutes();

		}
		return START_STICKY;
	}

	
	/* Accesors and Setters */

	/**
	 * @param list The list order we need, 0 is favorites, 1 is history, and 2 is stop.
	 * @return the order
	 */
	public int getSort(ListType list) {
		if (list == ListType.Favorites)
			return stopData.FavOrder;
		else if (list == ListType.History)
			return stopData.HistOrder;
		else if (list == ListType.Busses)
			return stopData.StopOrder;
		else
			return -1;
	}

	public void setSort(ListType list, int order) {
		if (list == ListType.Favorites)
			stopData.FavOrder = order;
		else if (list == ListType.History)
			stopData.HistOrder = order;
		else if (list == ListType.Busses)
			stopData.StopOrder = order;
	}

	public ArrayList<Route> getRoutes() {
		return searchRoutes;
	}


    public XmlRequest getMapRoutes() {
	    return mapRoutes;
    }

	public void addReminder(NotificationHandler not) {
		if (!reminders.contains(not)) {
			reminders.add(not);
		}
	}

	private void updateReminders() {
		for (Iterator<NotificationHandler> it = reminders.iterator(); it.hasNext(); )
			if (!it.next().IsSet)
				it.remove();
	}

	public NotificationHandler getReminder(Buss b) {
		for (NotificationHandler n : reminders)
			if (n.getBuss() == b)
				return n;

		return null;
	}

	public NotificationHandler getReminder(Buss.TimeBox t) {
		for (NotificationHandler n : reminders)
			if (n.getBussTime() == t)
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

	public double getMapRadius() {
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
		if (refreshList.containsKey(key))
			refreshList.remove(key);
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

		updateReminders();

		dumpData();

		if (refreshList != null && refreshList.size() != 0) {
			Iterator<Entry<String, onUpdate>> it = refreshList.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, onUpdate> pairs = it.next();
				if (pairs != null && pairs.getValue() != null)
					pairs.getValue().run();
			}
		}
	}

	public ArrayList<Alert> getStopAlerts(Stop s) {
		if (s == null) return null;
		ArrayList<Alert> retList = new ArrayList<Alert>();
        synchronized (stopData.Alerts) {
            for (Alert a : stopData.Alerts)
                if (a.affectsStop(s))
                    if (!retList.contains(a))
                        retList.add(a);
        }
        return retList;
	}

	public boolean doesBussHaveAlerts(Buss b) {
		if (b == null) return false;

        synchronized (stopData.Alerts) {
            for (Alert a : stopData.Alerts)
                for (int i : a.AffectedLines)
                    if (i == b.Route)
                        return true;
        }
		return false;
	}

	public boolean routeHasAlert(int route) {
        synchronized (stopData.Alerts) {
            for (Alert a : stopData.Alerts)
                for (Integer i : a.AffectedLines)
                    if (i == route)
                        return true;
        }
		return false;
	}

	private void updateSearchRoutes() {
        updatingSearchRoutes = true;
		new Request<AllRoutesJSONResult>(AllRoutesJSONResult.class,
				new Request.JSONcallback<AllRoutesJSONResult>() {
					public void run(AllRoutesJSONResult res, String s, int error) {
						Lock lock = new ReentrantLock();

						try {
							while (!lock.tryLock(100, TimeUnit.MILLISECONDS)) ;
							try {
                                searchRoutes.clear();
								Collections.addAll(searchRoutes, res.resultSet.route);
                                updatingSearchRoutes = false;
							} finally {
								lock.unlock();
							}
							changedSearchRoutes = true;
                            doUpdate(false);
						} catch (InterruptedException e) {
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
					public void run(DetourJSONResult r, String s,  int error) {
						if (error != 0 || r == null) return;
                        synchronized (stopData.Alerts) {
						    MainService.this.stopData.Alerts.clear();

                            if (r.resultSet != null && r.resultSet.detour != null)
                                for (ResultSet.Detour d : r.resultSet.detour)
                                    stopData.Alerts.add(new Alert(d));
                        }
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
						public void run(ArrivalJSONResult r, String s, int error) {
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
			//TODO warn that this is happeing, give option to opt out, show visual indicator when doing it (not intrucive but there)
			// and ofcorse provide a way to manauly call.
            updateSearchRoutes();
            updateMapRoutes();
            //TODO: make this only manualy trigarable.
			stopData.lastRouteUpdate = now;
		}
	}


	public void proccessArrivals(ArrivalJSONResult r) {
		if (r == null || r.resultSet == null || r.resultSet.errorMessage != null)
			return;

		ArrivalJSONResult.ResultSet rs = r.resultSet;
		if (rs.location != null) {
			for (ArrivalJSONResult.ResultSet.Location l : rs.location) {
				Stop temp = new Stop(l);

				if (rs.arrival != null) {
					int i = 0;
					for (ArrivalJSONResult.ResultSet.Arrival a : rs.arrival) {
						if (a == null || a.locid != temp.StopID) continue;

						Buss t = temp.getBuss(a.fullSign);
						if (t == null)
							temp.Busses.add(new Buss(a));
						else
							t.AddTime(a);

						rs.arrival[i++] = null;
					}
				}
				//TODO fix this stupidity
				Stop tempStop = getStop(temp);
				if (tempStop != null) {
                    synchronized (tempStop) {
    					tempStop.Update(temp, false);
                    }
                }
			}
		}
	}

	synchronized private void dumpData() {
		final Context c = getApplicationContext();

		try {
			String path = c.getString(R.string.data_path);
			FileOutputStream outputStream = c.openFileOutput(path, Context.MODE_PRIVATE);

            String data;
            synchronized (stopData) {
			     data = new Gson().toJson(stopData);
            }

			outputStream.write(data.getBytes());
			outputStream.close();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Thread dumpSearchRoutes  = new Thread() {
			synchronized public void run() {
				changedSearchRoutes = false;
				BufferedReader r = null;

				try {
					OutputStreamWriter ow = new OutputStreamWriter(c.openFileOutput("searchRoutes.json", Context.MODE_PRIVATE));
					wrapperSearch w = new wrapperSearch();
					w.list = searchRoutes;
					String data;
					synchronized (w) {
						data = new Gson().toJson(w);
					}
					ow.write(data);
					ow.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		if (changedSearchRoutes)
			dumpSearchRoutes.start();
	}

	synchronized private void readData() {
		final Context c = getApplicationContext();
		updatingMapRoutes = true;
		updatingSearchRoutes = true;

		try {
   			BufferedReader r = new BufferedReader(new InputStreamReader(c.openFileInput(c.getString(R.string.data_path))));
			stopData = new Gson().fromJson(r, DataStore.class);
			r.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stopData == null)
				stopData = new DataStore();
		}

		Thread getSearchRoutes  = new Thread() {
			synchronized public void run() {
				BufferedReader r = null;

				try {
					r = new BufferedReader(new InputStreamReader(c.openFileInput("searchRoutes.json")));
					if (r == null) return;

					wrapperSearch temp = new Gson().fromJson(r, wrapperSearch.class);

					if (temp != null)
						synchronized (searchRoutes) {
							searchRoutes = temp.list;
						}
				} catch (NullPointerException e) {
					e.printStackTrace();
				}  catch (Exception e) {
					e.printStackTrace();
				} finally {
					updatingSearchRoutes = false;
				}
			}
		};
		getSearchRoutes.start();

		Thread getMapRoutes  = new Thread() {
			synchronized public void run() {
				BufferedReader r = null;

				try {
					r = new BufferedReader(new InputStreamReader(c.openFileInput("mapRoutes.json")));
					if (r == null) return;

					XStream testStream = new XStream();
					testStream.setClassLoader(XmlRequest.class.getClassLoader());
					testStream.processAnnotations(XmlRequest.class);

					StringBuilder str = new StringBuilder();
					for (String s = null; (s = r.readLine()) != null;)
						str.append(s);

					parseRouteData((XmlRequest) testStream.fromXML(str.toString()), null);

				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		getMapRoutes.start();


	}
	private void updateMapRoutes() {
        updatingMapRoutes = true;
		new Request<XmlRequest>(XmlRequest.class,
				new Request.JSONcallback<XmlRequest>() {
					public void run(XmlRequest r, String s, int error) {
						parseRouteData(r, s);
                        doUpdate(false);
						updatingMapRoutes = false;
					}
				},
				"http://developer.trimet.org/gis/data/tm_routes.kml").start();
	}


	private void parseRouteData(XmlRequest r, String xml) {
		if (r == null)
			return;

		mapRoutes = r;

		try {
			OutputStreamWriter ow = new OutputStreamWriter(getApplicationContext().openFileOutput("mapRoutes.json", Context.MODE_PRIVATE));

			ow.write(xml);
			ow.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			updatingMapRoutes = false;
		}
	}

	private class wrapperSearch {
		ArrayList<Route> list  = new ArrayList<Route>();
	}

	private class DataStore {
		public int FavOrder = 0;
		public int HistOrder = 0;
		public int StopOrder = 0;
		public int RefreshDelay = 30;
		public double Radius = 900;// 1/2 mile in meters.
		public int menu = 0;

		public ArrayList<Stop> StopList = new ArrayList<Stop>();
		public ArrayList<Alert> Alerts = new ArrayList<Alert>();
		public long lastRouteUpdate = 0;
	}
}
