package com.example.tritracker.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
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

public class ActiveJSONRequest extends AsyncTask<String, String, String> {
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

	public ActiveJSONRequest(Context context, Activity activity) {
		this.context = context;
		this.activity = activity;
	}

	@Override
	protected void onPreExecute() {
		((RelativeLayout) activity.findViewById(R.id.NoClickScreen)).setVisibility(View.VISIBLE);
		activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);		
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		((RelativeLayout) activity.findViewById(R.id.NoClickScreen)).setVisibility(View.INVISIBLE);
		activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
		
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
				GlobalData.History.add(temp);
				GlobalData.CurrentStop = temp;
			} else {
				w.Update(temp, true);
				GlobalData.History.add(w);
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