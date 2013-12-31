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
import java.util.Date;
import java.util.Locale;
import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.example.tritracker.json.JsonRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Util {
	public static Stack<Class<?>> parents = new Stack<Class<?>>();
	private static Toast msg;
	private static Context c;

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

	public static void initToast(Context ic) {
		c = ic;
	}

	public static void showToast(String s, int d) {
		if (msg != null)
			msg.cancel();
		msg = Toast.makeText(c, s, d);
		msg.show();
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
					new JsonRequest(c)
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
		return (listHasStop(s, GlobalData.Favorites) != null ? true : false);
	}

	public static boolean histHasStop(Stop s) {
		return (listHasStop(s, GlobalData.History) != null ? true : false);
	}

	public static Stop listHasStop(Stop s, ArrayList<Stop> l) {
		if (s == null)
			return null;

		for (Stop stop : l)
			if (s.StopID == stop.StopID)
				return stop;

		return null;
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

			}
			br.close();
		} catch (JsonSyntaxException e) {

		} catch (FileNotFoundException e) {

		} catch (IOException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
