package com.example.tritracker.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.AsyncTask;

import com.example.tritracker.Buss;
import com.example.tritracker.GlobalData;
import com.example.tritracker.Stop;
import com.example.tritracker.Util;
import com.google.gson.Gson;

public class BackgroundJSONRequest extends AsyncTask<String, String, String> {
	private Context context = null;
	//private Activity activity = null;

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
		} catch (ClientProtocolException e) {
			// TODO Handle problems..
		} catch (IOException e) {
			// TODO Handle problems..
		}
		
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
		
		Gson gson = new Gson();
		results res = gson.fromJson(result, results.class);

		if (res.resultSet.errorMessage != null) return;
		
		ResultSet rs = res.resultSet;		
		Stop temp = new Stop(rs.location[0]);

		if (rs.arrival != null)
			for (Arrival a : rs.arrival)
				temp.Busses.add(new Buss(a));
		else
			temp.Busses = null;
		
		if (Util.histHasStop(temp))
			Util.listGetStop(temp, GlobalData.History).Update(temp, false);

		if (Util.favHasStop(temp))
			Util.listGetStop(temp, GlobalData.Favorites).Update(temp, false);
		
		if (GlobalData.CurrentStop != null && GlobalData.CurrentStop.StopID == temp.StopID)
			GlobalData.CurrentStop = temp;

		Util.dumpData(context);
	}
	
}