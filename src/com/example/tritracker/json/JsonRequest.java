package com.example.tritracker.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;

import com.example.tritracker.Buss;
import com.example.tritracker.GlobalData;
import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.StopView;
import com.example.tritracker.Util;
import com.google.gson.Gson;

public class JsonRequest extends AsyncTask<String, String, String> {
	private Context context = null;
	private Activity activity = null;

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

	public JsonRequest(Context context, Activity activity) {
		this.context = context;
		this.activity = activity;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		Gson gson = new Gson();
		results res = gson.fromJson(result, results.class);

		if (res.resultSet.errorMessage != null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);

			String error = res.resultSet.errorMessage.content;
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
		
		ResultSet rs = res.resultSet;		
		Stop temp = new Stop(rs.location[0]);

		if (rs.arrival != null)
			for (Arrival a : rs.arrival)
				temp.Busses.add(new Buss(a));
		else
			temp.Busses = null;

		GlobalData.CurrentStop = temp;

		if (!Util.histHasStop(GlobalData.CurrentStop))
			GlobalData.History.add(temp);

		Util.dumpData(context);
		context.startActivity(new Intent(context, StopView.class)
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

	}
	
}