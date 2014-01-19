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
	
	public static enum ListType {Favorites, History, Busses};

	public interface JSONcallback {
		public void run(JSONResult r, int error);
	}
	
	public static enum TimeType { Second, Minute, Hour, Day };
	
	public static long getTimeFromDate(Date d, TimeType type) {
		if(type == TimeType.Second)
			return (d.getTime() / 1000);
		else if(type == TimeType.Minute)
			return (d.getTime() / (1000 / 60));
		else if(type == TimeType.Hour)
			return (d.getTime() / (1000 / 60 / 60));
		else if(type == TimeType.Day)
			return (d.getTime() / (1000 / 60 / 60 / 24));
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
}
