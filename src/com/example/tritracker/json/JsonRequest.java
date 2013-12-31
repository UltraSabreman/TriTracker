package com.example.tritracker.json;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.example.tritracker.Buss;
import com.example.tritracker.GlobalData;
import com.example.tritracker.Stop;
import com.example.tritracker.StopView;
import com.example.tritracker.Util;
import com.google.gson.Gson;

public class JsonRequest extends AsyncTask<String, String, String> {
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
		} catch (ClientProtocolException e) {
			// TODO Handle problems..
		} catch (IOException e) {
			// TODO Handle problems..
		}
		return responseString;
	}

	public JsonRequest(Context context) {
		this.context = context;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		Gson gson = new Gson();
		ResultSet rs = gson.fromJson(result, results.class).resultSet;

		Stop temp = new Stop(rs.location[0]);
		for (Arrival a : rs.arrival)
			temp.Busses.add(new Buss(a));
		
		GlobalData.CurrentStop = temp;

		if (!Util.histHasStop(GlobalData.CurrentStop))
			GlobalData.History.add(temp);

		Util.dumpData(context);
		context.startActivity(new Intent(context, StopView.class)
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

	}

}