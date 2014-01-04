package com.example.tritracker.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.example.tritracker.Buss;
import com.example.tritracker.GlobalData;
import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.Util;
import com.example.tritracker.Activities.StopView;
import com.google.gson.Gson;

public class ForegroundJSONRequest extends AsyncTask<String, String, String> {
	private Context context = null;
	private Activity activity = null;
	private int stopId = 0;
	private int error = 0;

	@Override
	protected String doInBackground(String... uri) {
	    final HttpParams httpParams = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
		HttpClient httpclient = new DefaultHttpClient(httpParams);
		HttpResponse response;
		String responseString = null;
		try {					    
			stopId = Integer.parseInt(uri[1]);
			response = httpclient.execute(new HttpGet(uri[0]));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
			} else {
				// Closes the connection.
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		} catch (ConnectTimeoutException e) { 
			error = 1;
		} catch (NoHttpResponseException e) { 
			error = 2;
		} catch (ClientProtocolException e) {
			error = 3;
		} catch (IOException e) {
			error = 4;
		}
		
		return responseString;
	}

	public ForegroundJSONRequest(Context context, Activity activity) {
		this.context = context;
		this.activity = activity;
	}

	@Override
	protected void onPreExecute() {
		((RelativeLayout) activity.findViewById(R.id.NoClickScreen)).setVisibility(View.VISIBLE);
		activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);		
	}
	
	public interface checkStops {
	    public void doStops();
	}
	
    public void function() {
    	Stop s = Util.listGetStop(stopId, GlobalData.History);
		if (s != null) {
			GlobalData.CurrentStop = s;
			context.startActivity(new Intent(context, StopView.class)
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			return;
		}
		s = Util.listGetStop(stopId, GlobalData.Favorites);
		if (s != null) {
			GlobalData.CurrentStop = s;
			context.startActivity(new Intent(context, StopView.class)
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}			
		return;
    }
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		((RelativeLayout) activity.findViewById(R.id.NoClickScreen)).setVisibility(View.INVISIBLE);
		activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
		
		if (result == null) {
			if (error == 1)
				Util.messageDiag(activity, new checkStops() { public void doStops() { function(); }}, 
						"Connection Timed-Out", "The connection timed-out (Trimet's servers might be busy, or you could have a poor connection)." +
						"\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
			else if (error == 2)
				Util.messageDiag(activity, new checkStops() { public void doStops() { function(); }}, 
						"Malformed reponce", "Trimet didn't respond correctly (their servers may be under heavy load)" +
						"\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
			else if (error == 3)
				Util.messageDiag(activity, new checkStops() { public void doStops() { function(); }}, 
						"Error Connecting", "It looks like Trimet changed their API. Please contact the developer ASAP and this will be fixed." +
						"\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
			else if (error == 4)
				Util.messageDiag(activity, new checkStops() { public void doStops() { function(); }}, 
						"Are you connected?", "Can't reach the Trimet servers right now, are you connected to the internet?" +
						"\n\nIf you've visited this stop before, and you want to see the cached times, click ok.");
	
			return;
		}
		
		Gson gson = new Gson();
		JSONResult.ResultSet rs = gson.fromJson(result, JSONResult.class).resultSet;

		if (rs.errorMessage != null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);

			String error = rs.errorMessage.content;
			String id = "";
			Matcher m = Pattern.compile("([0-9]+)").matcher(error);
		    if (m.find()) {
		    	id = m.group(1);
		    }
			
			// 2. Chain together various setter methods to set the dialog characteristics
			builder.setMessage("A stop with the ID \"" + id + "\" doesn't exist.")
			       .setTitle(R.string.no_stop);
			
			builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               // User clicked OK button
		           }
		       });
			
			// 3. Get the AlertDialog from create()
			builder.create().show();
			return;
		}
		
		Stop temp = new Stop(rs.location[0]);
		temp.LastAccesed = new Date();

		if (rs.arrival != null)
			for (JSONResult.ResultSet.Arrival a : rs.arrival)
				temp.Busses.add(new Buss(a));
		else
			temp.Busses = null;

		Stop t = Util.listGetStop(temp.StopID, GlobalData.History);
		if (t == null) {
			Stop w = Util.listGetStop(temp.StopID, GlobalData.Favorites);
			if (w == null) {
				GlobalData.histAdaptor.add(temp);
				GlobalData.CurrentStop = temp;
			} else {
				w.Update(temp, true);
				GlobalData.histAdaptor.add(w);
				GlobalData.CurrentStop = w;
			}
		} else {
			t.Update(temp, true);
			GlobalData.CurrentStop = t;
		}

		Util.refreshAdaptors();
		Util.dumpData(context);
		context.startActivity(new Intent(context, StopView.class)
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

	}
	
}