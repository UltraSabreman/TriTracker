package com.example.tritracker.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

import android.content.Context;
import android.os.AsyncTask;

import com.example.tritracker.Buss;
import com.example.tritracker.GlobalData;
import com.example.tritracker.Stop;
import com.example.tritracker.Stop.Alert;
import com.example.tritracker.Util;
import com.google.gson.Gson;

public class BackgroundJSONRequest extends AsyncTask<String, String, String> {
	private Context context = null;

	@Override
	protected String doInBackground(String... uri) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		String responseString = null;
		try {			
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
		} catch (NoHttpResponseException e) { 
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}
		//this will silently fail on all issues, since this a background request.
		
		return responseString;
	}

	public BackgroundJSONRequest(Context context) {
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		
		if (result == null || result.compareTo("{\"resultSet\":{}}") == 0) return;
		
		Gson gson = new Gson();
		JSONResult res = gson.fromJson(result, JSONResult.class);
		
		if (res.resultSet.detour != null) {
			for (Stop s : GlobalData.History)
				if (s.Alerts != null) {
					s.Alerts.clear();
					s.Alerts = null;
				}
			for (Stop s : GlobalData.Favorites)
				if (s.Alerts != null) {
					s.Alerts.clear();
					s.Alerts = null;
				}
			
			for (JSONResult.ResultSet.Detour d : res.resultSet.detour) {
				
				ArrayList<String> stopIds = new ArrayList<String>();
				Matcher m = Pattern.compile("Stop ID ([0-9]+)").matcher(d.desc);
				while (m.find()) {
					stopIds.add(m.group(1));
			    }
			    
				for (String si : stopIds){
					Stop hist = Util.listGetStop(Integer.parseInt(si), GlobalData.History);
					if (hist != null) {
						hist.Alerts = new ArrayList<Alert>();
						hist.Alerts.add(new Stop.Alert(d.desc, Integer.parseInt(d.route[0].route))); //Is this safe?
					}
					
					
					Stop fav = Util.listGetStop(Integer.parseInt(si), GlobalData.Favorites);
					if (fav != null) {
						fav.Alerts = new ArrayList<Alert>();
						fav.Alerts.add(new Stop.Alert(d.desc, Integer.parseInt(d.route[0].route))); //Is this safe?
					}
					
				}
			}
			return;
		}

		if (res == null || res.resultSet == null || res.resultSet.errorMessage != null) return;
		
		JSONResult.ResultSet rs = res.resultSet;	
		ArrayList<Stop> stops = new ArrayList<Stop>();
		for (JSONResult.ResultSet.Location l : rs.location) {
			stops.add(new Stop(l));
		}
		
		for (Stop s: stops) {
			if (rs.arrival != null)
				for (JSONResult.ResultSet.Arrival a : rs.arrival) {
						if (s.StopID == a.locid)
							s.Busses.add(new Buss(a));
					}
			
			
			if (Util.histHasStop(s)) 
				Util.listGetStop(s.StopID, GlobalData.History).Update(s, false);	
	
			if (Util.favHasStop(s))
				Util.listGetStop(s.StopID, GlobalData.Favorites).Update(s, false);			
			
			if (GlobalData.CurrentStop != null && GlobalData.CurrentStop.StopID == s.StopID) 
				GlobalData.CurrentStop.Update(s, false);
					
		}
		Util.refreshAdaptors();
		Util.dumpData(context);	
	}
}