package com.example.tritracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.example.tritracker.activities.SpinnerPopupActivity;
import com.example.tritracker.json.ForgroundRequestManager.checkStops;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;

public class Util {
	public static Stack<Class<?>> parents = new Stack<Class<?>>();
	private static Toast msg;
	private static Context c;
	public static SpinnerPopupActivity wait;
	
	public static enum ListType {Favorites, History, Busses, Routes};	
	public static enum TimeType { Second, Minute, Hour, Day };
	
	public static long getTimeFromDate(Date d, TimeType type) {
		if(type == TimeType.Second) {
			long time = (d.getTime() / 1000);
			return time;
		}else if(type == TimeType.Minute) {
			long time = (d.getTime() / (1000 / 60));
			return time;
		}else if(type == TimeType.Hour) {
			long time = (d.getTime() / (1000 / 60 / 60));
			return time;
		} else if(type == TimeType.Day) {
			long time = (d.getTime() / (1000 / 60 / 60 / 24));
			return time;
		}
		
		return d.getTime();
	}
	
	public static Date dateFromString(String s) {
		if (s == null)
			return null;
		try {
			s = s.replace("T", "");
			return new SimpleDateFormat("yyyy-MM-ddHH:mm:ss.SSSZ", Locale.US).parse(s);
		} catch (ParseException e) {
			return null;
		}
	}

	public static int getBussMinutes(Buss b, int pos) {
		Date est = null;

        //TODO make this work with more.
		if (b.times.get(0) != null && b.times.get(0).Status.compareTo("estimated") == 0) {
			est = new Date(b.times.get(pos).EstimatedTime.getTime() - new Date().getTime());
		} else {
			est = new Date(b.times.get(pos).ScheduledTime.getTime() - new Date().getTime());
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

	
	public static void createSpinner(final Activity act) {
		act.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				act.startActivity(new Intent(act, SpinnerPopupActivity.class));
			}
		});
	}
	
	public static void hideSpinner() {
		if (wait != null) {
			wait.finish();
			wait = null;
		}
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
				if (myFunc != null)
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
