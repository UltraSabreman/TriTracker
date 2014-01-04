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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.example.tritracker.json.JSONRequestManger;
import com.example.tritracker.json.ForegroundJSONRequest.checkStops;
import com.example.tritracker.json.BackgroundJSONRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
	
	public static int getBussMinutes(Buss b) {
		Date est = null;
		
    	if (b.Status.compareTo("estimated") == 0 && b.EstimatedTime != null) {
    		est = new Date(b.EstimatedTime.getTime() - new Date().getTime());
    	} else {
        	est = new Date(b.ScheduledTime.getTime() - new Date().getTime());
    	}
     	
		return mToS(est.getTime())/60;
	}
	
	public static int mToS(long mill) {
		return (int)(mill / 1000);
	}
	
	public static void refreshAdaptors() {
		if (GlobalData.favAdaptor != null)
			GlobalData.favAdaptor.notifyDataSetChanged();
		if (GlobalData.histAdaptor != null)
			GlobalData.histAdaptor.notifyDataSetChanged();
		if (GlobalData.bussAdaptor != null)
			GlobalData.bussAdaptor.notifyDataSetChanged();
	}
	
	public static void updateAllStops(Context c, Activity a) {
		String stops = "";
		for(Stop s : GlobalData.History) 
			stops += String.valueOf(s.StopID) + ",";
		
		for(Stop s : GlobalData.Favorites) 
			if (!histHasStop(s)) 
				stops += String.valueOf(s.StopID) + ",";
		
		if (stops.compareTo("") != 0) {
			stops = stops.substring(0, stops.length() - 1);
			
			new BackgroundJSONRequest(c, a, "http://developer.trimet.org/ws/V1/arrivals?locIDs="
					+ stops
					+ "&json=true&appID="
					+ c.getString(R.string.appid)).start();
		}
		
		String busses = "";
		for(Stop s : GlobalData.Favorites)
			if (!histHasStop(s))
				busses += Util.getListOfLines(s, false) + ",";
				
		for(Stop s : GlobalData.History)
			busses += Util.getListOfLines(s, false) + ",";
		
		if (busses.compareTo("") != 0) {
			busses = busses.substring(0, busses.length() - 1);
			
			new BackgroundJSONRequest(c, a, "http://developer.trimet.org/ws/V1/detours?routes="
					+ busses
					+ "&json=true&appID="
					+ c.getString(R.string.appid)).start();			
		}
		refreshAdaptors();
		dumpData(c);
	}
	
	public static void buildSortDialog(final Activity a, final int whichList) {
		AlertDialog.Builder builder = new AlertDialog.Builder(a);
		
		builder.setTitle("Sort By");
		String [] list = new String [] {"Stop name", "Stop ID", "Last Accesed"};
		if (whichList == 2)
			list = new String [] {"Buss name", "Buss Route", "Arrival Time"};
		
		
		builder.setSingleChoiceItems(list ,(whichList == 0 ? GlobalData.FavOrder : (whichList == 1 ? GlobalData.HistOrder : GlobalData.StopOrder)), 
			new DialogInterface.OnClickListener() {
			@Override
            public void onClick(DialogInterface dialog, int which) {
				if (whichList == 0)
					GlobalData.FavOrder = which;
				else if (whichList == 1)
					GlobalData.HistOrder = which;
				else 
					GlobalData.StopOrder = which;
				
			}
		}); 
				
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   Util.refreshAdaptors();
	           }
		});
	       
		builder.create().show();
	}
	
	public static String getListOfLines(Stop s, boolean test) {
		//this lists the routes, and adds commas between them.
		if (s.Busses != null && s.Busses.size() != 0) {
			if (test) {
				String str = "";
				for (Buss b :  s.Busses) {
					String [] words = b.SignLong.split(" ");
					String tempRoute = words[0];
					if (tempRoute.compareTo("MAX") == 0)
						tempRoute = (words[1].isEmpty() ? words[2] : words[1]) + "-Line";
					if (!str.contains(tempRoute))
							str+= tempRoute + " ";
				}
				
				return str.replaceAll("( [0-9a-zA-Z])", ",$1");
			} else {
				String str = "";
				for (Buss b :  s.Busses) {
					String tempRoute = String.valueOf(b.Route);
					if (!str.contains(tempRoute)) {
						str += (tempRoute + ",");
					}
				}
				
				return str.substring(0, str.length() - 1);
			}
		}
		return "";
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
			return true;
		}
		return false;
	}
	
	public static void subscribeToEdit(final Context c, final Activity a, int name) {
		EditText edit = (EditText) a.findViewById(name);

		edit.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE)) {
					EditText edit = (EditText) a.findViewById(R.id.UIStopIDBox);
					new JSONRequestManger(c, a, Integer.parseInt(edit.getText().toString())).start();
					edit.getText().clear();

				}
				return false;
			}
		});
	}

	public static boolean favHasStop(Stop s) {
		return (listGetStop(s.StopID, GlobalData.Favorites) != null ? true : false);
	}

	public static boolean histHasStop(Stop s) {
		return (listGetStop(s.StopID, GlobalData.History) != null ? true : false);
	}

	public static Stop listGetStop(int stopID, ArrayList<Stop> l) {
		for (Stop s : l)
			if (stopID == s.StopID)
				return s;

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
		/*File test = new File(c.getString(R.string.data_path));
		if (test.exists())
			test.delete();*/
		
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					c.openFileOutput(c.getString(R.string.data_path),
							Context.MODE_PRIVATE)));

			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
			String data = gson.toJson(GlobalData.getJsonWrap());

			bw.write(data);
			bw.close();
		} catch (JsonSyntaxException e) {
		} catch (FileNotFoundException e1) {
		} catch (IOException e2) {
		}
	}
	
	public static void messageDiag(Activity act, final checkStops myFunc, String title, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		
		builder.setTitle(title).setMessage(msg);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   if (myFunc != null)
	        		   myFunc.doStops();
	           }
		});
		builder.setNegativeButton("Canel", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	           }
		});
		builder.create().show();
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

			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
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
	
	public static void restartTimer(final Context c, final Activity a) {
		if (timerRunnable != null)
			timerHandler.removeCallbacks(timerRunnable);
		
		timerRunnable = new Runnable() {
	        @Override
	        public void run() {
	        	Util.updateAllStops(c, a);
	        	
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
