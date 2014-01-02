package com.example.tritracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.example.tritracker.json.ActiveJSONRequest;
import com.example.tritracker.json.BackgroundJSONRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Util {
	public static Stack<Class<?>> parents = new Stack<Class<?>>();
	private static Toast msg;
	private static Context c;
    private static Handler timerHandler = new Handler();
    private static Runnable timerRunnable = null;

	public static Date dateFromString(String s) {
		if (s == null)
			return null;
		try {
			s = s.replace("T", "");
			return new SimpleDateFormat("yyyy-MM-ddHH:mm:ss.SSSZ", Locale.US)
					.parse(s);
		} catch (ParseException e) {
			return null;
		}
	}
	
	public static void updateAllStops(Context c) {
		for(Stop s : GlobalData.History) {
			new BackgroundJSONRequest(c)
			.execute("http://developer.trimet.org/ws/V1/arrivals?locIDs="
					+ s.StopID
					+ "&json=true&appID="
					+ c.getString(R.string.appid));
		}
		for(Stop s : GlobalData.Favorites) {
			if (!histHasStop(s)) {
				new BackgroundJSONRequest(c)
				.execute("http://developer.trimet.org/ws/V1/arrivals?locIDs="
						+ s.StopID
						+ "&json=true&appID="
						+ c.getString(R.string.appid));
			}
		}

		if (GlobalData.favAdaptor != null)
			GlobalData.favAdaptor.notifyDataSetChanged();
		if (GlobalData.histAdaptor != null)
			GlobalData.histAdaptor.notifyDataSetChanged();
		if (GlobalData.bussAdaptor != null)
			GlobalData.bussAdaptor.notifyDataSetChanged();
	}
	
	public static void initListsAndAdaptors(Context c) {
		
	}
	
	public static void getAlerts() {
		//TODO do me
	}
	
	public static void initToast(Context ic) {
		c = ic;
	}

	public static void showToast(String s, int d) {
		if (msg != null)
			msg.cancel();
		msg = Toast.makeText(c, s, d);
		msg.show();
	}
	
	
	public static boolean sortList(int Type) {
		if (Type == 0) { //Favorites
			if (GlobalData.Favorites == null) return false;
			Collections.sort(GlobalData.Favorites, new StopSorter(GlobalData.FavOrder));
			if (GlobalData.FavOrder == 2) //By time
				Collections.reverse(GlobalData.Favorites);
			return true;
		} else if (Type == 1) { //History
			if (GlobalData.History == null) return false;
			Collections.sort(GlobalData.History, new StopSorter(GlobalData.HistOrder));
			if (GlobalData.HistOrder == 2) //By time
				Collections.reverse(GlobalData.History);
			return true;
		} else if (Type == 2) { //Buss
			if (GlobalData.CurrentStop == null || GlobalData.CurrentStop.Busses == null) return false;
			Collections.sort(GlobalData.CurrentStop.Busses, new BussSorter(GlobalData.StopOrder));
			if (GlobalData.StopOrder == 2) //By time
				Collections.reverse(GlobalData.CurrentStop.Busses);
			return true;
		}
		return false;
	}
	
	public static void incSortType(int Type) {
		if (Type == 0)
			GlobalData.FavOrder = (GlobalData.FavOrder + 1) % 3;
		else if(Type == 1)
			GlobalData.HistOrder = (GlobalData.HistOrder + 1) % 3;
		else if(Type == 2)
			GlobalData.StopOrder = (GlobalData.StopOrder + 1) % 3;
	}
	
	
	public static void subscribeToEdit(final Context c, final Activity a,
			int name) {
		EditText edit = (EditText) a.findViewById(R.id.UIStopIDBox);

		edit.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE)) {
					EditText edit = (EditText) a.findViewById(R.id.UIStopIDBox);
					// GlobalData.CurrentStop =
					new ActiveJSONRequest(c, a)
							.execute("http://developer.trimet.org/ws/V1/arrivals?locIDs="
									+ edit.getText().toString()
									+ "&json=true&appID="
									+ c.getString(R.string.appid));

					edit.getText().clear();

				}
				return false;
			}
		});
	}

	public static boolean favHasStop(Stop s) {
		return (listGetStop(s, GlobalData.Favorites) != null ? true : false);
	}

	public static boolean histHasStop(Stop s) {
		return (listGetStop(s, GlobalData.History) != null ? true : false);
	}

	public static Stop listGetStop(Stop s, ArrayList<Stop> l) {
		if (s == null)
			return null;

		for (Stop stop : l)
			if (s.StopID == stop.StopID)
				return stop;

		return null;
	}
	
	public static void removeStop(Stop s, ArrayList<Stop> l) {
		for (Stop stop : l){
			if (s.StopID == stop.StopID){
				l.remove(stop);
				return;
			}
		}
	}

	public static void dumpData(Context c) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					c.openFileOutput(c.getString(R.string.data_path),
							Context.MODE_PRIVATE)));

			Gson gson = new Gson();
			String data = gson.toJson(GlobalData.getJsonWrap());

			bw.write(data);
			bw.close();
		} catch (JsonSyntaxException e) {

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	public static void readData(Context c) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					c.openFileInput(c.getString(R.string.data_path))));

			String fileContents = "";
			String line;
			while ((line = br.readLine()) != null) {
				fileContents += line;
			}

			Gson gson = new Gson();
			GlobalData.JsonWrapper wrap = gson.fromJson(fileContents,
					GlobalData.JsonWrapper.class);
			if (wrap != null) {
				if (wrap.Favorites != null) {
					GlobalData.Favorites = new ArrayList<Stop>(wrap.Favorites);
					GlobalData.FavOrder = wrap.FavOrder;
				}
				if (wrap.History != null) {
					GlobalData.History = new ArrayList<Stop>(wrap.History);
					GlobalData.HistOrder = wrap.HistOrder;
				}
				GlobalData.StopOrder = wrap.StopOrder;
				GlobalData.RefreshDelay = wrap.RefreshDelay;

			}
			br.close();
		} catch (JsonSyntaxException e) {

		} catch (FileNotFoundException e) {

		} catch (IOException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static class StopSorter implements Comparator<Stop> {
		private int compareType = 0;
		public StopSorter(int t) {
			compareType = t;
			//0 == By Name
			//1 == By ID
			//2 == By Acces Date
		}
		
	    @Override
	    public int compare(Stop s, Stop s2) {
	    	if (s == null || s2 == null) return 0;
	    	if (compareType == 0) 
	    		return s.Name.compareTo(s2.Name);
	    	if (compareType == 1)
	    		return (s.StopID < s2.StopID ? -1 : (s.StopID > s2.StopID ? 1 : 0));
	    	else {
	    		if (s.LastAccesed != null && s2.LastAccesed != null)
	    			return s.LastAccesed.compareTo(s2.LastAccesed);
	    		else
	    			return 0;
	    	}
	    }
	}
	
	private static class BussSorter implements Comparator<Buss> {
		private int compareType = 0;
		public BussSorter(int t) {
			compareType = t;
			//0 == By Name
			//1 == By Line
			//2 == By Arrival Time
		}
		
	    @Override
	    public int compare(Buss o1, Buss o2) {
	    	if (compareType == 0) 
	    		if (GlobalData.Orientation == 2)
	    			return o1.SignLong.compareTo(o2.SignLong);
	    		else
	    			return o1.SignShort.compareTo(o2.SignShort);
	    	if (compareType == 1)
	    		return (o1.Route < o2.Route ? -1 : (o1.Route > o2.Route ? 1 : 0));
	    	else
	    		if (o1.EstimatedTime != null && o2.EstimatedTime != null)
	    			return o1.EstimatedTime.compareTo(o2.EstimatedTime); //fix me
	    		else
	    			return o1.ScheduledTime.compareTo(o2.ScheduledTime); //fix me
	    }
	}
	
	public static void restartTimer(final Context c) {
		if (timerRunnable != null)
			timerHandler.removeCallbacks(timerRunnable);
		
		timerRunnable = new Runnable() {
	        @Override
	        public void run() {
	        	Util.updateAllStops(c);
	        	
	        	if (GlobalData.RefreshDelay > 0)
	        		timerHandler.postDelayed(this, (int)(GlobalData.RefreshDelay * 1000));
	        }
	    };
	    
		if (GlobalData.RefreshDelay > 0)
			timerHandler.postDelayed(timerRunnable, (int)(GlobalData.RefreshDelay * 1000));
	}
	
	public static void stopTimer() {
		if (timerRunnable != null)
			timerHandler.removeCallbacks(timerRunnable);
	}
}
