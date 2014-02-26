package com.example.tritracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

import com.example.tritracker.activities.SpinnerPopupActivity;
import com.example.tritracker.json.ForgroundRequestManager.checkStops;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

	public static enum ListType {Favorites, History, Busses, Routes}

	;

	public static enum TimeType {Second, Minute, Hour, Day}

	;

	public static void fileLog(boolean on) {
		if (on) {
			Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread thread, Throwable ex) {

					PrintWriter pw;
					try {
						pw = new PrintWriter(
								new FileWriter(Environment.getExternalStorageDirectory()+"/error.log", true));
						ex.printStackTrace(pw);
						pw.flush();
						pw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		} else
			Thread.currentThread().setUncaughtExceptionHandler(null);
	}

	public static char[] oldChars = new char[5];
	public static String strip(String s)
	{
		final int inputLen = s.length();
		if ( oldChars.length < inputLen )
		{
			oldChars = new char[inputLen];
		}
		s.getChars(0, inputLen, oldChars, 0);

		char[] newChars = new char[s.length()];
		int newLen = 0;
		for (int j = 0; j < inputLen; j++) {
			char ch = oldChars[j];
			if (ch > ' ')
				newChars[newLen++] = ch;
		}
		return new String(newChars, 0, newLen);
	}


	public static void print(String s) {
		System.out.println(s);
	}

	public static long getTimeFromDate(Date d, TimeType type) {
		if (type == TimeType.Second) {
			long time = (d.getTime() / 1000);
			return time;
		} else if (type == TimeType.Minute) {
			long time = (d.getTime() / (1000 / 60));
			return time;
		} else if (type == TimeType.Hour) {
			long time = (d.getTime() / (1000 / 60 / 60));
			return time;
		} else if (type == TimeType.Day) {
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

	public static int getBussMinutes(Buss.TimeBox t) {
		Date est = null;

		//TODO make this work with more.
		if (t != null && t.Status.compareTo("estimated") == 0) {
			est = new Date(t.EstimatedTime.getTime() - new Date().getTime());
		} else {
			est = new Date(t.ScheduledTime.getTime() - new Date().getTime());
		}

		return mToS(est.getTime()) / 60;
	}

	public static int mToS(long mill) {
		return (int) (mill / 1000);
	}

    public static String removeRoutePrefix(String s, int route) {
        int max = s.indexOf("MAX");
        int wes = s.indexOf("WES");
        int rt = s.indexOf(String.valueOf(route));
        if (max != -1 || wes != -1 || rt != -1)
            return s.substring(s.indexOf(" ") + 1);
        return s;
    }


	public static String getListOfLines(Stop s) {
		// this lists the routes, and adds commas between them.
		if (s == null) return null;
		if (s.Busses != null && s.Busses.size() != 0) {
            StringBuilder str = new StringBuilder();
            for (Buss b : s.Busses) {
                String ss = RouteNamer.getMedName(b.Route);
                if (!str.toString().contains(ss))
                    str.append(ss).append(" ");
            }

            return str.toString().replaceAll("( [0-9a-zA-Z])", ",$1");
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
