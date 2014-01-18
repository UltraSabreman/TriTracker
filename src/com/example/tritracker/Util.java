package com.example.tritracker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.example.tritracker.activities.StopListFragment.checkStops;
import com.example.tritracker.json.JSONResult;

public class Util {
	public static Stack<Class<?>> parents = new Stack<Class<?>>();
	private static Toast msg;
	private static Context c;

	public interface JSONcallback {
		public void run(JSONResult r, int error);
	}
	
	public static long getTimeFromDate(Date d, String type) {
		if(type.toLowerCase(Locale.US).compareTo("s") == 0)
			return d.getTime() / 1000;
		else if(type.toLowerCase(Locale.US).compareTo("m") == 0)
			return d.getTime() / (1000 / 60);
		else if(type.toLowerCase(Locale.US).compareTo("h") == 0)
			return d.getTime() / (1000 / 60 / 60);
		else if(type.toLowerCase(Locale.US).compareTo("d") == 0)
			return d.getTime() / (1000 / 60 / 60 / 24);
		else
			return d.getTime();
	}
	
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

		return mToS(est.getTime()) / 60;
	}

	public static int mToS(long mill) {
		return (int) (mill / 1000);
	}


	public static void buildSortDialog(final Activity a, final int whichList) {
		/*AlertDialog.Builder builder = new AlertDialog.Builder(a);

		builder.setTitle("Sort By");
		String[] list = new String[] { "Stop name", "Stop ID", "Last Accesed" };
		if (whichList == 2)
			list = new String[] { "Buss name", "Buss Route", "Arrival Time" };

		builder.setSingleChoiceItems(list,
				(whichList == 0 ? GlobalData.FavOrder
						: (whichList == 1 ? GlobalData.HistOrder
								: GlobalData.StopOrder)),
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
				//TODO fixme
				//Util.refreshAdaptors();
			}
		});

		builder.create().show();*/
		//TODO Sorting
	}

	public static String getListOfLines(Stop s, boolean test) {
		// this lists the routes, and adds commas between them.
		if (s == null) return null;
		if (s.Busses != null && s.Busses.size() != 0) {
			if (test) {
				String str = "";
				for (Buss b : s.Busses) {
					String[] words = b.SignLong.split(" ");
					String tempRoute = words[0];
					if (tempRoute.compareTo("MAX") == 0)
						tempRoute = (words[1].isEmpty() ? words[2] : words[1])
								+ "-Line";
					if (!str.contains(tempRoute))
						str += tempRoute + " ";
				}

				return str.replaceAll("( [0-9a-zA-Z])", ",$1");
			} else {
				String str = "";
				for (Buss b : s.Busses) {
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
		/*if (Type == 0) { // Favorites
			if (GlobalData.Favorites == null)
				return false;
			Collections.sort(GlobalData.Favorites, new StopSorter(
					GlobalData.FavOrder));
			if (GlobalData.FavOrder == 2) // By time
				Collections.reverse(GlobalData.Favorites);
			return true;
		} else if (Type == 1) { // History
			if (GlobalData.History == null)
				return false;
			Collections.sort(GlobalData.History, new StopSorter(
					GlobalData.HistOrder));
			if (GlobalData.HistOrder == 2) // By time
				Collections.reverse(GlobalData.History);
			return true;
		} else if (Type == 2) { // Buss
			if (GlobalData.CurrentStop == null
					|| GlobalData.CurrentStop.Busses == null)
				return false;
			Collections.sort(GlobalData.CurrentStop.Busses, new BussSorter(
					GlobalData.StopOrder));
			return true;
		}*/
		//TODO Sorting
		return false;
	}

	public static void messageDiag(final Activity act, final checkStops myFunc,
			final String title, final String msg) {
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(act);
		
				builder.setTitle(title).setMessage(msg);
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (myFunc != null)
							myFunc.doStops();
					}
				});
				builder.setNegativeButton("Canel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
							}
						});
				builder.create().show();
			}
		});
	}


	private static class StopSorter implements Comparator<Stop> {
		private int compareType = 0;

		public StopSorter(int t) {
			compareType = t;
			// 0 == By Name
			// 1 == By ID
			// 2 == By Acces Date
		}

		@Override
		public int compare(Stop s, Stop s2) {
			if (s == null || s2 == null)
				return 0;
			if (compareType == 0)
				return s.Name.compareTo(s2.Name);
			if (compareType == 1)
				return (s.StopID < s2.StopID ? -1 : (s.StopID > s2.StopID ? 1
						: 0));
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
			// 0 == By Name
			// 1 == By Line
			// 2 == By Arrival Time
		}

		@Override
		public int compare(Buss o1, Buss o2) {
			if (compareType == 0)
				/*if (GlobalData.Orientation == 2)
					return o1.SignLong.compareTo(o2.SignLong);
				else*/
				//TODO Sorting
					return o1.SignShort.compareTo(o2.SignShort);
			if (compareType == 1)
				return (o1.Route < o2.Route ? -1
						: (o1.Route > o2.Route ? 1 : 0));
			else if (o1.EstimatedTime != null && o2.EstimatedTime != null)
				return o1.EstimatedTime.compareTo(o2.EstimatedTime); // fix me
			else
				return o1.ScheduledTime.compareTo(o2.ScheduledTime); // fix me
		}
	}
}
